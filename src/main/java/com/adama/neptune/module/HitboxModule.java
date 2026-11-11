package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.ColorSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.util.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class HitboxModule extends BKModule {
    private final List<Setting> settings;

    public HitboxModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.hitboxesEnabled, value -> {
                    config.hitboxesEnabled = value;
                    config.markDirty();
                }),
                new SliderSetting("Style", 0.0D, 4.0D, 1.0D, 0,
                        () -> config.hitboxStyle,
                        value -> {
                            config.hitboxStyle = (int) Math.round(value);
                            config.markDirty();
                        }),
                new ColorSetting("Player", () -> config.hitboxColor, value -> {
                    config.hitboxColor = value;
                    config.markDirty();
                }),
                new ColorSetting("Sneaking", () -> config.hitboxSneakColor, value -> {
                    config.hitboxSneakColor = value;
                    config.markDirty();
                }),
                new SliderSetting("Line width", 0.5D, 3.0D, 0.1D, 1,
                        () -> config.hitboxLineWidth,
                        value -> {
                            config.hitboxLineWidth = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Fill opacity", 0.0D, 0.6D, 0.02D, 2,
                        () -> config.hitboxFillOpacity,
                        value -> {
                            config.hitboxFillOpacity = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Expand", 0.0D, 0.5D, 0.01D, 2,
                        () -> config.hitboxExpand,
                        value -> {
                            config.hitboxExpand = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Range", 16.0D, 128.0D, 1.0D, 0,
                        () -> config.hitboxRange,
                        value -> {
                            config.hitboxRange = (float) value;
                            config.markDirty();
                        }),
                new BoolSetting("Show labels", () -> config.hitboxShowLabel, value -> {
                    config.hitboxShowLabel = value;
                    config.markDirty();
                }),
                new BoolSetting("Show invisible", () -> config.hitboxShowInvisible, value -> {
                    config.hitboxShowInvisible = value;
                    config.markDirty();
                })
        );
    }

    @Override
    public String name() {
        return "Hitboxes";
    }

    @Override
    public String category() {
        return "Render";
    }

    @Override
    public int icon() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return config.hitboxesEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.hitboxesEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onRenderWorld(RenderWorldLastEvent event) {
        render(event.partialTicks);
    }

    public void render(float partialTicks) {
        if (!config.hitboxesEnabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;
        double rangeSq = config.hitboxRange * config.hitboxRange;

        boolean depthWasEnabled = GlUtil.get(GL11.GL_DEPTH_TEST);
        boolean blendWasEnabled = GlUtil.get(GL11.GL_BLEND);
        boolean lightingWasEnabled = GlUtil.get(GL11.GL_LIGHTING);
        boolean textureWasEnabled = GlUtil.get(GL11.GL_TEXTURE_2D);
        int previousDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(config.hitboxLineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        if (config.hitboxStyle == 3 || config.hitboxStyle == 4) {
            renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!shouldRender(mc, player, rangeSq)) continue;
                AxisAlignedBB box = boxOf(player, partialTicks, viewerX, viewerY, viewerZ);
                int color = player.isSneaking() ? config.hitboxSneakColor : config.hitboxColor;
                fill(renderer, box, color, config.hitboxFillOpacity);
            }
            tessellator.draw();
        }

        if (config.hitboxStyle != 3) {
            renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!shouldRender(mc, player, rangeSq)) continue;
                AxisAlignedBB box = boxOf(player, partialTicks, viewerX, viewerY, viewerZ);
                int color = player.isSneaking() ? config.hitboxSneakColor : config.hitboxColor;
                float red = ((color >> 16) & 255) / 255.0F;
                float green = ((color >> 8) & 255) / 255.0F;
                float blue = (color & 255) / 255.0F;
                float alpha = ((color >> 24) & 255) / 255.0F;
                if (alpha <= 0.0F) alpha = 1.0F;
                if (config.hitboxStyle == 1) corners(renderer, box, red, green, blue, alpha);
                else if (config.hitboxStyle == 2) cross(renderer, box, red, green, blue, alpha);
                else box(renderer, box, red, green, blue, alpha);
            }
            tessellator.draw();
        }

        if (config.hitboxShowLabel) {
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (!shouldRender(mc, player, rangeSq)) continue;
                AxisAlignedBB box = boxOf(player, partialTicks, viewerX, viewerY, viewerZ);
                String text = player.getName();
                GlStateManager.pushMatrix();
                GlStateManager.translate((box.minX + box.maxX) / 2.0D, box.maxY + 0.35D, (box.minZ + box.maxZ) / 2.0D);
                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-0.025F, -0.025F, 0.025F);
                mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2, 0, 0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlUtil.set(GL11.GL_BLEND, blendWasEnabled);
        GlUtil.set(GL11.GL_LIGHTING, lightingWasEnabled);
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWasEnabled);
        GL11.glDepthFunc(previousDepthFunc);
        GlStateManager.depthFunc(previousDepthFunc);
        GlUtil.set(GL11.GL_DEPTH_TEST, depthWasEnabled);
    }

    private boolean shouldRender(Minecraft mc, EntityPlayer player, double rangeSq) {
        if (player == mc.thePlayer) return false;
        if (player.isInvisible() && !config.hitboxShowInvisible) return false;
        return player.getDistanceSqToEntity(mc.thePlayer) <= rangeSq;
    }

    private AxisAlignedBB boxOf(EntityPlayer player, float partialTicks, double viewerX, double viewerY, double viewerZ) {
        double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        AxisAlignedBB box = player.getEntityBoundingBox().offset(interpX - player.posX, 0.0D, interpZ - player.posZ);
        if (config.hitboxExpand > 0.0F) box = box.expand(config.hitboxExpand, config.hitboxExpand * 0.5D, config.hitboxExpand);
        return box.offset(-viewerX, -viewerY, -viewerZ);
    }

    private void fill(WorldRenderer renderer, AxisAlignedBB box, int color, float opacity) {
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = opacity;
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ;
        double x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        quad(renderer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        quad(renderer, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, red, green, blue, alpha);
        quad(renderer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, red, green, blue, alpha);
        quad(renderer, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        quad(renderer, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
        quad(renderer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, red, green, blue, alpha);
    }

    private void quad(WorldRenderer renderer, double ax, double ay, double az, double bx, double by, double bz,
                      double cx, double cy, double cz, double dx, double dy, double dz,
                      float red, float green, float blue, float alpha) {
        renderer.pos(ax, ay, az).color(red, green, blue, alpha).endVertex();
        renderer.pos(bx, by, bz).color(red, green, blue, alpha).endVertex();
        renderer.pos(cx, cy, cz).color(red, green, blue, alpha).endVertex();
        renderer.pos(dx, dy, dz).color(red, green, blue, alpha).endVertex();
    }

    private void box(WorldRenderer renderer, AxisAlignedBB box, float red, float green, float blue, float alpha) {
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        line(renderer, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
        line(renderer, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
        line(renderer, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
        line(renderer, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);
        line(renderer, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
        line(renderer, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
        line(renderer, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
        line(renderer, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
        line(renderer, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
        line(renderer, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
        line(renderer, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
        line(renderer, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);
    }

    private void corners(WorldRenderer renderer, AxisAlignedBB box, float red, float green, float blue, float alpha) {
        double len = 0.35D;
        double[][] signs = {{0, 0, 0}, {0, 0, 1}, {0, 1, 0}, {0, 1, 1}, {1, 0, 0}, {1, 0, 1}, {1, 1, 0}, {1, 1, 1}};
        for (double[] s : signs) {
            double x = s[0] == 0 ? box.minX : box.maxX;
            double y = s[1] == 0 ? box.minY : box.maxY;
            double z = s[2] == 0 ? box.minZ : box.maxZ;
            double sx = s[0] == 0 ? len : -len;
            double sy = s[1] == 0 ? len : -len;
            double sz = s[2] == 0 ? len : -len;
            line(renderer, x, y, z, x + sx, y, z, red, green, blue, alpha);
            line(renderer, x, y, z, x, y + sy, z, red, green, blue, alpha);
            line(renderer, x, y, z, x, y, z + sz, red, green, blue, alpha);
        }
    }

    private void cross(WorldRenderer renderer, AxisAlignedBB box, float red, float green, float blue, float alpha) {
        double cx = (box.minX + box.maxX) / 2.0D;
        double cy = (box.minY + box.maxY) / 2.0D;
        double cz = (box.minZ + box.maxZ) / 2.0D;
        line(renderer, box.minX, cy, cz, box.maxX, cy, cz, red, green, blue, alpha);
        line(renderer, cx, box.minY, cz, cx, box.maxY, cz, red, green, blue, alpha);
        line(renderer, cx, cy, box.minZ, cx, cy, box.maxZ, red, green, blue, alpha);
    }

    private void line(WorldRenderer renderer, double x1, double y1, double z1, double x2, double y2, double z2,
                      float red, float green, float blue, float alpha) {
        renderer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }
}
