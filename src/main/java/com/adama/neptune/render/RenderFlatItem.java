package com.adama.neptune.render;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.util.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class RenderFlatItem extends RenderEntityItem {
    private final NeptuneConfig config;
    private final Minecraft mc = Minecraft.getMinecraft();

    public RenderFlatItem(RenderManager manager, RenderItem renderItem, NeptuneConfig config) {
        super(manager, renderItem);
        this.config = config;
    }

    @Override
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getEntityItem();
        if (stack == null) return;

        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        float scale = (float) config.number("itemphysics.scale", 0.5D);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.12D, z);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
    }
}
