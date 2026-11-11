package com.adama.neptune.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class NeptuneConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private transient File file;
    private transient long dirtyAt;

    public boolean freelookEnabled = true;
    public int freelookKey = Keyboard.KEY_LMENU;
    public float freelookSensitivity = 1.0F;

    public boolean zoomEnabled = true;
    public int zoomKey = Keyboard.KEY_C;
    public float zoomFactor = 4.0F;
    public float zoomStep = 0.5F;
    public float zoomMin = 1.5F;
    public float zoomMax = 8.0F;
    public int zoomDurationMs = 200;
    public boolean scaleSensitivity = true;
    public boolean showZoomHud = false;

    public int guiKey = Keyboard.KEY_RSHIFT;

    public boolean hitboxesEnabled = true;
    public int hitboxColor = 0xFFFF4040;
    public int hitboxSneakColor = 0xFF40A0FF;
    public float hitboxRange = 48.0F;
    public int hitboxToggleKey = Keyboard.KEY_H;
    public int hitboxStyle = 0;
    public float hitboxLineWidth = 1.5F;
    public float hitboxFillOpacity = 0.18F;
    public float hitboxExpand = 0.0F;
    public boolean hitboxShowLabel = false;
    public boolean hitboxShowInvisible = false;

    public boolean coordsEnabled = true;
    public int coordsX = 4;
    public int coordsY = 4;
    public boolean coordsShowFps = true;
    public boolean coordsShowChunk = true;

    public boolean keystrokesEnabled = true;
    public int keystrokesX = 4;
    public int keystrokesY = 46;
    public boolean keystrokesShowCps = true;

    public boolean armorEnabled = true;
    public int armorX = 4;
    public int armorY = 112;

    public boolean toggleSprintEnabled = false;
    public boolean fullbrightEnabled = false;

    public boolean motionBlurEnabled = false;
    public float motionBlurIntensity = 0.35F;
    public float motionBlurMax = 0.6F;
    public float motionBlurThreshold = 0.02F;
    public boolean motionBlurCamera = true;
    public boolean motionBlurHalfRes = false;

    public boolean itemPhysicsEnabled = false;
    public float itemPhysicsRotation = 0.5F;

    public float animationSpeed = 1.0F;

    public Map<String, Boolean> toggles = new LinkedHashMap<>();
    public Map<String, Integer> positionsX = new LinkedHashMap<>();
    public Map<String, Integer> positionsY = new LinkedHashMap<>();
    public Map<String, Double> numbers = new LinkedHashMap<>();
    public Map<String, Integer> colors = new LinkedHashMap<>();
    public Map<String, Integer> keybinds = new LinkedHashMap<>();
    public Map<String, String> strings = new LinkedHashMap<>();

    public boolean toggle(String id, boolean def) {
        Boolean value = toggles.get(id);
        return value == null ? def : value;
    }

    public void setToggle(String id, boolean value) {
        toggles.put(id, value);
        markDirty();
    }

    public int posX(String id, int def) {
        Integer value = positionsX.get(id);
        return value == null ? def : value;
    }

    public int posY(String id, int def) {
        Integer value = positionsY.get(id);
        return value == null ? def : value;
    }

    public void setPos(String id, int x, int y) {
        positionsX.put(id, x);
        positionsY.put(id, y);
        markDirty();
    }

    public double number(String id, double def) {
        Double value = numbers.get(id);
        return value == null ? def : value;
    }

    public void setNumber(String id, double value) {
        numbers.put(id, value);
        markDirty();
    }

    public int color(String id, int def) {
        Integer value = colors.get(id);
        return value == null ? def : value;
    }

    public void setColor(String id, int value) {
        colors.put(id, value);
        markDirty();
    }

    public int key(String id, int def) {
        Integer value = keybinds.get(id);
        return value == null ? def : value;
    }

    public void setKey(String id, int value) {
        keybinds.put(id, value);
        markDirty();
    }

    public String text(String id, String def) {
        String value = strings.get(id);
        return value == null ? def : value;
    }

    public void setText(String id, String value) {
        strings.put(id, value);
        markDirty();
    }

    public void markDirty() {
        if (dirtyAt == 0L) dirtyAt = System.currentTimeMillis();
    }

    public void tick() {
        if (dirtyAt > 0L && System.currentTimeMillis() - dirtyAt > 400L) {
            save();
            dirtyAt = 0L;
        }
    }

    public static NeptuneConfig load(File dir) {
        File target = new File(dir, "neptune.json");
        NeptuneConfig cfg = null;
        if (target.exists()) {
            try (FileReader reader = new FileReader(target)) {
                cfg = GSON.fromJson(reader, NeptuneConfig.class);
            } catch (Exception ignored) {
            }
        }
        if (cfg == null) cfg = new NeptuneConfig();
        cfg.file = target;
        cfg.save();
        return cfg;
    }

    public void save() {
        if (file == null) return;
        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try (Writer writer = new FileWriter(temp)) {
            GSON.toJson(this, writer);
        } catch (IOException ignored) {
            return;
        }
        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }
}
