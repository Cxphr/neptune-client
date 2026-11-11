package com.adama.neptune.event;

import com.adama.neptune.NeptuneClient;
import com.adama.neptune.module.BKModule;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEvents {
    private static final ResourceLocation NEPTUNE_BACKGROUND = new ResourceLocation("neptune:textures/gui/background.png");

    private final NeptuneClient mod;
    private long lastNanos;

    public ClientEvents(NeptuneClient mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            mod.modules.onClientTick();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (BKModule module : mod.modules.modules()) module.onFrameEnd();
            return;
        }
        for (BKModule module : mod.modules.modules()) module.onFrameStart();
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0.016F : (now - lastNanos) / 1_000_000_000.0F;
        lastNanos = now;
        if (dt > 0.25F) dt = 0.25F;
        mod.modules.onRenderTick(dt);
    }

    @SubscribeEvent
    public void onCamera(EntityViewRenderEvent.CameraSetup event) {
        for (BKModule module : mod.modules.modules()) module.onCamera(event);
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        for (BKModule module : mod.modules.modules()) module.onFov(event);
    }

    @SubscribeEvent
    public void onFog(EntityViewRenderEvent.FogDensity event) {
        for (BKModule module : mod.modules.modules()) module.onFogDensity(event);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        for (BKModule module : mod.modules.modules()) module.onRenderWorld(event);
    }

    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        for (BKModule module : mod.modules.modules()) module.onBlockHighlight(event);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        for (BKModule module : mod.modules.modules()) module.onMouse(event);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        for (BKModule module : mod.modules.modules()) module.onChat(event);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        for (BKModule module : mod.modules.modules()) module.onAttack(event);
    }

    @SubscribeEvent
    public void onBackground(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (!(event.gui instanceof GuiMainMenu)) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        int w = event.gui.width;
        int h = event.gui.height;
        mc.getTextureManager().bindTexture(NEPTUNE_BACKGROUND);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, w, h, w, h);
        Gui.drawRect(0, 0, w, h, 0x66000000);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        mod.modules.onGuiOpen();
    }

    @SubscribeEvent
    public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings != null && mc.gameSettings.hideGUI) return;
        for (BKModule module : mod.modules.modules()) module.onRenderOverlayPre(event);
    }

    @SubscribeEvent
    public void onOverlayPost(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings != null && mc.gameSettings.hideGUI) return;
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            mod.modules.renderHud();
        }
        for (BKModule module : mod.modules.modules()) module.onRenderOverlay(event);
    }
}
