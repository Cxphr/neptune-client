package com.adama.neptune.gui;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.module.BKModule;
import com.adama.neptune.module.ModuleManager;
import com.adama.neptune.util.IconRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGui extends GuiScreen {
    private static final int PANEL_W = 430;
    private static final int PANEL_H = 268;
    private static final int HEADER_H = 34;
    private static final int FOOTER_H = 20;
    private static final int SIDEBAR_W = 104;
    private static final int ROW_H = 22;

    private static final int ACCENT = 0xFF2E6BD6;
    private static final int ACCENT_SOFT = 0xFF8FB6F0;
    private static final int BG = 0xF0111620;
    private static final int SIDE = 0xFF0C1017;
    private static final int ROW = 0xFF151C26;
    private static final int ROW_ON = 0xFF17222F;
    private static final int TEXT = 0xFFE6EDF3;
    private static final int MUTED = 0xFF8A94A6;

    private final NeptuneConfig config;
    private final ModuleManager modules;
    private final List<String> categories = Arrays.asList("Movement", "Render", "HUD", "Utility");
    private final Set<String> expanded = new HashSet<>();

    private String category = "Movement";
    private int scroll;
    private int guiLeft;
    private int guiTop;
    private long openedAt;

    private Setting active;
    private int activeX;
    private int activeY;
    private int activeW;

    public ClickGui(NeptuneConfig config, ModuleManager modules) {
        this.config = config;
        this.modules = modules;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        openedAt = System.currentTimeMillis();
        active = null;
    }

    private int contentX() {
        return guiLeft + SIDEBAR_W + 10;
    }

    private int contentW() {
        return PANEL_W - SIDEBAR_W - 20;
    }

    private int contentTop() {
        return guiTop + HEADER_H + 8;
    }

    private int contentBottom() {
        return guiTop + PANEL_H - FOOTER_H - 6;
    }

    private int maxScroll() {
        int total = 0;
        for (BKModule module : modules.byCategory(category)) {
            total += ROW_H;
            if (expanded.contains(module.name())) {
                for (Setting setting : module.settings()) total += setting.height();
            }
        }
        return Math.max(0, total - (contentBottom() - contentTop()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        guiLeft = (width - PANEL_W) / 2;
        guiTop = (height - PANEL_H) / 2;
        scroll = MathHelper.clamp_int(scroll, 0, maxScroll());

        float fade = Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 160.0F);
        drawRect(0, 0, width, height, ((int) (fade * 120)) << 24);
        drawRect(guiLeft - 3, guiTop - 3, guiLeft + PANEL_W + 3, guiTop + PANEL_H + 3, 0x40000000);
        drawRect(guiLeft, guiTop, guiLeft + PANEL_W, guiTop + PANEL_H, BG);
        drawGradientRect(guiLeft, guiTop, guiLeft + PANEL_W, guiTop + HEADER_H, 0xFF1B2635, 0xFF111620);
        drawGradientRect(guiLeft, guiTop + HEADER_H, guiLeft + PANEL_W, guiTop + HEADER_H + 1, 0xFF3B82F6, 0xFF8B5CF6);

        mc.fontRendererObj.drawStringWithShadow("NEPTUNE", guiLeft + 12, guiTop + 8, TEXT);
        mc.fontRendererObj.drawStringWithShadow("CLIENT", guiLeft + 12, guiTop + 19, 0xFF8FB6F0);
        String version = "v" + com.adama.neptune.NeptuneClient.VERSION;
        mc.fontRendererObj.drawStringWithShadow(version, guiLeft + PANEL_W - 12 - mc.fontRendererObj.getStringWidth(version), guiTop + 12, MUTED);

        drawRect(guiLeft, guiTop + HEADER_H + 1, guiLeft + SIDEBAR_W, guiTop + PANEL_H - FOOTER_H, SIDE);
        drawRect(guiLeft + SIDEBAR_W, guiTop + HEADER_H + 1, guiLeft + SIDEBAR_W + 1, guiTop + PANEL_H - FOOTER_H, 0xFF1B2430);

        int sidebarY = guiTop + HEADER_H + 10;
        for (String name : categories) {
            boolean selected = name.equals(category);
            boolean hover = inside(mouseX, mouseY, guiLeft + 6, sidebarY, SIDEBAR_W - 12, 20);
            if (selected) {
                drawGradientRect(guiLeft + 6, sidebarY, guiLeft + SIDEBAR_W - 6, sidebarY + 20, 0x553B82F6, 0x338B5CF6);
                drawGradientRect(guiLeft + 6, sidebarY, guiLeft + 8, sidebarY + 20, 0xFF3B82F6, 0xFF8B5CF6);
            } else if (hover) {
                drawRect(guiLeft + 6, sidebarY, guiLeft + SIDEBAR_W - 6, sidebarY + 20, 0x221E2A3A);
            }
            IconRenderer.draw(categoryIcon(name), guiLeft + 12, sidebarY + 5, 10, selected ? 0xFFCFE3FF : MUTED);
            mc.fontRendererObj.drawStringWithShadow(name, guiLeft + 28, sidebarY + 6, selected ? TEXT : MUTED);
            sidebarY += 24;
        }

        int scale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(contentX() * scale, mc.displayHeight - contentBottom() * scale,
                contentW() * scale, (contentBottom() - contentTop()) * scale);

        int y = contentTop() - scroll;
        for (BKModule module : modules.byCategory(category)) {
            boolean hover = inside(mouseX, mouseY, contentX(), y, contentW(), ROW_H)
                    && mouseY >= contentTop() && mouseY <= contentBottom();
            boolean on = module.isEnabled();
            drawRect(contentX(), y, contentX() + contentW(), y + ROW_H - 2, on ? ROW_ON : (hover ? ROW : 0x00000000));
            if (on) drawRect(contentX(), y, contentX() + 2, y + ROW_H - 2, ACCENT);
            if (hover) drawRect(contentX(), y, contentX() + contentW(), y + 1, 0x332E6BD6);
            IconRenderer.draw(categoryIcon(module.category()), contentX() + 8, y + 6, 9, on ? 0xFFCFE3FF : MUTED);
            mc.fontRendererObj.drawStringWithShadow(module.name(), contentX() + 22, y + 7, on ? TEXT : MUTED);
            if (!module.settings().isEmpty()) {
                String chevron = expanded.contains(module.name()) ? "-" : "+";
                int cx = contentX() + contentW() - 14;
                mc.fontRendererObj.drawStringWithShadow(chevron, cx, y + 7, hover ? ACCENT_SOFT : MUTED);
            }
            y += ROW_H;
            if (expanded.contains(module.name())) {
                for (Setting setting : module.settings()) {
                    setting.render(contentX() + 8, y, contentW() - 8, mouseX, mouseY);
                    y += setting.height();
                }
                drawRect(contentX() + 8, y + 1, contentX() + contentW() - 8, y + 2, 0xFF1E2836);
                y += 4;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawRect(guiLeft, guiTop + PANEL_H - FOOTER_H, guiLeft + PANEL_W, guiTop + PANEL_H - FOOTER_H + 1, 0xFF1B2430);
        int kbX = guiLeft + PANEL_W - 86;
        boolean kbHover = inside(mouseX, mouseY, kbX, guiTop + PANEL_H - FOOTER_H + 3, 76, 14);
        drawRect(kbX, guiTop + PANEL_H - FOOTER_H + 3, kbX + 76, guiTop + PANEL_H - FOOTER_H + 17, kbHover ? 0xFF2A3550 : 0xFF1A2230);
        mc.fontRendererObj.drawStringWithShadow("Keybinds", kbX + 12, guiTop + PANEL_H - FOOTER_H + 6, 0xFFCFE3FF);
        String hint = "RightShift closes  ·  click a row to toggle";
        mc.fontRendererObj.drawStringWithShadow(hint, guiLeft + 10,
                guiTop + PANEL_H - FOOTER_H + 6, MUTED);
    }

    private static int categoryIcon(String category) {
        if ("Movement".equals(category)) return IconRenderer.MOVEMENT;
        if ("Render".equals(category)) return IconRenderer.RENDER;
        if ("HUD".equals(category)) return IconRenderer.HUD;
        if ("Utility".equals(category)) return IconRenderer.UTILITY;
        return IconRenderer.CLIENT;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        if (inside(mouseX, mouseY, guiLeft + PANEL_W - 86, guiTop + PANEL_H - FOOTER_H + 3, 76, 14)) {
            mc.displayGuiScreen(new KeybindScreen(config, modules));
            return;
        }

        int sidebarY = guiTop + HEADER_H + 10;
        for (String name : categories) {
            if (inside(mouseX, mouseY, guiLeft + 6, sidebarY, SIDEBAR_W - 12, 20)) {
                category = name;
                scroll = 0;
                return;
            }
            sidebarY += 24;
        }

        if (mouseX < contentX() || mouseX > contentX() + contentW()) return;
        if (mouseY < contentTop() || mouseY > contentBottom()) return;

        int y = contentTop() - scroll;
        for (BKModule module : modules.byCategory(category)) {
            if (mouseY >= y && mouseY < y + ROW_H) {
                if (mouseX > contentX() + contentW() - 22) {
                    if (!expanded.remove(module.name())) expanded.add(module.name());
                } else {
                    module.setEnabled(!module.isEnabled());
                }
                return;
            }
            y += ROW_H;
            if (expanded.contains(module.name())) {
                for (Setting setting : module.settings()) {
                    if (setting.mouseClicked(mouseX, mouseY, mouseButton, contentX() + 8, y, contentW() - 8)) {
                        active = setting;
                        activeX = contentX() + 8;
                        activeY = y;
                        activeW = contentW() - 8;
                        return;
                    }
                    y += setting.height();
                }
                y += 4;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (active != null) active.mouseDragged(mouseX, mouseY, activeX, activeY, activeW);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (active != null) {
            active.mouseReleased();
            active = null;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll = MathHelper.clamp_int(scroll - Integer.signum(wheel) * 14, 0, maxScroll());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
