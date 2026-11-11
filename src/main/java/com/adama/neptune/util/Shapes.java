package com.adama.neptune.util;

import net.minecraft.client.gui.Gui;

public final class Shapes {
    private Shapes() {
    }

    public static void roundedRect(int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.min(radius, Math.min(width, height) / 2);
        if (r <= 0) {
            Gui.drawRect(x, y, x + width, y + height, color);
            return;
        }
        Gui.drawRect(x + r, y, x + width - r, y + height, color);
        Gui.drawRect(x, y + r, x + r, y + height - r, color);
        Gui.drawRect(x + width - r, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; i++) {
            int inset = r - (int) Math.sqrt((double) (r * r - (r - i) * (r - i)));
            Gui.drawRect(x + inset, y + i, x + width - inset, y + i + 1, color);
            Gui.drawRect(x + inset, y + height - 1 - i, x + width - inset, y + height - i, color);
        }
    }

    public static void roundedBorder(int x, int y, int width, int height, int radius, int color) {
        roundedRect(x, y, width, height, radius, color);
        roundedRect(x + 1, y + 1, width - 2, height - 2, Math.max(0, radius - 1), 0x00000000);
    }

    public static void card(int x, int y, int width, int height, int radius, int fill, int border) {
        roundedRect(x, y, width, height, radius, border);
        roundedRect(x + 1, y + 1, width - 2, height - 2, Math.max(0, radius - 1), fill);
    }
}
