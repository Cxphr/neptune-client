package com.adama.neptune.gui;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.module.BKModule;
import com.adama.neptune.module.ModuleManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeybindScreen extends GuiScreen {
    private static final String[][] ROWS_65 = {
            {"GRAVE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "MINUS", "EQUALS", "BACK:2"},
            {"TAB:1.5", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "LBRACKET", "RBRACKET", "BACKSLASH:1.5"},
            {"CAPITAL:1.8", "A", "S", "D", "F", "G", "H", "J", "K", "L", "SEMICOLON", "APOSTROPHE", "RETURN:2.2"},
            {"LSHIFT:2.3", "Z", "X", "C", "V", "B", "N", "M", "COMMA", "PERIOD", "SLASH", "RSHIFT:2.7"},
            {"LCONTROL:1.4", "LWIN:1.1", "LALT:1.1", "SPACE:6.2", "RALT:1.1", "RWIN:1.1", "RCTRL:1.4", "LEFT", "UP", "DOWN", "RIGHT"}
    };

    private static final String[][] ROWS_TKL = {
            {"ESCAPE", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"},
            {"GRAVE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "MINUS", "EQUALS", "BACK:2"},
            {"TAB:1.5", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "LBRACKET", "RBRACKET", "BACKSLASH:1.5"},
            {"CAPITAL:1.8", "A", "S", "D", "F", "G", "H", "J", "K", "L", "SEMICOLON", "APOSTROPHE", "RETURN:2.2"},
            {"LSHIFT:2.3", "Z", "X", "C", "V", "B", "N", "M", "COMMA", "PERIOD", "SLASH", "RSHIFT:2.7"},
            {"LCONTROL:1.4", "LWIN:1.1", "LALT:1.1", "SPACE:6.2", "RALT:1.1", "RWIN:1.1", "RCTRL:1.4", "LEFT", "UP", "DOWN", "RIGHT"}
    };

    private static final String[][] ROWS_THINKPAD = {
            {"ESCAPE", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"},
            {"GRAVE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "MINUS", "EQUALS", "BACK:2"},
            {"TAB:1.5", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "LBRACKET", "RBRACKET", "BACKSLASH:1.5"},
            {"CAPITAL:1.8", "A", "S", "D", "F", "G", "H", "J", "K", "L", "SEMICOLON", "APOSTROPHE", "RETURN:2.2"},
            {"LSHIFT:2.3", "Z", "X", "C", "V", "B", "N", "M", "COMMA", "PERIOD", "SLASH", "RSHIFT:2.7"},
            {"LCONTROL:1.4", "FN:1.0", "LWIN:1.1", "LALT:1.1", "SPACE:6.2", "RALT:1.1", "RCTRL:1.3", "LEFT", "UP", "DOWN", "RIGHT"},
            {"INSERT", "HOME", "PAGEUP"},
            {"DELETE", "END", "PAGEDOWN"}
    };

    private static final String[][] ROWS_100 = {
            {"ESCAPE", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12", "NUMLOCK", "DIVIDE", "MULTIPLY", "SUBTRACT"},
            {"GRAVE", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "MINUS", "EQUALS", "BACK:2", "NUMPAD7", "NUMPAD8", "NUMPAD9", "ADD:1.4"},
            {"TAB:1.5", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "LBRACKET", "RBRACKET", "BACKSLASH:1.5", "NUMPAD4", "NUMPAD5", "NUMPAD6"},
            {"CAPITAL:1.8", "A", "S", "D", "F", "G", "H", "J", "K", "L", "SEMICOLON", "APOSTROPHE", "RETURN:2.2", "NUMPAD1", "NUMPAD2", "NUMPAD3", "ENTER:1.4"},
            {"LSHIFT:2.3", "Z", "X", "C", "V", "B", "N", "M", "COMMA", "PERIOD", "SLASH", "RSHIFT:2.7", "NUMPAD0:2.2", "DECIMAL"},
            {"LCONTROL:1.4", "LWIN:1.1", "LALT:1.1", "SPACE:6.2", "RALT:1.1", "RWIN:1.1", "RCTRL:1.4", "LEFT", "UP", "DOWN", "RIGHT"}
    };

    private static final int ACCENT_A = 0xFF3B82F6;
    private static final int ACCENT_B = 0xFF8B5CF6;

    private final NeptuneConfig config;
    private final ModuleManager manager;
    private final List<Action> actions = new ArrayList<>();
    private final List<int[]> keyHits = new ArrayList<>();
    private final List<Integer> keyCodes = new ArrayList<>();

    private int selected;
    private int scroll;
    private String layout = "65";
    private int guiLeft;
    private int guiTop;

    private static final class Action {
        final String id;
        final String label;

        Action(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public KeybindScreen(NeptuneConfig config, ModuleManager manager) {
        this(config, manager, null);
    }

    public KeybindScreen(NeptuneConfig config, ModuleManager manager, String initialAction) {
        this.config = config;
        this.manager = manager;
        actions.add(new Action("gui", "Open GUI"));
        actions.add(new Action("freelook", "Freelook"));
        actions.add(new Action("zoom", "Zoom"));
        for (BKModule module : manager.modules()) {
            actions.add(new Action("toggle." + module.id(), module.name()));
        }
        if (initialAction != null) {
            for (int i = 0; i < actions.size(); i++) {
                if (actions.get(i).id.equals(initialAction)) {
                    selected = i;
                    break;
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int panelW = 520;
        int panelH = 300;
        guiLeft = (width - panelW) / 2;
        guiTop = (height - panelH) / 2;
        drawRect(0, 0, width, height, 0x90000000);
        drawRect(guiLeft - 2, guiTop - 2, guiLeft + panelW + 2, guiTop + panelH + 2, 0x60000000);
        drawRect(guiLeft, guiTop, guiLeft + panelW, guiTop + panelH, 0xFF101010);
        drawGradientRect(guiLeft, guiTop, guiLeft + panelW, guiTop + 30, 0xFF181818, 0xFF080808);
        drawGradientRect(guiLeft, guiTop + 30, guiLeft + panelW, guiTop + 31, ACCENT_A, ACCENT_B);
        mc.fontRendererObj.drawStringWithShadow("KEYBINDS", guiLeft + 12, guiTop + 10, 0xFFE6EDF3);
        mc.fontRendererObj.drawStringWithShadow("click an action, then a key", guiLeft + 100, guiTop + 10, 0xFF8A94A6);

        int listX = guiLeft + 10;
        int listY = guiTop + 40;
        int listW = 150;
        int listH = panelH - 80;
        drawRect(listX, listY, listX + listW, listY + listH, 0xFF0C0C0C);
        int y = listY + 4 - scroll;
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (y + 16 >= listY && y <= listY + listH) {
                boolean sel = i == selected;
                boolean hover = inside(mouseX, mouseY, listX + 2, y, listW - 4, 16);
                if (sel) drawGradientRect(listX + 2, y, listX + listW - 2, y + 16, 0x33FFFFFF, 0x22FFFFFF);
                else if (hover) drawRect(listX + 2, y, listX + listW - 2, y + 16, 0x221E2A3A);
                mc.fontRendererObj.drawStringWithShadow(action.label, listX + 8, y + 4, sel ? 0xFFE6EDF3 : 0xFFB6C1D2);
                String key = keyName(manager.bindingCode(action.id));
                mc.fontRendererObj.drawStringWithShadow(key, listX + listW - 8 - mc.fontRendererObj.getStringWidth(key), y + 4, sel ? 0xFFDDDDDD : 0xFF808080);
            }
            y += 18;
        }

        String[] tabs = {"65", "TKL", "ThinkPad", "100"};
        int tabX = listX + listW + 10;
        for (String tab : tabs) {
            boolean active = tab.equals(layout);
            int tabW = 52;
            drawRect(tabX, guiTop + 38, tabX + tabW, guiTop + 54, active ? 0xFF24305A : 0xFF141B26);
            mc.fontRendererObj.drawStringWithShadow(tab, tabX + 8, guiTop + 43, active ? 0xFFE6EDF3 : 0xFF8A94A6);
            tabX += tabW + 4;
        }

        keyHits.clear();
        keyCodes.clear();
        String[][] rows = layoutRows();
        int keyboardX = listX + listW + 10;
        int keyboardY = guiTop + 62;
        int keyboardW = panelW - (keyboardX - guiLeft) - 10;
        float unit = keyboardW / (maxUnits(rows) + 0.2F);
        int keyHeight = 17;
        int rowY = keyboardY;
        for (String[] row : rows) {
            float x = keyboardX;
            for (String token : row) {
                String label = token;
                float units = 1.0F;
                int split = token.indexOf(':');
                if (split > 0) {
                    label = token.substring(0, split);
                    try {
                        units = Float.parseFloat(token.substring(split + 1));
                    } catch (NumberFormatException ignored) {
                    }
                }
                int code = Keyboard.getKeyIndex(label);
                int w = Math.round(units * unit) - 2;
                boolean hover = inside(mouseX, mouseY, Math.round(x), rowY, w, keyHeight);
                drawKeyCap(Math.round(x), rowY, w, keyHeight, pretty(label), hover);
                keyHits.add(new int[]{Math.round(x), rowY, w, keyHeight});
                keyCodes.add(code);
                x += units * unit;
            }
            rowY += keyHeight + 3;
        }

        int mouseKeyY = rowY + 6;
        String[] mouseLabels = {"LMB", "RMB", "MMB"};
        int[] mouseCodes = {-100, -99, -98};
        int mx = keyboardX;
        for (int i = 0; i < mouseLabels.length; i++) {
            boolean hover = inside(mouseX, mouseY, mx, mouseKeyY, 44, 16);
            drawKeyCap(mx, mouseKeyY, 44, 16, mouseLabels[i], hover);
            keyHits.add(new int[]{mx, mouseKeyY, 44, 16});
            keyCodes.add(mouseCodes[i]);
            mx += 48;
        }

        int buttonsY = guiTop + panelH - 26;
        drawKeyCap(guiLeft + 10, buttonsY, 70, 16, "UNBIND", inside(mouseX, mouseY, guiLeft + 10, buttonsY, 70, 16));
        String hint = "ESC closes  ·  right click an action to clear";
        mc.fontRendererObj.drawStringWithShadow(hint, guiLeft + 90, buttonsY + 4, 0xFF8A94A6);
    }

    private void drawKeyCap(int x, int y, int w, int h, String label, boolean hover) {
        drawRect(x, y, x + w, y + h, hover ? 0xFF2A3550 : 0xFF1A2230);
        drawRect(x, y, x + w, y + 1, hover ? 0xFF5B7BD6 : 0xFF2E3948);
        drawRect(x, y + h - 1, x + w, y + h, 0xFF0B0F16);
        mc.fontRendererObj.drawStringWithShadow(label, x + (w - mc.fontRendererObj.getStringWidth(label)) / 2, y + (h - 8) / 2, 0xFFDCE6F5);
    }

    private static float maxUnits(String[][] rows) {
        float max = 0.0F;
        for (String[] row : rows) {
            float sum = 0.0F;
            for (String token : row) {
                int split = token.indexOf(':');
                if (split > 0) {
                    try {
                        sum += Float.parseFloat(token.substring(split + 1));
                    } catch (NumberFormatException ignored) {
                        sum += 1.0F;
                    }
                } else {
                    sum += 1.0F;
                }
            }
            if (sum > max) max = sum;
        }
        return max;
    }

    private String[][] layoutRows() {
        if ("TKL".equals(layout)) return ROWS_TKL;
        if ("ThinkPad".equals(layout)) return ROWS_THINKPAD;
        if ("100".equals(layout)) return ROWS_100;
        return ROWS_65;
    }

    private static String pretty(String label) {
        if (label.length() <= 1) return label;
        if ("GRAVE".equals(label)) return "`";
        if ("MINUS".equals(label)) return "-";
        if ("EQUALS".equals(label)) return "=";
        if ("LBRACKET".equals(label)) return "[";
        if ("RBRACKET".equals(label)) return "]";
        if ("BACKSLASH".equals(label)) return "\\";
        if ("SEMICOLON".equals(label)) return ";";
        if ("APOSTROPHE".equals(label)) return "'";
        if ("COMMA".equals(label)) return ",";
        if ("PERIOD".equals(label)) return ".";
        if ("SLASH".equals(label)) return "/";
        if ("BACK".equals(label)) return "Bksp";
        if ("RETURN".equals(label)) return "Enter";
        if ("CAPITAL".equals(label)) return "Caps";
        if ("LSHIFT".equals(label)) return "Shift";
        if ("RSHIFT".equals(label)) return "Shift";
        if ("LCONTROL".equals(label)) return "Ctrl";
        if ("RCTRL".equals(label)) return "Ctrl";
        if ("LALT".equals(label)) return "Alt";
        if ("RALT".equals(label)) return "Alt";
        if ("LWIN".equals(label)) return "Win";
        if ("RWIN".equals(label)) return "Win";
        if ("SPACE".equals(label)) return "";
        if ("ESCAPE".equals(label)) return "Esc";
        if ("TAB".equals(label)) return "Tab";
        if ("NUMLOCK".equals(label)) return "Num";
        if ("DIVIDE".equals(label)) return "/";
        if ("MULTIPLY".equals(label)) return "*";
        if ("SUBTRACT".equals(label)) return "-";
        if ("ADD".equals(label)) return "+";
        if ("DECIMAL".equals(label)) return ".";
        if ("ENTER".equals(label)) return "Enter";
        if (label.startsWith("NUMPAD")) return label.substring(6);
        return label;
    }

    private static String keyName(int code) {
        if (code == 0) return "NONE";
        if (code == -100) return "LMB";
        if (code == -99) return "RMB";
        if (code == -98) return "MMB";
        String name = Keyboard.getKeyName(code);
        return name == null ? "?" : name;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1) {
            if (inside(mouseX, mouseY, guiLeft + 10, guiTop + 40, 150, 300 - 80)) {
                int index = (mouseY - (guiTop + 40) + scroll - 4) / 18;
                if (index >= 0 && index < actions.size()) {
                    manager.setBindingCode(actions.get(index).id, 0);
                }
            }
            return;
        }
        if (mouseButton != 0) return;

        int listX = guiLeft + 10;
        int listY = guiTop + 40;
        if (inside(mouseX, mouseY, listX, listY, 150, 300 - 80)) {
            int index = (mouseY - listY + scroll - 4) / 18;
            if (index >= 0 && index < actions.size()) {
                selected = index;
                return;
            }
        }

        String[] tabs = {"65", "TKL", "ThinkPad", "100"};
        int tabX = listX + 150 + 10;
        for (String tab : tabs) {
            if (inside(mouseX, mouseY, tabX, guiTop + 38, 52, 16)) {
                layout = tab;
                return;
            }
            tabX += 56;
        }

        if (inside(mouseX, mouseY, guiLeft + 10, guiTop + 300 - 26, 70, 16)) {
            manager.setBindingCode(actions.get(selected).id, 0);
            return;
        }

        for (int i = 0; i < keyHits.size() && i < keyCodes.size(); i++) {
            int[] rect = keyHits.get(i);
            if (inside(mouseX, mouseY, rect[0], rect[1], rect[2], rect[3])) {
                manager.setBindingCode(actions.get(selected).id, keyCodes.get(i));
                return;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll -= Integer.signum(wheel) * 18;
            int max = Math.max(0, actions.size() * 18 - (300 - 80));
            if (scroll < 0) scroll = 0;
            if (scroll > max) scroll = max;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new ClickGui(config, manager));
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
