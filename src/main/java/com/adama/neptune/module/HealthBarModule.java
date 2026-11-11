package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.Arrays;
import java.util.List;

public class HealthBarModule extends HudModule {
    private final List<Setting> settings;

    public HealthBarModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new BoolSetting("Show numbers", () -> config.toggle("healthbar.numbers", true),
                        value -> config.setToggle("healthbar.numbers", value)),
                new BoolSetting("Absorption", () -> config.toggle("healthbar.absorption", true),
                        value -> config.setToggle("healthbar.absorption", value)),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.posX("healthbar", 4),
                        value -> config.setPos("healthbar", (int) Math.round(value), config.posY("healthbar", 180))),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.posY("healthbar", 180),
                        value -> config.setPos("healthbar", config.posX("healthbar", 4), (int) Math.round(value))),
                new SliderSetting("Width", 3.0D, 12.0D, 1.0D, 0,
                        () -> config.number("healthbar.width", 6.0D),
                        value -> config.setNumber("healthbar.width", value)),
                new SliderSetting("Height", 30.0D, 120.0D, 1.0D, 0,
                        () -> config.number("healthbar.height", 64.0D),
                        value -> config.setNumber("healthbar.height", value))
        );
    }

    @Override
    public String name() {
        return "Health Bar";
    }

    @Override
    public String id() {
        return "healthbar";
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("healthbar.on", true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("healthbar.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        int x = config.posX("healthbar", 4);
        int y = config.posY("healthbar", 180);
        int width = (int) config.number("healthbar.width", 6.0D);
        int height = (int) config.number("healthbar.height", 64.0D);

        float max = mc.thePlayer.getMaxHealth();
        float health = Math.min(mc.thePlayer.getHealth(), max);
        float absorption = config.toggle("healthbar.absorption", true) ? mc.thePlayer.getAbsorptionAmount() : 0.0F;
        float fillRatio = max <= 0.0F ? 0.0F : health / max;

        Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF0B0F16);
        Gui.drawRect(x, y, x + width, y + height, 0xFF1A2230);

        int fill = Math.round(height * fillRatio);
        int color = healthColor(fillRatio);
        Gui.drawRect(x, y + height - fill, x + width, y + height, color);

        if (absorption > 0.0F) {
            int absorb = Math.min(height - fill, Math.round(height * (absorption / max)));
            Gui.drawRect(x, y + height - fill - absorb, x + width, y + height - fill, 0xFFFFC94D);
        }

        if (config.toggle("healthbar.numbers", true)) {
            String text = (int) Math.ceil(health) + "/" + (int) max + (absorption > 0.0F ? " +" + (int) Math.ceil(absorption) : "");
            mc.fontRendererObj.drawStringWithShadow(text, x + width + 3, y + height / 2 - 4, 0xFFE6EDF3);
        }
    }

    @Override
    public int width() {
        return (int) config.number("healthbar.width", 6.0D) + 30;
    }

    @Override
    public int height() {
        return (int) config.number("healthbar.height", 64.0D);
    }

    private static int healthColor(float ratio) {
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int red = Math.round(255.0F * (1.0F - ratio));
        int green = Math.round(255.0F * ratio);
        return 0xFF000000 | (red << 16) | (green << 8) | 0x30;
    }
}
