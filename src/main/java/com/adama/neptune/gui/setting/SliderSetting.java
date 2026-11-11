package com.adama.neptune.gui.setting;

import net.minecraft.client.Minecraft;
import com.adama.neptune.gui.Theme;
import net.minecraft.client.gui.Gui;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class SliderSetting extends Setting {
    private final double min;
    private final double max;
    private final double step;
    private final int decimals;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private boolean dragging;

    public SliderSetting(String label, double min, double max, double step, int decimals, DoubleSupplier getter, DoubleConsumer setter) {
        super(label);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimals;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return 26;
    }

    private double value() {
        return getter.getAsDouble();
    }

    private String formatted() {
        return String.format("%." + decimals + "f", value());
    }

    private void apply(int mouseX, int x, int width) {
        double pct = (mouseX - x) / (double) width;
        if (pct < 0.0D) pct = 0.0D;
        if (pct > 1.0D) pct = 1.0D;
        double raw = min + (max - min) * pct;
        if (step > 0.0D) raw = Math.round(raw / step) * step;
        if (raw < min) raw = min;
        if (raw > max) raw = max;
        setter.accept(raw);
    }

    @Override
    public void render(int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawStringWithShadow(label, x, y + 2, Theme.TEXT);
        String val = formatted();
        mc.fontRendererObj.drawStringWithShadow(val, x + width - mc.fontRendererObj.getStringWidth(val), y + 2, Theme.MUTED);
        int trackY = y + 15;
        Gui.drawRect(x, trackY, x + width, trackY + 4, Theme.SECONDARY);
        double pct = (value() - min) / (max - min);
        int fill = (int) Math.round(width * pct);
        Gui.drawRect(x, trackY, x + fill, trackY + 4, 0xFF404040);
        Gui.drawRect(x + fill - 2, trackY - 2, x + fill + 2, trackY + 6, Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button, int x, int y, int width) {
        if (mouseY >= y + 10 && mouseY <= y + 22 && mouseX >= x - 2 && mouseX <= x + width + 2) {
            dragging = true;
            apply(mouseX, x, width);
            return true;
        }
        return false;
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
