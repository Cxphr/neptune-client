package com.adama.neptune.util;

import net.minecraft.client.gui.Gui;

public final class IconRenderer {
    public static final int MOVEMENT = 0;
    public static final int RENDER = 1;
    public static final int HUD = 2;
    public static final int UTILITY = 3;
    public static final int CLIENT = 4;
    public static final int SEARCH = 5;
    public static final int CHEVRON_DOWN = 6;
    public static final int CHEVRON_RIGHT = 7;

    private IconRenderer() {
    }

    public static void draw(int icon, int x, int y, int size, int color) {
        int unit = Math.max(1, size / 8);
        switch (icon) {
            case MOVEMENT:
                bar(x + size / 2 - unit / 2, y, unit, size, color);
                bar(x + size / 2 - unit * 2, y + size / 2, unit * 2, unit * 2, color);
                bar(x + size / 2 + unit, y + size / 2, unit * 2, unit * 2, color);
                bar(x + size / 2 - unit / 2, y - unit, unit * 3, unit, color);
                break;
            case RENDER:
                bar(x, y + size / 3, size, unit, color);
                bar(x, y + size - size / 3 - unit, size, unit, color);
                bar(x, y + size / 3, unit, size - 2 * (size / 3), color);
                bar(x + size - unit, y + size / 3, unit, size - 2 * (size / 3), color);
                bar(x + size / 2 - unit, y + size / 2 - unit, unit * 2, unit * 2, color);
                break;
            case HUD:
                bar(x, y, size, unit, color);
                bar(x, y + size / 2 - unit / 2, size * 2 / 3, unit, color);
                bar(x, y + size - unit, size / 2, unit, color);
                break;
            case UTILITY:
                bar(x + size / 4, y + size / 4, size / 2, unit, color);
                bar(x + size / 4, y + size / 2, size / 2, unit, color);
                bar(x + size / 4, y + size * 3 / 4 - unit, size / 2, unit, color);
                bar(x + size / 4, y + size / 4, unit, size / 2, color);
                break;
            case CLIENT:
                bar(x + size / 3, y, size / 3, unit, color);
                bar(x + size / 3, y + size - unit, size / 3, unit, color);
                bar(x, y + size / 3, unit, size / 3, color);
                bar(x + size - unit, y + size / 3, unit, size / 3, color);
                bar(x + size / 4, y + size / 4, size / 2, size / 2, color);
                break;
            case SEARCH:
                bar(x + size / 6, y + size / 6, size / 2, unit, color);
                bar(x + size / 6, y + size / 6, unit, size / 2, color);
                bar(x + size / 6 + size / 2 - unit, y + size / 6, unit, size / 2, color);
                bar(x + size / 2, y + size / 2, unit, size / 2, color);
                bar(x + size / 2, y + size / 2, size / 2, unit, color);
                break;
            case CHEVRON_DOWN:
                bar(x + size / 4, y + size / 3, unit, size / 2, color);
                bar(x + size * 3 / 4 - unit, y + size / 3, unit, size / 2, color);
                bar(x + size / 4, y + size / 3, size / 2, unit, color);
                break;
            case CHEVRON_RIGHT:
                bar(x + size / 4, y + size / 4, size / 2, unit, color);
                bar(x + size / 4, y + size * 3 / 4 - unit, size / 2, unit, color);
                bar(x + size * 3 / 4 - unit, y + size / 4, unit, size / 2, color);
                break;
            default:
                bar(x, y, size, size, color);
                break;
        }
    }

    private static void bar(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }
}
