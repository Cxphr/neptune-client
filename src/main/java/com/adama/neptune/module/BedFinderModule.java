package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.util.GlUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BedFinderModule extends HudModule {
    private final List<Setting> settings;
    private final List<Bed> beds = new ArrayList<>();
    private long lastScan;

    private static final class Count {
        final ItemStack stack;
        final int amount;

        Count(ItemStack stack, int amount) {
            this.stack = stack;
            this.amount = amount;
        }
    }

    private static final class Bed {
        final BlockPos pos;
        final double distance;
        final String direction;
        final List<Count> counts;
        final String summary;

        Bed(BlockPos pos, double distance, String direction, List<Count> counts, String summary) {
            this.pos = pos;
            this.distance = distance;
            this.direction = direction;
            this.counts = counts;
            this.summary = summary;
        }
    }

    public BedFinderModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new BoolSetting("World overlay", () -> config.toggle("bedfinder.overlay", true),
                        value -> config.setToggle("bedfinder.overlay", value)),
                new BoolSetting("Block icons", () -> config.toggle("bedfinder.icons", true),
                        value -> config.setToggle("bedfinder.icons", value)),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.posX("bedfinder", 4),
                        value -> config.setPos("bedfinder", (int) Math.round(value), config.posY("bedfinder", 250))),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.posY("bedfinder", 250),
                        value -> config.setPos("bedfinder", config.posX("bedfinder", 4), (int) Math.round(value))),
                new SliderSetting("Radius", 4.0D, 32.0D, 1.0D, 0,
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
    public void onClientTick() {
        if (!isEnabled()) return;
        long now = System.currentTimeMillis();
        if (now - lastScan > 500L) {
            scan();
            lastScan = now;
        }
    }

    private void scan() {
        beds.clear();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        int radius = (int) config.number("bedfinder.radius", 12.0D);
        BlockPos center = new BlockPos((int) Math.floor(mc.thePlayer.posX),
                (int) Math.floor(mc.thePlayer.posY), (int) Math.floor(mc.thePlayer.posZ));
        List<BlockPos> found = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int y = center.getY() + dy;
                    if (y < 0 || y > 255) continue;
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed) {
                        found.add(pos);
                    }
                }
            }
        }
        found.sort(Comparator.comparingDouble(pos -> distanceSq(pos, center)));
        List<BlockPos> shown = new ArrayList<>();
        for (BlockPos pos : found) {
            if (shown.size() >= 8) break;
            boolean duplicate = false;
            for (BlockPos other : shown) {
                if (Math.abs(other.getX() - pos.getX()) <= 1 && Math.abs(other.getY() - pos.getY()) <= 1
                        && Math.abs(other.getZ() - pos.getZ()) <= 1) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;
            shown.add(pos);
            int dx = pos.getX() - center.getX();
            int dz = pos.getZ() - center.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            List<Count> counts = surrounding(mc, pos);
            beds.add(new Bed(pos, distance, direction(dx, dz), counts, summary(counts)));
        }
    }

    private List<Count> surrounding(Minecraft mc, BlockPos bed) {
        Map<String, Count> counts = new HashMap<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = bed.add(dx, dy, dz);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    Block block = state.getBlock();
                    if (block instanceof BlockBed) continue;
                    if (block.getMaterial().isReplaceable()) continue;
                    int meta = block.getMetaFromState(state);
                    ItemStack stack = new ItemStack(block, 1, meta);
                    String name = stack.getDisplayName();
                    String key = name + "#" + meta;
                    Count existing = counts.get(key);
                    if (existing == null) {
                        counts.put(key, new Count(stack, 1));
                    } else {
                        counts.put(key, new Count(existing.stack, existing.amount + 1));
                    }
                }
            }
        }
        List<Count> list = new ArrayList<>(counts.values());
        list.sort((a, b) -> Integer.compare(b.amount, a.amount));
        return list;
    }

    private static String summary(List<Count> counts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < counts.size() && i < 3; i++) {
            if (i > 0) builder.append("  ");
            builder.append(counts.get(i).stack.getDisplayName()).append(" x").append(counts.get(i).amount);
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
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        int x = config.posX("bedfinder", 4);
        int y = config.posY("bedfinder", 250);
        boolean icons = config.toggle("bedfinder.icons", true);
        if (beds.isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("No beds nearby", x, y, 0xFF9AA6B8);
            return;
        }
        for (Bed bed : beds) {
            String text = "Bed " + Math.round(bed.distance) + "m " + bed.direction + "  " + bed.summary;
            mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFE6EDF3);
            y += 10;
            if (icons && !bed.counts.isEmpty()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                RenderHelper.enableGUIStandardItemLighting();
                int iconX = x;
                for (int i = 0; i < bed.counts.size() && i < 4; i++) {
                    Count count = bed.counts.get(i);
                    mc.getRenderItem().renderItemAndEffectIntoGUI(count.stack, iconX, y);
                    mc.fontRendererObj.drawStringWithShadow("x" + count.amount, iconX + 17, y + 5, 0xFFCFE3FF);
                    iconX += 40;
                }
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                y += 18;
            }
        }
    }

    @Override
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || !config.toggle("bedfinder.overlay", true)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;

        boolean depthWas = GlUtil.get(GL11.GL_DEPTH_TEST);
        boolean blendWas = GlUtil.get(GL11.GL_BLEND);
        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        boolean textureWas = GlUtil.get(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(1.5F);
        GlUtil.set(GL11.GL_DEPTH_TEST, true);
        GlUtil.set(GL11.GL_BLEND, true);
        GlUtil.set(GL11.GL_TEXTURE_2D, false);
        GlUtil.set(GL11.GL_LIGHTING, false);
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (Bed bed : beds) {
            AxisAlignedBB box = new AxisAlignedBB(bed.pos, bed.pos.add(1, 1, 1)).expand(0.01D, 0.01D, 0.01D)
                    .offset(-viewerX, -viewerY, -viewerZ);
            edges(renderer, box, 0.35F, 0.75F, 1.0F, 0.9F);
        }
        tessellator.draw();

        GlUtil.set(GL11.GL_DEPTH_TEST, depthWas);
        GlUtil.set(GL11.GL_BLEND, blendWas);
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWas);
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
        GlStateManager.depthMask(true);
        GL11.glLineWidth(1.0F);

        GlUtil.set(GL11.GL_TEXTURE_2D, true);
        for (Bed bed : beds) {
            String text = "Bed " + Math.round(bed.distance) + "m " + bed.direction;
            GlStateManager.pushMatrix();
            GlStateManager.translate(bed.pos.getX() + 0.5D - viewerX, bed.pos.getY() + 1.4D - viewerY,
                    bed.pos.getZ() + 0.5D - viewerZ);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.025F, -0.025F, 0.025F);
            mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2, 0, 0xFF8FD3FF);
            GlStateManager.popMatrix();
        }
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWas);
    }

    private void edges(WorldRenderer renderer, AxisAlignedBB box, float red, float green, float blue, float alpha) {
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        edge(renderer, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
        edge(renderer, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
        edge(renderer, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        edge(renderer, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);
        edge(renderer, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
        edge(renderer, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
        edge(renderer, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        edge(renderer, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
        edge(renderer, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
        edge(renderer, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
        edge(renderer, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
        edge(renderer, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);
    }

    private void edge(WorldRenderer renderer, double x1, double y1, double z1, double x2, double y2, double z2,
                      float red, float green, float blue, float alpha) {
        renderer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public int width() {
        Minecraft mc = Minecraft.getMinecraft();
        int width = 0;
        for (Bed bed : beds) {
            width = Math.max(width, mc.fontRendererObj.getStringWidth("Bed " + Math.round(bed.distance) + "m "
                    + bed.direction + "  " + bed.summary));
        }
        return width;
    }

    @Override
    public int height() {
        boolean icons = config.toggle("bedfinder.icons", true);
        int row = icons ? 28 : 10;
        return Math.max(row, beds.size() * row);
    }
}
