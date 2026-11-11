package com.adama.neptune.gui.setting;

import net.minecraft.client.Minecraft;
import com.adama.neptune.gui.Theme;
import net.minecraft.client.gui.Gui;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BoolSetting extends Setting {
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public BoolSetting(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return 20;
    }

    @Override
    public void render(int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawStringWithShadow(label, x, y + 5, Theme.TEXT);
        boolean on = getter.getAsBoolean();
        int sw = 22;
        int sh = 10;
        int sx = x + width - sw;
        int sy = y + 4;
        Gui.drawRect(sx, sy, sx + sw, sy + sh, on ? Theme.ACTIVE : Theme.SECONDARY);
        int knobLeft = on ? sx + sw / 2 : sx + 1;
        int knobRight = on ? sx + sw - 1 : sx + sw / 2;
        Gui.drawRect(knobLeft, sy + 1, knobRight, sy + sh - 1, on ? Theme.TEXT : Theme.MUTED);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button, int x, int y, int width) {
        int sx = x + width - 22;
        if (mouseX >= sx && mouseX <= sx + 22 && mouseY >= y + 4 && mouseY <= y + 14) {
            setter.accept(!getter.getAsBoolean());
            return true;
        }
        return false;
    }
}
