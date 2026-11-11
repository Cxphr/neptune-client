package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class FreelookModule extends BKModule {
    private final List<Setting> settings;
    private KeyBinding keyBinding;
    private boolean active;
    private float yaw;
    private float pitch;

    public FreelookModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.freelookEnabled, value -> {
                    config.freelookEnabled = value;
                    if (!value) setActive(false);
                    config.markDirty();
                }),
                new SliderSetting("Sensitivity", 0.2D, 2.0D, 0.05D, 2,
                        () -> config.freelookSensitivity,
                        value -> {
                            config.freelookSensitivity = (float) value;
                            config.markDirty();
                        })
        );
    }

    @Override
    public String name() {
        return "Freelook";
    }

    @Override
    public String category() {
        return "Movement";
    }

    @Override
    public boolean isEnabled() {
        return config.freelookEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.freelookEnabled = enabled;
        if (!enabled) setActive(false);
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean on) {
        if (on == active) return;
        if (on) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null) return;
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        }
        active = on;
    }

    public void onMouseDelta(int dx, int dy, float zoomScale) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        f = f * f * f * 8.0F;
        f *= config.freelookSensitivity;
        f *= zoomScale;
        yaw += dx * f * 0.15F;
        float delta = (mc.gameSettings.invertMouse ? dy : -dy) * f * 0.15F;
        pitch = MathHelper.clamp_float(pitch + delta, -90.0F, 90.0F);
    }

    @Override
    public void onCamera(EntityViewRenderEvent.CameraSetup event) {
        applyCamera(event);
    }

    public void applyCamera(EntityViewRenderEvent.CameraSetup event) {
        if (!active) return;
        event.yaw = yaw;
        event.pitch = pitch;
    }

    @Override
    public void onReset() {
        setActive(false);
    }
}
