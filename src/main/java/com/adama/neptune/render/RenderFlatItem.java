package com.adama.neptune.render;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.util.GlUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
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
        TextureAtlasSprite sprite = mc.getRenderItem().getItemModelMesher()
                .getParticleIcon(stack.getItem(), stack.getMetadata());
        if (sprite == null) return;

        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        float angle = (entity.getAge() + partialTicks) * config.itemPhysicsRotation * 3.0F;

        boolean lightingWas = GlUtil.get(GL11.GL_LIGHTING);
        boolean blendWas = GlUtil.get(GL11.GL_BLEND);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.12D, z);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angle, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlUtil.set(GL11.GL_LIGHTING, false);
        GlUtil.set(GL11.GL_BLEND, true);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(-0.5D, 0.0D, -0.5D).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        renderer.pos(0.5D, 0.0D, -0.5D).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        renderer.pos(0.5D, 0.0D, 0.5D).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        renderer.pos(-0.5D, 0.0D, 0.5D).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        tessellator.draw();

        GlStateManager.disableRescaleNormal();
        GlUtil.set(GL11.GL_BLEND, blendWas);
        GlUtil.set(GL11.GL_LIGHTING, lightingWas);
        GlStateManager.popMatrix();
    }
}
