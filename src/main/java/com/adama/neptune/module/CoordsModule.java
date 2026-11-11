package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoordsModule extends HudModule {
    private final List<Setting> settings;
    private final List<String> cached = new ArrayList<>();
    private long cachedAt;

    public CoordsModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.coordsEnabled, value -> {
                    config.coordsEnabled = value;
                    config.markDirty();
                }),
                new BoolSetting("Show FPS", () -> config.coordsShowFps, value -> {
                    config.coordsShowFps = value;
                    config.markDirty();
                }),
                new BoolSetting("Show chunk", () -> config.coordsShowChunk, value -> {
                    config.coordsShowChunk = value;
                    config.markDirty();
                }),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.coordsX,
                        value -> {
                            config.coordsX = (int) Math.round(value);
                            config.markDirty();
                        }),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.coordsY,
                        value -> {
                            config.coordsY = (int) Math.round(value);
                            config.markDirty();
                        })
        );
    }

    @Override
    public String name() {
        return "Coordinates";
    }

    @Override
    public boolean isEnabled() {
        return config.coordsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.coordsEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    private void refresh() {
        Minecraft mc = Minecraft.getMinecraft();
        cached.clear();
        if (mc.thePlayer == null) return;
        cached.add("XYZ " + MathHelper.floor_double(mc.thePlayer.posX) + " "
                + MathHelper.floor_double(mc.thePlayer.posY) + " "
                + MathHelper.floor_double(mc.thePlayer.posZ));
        if (config.coordsShowChunk) {
            cached.add("Chunk " + mc.thePlayer.chunkCoordX + " " + mc.thePlayer.chunkCoordZ);
        }
        String[] directions = {"south", "west", "north", "east"};
        int index = MathHelper.floor_double(mc.thePlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        cached.add("Facing " + directions[index]);
        if (config.coordsShowFps) {
            cached.add("FPS " + Minecraft.getDebugFPS());
        }
        cachedAt = Minecraft.getSystemTime();
    }

    private List<String> lines() {
        if (cached.isEmpty() || Minecraft.getSystemTime() - cachedAt > 100L) {
            refresh();
        }
        return cached;
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        int y = config.coordsY;
        for (String line : lines()) {
            mc.fontRendererObj.drawStringWithShadow(line, config.coordsX, y, 0xFFFFFFFF);
            y += 10;
        }
    }

    @Override
    public int width() {
        Minecraft mc = Minecraft.getMinecraft();
        int width = 0;
        for (String line : lines()) {
            width = Math.max(width, mc.fontRendererObj.getStringWidth(line));
        }
        return width;
    }

    @Override
    public int height() {
        return lines().size() * 10;
    }
}
