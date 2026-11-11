package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class KeystrokesModule extends HudModule {
    private static final int SIZE = 16;
    private static final int GAP = 2;

    private final List<Setting> settings;
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean leftDown;
    private boolean rightDown;

    public KeystrokesModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.keystrokesEnabled, value -> {
                    config.keystrokesEnabled = value;
                    config.markDirty();
                }),
                new BoolSetting("Show CPS", () -> config.keystrokesShowCps, value -> {
                    config.keystrokesShowCps = value;
                    config.markDirty();
                }),
                new SliderSetting("X", 0.0D, 1000.0D, 1.0D, 0,
                        () -> config.keystrokesX,
                        value -> {
                            config.keystrokesX = (int) Math.round(value);
                            config.markDirty();
                        }),
                new SliderSetting("Y", 0.0D, 600.0D, 1.0D, 0,
                        () -> config.keystrokesY,
                        value -> {
                            config.keystrokesY = (int) Math.round(value);
                            config.markDirty();
                        })
        );
    }

    @Override
    public String name() {
        return "Keystrokes";
    }

    @Override
    public boolean isEnabled() {
        return config.keystrokesEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.keystrokesEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    public void onMouse(MouseEvent event) {
        if (event.button == 0) {
            leftDown = event.buttonstate;
            if (event.buttonstate) leftClicks.addLast(Minecraft.getSystemTime());
        } else if (event.button == 1) {
            rightDown = event.buttonstate;
            if (event.buttonstate) rightClicks.addLast(Minecraft.getSystemTime());
        }
    }

    private int cps(Deque<Long> clicks) {
        long now = Minecraft.getSystemTime();
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst();
        return clicks.size();
    }

    @Override
    public void render() {
        Minecraft mc = Minecraft.getMinecraft();
        int x = config.keystrokesX;
        int y = config.keystrokesY;
        boolean forward = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        boolean left = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        boolean back = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        boolean right = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());

        key(mc, x + SIZE + GAP, y, SIZE, "W", forward);
        key(mc, x, y + SIZE + GAP, SIZE, "A", left);
        key(mc, x + SIZE + GAP, y + SIZE + GAP, SIZE, "S", back);
        key(mc, x + 2 * (SIZE + GAP), y + SIZE + GAP, SIZE, "D", right);

        int mouseY = y + 2 * (SIZE + GAP);
        key(mc, x, mouseY, SIZE * 2 + GAP, "LMB", leftDown);
        key(mc, x + 2 * (SIZE + GAP), mouseY, SIZE, "RMB", rightDown);

        if (config.keystrokesShowCps) {
            String text = "CPS " + cps(leftClicks) + " | " + cps(rightClicks);
            mc.fontRendererObj.drawStringWithShadow(text, x, mouseY + SIZE + 3, 0xFFCFE3FF);
        }
    }

    private void key(Minecraft mc, int x, int y, int width, String label, boolean pressed) {
        Gui.drawRect(x, y, x + width, y + SIZE, pressed ? 0xFF2E6BD6 : 0x99101820);
        Gui.drawRect(x, y, x + width, y + 1, 0xFF3A4a5E);
        Gui.drawRect(x, y + SIZE - 1, x + width, y + SIZE, 0xFF3A4A5E);
        Gui.drawRect(x, y, x + 1, y + SIZE, 0xFF3A4A5E);
        Gui.drawRect(x + width - 1, y, x + width, y + SIZE, 0xFF3A4A5E);
        int textX = x + (width - mc.fontRendererObj.getStringWidth(label)) / 2;
        int textY = y + (SIZE - 8) / 2;
        mc.fontRendererObj.drawStringWithShadow(label, textX, textY, pressed ? 0xFFFFFFFF : 0xFFC7D0DC);
    }

    @Override
    public int width() {
        return 3 * SIZE + 2 * GAP;
    }

    @Override
    public int height() {
        return 3 * (SIZE + GAP) + 12;
    }
}
