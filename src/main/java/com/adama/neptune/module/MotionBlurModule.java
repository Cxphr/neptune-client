package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.util.Ease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MotionBlurModule extends BKModule {
    private final List<Setting> settings;
    private int texture = -1;
    private int textureWidth;
    private int textureHeight;
    private float blur;
    private float lastDt = 0.016F;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;
    private boolean hasLast;

    public MotionBlurModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.motionBlurEnabled, value -> {
                    config.motionBlurEnabled = value;
                    if (!value) blur = 0.0F;
                    config.markDirty();
                }),
                new SliderSetting("Intensity", 0.05D, 1.5D, 0.05D, 2,
                        () -> config.motionBlurIntensity,
                        value -> {
                            config.motionBlurIntensity = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Max opacity", 0.05D, 0.9D, 0.05D, 2,
                        () -> config.motionBlurMax,
                        value -> {
                            config.motionBlurMax = (float) value;
                            config.markDirty();
                        }),
                new SliderSetting("Threshold", 0.0D, 2.0D, 0.05D, 2,
                        () -> config.motionBlurThreshold,
                        value -> {
                            config.motionBlurThreshold = (float) value;
                            config.markDirty();
                        }),
                new BoolSetting("Camera motion", () -> config.motionBlurCamera, value -> {
                    config.motionBlurCamera = value;
                    config.markDirty();
                })
        );
    }

    @Override
    public String name() {
        return "Motion Blur";
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
        return config.motionBlurEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.motionBlurEnabled = enabled;
        if (!enabled) blur = 0.0F;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onRenderTick(float dt) {
        lastDt = dt <= 0.0F ? 0.016F : dt;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            hasLast = false;
            blur = 0.0F;
            return;
        }
        if (!hasLast) {
            lastX = mc.thePlayer.posX;
            lastY = mc.thePlayer.posY;
            lastZ = mc.thePlayer.posZ;
            lastYaw = mc.thePlayer.rotationYaw;
            lastPitch = mc.thePlayer.rotationPitch;
            hasLast = true;
        }
        double dx = mc.thePlayer.posX - lastX;
        double dy = mc.thePlayer.posY - lastY;
        double dz = mc.thePlayer.posZ - lastZ;
        lastX = mc.thePlayer.posX;
        lastY = mc.thePlayer.posY;
        lastZ = mc.thePlayer.posZ;
        float speed = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / lastDt;
        if (config.motionBlurCamera) {
            float yaw = Math.abs(mc.thePlayer.rotationYaw - lastYaw);
            float pitch = Math.abs(mc.thePlayer.rotationPitch - lastPitch);
            lastYaw = mc.thePlayer.rotationYaw;
            lastPitch = mc.thePlayer.rotationPitch;
            speed += (yaw + pitch) * 0.05F;
        }
        float target = Ease.clamp01((speed - config.motionBlurThreshold) * config.motionBlurIntensity);
        if (!config.motionBlurEnabled) target = 0.0F;
        blur = Ease.expSmooth(blur, target, lastDt, 0.08F);
    }

    @Override
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!config.motionBlurEnabled || blur <= 0.01F) return;
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.hideGUI) return;
        if (mc.currentScreen != null) return;

        int width = mc.displayWidth;
        int height = mc.displayHeight;
        if (width <= 0 || height <= 0) return;
        ensureTexture(width, height);

        float alpha = Ease.clamp01(blur) * config.motionBlurMax;
        if (alpha > 0.01F) drawFullscreen(texture, width, height, alpha);

        GlStateManager.bindTexture(texture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    private void ensureTexture(int width, int height) {
        if (texture != -1 && width == textureWidth && height == textureHeight) return;
        if (texture != -1) GlStateManager.deleteTexture(texture);
        texture = GlStateManager.generateTexture();
        GlStateManager.bindTexture(texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        textureWidth = width;
        textureHeight = height;
    }

    private void drawFullscreen(int textureId, int width, int height, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, -1.0D, 1.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.bindTexture(textureId);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        renderer.pos(width, 0.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        renderer.pos(width, height, 0.0D).tex(1.0D, 0.0D).endVertex();
        renderer.pos(0.0D, height, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    public void onReset() {
        hasLast = false;
        blur = 0.0F;
        if (texture != -1) {
            GlStateManager.deleteTexture(texture);
            texture = -1;
            textureWidth = 0;
            textureHeight = 0;
        }
    }
}
