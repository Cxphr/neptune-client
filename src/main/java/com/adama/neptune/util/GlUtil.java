package com.adama.neptune.util;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public final class GlUtil {
    private GlUtil() {
    }

    public static boolean get(int capability) {
        return GL11.glIsEnabled(capability);
    }

    public static void set(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
        switch (capability) {
            case GL11.GL_LIGHTING:
                if (enabled) GlStateManager.enableLighting();
                else GlStateManager.disableLighting();
                break;
            case GL11.GL_BLEND:
                if (enabled) GlStateManager.enableBlend();
                else GlStateManager.disableBlend();
                break;
            case GL11.GL_TEXTURE_2D:
                if (enabled) GlStateManager.enableTexture2D();
                else GlStateManager.disableTexture2D();
                break;
            case GL11.GL_DEPTH_TEST:
                if (enabled) GlStateManager.enableDepth();
                else GlStateManager.disableDepth();
                break;
            case GL11.GL_CULL_FACE:
                if (enabled) GlStateManager.enableCull();
                else GlStateManager.disableCull();
                break;
            case GL11.GL_ALPHA_TEST:
                if (enabled) GlStateManager.enableAlpha();
                else GlStateManager.disableAlpha();
                break;
            case GL11.GL_FOG:
                if (enabled) GlStateManager.enableFog();
                else GlStateManager.disableFog();
                break;
            default:
                break;
        }
    }
}
