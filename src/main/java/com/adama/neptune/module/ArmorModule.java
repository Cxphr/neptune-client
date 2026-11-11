package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ArmorModule extends HudModule {
    private final List<Setting> settings;

    public ArmorModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.armorEnabled, value -> {
                    config.armorEnabled = value;
                    config.markDirty();
                }),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.armorX,
                        value -> {
                            config.armorX = (int) Math.round(value);
                            config.markDirty();
                        }),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.armorY,
                        value -> {
                            config.armorY = (int) Math.round(value);
                            config.markDirty();
                        })
        );
    }

    @Override
    public String name() {
        return "Armor Status";
    }

    @Override
    public boolean isEnabled() {
        return config.armorEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.armorEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        int x = config.armorX;
        int y = config.armorY;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        for (int slot = 3; slot >= 0; slot--) {
            ItemStack stack = mc.thePlayer.inventory.armorInventory[slot];
            if (stack == null) continue;
            int slotX = x + (3 - slot) * 18;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, slotX, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, slotX, y, null);
        }
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null) {
            int slotX = x + 4 * 18;
            mc.getRenderItem().renderItemAndEffectIntoGUI(held, slotX, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, held, slotX, y, null);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public int width() {
        return 5 * 18;
    }

    @Override
    public int height() {
        return 18;
    }
}
