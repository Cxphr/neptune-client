# 🧠 Neptune Client — Architecture

This document explains how the client is put together so you can extend it without reverse-engineering the whole tree.

---

## 🧱 Stack

| Piece | Value |
|---|---|
| Minecraft | 1.8.9 |
| Forge | 11.15.1.2318 |
| Mappings | MCP `stable_20` |
| Build | ForgeGradle `2.1-SNAPSHOT` + Gradle `4.10.3` |
| Java | 8 |
| Mod id | `neptune` |

---

## 🗂️ Layout

```
src/main/java/com/adama/neptune/
├── NeptuneClient.java        @Mod entrypoint
├── config/
│   └── NeptuneConfig.java    JSON config + debounced/atomic saves + generic maps
├── event/
│   └── ClientEvents.java     Forge event subscriptions, fans out to modules
├── input/
│   └── CapturingMouseHelper.java  intercepts mouse deltas for freelook/zoom
├── gui/
│   ├── ClickGui.java         click GUI (icons, categories, settings)
│   ├── KeybindScreen.java    on-screen keyboard layouts
│   └── setting/              BoolSetting, SliderSetting, ColorSetting
├── module/
│   ├── BKModule.java         base class + lifecycle hooks
│   ├── HudModule.java        HUD base (render/width/height)
│   ├── ModuleManager.java    registry, keybinds, ticks, HUD pass
│   ├── HudPack.java          small HUD modules
│   ├── VisualModules.java    crosshair, block outline, chunk borders, FOV
│   ├── UtilityModules.java   AutoGG, chat timestamps
│   └── …                     freelook, zoom, hitbox, motion blur, item physics…
├── render/
│   └── RenderFlatItem.java   custom EntityItem renderer for Item Physics
└── util/
    ├── Ease.java             smoothstep / out-cubic / exponential smoothing
    └── IconRenderer.java     code-drawn GUI icons (no image assets)
```

---

## 🔄 Lifecycle

`NeptuneClient` is the `@Mod` entrypoint.

1. **`preInit`** — load `config/neptune.json`, construct `ModuleManager`, register a shutdown hook that flushes config.
2. **`init`** — register keybinds with Forge; create `ClientEvents` and register it on **both** `MinecraftForge.EVENT_BUS` and `FMLCommonHandler.instance().bus()`.

Forge 1.8.9 splits events between those buses, so registering one handler on both is intentional.

---

## 📡 Event flow

```
Minecraft frame
   │
   ├─ ClientTickEvent (END) ──► ModuleManager.onClientTick()
   │                              ├─ config.tick() (debounced save)
   │                              ├─ freelook hold state
   │                              ├─ module toggle key edges
   │                              ├─ GUI open/close key
   │                              └─ each module.onClientTick()
   │
   ├─ RenderTickEvent (START) ─► onFrameStart() + onRenderTick(dt)
   │  RenderTickEvent (END) ───► onFrameEnd()
   │
   ├─ CameraSetup ────────────► module.onCamera()      (freelook angles)
   ├─ FOVModifier ────────────► module.onFov()         (zoom, FOV changer)
   ├─ FogDensity ─────────────► module.onFogDensity()
   ├─ RenderWorldLastEvent ───► module.onRenderWorld() (hitboxes, chunk borders)
   ├─ DrawBlockHighlightEvent ► module.onBlockHighlight()
   ├─ MouseEvent ─────────────► module.onMouse()       (CPS, wheel zoom)
   ├─ ClientChatReceivedEvent ► module.onChat()        (AutoGG, timestamps)
   ├─ AttackEntityEvent ──────► module.onAttack()      (combo counter)
   ├─ RenderGameOverlayEvent.Pre  ► module.onRenderOverlayPre()
   └─ RenderGameOverlayEvent.Post ► HUD render + module.onRenderOverlay()
```

`ClientEvents` never hard-codes modules: it iterates `ModuleManager.modules()` and calls the matching hook, so adding a module requires no event wiring.

---

## 🧩 Module system

Every feature extends `BKModule`:

```java
public String name();
public String category();      // Movement / Render / HUD / Utility
public String id();            // stable key for config + keybinds
public boolean isEnabled();
public void setEnabled(boolean);
public List<Setting> settings();
// hooks: onClientTick, onRenderTick, onFrameStart/End, onCamera, onFov,
//        onRenderWorld, onBlockHighlight, onMouse, onChat, onAttack, ...
```

`HudModule` adds `render()`, `width()` and `height()`.

Settings are lambdas:

```java
new SliderSetting("Range", 4, 48, 1, 0,
        () -> config.number("hitbox.range", 16.0),
        v -> config.setNumber("hitbox.range", v));
```

The GUI only knows the `Setting` interface, so new controls are reusable.

---

## 💾 Config

`NeptuneConfig` is a Gson-serialized POJO.

- **Typed fields** for the core modules (`zoomFactor`, `hitboxColor`, …).
- **Generic maps** for newer modules and keybinds:
  - `toggles`, `positionsX/Y`, `numbers`, `colors`, `keybinds`, `strings`
- `markDirty()` + `tick()` debounce writes to **400 ms** (sliders no longer write on every mouse pixel).
- `save()` writes `neptune.json.tmp` and atomically moves it over `neptune.json`.

File location: `config/neptune.json`.

---

## 🖥️ GUI

`ClickGui` (RightShift) is rendered with vanilla `GuiScreen` drawing helpers:

- category rail with `IconRenderer` icons,
- module rows with enable state and expandable settings,
- gradient sliders, hue color pickers, keybind chips,
- **Keybinds** button opens `KeybindScreen`.

`KeybindScreen` draws four physical layouts (`65`, `TKL`, `ThinkPad`, `100`) and mouse buttons. Selecting an action then clicking a key writes straight to `NeptuneConfig` and updates the registered `KeyBinding` code.

`IconRenderer` draws icons from rectangles — no texture assets, so the GUI is resolution-independent and Faithful-32x safe.

---

## 🎨 Rendering notes

### Hitboxes
- Box/corner/cross/filled styles, colors, width, fill opacity, expand, range, labels.
- Depth state is saved, forced with raw `glEnable(GL_DEPTH_TEST)` + `glDepthFunc(GL_LEQUAL)`, then restored. There is **no** depth-disable path.

### Motion Blur
- Copies the main framebuffer into a history texture (`glCopyTexSubImage2D`) and redraws it full-screen with alpha proportional to smoothed player/camera velocity.
- Skips when a screen is open or a world is absent; texture is reallocated on resize.

### Item Physics
- Replaces the `EntityItem` renderer in `RenderManager.entityRenderMap` with `RenderFlatItem`, which draws the item's particle sprite as a flat, spinning quad.
- Restores the original renderer when disabled.

### GL Debug
- Applies capability overrides at `RenderTickEvent.START` and restores them at `END`, recording the previous state first. Depth test is intentionally not exposed.

---

## ➕ Adding a module

1. Extend `BKModule` (or `HudModule`).
2. Return `name()`, `category()`, `id()`, `icon()`.
3. Implement the hooks you need.
4. Deserialize state through `config.toggle(id + ".on", default)` etc.
5. Add it to `ModuleManager`'s constructor.
6. Build with `clean build` and test.

No changes to `ClientEvents` are required.

---

## 🧪 Build & verify

```bash
export JAVA_HOME=/path/to/jdk8
gradle clean build
```

`docs/VERIFY.md` tracks what is implemented, what is deferred, and what still needs a live-client run.

---

## ⚠️ Limitations

- Not runtime-tested in every GUI scale / OptiFine configuration.
- Item Physics is a flat-sprite approximation.
- Motion Blur is an afterimage, not a shader-based directional blur.
- No combat automation, ESP, reach, scaffold, velocity, injector, or token login.
