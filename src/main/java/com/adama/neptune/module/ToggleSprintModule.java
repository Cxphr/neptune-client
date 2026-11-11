package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class ToggleSprintModule extends BKModule {
    private final List<Setting> settings;

    public ToggleSprintModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.toggleSprintEnabled, value -> {
                    config.toggleSprintEnabled = value;
                    config.markDirty();
                })
        );
    }

    @Override
    public String name() {
        return "Toggle Sprint";
    }

    @Override
    public String category() {
        return "Movement";
    }

    @Override
    public boolean isEnabled() {
        return config.toggleSprintEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.toggleSprintEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onClientTick() {
        if (!config.toggleSprintEnabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        boolean forward = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        if (forward && !mc.thePlayer.isSneaking() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
