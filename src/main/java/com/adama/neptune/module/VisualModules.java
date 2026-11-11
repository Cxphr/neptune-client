package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.ColorSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.util.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

class CrosshairModule extends BKModule {
    private final List<Setting> settings;

    CrosshairModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("Size", 2.0D, 20.0D, 1.0D, 0,
                        () -> config.number("crosshair.size", 7.0D),
                        value -> config.setNumber("crosshair.size", value)),
                new SliderSetting("Gap", 0.0D, 12.0D, 1.0D, 0,
                        () -> config.number("crosshair.gap", 3.0D),
                        value -> config.setNumber("crosshair.gap", value)),
                new SliderSetting("Thickness", 1.0D, 4.0D, 1.0D, 0,
                        () -> config.number("crosshair.thick", 1.0D),
                        value -> config.setNumber("crosshair.thick", value)),
                new ColorSetting("Color", () -> config.color("crosshair.color", 0xFFFFFFFF),
                        value -> config.setColor("crosshair.color", value)),
                new BoolSetting("Dot", () -> config.toggle("crosshair.dot", true),
                        value -> config.setToggle("crosshair.dot", value))
        );
    }

    @Override
    public String name() {
        return "Crosshair";
    }

    @Override
    public String id() {
        return "crosshair";
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
        return config.toggle("crosshair.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("crosshair.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!isEnabled() || event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;
        event.setCanceled(true);
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int cx = resolution.getScaledWidth() / 2;
        int cy = resolution.getScaledHeight() / 2;
        int size = (int) config.number("crosshair.size", 7.0D);
        int gap = (int) config.number("crosshair.gap", 3.0D);
        int thick = (int) config.number("crosshair.thick", 1.0D);
        int color = config.color("crosshair.color", 0xFFFFFFFF);
        Gui.drawRect(cx - gap - size, cy, cx - gap, cy + thick, color);
        Gui.drawRect(cx + gap, cy, cx + gap + size, cy + thick, color);
        Gui.drawRect(cx, cy - gap - size, cx + thick, cy - gap, color);
        Gui.drawRect(cx, cy + gap, cx + thick, cy + gap + size, color);
        if (config.toggle("crosshair.dot", true)) {
            Gui.drawRect(cx, cy, cx + 1, cy + 1, color);
        }
    }
}

class BlockOutlineModule extends BKModule {
    private final List<Setting> settings;

    BlockOutlineModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new ColorSetting("Color", () -> config.color("blockoutline.color", 0xFFFFFFFF),
                        value -> config.setColor("blockoutline.color", value)),
                new SliderSetting("Width", 0.5D, 4.0D, 0.1D, 1,
                        () -> config.number("blockoutline.width", 1.5D),
                        value -> config.setNumber("blockoutline.width", value))
        );
    }

    @Override
    public String name() {
        return "Block Outline";
    }

    @Override
    public String id() {
        return "blockoutline";
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
        return config.toggle("blockoutline.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("blockoutline.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        if (!isEnabled() || event.target == null) return;
        if (event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos pos = event.target.getBlockPos();
        AxisAlignedBB box = mc.theWorld.getBlockState(pos).getBlock()
                .getSelectedBoundingBox(mc.theWorld, pos)
                .expand(0.002D, 0.002D, 0.002D);
        event.setCanceled(true);

        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;
        int color = config.color("blockoutline.color", 0xFFFFFFFF);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = ((color >> 24) & 255) / 255.0F;
        if (alpha <= 0.0F) alpha = 1.0F;

        boolean blendWas = GlUtil.get(GL11.GL_BLEND);
        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        boolean textureWas = GlUtil.get(GL11.GL_TEXTURE_2D);
        GlStateManager.pushMatrix();
        GlUtil.set(GL11.GL_BLEND, true);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlUtil.set(GL11.GL_TEXTURE_2D, false);
        GlUtil.set(GL11.GL_LIGHTING, false);
        GL11.glLineWidth((float) config.number("blockoutline.width", 1.5D));
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        line(renderer, box, viewerX, viewerY, viewerZ, red, green, blue, alpha);
        tessellator.draw();
        GL11.glLineWidth(1.0F);
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWas);
        GlUtil.set(GL11.GL_BLEND, blendWas);
        GlStateManager.popMatrix();
    }

    private void line(WorldRenderer renderer, AxisAlignedBB box, double vx, double vy, double vz,
                      float red, float green, float blue, float alpha) {
        double x1 = box.minX - vx, y1 = box.minY - vy, z1 = box.minZ - vz;
        double x2 = box.maxX - vx, y2 = box.maxY - vy, z2 = box.maxZ - vz;
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
}

class ChunkBordersModule extends BKModule {
    private final List<Setting> settings;

    ChunkBordersModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new ColorSetting("Color", () -> config.color("chunkborders.color", 0x66FFFFFF),
                        value -> config.setColor("chunkborders.color", value)),
                new SliderSetting("Height", 16.0D, 256.0D, 1.0D, 0,
                        () -> config.number("chunkborders.height", 256.0D),
                        value -> config.setNumber("chunkborders.height", value))
        );
    }

    @Override
    public String name() {
        return "Chunk Borders";
    }

    @Override
    public String id() {
        return "chunkborders";
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
        return config.toggle("chunkborders.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("chunkborders.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        int chunkX = mc.thePlayer.chunkCoordX * 16;
        int chunkZ = mc.thePlayer.chunkCoordZ * 16;
        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;
        int color = config.color("chunkborders.color", 0x66FFFFFF);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = ((color >> 24) & 255) / 255.0F;
        if (alpha <= 0.0F) alpha = 0.4F;
        double height = config.number("chunkborders.height", 256.0D);
        double baseY = 0.0D - viewerY;

        boolean blendWas = GlUtil.get(GL11.GL_BLEND);
        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        boolean textureWas = GlUtil.get(GL11.GL_TEXTURE_2D);
        GlStateManager.pushMatrix();
        GlUtil.set(GL11.GL_BLEND, true);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlUtil.set(GL11.GL_TEXTURE_2D, false);
        GlUtil.set(GL11.GL_LIGHTING, false);
        GL11.glLineWidth(1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        double x1 = chunkX - viewerX;
        double x2 = chunkX + 16 - viewerX;
        double z1 = chunkZ - viewerZ;
        double z2 = chunkZ + 16 - viewerZ;
        renderer.pos(x1, baseY, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x1, baseY + height, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, baseY, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, baseY + height, z1).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, baseY, z2).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, baseY + height, z2).color(red, green, blue, alpha).endVertex();
        renderer.pos(x1, baseY, z2).color(red, green, blue, alpha).endVertex();
        renderer.pos(x1, baseY + height, z2).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWas);
        GlUtil.set(GL11.GL_BLEND, blendWas);
        GlStateManager.popMatrix();
    }
}

class FovModule extends BKModule {
    private final List<Setting> settings;

    FovModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("Multiplier", 0.5D, 1.5D, 0.05D, 2,
                        () -> config.number("fov.mult", 1.0D),
                        value -> config.setNumber("fov.mult", value))
        );
    }

    @Override
    public String name() {
        return "FOV Changer";
    }

    @Override
    public String id() {
        return "fov";
    }

    @Override
    public String category() {
        return "Render";
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("fov.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("fov.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (!isEnabled()) return;
        event.setFOV((float) (event.getFOV() * config.number("fov.mult", 1.0D)));
    }
}
