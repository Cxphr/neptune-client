package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.util.Arrays;
import java.util.List;

public class WindowModule extends BKModule {
    private final List<Setting> settings;
    private int applied = -1;
    private int pending = -1;
    private long applyAt;

    public WindowModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("Mode (0 windowed, 1 borderless, 2 fullscreen)", 0.0D, 2.0D, 1.0D, 0,
                        () -> config.number("window.mode", 0.0D),
                        value -> {
                            config.setNumber("window.mode", value);
                            pending = (int) Math.round(value);
                            applyAt = System.currentTimeMillis() + 300L;
                        })
        );
    }

    @Override
    public String name() {
        return "Window Mode";
    }

    @Override
    public String id() {
        return "window";
    }

    @Override
    public String category() {
        return "Utility";
    }

    @Override
    public int icon() {
        return 3;
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("window.on", true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("window.on", enabled);
        if (!enabled) {
            pending = 0;
            applyAt = System.currentTimeMillis() + 300L;
        }
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onClientTick() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;
        if (pending < 0) return;
        if (System.currentTimeMillis() < applyAt) return;
        if (apply(mc, pending)) {
            applied = pending;
            pending = -1;
        } else {
            applyAt = System.currentTimeMillis() + 300L;
        }
    }

    private boolean apply(Minecraft mc, int mode) {
        if (mode == 2) {
            if (!Display.isFullscreen()) {
                mc.toggleFullscreen();
            }
            return true;
        }
        if (Display.isFullscreen()) {
            mc.toggleFullscreen();
            return false;
        }
        try {
            if (mode == 1) {
                DisplayMode desktop = Display.getDesktopDisplayMode();
                Display.setDisplayMode(new DisplayMode(desktop.getWidth(), desktop.getHeight()));
                Display.setLocation(0, 0);
                mc.resize(desktop.getWidth(), desktop.getHeight());
            } else if (mode == 0) {
                Display.setDisplayMode(new DisplayMode(1280, 720));
                Display.setLocation(100, 100);
                mc.resize(1280, 720);
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
