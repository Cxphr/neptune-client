package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.util.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class EnemyHealthModule extends BKModule {
    private final List<Setting> settings;

    public EnemyHealthModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new BoolSetting("Show text", () -> config.toggle("enemyhealth.text", false),
                        value -> config.setToggle("enemyhealth.text", value)),
                new BoolSetting("Absorption", () -> config.toggle("enemyhealth.absorption", true),
                        value -> config.setToggle("enemyhealth.absorption", value)),
                new SliderSetting("Range", 8.0D, 64.0D, 1.0D, 0,
                        () -> config.number("enemyhealth.range", 32.0D),
                        value -> config.setNumber("enemyhealth.range", value)),
                new SliderSetting("Width", 2.0D, 10.0D, 1.0D, 0,
                        () -> config.number("enemyhealth.width", 4.0D),
                        value -> config.setNumber("enemyhealth.width", value)),
                new SliderSetting("Height", 10.0D, 60.0D, 1.0D, 0,
                        () -> config.number("enemyhealth.height", 24.0D),
                        value -> config.setNumber("enemyhealth.height", value))
        );
    }

    @Override
    public String name() {
        return "Enemy Health";
    }

    @Override
    public String id() {
        return "enemyhealth";
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
        return config.toggle("enemyhealth.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("enemyhealth.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        double range = config.number("enemyhealth.range", 32.0D);
        double rangeSq = range * range;
        int width = (int) config.number("enemyhealth.width", 4.0D);
        int height = (int) config.number("enemyhealth.height", 24.0D);
        boolean showText = config.toggle("enemyhealth.text", false);
        boolean absorption = config.toggle("enemyhealth.absorption", true);

        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;

        boolean depthWas = GlUtil.get(GL11.GL_DEPTH_TEST);
        boolean blendWas = GlUtil.get(GL11.GL_BLEND);
        boolean textureWas = GlUtil.get(GL11.GL_TEXTURE_2D);
        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        GlUtil.set(GL11.GL_DEPTH_TEST, true);
        GlUtil.set(GL11.GL_BLEND, true);
        GlUtil.set(GL11.GL_TEXTURE_2D, false);
        GlUtil.set(GL11.GL_LIGHTING, false);
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;
            if (player.getDistanceSqToEntity(mc.thePlayer) > rangeSq) continue;
            double interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
            double interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
            double interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

            GlStateManager.pushMatrix();
            GlStateManager.translate(interpX - viewerX, interpY + player.height + 0.45D - viewerY, interpZ - viewerZ);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.025F, -0.025F, 0.025F);

            renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float max = player.getMaxHealth();
            float health = Math.min(player.getHealth(), max);
            float ratio = max <= 0.0F ? 0.0F : health / max;
            float left = -width / 2.0F;
            float top = -height;
            quad(renderer, left - 1.0F, top - 1.0F, left + width + 1.0F, 1.0F, 0xAA000000);
            int fill = Math.round(height * ratio);
            quad(renderer, left, 0.0F - fill, left + width, 0.0F, healthColor(ratio));
            if (absorption) {
                float absorb = player.getAbsorptionAmount();
                int absorbPixels = Math.min(height - fill, Math.round(height * (absorb / max)));
                if (absorbPixels > 0) {
                    quad(renderer, left, -fill - absorbPixels, left + width, -fill, 0xFFFFC94D);
                }
            }
            tessellator.draw();
            if (showText) {
                String text = String.format("%.0f", health);
                GlStateManager.enableTexture2D();
                int textWidth = mc.fontRendererObj.getStringWidth(text);
                mc.fontRendererObj.drawString(text, -textWidth / 2, (int) top - 12, 0xFFFFFFFF);
                GlStateManager.disableTexture2D();
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlUtil.set(GL11.GL_DEPTH_TEST, depthWas);
        GlUtil.set(GL11.GL_BLEND, blendWas);
        GlUtil.set(GL11.GL_TEXTURE_2D, textureWas);
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
    }

    private void quad(WorldRenderer renderer, float x1, float y1, float x2, float y2, int color) {
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alpha = ((color >> 24) & 255) / 255.0F;
        if (alpha <= 0.0F) alpha = 1.0F;
        renderer.pos(x1, y2, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y2, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos(x2, y1, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos(x1, y1, 0.0D).color(red, green, blue, alpha).endVertex();
    }

    private static int healthColor(float ratio) {
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int red = Math.round(255.0F * (1.0F - ratio));
        int green = Math.round(255.0F * ratio);
        return 0xFF000000 | (red << 16) | (green << 8) | 0x40;
    }
}
