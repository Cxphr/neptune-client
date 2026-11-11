package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.List;

public class FullbrightModule extends BKModule {
    private final List<Setting> settings;
    private float originalGamma = -1.0F;

    public FullbrightModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.fullbrightEnabled, value -> {
                    config.fullbrightEnabled = value;
                    apply();
                    config.markDirty();
                })
        );
    }

    @Override
    public String name() {
        return "Fullbright";
    }

    @Override
    public String category() {
        return "Render";
    }

    @Override
    public boolean isEnabled() {
        return config.fullbrightEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.fullbrightEnabled = enabled;
        apply();
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    public void sync() {
        apply();
    }

    private void apply() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings == null) return;
        if (config.fullbrightEnabled) {
            if (originalGamma < 0.0F) originalGamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 10.0F;
        } else if (originalGamma >= 0.0F) {
            mc.gameSettings.gammaSetting = originalGamma;
            originalGamma = -1.0F;
        }
    }

    @Override
    public void onReset() {
        config.fullbrightEnabled = false;
        apply();
        config.markDirty();
    }
}
