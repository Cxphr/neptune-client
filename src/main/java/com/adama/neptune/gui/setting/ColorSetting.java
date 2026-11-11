package com.adama.neptune.gui.setting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ColorSetting extends Setting {
    private final IntSupplier getter;
    private final IntConsumer setter;
    private boolean dragging;

    public ColorSetting(String label, IntSupplier getter, IntConsumer setter) {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return 24;
    }

    @Override
    public void render(int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawStringWithShadow(label, x, y + 1, 0xFFC7D0DC);
        int barY = y + 12;
        for (int i = 0; i < width; i++) {
            float hue = i / (float) width;
            int color = 0xFF000000 | Color.HSBtoRGB(hue, 0.75F, 1.0F);
            Gui.drawRect(x + i, barY, x + i + 1, barY + 6, color);
        }
        int current = getter.getAsInt();
        float[] hsb = Color.RGBtoHSB((current >> 16) & 255, (current >> 8) & 255, current & 255, null);
        int marker = x + Math.round(hsb[0] * (width - 1));
        Gui.drawRect(marker - 1, barY - 2, marker + 1, barY + 8, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button, int x, int y, int width) {
        if (mouseY >= y + 10 && mouseY <= y + 20 && mouseX >= x && mouseX <= x + width) {
            dragging = true;
            apply(mouseX, x, width);
            return true;
        }
        return false;
    }

    private void apply(int mouseX, int x, int width) {
        float hue = Math.max(0.0F, Math.min(1.0F, (mouseX - x) / (float) width));
        setter.accept(0xFF000000 | Color.HSBtoRGB(hue, 0.75F, 1.0F));
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int x, int y, int width) {
        if (dragging) apply(mouseX, x, width);
    }

    @Override
    public void mouseReleased() {
        dragging = false;
    }
}
