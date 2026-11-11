# 🚀 Neptune Client

> A clean-room **Minecraft 1.8.9 Forge** client focused on QoL, HUD and rendering — with a custom click GUI and an on-screen keybind editor.

<p align="left">
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.8.9-3B82F6?style=flat-square">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-11.15.1.2318-8B5CF6?style=flat-square">
  <img alt="Java" src="https://img.shields.io/badge/Java-8-orange?style=flat-square">
  <img alt="Built" src="https://img.shields.io/badge/build-Gradle%204.10.3-22c55e?style=flat-square">
</p>

Neptune is original code. It does **not** contain Lunar Client code, assets, or branding, and it never disables the depth test for entity rendering.

---

## ✨ Features

### 🎯 Movement
- 🎥 **Freelook** — look around without changing your movement axis (Left Alt)
- 🔍 **Smooth Zoom** — eased FOV, wheel-adjustable, sensitivity scaling (C)
- 🏃 **Toggle Sprint**

### 🎨 Render
- 🫥 **Hitboxes** — box / corners / cross / filled, colors, line width, fill opacity, expand, range, labels
- 🌫️ **Motion Blur** — velocity-driven afterimage with intensity / max-opacity / threshold sliders
- 📦 **Item Physics** — dropped items lie flat and spin (rotation-speed slider)
- ☀️ **Fullbright**
- ➕ **Crosshair**, **Block Outline**, **Chunk Borders**, **FOV Changer**
- 🧪 **GL Debug** — lighting/blend/textures/cull/alpha/fog off, wireframe, thick lines (state restored every frame)

### 📊 HUD
- 🧭 Coordinates · 📶 FPS · 🕒 Clock · ⏱️ Stopwatch · 📅 Day Counter
- 📡 Ping · 💾 Memory · ⏳ Playtime · 🍗 Saturation · 🌐 Server Address
- 🎒 Item Counter · 🔥 Combo · 🎯 Reach · ➡️ Direction · 🧪 Potion Status
- ⌨️ Keystrokes + CPS · 🛡️ Armor + held item
- ❤️ **Vertical Health Bar** (health + absorption, numbers, size sliders)
- 🛏️ **Bed Finder** — nearby beds with distance, direction and surrounding block counts

### 🛠️ Utility
- 💬 **AutoGG** (opt-in chat, configurable delay/cooldown)
- 🕰️ **Chat Timestamps**

### 🖥️ Client
- ⌨️ **Ctrl+R** opens the GUI (RShift still works)
- 🗂️ Single page with collapsible **category bars** (cheat-client style) — no separate pages
- 🎨 **Appearance** bar: module background, border, text, muted and accent colors
- 🖼️ Space background image on the home screen and behind the GUI
- 🔘 Card mode and compact vertical mode, switchable in the header
- 🎹 Inline keybind capture on each module (click the key chip, press a key)
- 🪟 Window Mode module: windowed / borderless / fullscreen
- 💾 Debounced JSON config at `config/neptune.json`
- 🧵 Shared easing utilities for smooth GUI/zoom animation

---

## 📦 Install

1. Install **Minecraft 1.8.9** with **Forge 11.15.1.2318**.
2. Drop `neptune-client-1.2.0.jar` into `.minecraft/mods/`.
3. Launch, press **RightShift** for the GUI.

Default keys: `LAlt` freelook · `C` zoom · `H` hitbox toggle · `RightShift` GUI. Rebind everything in the GUI → **Keybinds**.

---

## 🛠️ Build

Requires **JDK 8** and **Gradle 4.10.3** (ForgeGradle 2.1, MCP `stable_20`).

```bash
export JAVA_HOME=/path/to/jdk8
gradle clean build
# jar -> build/libs/neptune-client-1.2.0.jar
```

> The incremental compiler can miss package-private classes after large edits; `clean build` is the reliable path.

---

## 🧠 How it works

See **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** for the event flow, module lifecycle, config system, GUI structure and a guide to adding modules. Verification notes live in **[docs/VERIFY.md](docs/VERIFY.md)**.

---

## 🔒 Scope

- Client-side only; no packet manipulation, no automation, no injector.
- Depth test is explicitly enabled for world-space rendering — hitboxes are occluded by terrain. There is no through-wall/X-ray path.
- AutoGG sends chat automatically; use it only where server rules allow.
