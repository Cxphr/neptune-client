# VERIFY — Neptune Client 1.1.0

Built: `neptune-client-1.1.0.jar` (copied to `~/Desktop/NeptuneClient-1.1.0.jar`).
Build: ForgeGradle 2.1 + Gradle 4.10.3 + Temurin 8u512, Minecraft
1.8.9-11.15.1.2318, MCP stable_20. Compiles and assembles clean.

## Modules in 1.1.0
- Movement: Freelook, Smooth Zoom, Toggle Sprint
- Render: Motion Blur (velocity), Item Physics, Hitboxes (box/corners/cross/filled,
  colors, line width, fill opacity, expand, range, labels), Fullbright,
  Crosshair, Block Outline, Chunk Borders, FOV Changer,
  GL Debug (lighting/blend/textures/cull/alpha/fog off, wireframe, thick lines;
  state saved and restored each frame)
- HUD: FPS, Clock, Stopwatch, Day, Ping, Memory, Playtime, Saturation,
  Server Address, Item Counter, Combo, Reach, Direction, Potion Status,
  Coordinates, Keystrokes+CPS, Armor, Vertical Health Bar,
  Bed Finder (nearby beds + surrounding block counts in a HUD list)
- Utility: AutoGG (opt-in chat), Chat Timestamps
- Client: Click GUI (icons, blue-purple accent), on-screen Keybinds GUI
  (65% / TKL / ThinkPad / 100%, mouse buttons), debounced config saves.

## Depth / hitboxes
- Raw `glEnable(GL_DEPTH_TEST)` + `glDepthFunc(GL_LEQUAL)` with save/restore.
- No `glDisable(GL_DEPTH_TEST)` path; boxes are occluded by terrain.

## Not included / deferred
Waypoints, MiniMap, 3D Skins, 2D Items, Clear Glass, Color Saturation,
Damage Tint, Hurt Cam, Inventory Blur, Fog Customizer, Particles,
Perspective, Time/Weather Changer, Scrollable Tooltips, Better Sounds,
1.7 Animations, Nametags, Chat mods beyond timestamps, WorldEdit CUI,
Nick Hider, Pack Organizer, Item Tracker, Hypixel/API modules, and all
combat/ESP/reach/scaffold/velocity/injector/token features.

Bed Finder scans loaded world data only; it does not render anything
through walls. GL Debug intentionally has no depth-test toggle, so there
is still no depth-disable/through-wall path anywhere in the client.

## Unverified
- Not yet run in a live 1.8.9 Forge client.
- Item Physics uses a flat sprite renderer; visual quality vs Lunar is approximate.
- Motion blur auto-disables when a screen is open; OptiFine shader behavior untested.
