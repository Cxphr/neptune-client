package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class AutoGGModule extends BKModule {
    private final List<Setting> settings;
    private long sendAt;
    private long cooldownUntil;
    private boolean pending;

    AutoGGModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("Delay", 0.0D, 5.0D, 0.5D, 1,
                        () -> config.number("autogg.delay", 1.0D),
                        value -> config.setNumber("autogg.delay", value)),
                new SliderSetting("Cooldown", 5.0D, 60.0D, 1.0D, 0,
                        () -> config.number("autogg.cooldown", 20.0D),
                        value -> config.setNumber("autogg.cooldown", value))
        );
    }

    @Override
    public String name() {
        return "AutoGG";
    }

    @Override
    public String id() {
        return "autogg";
    }

    @Override
    public String category() {
        return "Utility";
    }

    @Override
    public int icon() {
        return 3;
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("autogg.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("autogg.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled() || pending) return;
        long now = System.currentTimeMillis();
        if (now < cooldownUntil) return;
        String message = event.message.getUnformattedText().toLowerCase();
        if (message.contains("won the game") || message.contains("you won") || message.contains("1st killer")
                || message.contains("victory") || message.contains("game over") || message.contains("winner:")) {
            pending = true;
            sendAt = now + (long) (config.number("autogg.delay", 1.0D) * 1000.0D);
        }
    }

    @Override
    public void onClientTick() {
        if (!pending) return;
        if (System.currentTimeMillis() < sendAt) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage(config.text("autogg.message", "gg"));
            cooldownUntil = System.currentTimeMillis() + (long) (config.number("autogg.cooldown", 20.0D) * 1000.0D);
        }
        pending = false;
    }
}

class ChatTimestampsModule extends BKModule {
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private final List<Setting> settings;

    ChatTimestampsModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled)
        );
    }

    @Override
    public String name() {
        return "Chat Timestamps";
    }

    @Override
    public String id() {
        return "chattimestamps";
    }

    @Override
    public String category() {
        return "Utility";
    }

    @Override
    public int icon() {
        return 3;
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("chattimestamps.on", false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("chattimestamps.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled()) return;
        String prefix = "[" + format.format(new Date()) + "] ";
        event.message = new ChatComponentText(prefix).appendSibling(event.message);
    }
}
