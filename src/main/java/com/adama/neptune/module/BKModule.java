package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.Setting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import java.util.Collections;
import java.util.List;

public abstract class BKModule {
    protected final NeptuneConfig config;

    protected BKModule(NeptuneConfig config) {
        this.config = config;
    }

    public abstract String name();

    public abstract String category();

    public String id() {
        return name().toLowerCase().replace(' ', '_');
    }

    public int icon() {
        return 0;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean enabled) {
    }

    public void onClientTick() {
    }

    public void onRenderTick(float dt) {
    }

    public void onFrameStart() {
    }

    public void onFrameEnd() {
    }

    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
    }

    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
    }

    public void onRenderWorld(RenderWorldLastEvent event) {
    }

    public void onFov(EntityViewRenderEvent.FOVModifier event) {
    }

    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
    }

    public void onCamera(EntityViewRenderEvent.CameraSetup event) {
    }

    public void onBlockHighlight(DrawBlockHighlightEvent event) {
    }

    public void onMouse(MouseEvent event) {
    }

    public void onChat(ClientChatReceivedEvent event) {
    }

    public void onAttack(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
    }

    public void onReset() {
    }

    public List<Setting> settings() {
        return Collections.emptyList();
    }
}
