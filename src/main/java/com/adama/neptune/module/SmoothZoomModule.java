package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class SmoothZoomModule extends BKModule {
    private final List<Setting> settings;
    private KeyBinding keyBinding;
    private float progress;
    private float factor = 1.0F;

    public SmoothZoomModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.zoomEnabled, value -> {
                    config.zoomEnabled = value;
                    if (!value) resetZoom();
                    config.markDirty();
                }),
                new SliderSetting("Zoom", 1.5D, 8.0D, 0.1D, 1,
                        () -> config.zoomFactor,
                        value -> {
                            config.zoomFactor = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Duration", 0.0D, 600.0D, 10.0D, 0,
                        () -> config.zoomDurationMs,
                        value -> {
                            config.zoomDurationMs = (int) Math.round(value);
                            config.markDirty();
                        }),
                new BoolSetting("Scale sensitivity", () -> config.scaleSensitivity, value -> {
                    config.scaleSensitivity = value;
                    config.markDirty();
                }),
                new BoolSetting("Zoom indicator", () -> config.showZoomHud, value -> {
                    config.showZoomHud = value;
                    config.markDirty();
                })
        );
    }

    @Override
    public String name() {
        return "Smooth Zoom";
    }

    @Override
    public String category() {
        return "Movement";
    }

    @Override
    public boolean isEnabled() {
        return config.zoomEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.zoomEnabled = enabled;
        if (!enabled) resetZoom();
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    public void setKeyBinding(KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
    }

    public KeyBinding keyBinding() {
        return keyBinding;
    }

    private boolean keyDown() {
        return Keyboard.isKeyDown(config.zoomKey);
    }

    @Override
    public void onRenderTick(float dt) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean held = config.zoomEnabled && mc.currentScreen == null && keyDown();
        float target = held ? 1.0F : 0.0F;
        float duration = Math.max(1, config.zoomDurationMs) / 1000.0F;
        float step = dt / duration;
        if (progress < target) progress = Math.min(target, progress + step);
        else if (progress > target) progress = Math.max(target, progress - step);
        float eased = progress * progress * (3.0F - 2.0F * progress);
        factor = 1.0F + (config.zoomFactor - 1.0F) * eased;
    }

    public float sensitivityScale() {
        if (!config.zoomEnabled || !config.scaleSensitivity) return 1.0F;
        return 1.0F / Math.max(1.0F, factor);
    }

    public void applyFov(EntityViewRenderEvent.FOVModifier event) {
        if (factor > 1.001F) {
            event.setFOV(event.getFOV() / factor);
        }
    }

    public void onMouseWheel(MouseEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!config.zoomEnabled || event.dwheel == 0 || mc.currentScreen != null) return;
        if (!keyDown()) return;
        float next = config.zoomFactor + Math.signum(event.dwheel) * config.zoomStep;
        config.zoomFactor = MathHelper.clamp_float(next, config.zoomMin, config.zoomMax);
        config.markDirty();
        event.setCanceled(true);
    }

    @Override
    public void onMouse(MouseEvent event) {
        onMouseWheel(event);
    }

    public void renderHud() {
        if (!config.showZoomHud || factor <= 1.01F) return;
        Minecraft mc = Minecraft.getMinecraft();
        String text = String.format("Zoom %.1fx", factor);
        int screenWidth = new ScaledResolution(mc).getScaledWidth();
        int width = mc.fontRendererObj.getStringWidth(text);
        mc.fontRendererObj.drawStringWithShadow(text, (screenWidth - width) / 2, 4, 0xFFCFE3FF);
    }

    public void resetZoom() {
        progress = 0.0F;
        factor = 1.0F;
    }

    @Override
    public void onReset() {
        resetZoom();
    }
}
