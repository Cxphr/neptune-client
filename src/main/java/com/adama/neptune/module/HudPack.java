package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

abstract class BasicHudModule extends HudModule {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final String id;
    protected final String display;
    protected final int defaultX;
    protected final int defaultY;
    protected final boolean defaultOn;

    BasicHudModule(NeptuneConfig config, String id, String display, int defaultX, int defaultY, boolean defaultOn) {
        super(config);
        this.id = id;
        this.display = display;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.defaultOn = defaultOn;
    }

    @Override
    public String name() {
        return display;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return config.toggle(id + ".on", defaultOn);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle(id + ".on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.posX(id, defaultX),
                        value -> config.setPos(id, (int) Math.round(value), config.posY(id, defaultY))),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.posY(id, defaultY),
                        value -> config.setPos(id, config.posX(id, defaultX), (int) Math.round(value)))
        );
    }

    protected int color() {
        return config.color(id + ".color", 0xFFFFFFFF);
    }

    protected abstract List<String> lines();

    @Override
    public void render() {
        int x = config.posX(id, defaultX);
        int y = config.posY(id, defaultY);
        for (String line : lines()) {
            mc.fontRendererObj.drawStringWithShadow(line, x, y, color());
            y += 10;
        }
    }

    @Override
    public int width() {
        int width = 0;
        for (String line : lines()) width = Math.max(width, mc.fontRendererObj.getStringWidth(line));
        return width;
    }

    @Override
    public int height() {
        return lines().size() * 10;
    }
}

class FpsHud extends BasicHudModule {
    FpsHud(NeptuneConfig config) {
        super(config, "fps", "FPS", 4, 4, true);
    }

    @Override
    protected List<String> lines() {
        return Collections.singletonList("FPS " + Minecraft.getDebugFPS());
    }
}

class ClockHud extends BasicHudModule {
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private String cached = "";
    private long cachedAt;

    ClockHud(NeptuneConfig config) {
        super(config, "clock", "Clock", 4, 26, true);
    }

    @Override
    protected List<String> lines() {
        long now = System.currentTimeMillis();
        if (now - cachedAt > 500L) {
            cached = format.format(new Date(now));
            cachedAt = now;
        }
        return Collections.singletonList(cached);
    }
}

class StopwatchHud extends BasicHudModule {
    private long startedAt;

    StopwatchHud(NeptuneConfig config) {
        super(config, "stopwatch", "Stopwatch", 4, 38, false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) startedAt = System.currentTimeMillis();
    }

    @Override
    public void onClientTick() {
        if (isEnabled() && startedAt == 0L) startedAt = System.currentTimeMillis();
    }

    @Override
    protected List<String> lines() {
        long elapsed = startedAt == 0L ? 0L : System.currentTimeMillis() - startedAt;
        long seconds = elapsed / 1000L;
        return Collections.singletonList(String.format("%02d:%02d.%01d", seconds / 60L, seconds % 60L, (elapsed % 1000L) / 100L));
    }
}

class DayHud extends BasicHudModule {
    DayHud(NeptuneConfig config) {
        super(config, "day", "Day Counter", 4, 50, true);
    }

    @Override
    protected List<String> lines() {
        if (mc.theWorld == null) return Collections.singletonList("Day -");
        return Collections.singletonList("Day " + (mc.theWorld.getWorldTime() / 24000L));
    }
}

class PingHud extends BasicHudModule {
    PingHud(NeptuneConfig config) {
        super(config, "ping", "Ping", 4, 62, true);
    }

    @Override
    protected List<String> lines() {
        if (mc.getNetHandler() == null || mc.thePlayer == null) return Collections.singletonList("Ping -");
        net.minecraft.client.network.NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        int ping = info == null ? 0 : info.getResponseTime();
        return Collections.singletonList("Ping " + ping + "ms");
    }
}

class MemoryHud extends BasicHudModule {
    MemoryHud(NeptuneConfig config) {
        super(config, "memory", "Memory", 4, 74, true);
    }

    @Override
    protected List<String> lines() {
        Runtime runtime = Runtime.getRuntime();
        long used = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        long max = runtime.maxMemory() / 1048576L;
        return Collections.singletonList("RAM " + used + "/" + max + "MB");
    }
}

class PlaytimeHud extends BasicHudModule {
    private final long sessionStart = System.currentTimeMillis();

    PlaytimeHud(NeptuneConfig config) {
        super(config, "playtime", "Playtime", 4, 86, false);
    }

    @Override
    protected List<String> lines() {
        long minutes = (System.currentTimeMillis() - sessionStart) / 60000L;
        return Collections.singletonList(String.format("Playtime %dh %02dm", minutes / 60L, minutes % 60L));
    }
}

class SaturationHud extends BasicHudModule {
    SaturationHud(NeptuneConfig config) {
        super(config, "saturation", "Saturation", 4, 98, false);
    }

    @Override
    protected List<String> lines() {
        if (mc.thePlayer == null) return Collections.singletonList("Sat -");
        return Collections.singletonList(String.format("Sat %.2f", mc.thePlayer.getFoodStats().getSaturationLevel()));
    }
}

class ServerHud extends BasicHudModule {
    ServerHud(NeptuneConfig config) {
        super(config, "server", "Server Address", 4, 110, false);
    }

    @Override
    protected List<String> lines() {
        String address = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "Singleplayer";
        return Collections.singletonList(address);
    }
}

class ItemCounterHud extends BasicHudModule {
    ItemCounterHud(NeptuneConfig config) {
        super(config, "itemcounter", "Item Counter", 4, 122, false);
    }

    @Override
    protected List<String> lines() {
        if (mc.thePlayer == null) return Collections.singletonList("Items -");
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) return Collections.singletonList("Items 0");
        int count = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() == held.getItem() && stack.getMetadata() == held.getMetadata()) {
                count += stack.stackSize;
            }
        }
        return Collections.singletonList(held.getDisplayName() + " x" + count);
    }
}

class ComboHud extends BasicHudModule {
    private int combo;
    private long lastHit;

    ComboHud(NeptuneConfig config) {
        super(config, "combo", "Combo Counter", 4, 134, true);
    }

    @Override
    public void onAttack(AttackEntityEvent event) {
        if (!isEnabled()) return;
        if (mc.thePlayer == null || event.entityPlayer != mc.thePlayer) return;
        Entity target = event.target;
        if (target instanceof EntityLivingBase) {
            combo++;
            lastHit = System.currentTimeMillis();
        }
    }

    @Override
    public void onClientTick() {
        if (combo > 0 && System.currentTimeMillis() - lastHit > 3000L) combo = 0;
    }

    @Override
    protected List<String> lines() {
        return Collections.singletonList("Combo " + combo);
    }
}

class ReachHud extends BasicHudModule {
    private double lastReach;

    ReachHud(NeptuneConfig config) {
        super(config, "reach", "Reach Display", 4, 146, false);
    }

    @Override
    protected List<String> lines() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.thePlayer != null) {
            lastReach = mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit);
        }
        return Collections.singletonList(String.format("Reach %.2f", lastReach));
    }
}

class DirectionHud extends BasicHudModule {
    DirectionHud(NeptuneConfig config) {
        super(config, "direction", "Direction", 4, 158, false);
    }

    @Override
    protected List<String> lines() {
        if (mc.thePlayer == null) return Collections.singletonList("-");
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int index = net.minecraft.util.MathHelper.floor_double(mc.thePlayer.rotationYaw * 8.0F / 360.0F + 0.5D) & 7;
        return Collections.singletonList(directions[index]);
    }
}

class PotionHud extends HudModule {
    private final List<Setting> settings;

    PotionHud(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", this::isEnabled, this::setEnabled),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.posX("potion", 4),
                        value -> config.setPos("potion", (int) Math.round(value), config.posY("potion", 170))),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.posY("potion", 170),
                        value -> config.setPos("potion", config.posX("potion", 4), (int) Math.round(value)))
        );
    }

    @Override
    public String name() {
        return "Potion Status";
    }

    @Override
    public String id() {
        return "potion";
    }

    @Override
    public boolean isEnabled() {
        return config.toggle("potion.on", true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setToggle("potion.on", enabled);
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        int x = config.posX("potion", 4);
        int y = config.posY("potion", 170);
        for (net.minecraft.potion.PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            net.minecraft.potion.Potion potion = effect.getPotionID() >= 0 && effect.getPotionID() < net.minecraft.potion.Potion.potionTypes.length ? net.minecraft.potion.Potion.potionTypes[effect.getPotionID()] : null;
            if (potion == null) continue;
            int seconds = effect.getDuration() / 20;
            String potionName = potion.getName() == null ? "Potion" : potion.getName();
            String text = potionName + (effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "")
                    + " " + String.format("%d:%02d", seconds / 60, seconds % 60);
            mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFDFE8F5);
            y += 10;
        }
    }

    @Override
    public int width() {
        Minecraft mc = Minecraft.getMinecraft();
        int width = 0;
        if (mc.thePlayer == null) return 0;
        for (net.minecraft.potion.PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            net.minecraft.potion.Potion potion = effect.getPotionID() >= 0 && effect.getPotionID() < net.minecraft.potion.Potion.potionTypes.length ? net.minecraft.potion.Potion.potionTypes[effect.getPotionID()] : null;
            if (potion == null) continue;
            String potionName = potion.getName() == null ? "Potion" : potion.getName();
            width = Math.max(width, mc.fontRendererObj.getStringWidth(potionName));
        }
        return width;
    }

    @Override
    public int height() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer == null ? 0 : mc.thePlayer.getActivePotionEffects().size() * 10;
    }
}
