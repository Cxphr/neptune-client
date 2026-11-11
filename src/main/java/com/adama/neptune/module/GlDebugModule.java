package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class GlDebugModule extends BKModule {
    private final List<Setting> settings;
    private boolean savedLighting;
    private boolean savedBlend;
    private boolean savedTexture;
    private boolean savedCull;
    private boolean savedAlpha;
    private boolean savedFog;
    private boolean applied;

    public GlDebugModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new BoolSetting("Disable lighting", () -> config.toggle("gldebug.light", false),
                        value -> config.setToggle("gldebug.light", value)),
                new BoolSetting("Disable blend", () -> config.toggle("gldebug.blend", false),
                        value -> config.setToggle("gldebug.blend", value)),
                new BoolSetting("Disable textures", () -> config.toggle("gldebug.texture", false),
                        value -> config.setToggle("gldebug.texture", value)),
                new BoolSetting("Disable culling", () -> config.toggle("gldebug.cull", false),
                        value -> config.setToggle("gldebug.cull", value)),
                new BoolSetting("Disable alpha test", () -> config.toggle("gldebug.alpha", false),
                        value -> config.setToggle("gldebug.alpha", value)),
                new BoolSetting("Disable fog", () -> config.toggle("gldebug.fog", false),
                        value -> config.setToggle("gldebug.fog", value)),
                new BoolSetting("Wireframe", () -> config.toggle("gldebug.wireframe", false),
                        value -> config.setToggle("gldebug.wireframe", value)),
                new BoolSetting("Thick lines", () -> config.toggle("gldebug.thicklines", false),
                        value -> config.setToggle("gldebug.thicklines", value))
        );
    }

    @Override
    public String name() {
        return "GL Debug";
    }

    @Override
    public String id() {
        return "gldebug";
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
        return config.toggle("gldebug.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("gldebug.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onFrameStart() {
        if (!isEnabled()) return;
        savedLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        savedBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        savedTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        savedCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        savedAlpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        savedFog = GL11.glIsEnabled(GL11.GL_FOG);

        apply(GL11.GL_LIGHTING, savedLighting, config.toggle("gldebug.light", false));
        apply(GL11.GL_BLEND, savedBlend, config.toggle("gldebug.blend", false));
        apply(GL11.GL_TEXTURE_2D, savedTexture, config.toggle("gldebug.texture", false));
        apply(GL11.GL_CULL_FACE, savedCull, config.toggle("gldebug.cull", false));
        apply(GL11.GL_ALPHA_TEST, savedAlpha, config.toggle("gldebug.alpha", false));
        apply(GL11.GL_FOG, savedFog, config.toggle("gldebug.fog", false));

        if (config.toggle("gldebug.wireframe", false)) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }
        if (config.toggle("gldebug.thicklines", false)) {
            GL11.glLineWidth(2.5F);
        }
        applied = true;
    }

    @Override
    public void onFrameEnd() {
        if (!applied) return;
        restore(GL11.GL_LIGHTING, savedLighting);
        restore(GL11.GL_BLEND, savedBlend);
        restore(GL11.GL_TEXTURE_2D, savedTexture);
        restore(GL11.GL_CULL_FACE, savedCull);
        restore(GL11.GL_ALPHA_TEST, savedAlpha);
        restore(GL11.GL_FOG, savedFog);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glLineWidth(1.0F);
        applied = false;
    }

    private static void apply(int capability, boolean wasEnabled, boolean disable) {
        boolean target = wasEnabled && !disable;
        if (target) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
        sync(capability, target);
    }

    private static void restore(int capability, boolean enabled) {
        if (enabled) {
            GL11.glEnable(capability);
        } else {
            GL11.glDisable(capability);
        }
        sync(capability, enabled);
    }

    private static void sync(int capability, boolean enabled) {
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
