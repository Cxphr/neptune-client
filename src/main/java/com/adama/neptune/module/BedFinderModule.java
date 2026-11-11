package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedFinderModule extends HudModule {
    private final List<Setting> settings;
    private final List<String> cached = new ArrayList<>();
    private long lastScan;

    public BedFinderModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new BoolSetting("Show blocks", () -> config.toggle("bedfinder.blocks", true),
                        value -> config.setToggle("bedfinder.blocks", value)),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.posX("bedfinder", 4),
                        value -> config.setPos("bedfinder", (int) Math.round(value), config.posY("bedfinder", 250))),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.posY("bedfinder", 250),
                        value -> config.setPos("bedfinder", config.posX("bedfinder", 4), (int) Math.round(value))),
                new SliderSetting("Radius", 4.0D, 24.0D, 1.0D, 0,
                        () -> config.number("bedfinder.radius", 12.0D),
                        value -> config.setNumber("bedfinder.radius", value))
        );
    }

    @Override
    public String name() {
        return "Bed Finder";
    }

    @Override
    public String id() {
        return "bedfinder";
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("bedfinder.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("bedfinder.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan > 500L) {
            scan(mc);
            lastScan = now;
        }
        int x = config.posX("bedfinder", 4);
        int y = config.posY("bedfinder", 250);
        for (String line : cached) {
            mc.fontRendererObj.drawStringWithShadow(line, x, y, 0xFFE6EDF3);
            y += 10;
        }
    }

    private void scan(Minecraft mc) {
        cached.clear();
        int radius = (int) config.number("bedfinder.radius", 12.0D);
        BlockPos center = new BlockPos((int) Math.floor(mc.thePlayer.posX),
                (int) Math.floor(mc.thePlayer.posY), (int) Math.floor(mc.thePlayer.posZ));
        List<BlockPos> beds = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int x = center.getX() + dx;
                    int y = center.getY() + dy;
                    int z = center.getZ() + dz;
                    if (y < 0 || y > 255) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed) {
                        beds.add(pos);
                    }
                }
            }
        }
        if (beds.isEmpty()) {
            cached.add("No beds within " + radius + "m");
            return;
        }
        beds.sort(Comparator.comparingDouble(pos -> distanceSq(pos, center)));
        List<BlockPos> shownBeds = new ArrayList<>();
        for (BlockPos bed : beds) {
            if (shownBeds.size() >= 4) break;
            boolean duplicate = false;
            for (BlockPos shownBed : shownBeds) {
                if (Math.abs(shownBed.getX() - bed.getX()) <= 1
                        && Math.abs(shownBed.getY() - bed.getY()) <= 1
                        && Math.abs(shownBed.getZ() - bed.getZ()) <= 1) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;
            shownBeds.add(bed);
            int dx = bed.getX() - center.getX();
            int dz = bed.getZ() - center.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            String line = "Bed " + Math.round(distance) + "m " + direction(dx, dz);
            if (config.toggle("bedfinder.blocks", true)) {
                line += "  " + surrounding(mc, bed);
            }
            cached.add(line);
        }
    }

    private String surrounding(Minecraft mc, BlockPos bed) {
        Map<String, Integer> counts = new HashMap<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = bed.add(dx, dy, dz);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block instanceof BlockBed) continue;
                    if (block.getMaterial().isReplaceable()) continue;
                    String name = new ItemStack(block).getDisplayName();
                    counts.put(name, counts.getOrDefault(name, 0) + 1);
                }
            }
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size() && i < 3; i++) {
            if (i > 0) builder.append(", ");
            builder.append(entries.get(i).getKey()).append("x").append(entries.get(i).getValue());
        }
        return builder.length() == 0 ? "bare" : builder.toString();
    }

    private static double distanceSq(BlockPos pos, BlockPos center) {
        double dx = pos.getX() - center.getX();
        double dy = pos.getY() - center.getY();
        double dz = pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static String direction(int dx, int dz) {
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360.0D;
        if (angle < 22.5D || angle >= 337.5D) return "E";
        if (angle < 67.5D) return "SE";
        if (angle < 112.5D) return "S";
        if (angle < 157.5D) return "SW";
        if (angle < 202.5D) return "W";
        if (angle < 247.5D) return "NW";
        if (angle < 292.5D) return "N";
        return "NE";
    }

    @Override
    public int width() {
        int width = 0;
        Minecraft mc = Minecraft.getMinecraft();
        for (String line : cached) width = Math.max(width, mc.fontRendererObj.getStringWidth(line));
        return width;
    }

    @Override
    public int height() {
        return Math.max(10, cached.size() * 10);
    }
}
