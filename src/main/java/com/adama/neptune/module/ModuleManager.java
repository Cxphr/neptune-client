package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.ClickGui;
import com.adama.neptune.input.CapturingMouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private final NeptuneConfig config;
    private final List<BKModule> modules = new ArrayList<>();
    private final FreelookModule freelook;
    private final SmoothZoomModule zoom;
    private final HitboxModule hitboxes;
    private final KeystrokesModule keystrokes;
    private final FullbrightModule fullbright;
    private final ItemPhysicsModule itemPhysics;
    private final MotionBlurModule motionBlur;
    private final Map<String, Boolean> keyState = new HashMap<>();

    private KeyBinding guiKey;
    private KeyBinding freelookKey;
    private KeyBinding zoomKey;
    private KeyBinding hitboxKey;

    private boolean guiDown;
    private boolean mouseInstalled;

    public ModuleManager(NeptuneConfig config) {
        this.config = config;
        this.freelook = new FreelookModule(config);
        this.zoom = new SmoothZoomModule(config);
        this.hitboxes = new HitboxModule(config);
        this.keystrokes = new KeystrokesModule(config);
        this.fullbright = new FullbrightModule(config);
        this.itemPhysics = new ItemPhysicsModule(config);
        this.motionBlur = new MotionBlurModule(config);

        modules.add(freelook);
        modules.add(zoom);
        modules.add(new ToggleSprintModule(config));
        modules.add(motionBlur);
        modules.add(hitboxes);
        modules.add(fullbright);
        modules.add(itemPhysics);

        modules.add(new FpsHud(config));
        modules.add(new ClockHud(config));
        modules.add(new StopwatchHud(config));
        modules.add(new DayHud(config));
        modules.add(new PingHud(config));
        modules.add(new MemoryHud(config));
        modules.add(new PlaytimeHud(config));
        modules.add(new SaturationHud(config));
        modules.add(new ServerHud(config));
        modules.add(new ItemCounterHud(config));
        modules.add(new ComboHud(config));
        modules.add(new ReachHud(config));
        modules.add(new DirectionHud(config));
        modules.add(new PotionHud(config));
        modules.add(new HealthBarModule(config));
        modules.add(new BedFinderModule(config));
        modules.add(new CoordsModule(config));
        modules.add(keystrokes);
        modules.add(new ArmorModule(config));

        modules.add(new CrosshairModule(config));
        modules.add(new BlockOutlineModule(config));
        modules.add(new ChunkBordersModule(config));
        modules.add(new FovModule(config));
        modules.add(new GlDebugModule(config));

        modules.add(new AutoGGModule(config));
        modules.add(new ChatTimestampsModule(config));

        fullbright.sync();
    }

    public void registerKeybinds() {
        guiKey = new KeyBinding("key.neptune.gui", config.guiKey, "key.categories.neptune");
        freelookKey = new KeyBinding("key.neptune.freelook", config.freelookKey, "key.categories.neptune");
        zoomKey = new KeyBinding("key.neptune.zoom", config.zoomKey, "key.categories.neptune");
        hitboxKey = new KeyBinding("key.neptune.hitboxes", config.hitboxToggleKey, "key.categories.neptune");
        ClientRegistry.registerKeyBinding(guiKey);
        ClientRegistry.registerKeyBinding(freelookKey);
        ClientRegistry.registerKeyBinding(zoomKey);
        ClientRegistry.registerKeyBinding(hitboxKey);
    }

    public FreelookModule freelook() {
        return freelook;
    }

    public SmoothZoomModule zoom() {
        return zoom;
    }

    public KeystrokesModule keystrokes() {
        return keystrokes;
    }

    public List<BKModule> modules() {
        return modules;
    }

    public List<BKModule> byCategory(String category) {
        List<BKModule> result = new ArrayList<>();
        for (BKModule module : modules) {
            if (module.category().equals(category)) result.add(module);
        }
        return result;
    }

    public int bindingCode(String id) {
        if ("gui".equals(id)) return config.guiKey;
        if ("freelook".equals(id)) return config.freelookKey;
        if ("zoom".equals(id)) return config.zoomKey;
        if ("toggle.hitboxes".equals(id)) return config.key("toggle.hitboxes", config.hitboxToggleKey);
        if (id.startsWith("toggle.")) return config.key(id, 0);
        return 0;
    }

    public void setBindingCode(String id, int code) {
        if ("gui".equals(id)) {
            config.guiKey = code;
            if (guiKey != null) guiKey.setKeyCode(code);
        } else if ("freelook".equals(id)) {
            config.freelookKey = code;
            if (freelookKey != null) freelookKey.setKeyCode(code);
        } else if ("zoom".equals(id)) {
            config.zoomKey = code;
            if (zoomKey != null) zoomKey.setKeyCode(code);
        } else if ("toggle.hitboxes".equals(id)) {
            config.hitboxToggleKey = code;
            if (hitboxKey != null) hitboxKey.setKeyCode(code);
            config.setKey(id, code);
        } else if (id.startsWith("toggle.")) {
            config.setKey(id, code);
        }
        config.markDirty();
    }

    private static boolean isDown(int code) {
        if (code == 0) return false;
        if (code >= -100 && code <= -98) {
            return Mouse.isButtonDown(-100 - code);
        }
        return Keyboard.isKeyDown(code);
    }

    private void ensureMouseHelper() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mouseInstalled) return;
        if (mc.mouseHelper == null) return;
        if (!(mc.mouseHelper instanceof CapturingMouseHelper)) {
            mc.mouseHelper = new CapturingMouseHelper(this);
        }
        mouseInstalled = true;
    }

    public void onClientTick() {
        config.tick();
        Minecraft mc = Minecraft.getMinecraft();
        ensureMouseHelper();
        itemPhysics.sync();

        boolean inWorld = mc.thePlayer != null && mc.theWorld != null;
        boolean screenOpen = mc.currentScreen != null;

        if (inWorld) {
            freelook.setActive(config.freelookEnabled && !screenOpen && isDown(config.freelookKey));
            for (BKModule module : modules) {
                int code = bindingCode("toggle." + module.id());
                if (code != 0) {
                    boolean down = !screenOpen && isDown(code);
                    if (down && !Boolean.TRUE.equals(keyState.get(module.id()))) {
                        module.setEnabled(!module.isEnabled());
                    }
                    keyState.put(module.id(), down);
                }
            }
        } else {
            freelook.setActive(false);
            zoom.resetZoom();
        }

        boolean gui = isDown(config.guiKey);
        if (gui && !guiDown) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ClickGui(config, this));
            } else if (mc.currentScreen instanceof ClickGui) {
                mc.displayGuiScreen(null);
            }
        }
        guiDown = gui;

        for (BKModule module : modules) {
            module.onClientTick();
        }
    }

    public void onRenderTick(float dt) {
        for (BKModule module : modules) {
            module.onRenderTick(dt);
        }
    }

    public void onGuiOpen() {
        freelook.setActive(false);
    }

    public void renderHud() {
        for (BKModule module : modules) {
            if (module instanceof HudModule && module.isEnabled()) {
                ((HudModule) module).render();
            }
        }
    }
}
