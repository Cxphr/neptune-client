package com.adama.neptune.gui;

import com.adama.neptune.NeptuneClient;
import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.module.BKModule;
import com.adama.neptune.module.ModuleManager;
import com.adama.neptune.util.Ease;
import com.adama.neptune.util.IconRenderer;
import com.adama.neptune.util.Shapes;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClickGui extends GuiScreen {
    private static final String[] CATEGORIES = {"Movement", "Render", "HUD", "Utility"};
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        DESCRIPTIONS.put("freelook", "Camera-only free look");
        DESCRIPTIONS.put("smooth_zoom", "Progressive smooth zoom");
        DESCRIPTIONS.put("toggle_sprint", "Always sprint while moving");
        DESCRIPTIONS.put("motion_blur", "Velocity-based screen blur");
        DESCRIPTIONS.put("hitboxes", "Player hitbox renderer");
        DESCRIPTIONS.put("fullbright", "Maximum client brightness");
        DESCRIPTIONS.put("item_physics", "Flat, spinning dropped items");
        DESCRIPTIONS.put("fps", "Frames per second");
        DESCRIPTIONS.put("clock", "Local time");
        DESCRIPTIONS.put("stopwatch", "Session stopwatch");
        DESCRIPTIONS.put("day", "World day number");
        DESCRIPTIONS.put("ping", "Server latency");
        DESCRIPTIONS.put("memory", "JVM memory usage");
        DESCRIPTIONS.put("playtime", "Session playtime");
        DESCRIPTIONS.put("saturation", "Food saturation");
        DESCRIPTIONS.put("server", "Current server address");
        DESCRIPTIONS.put("itemcounter", "Held item inventory count");
        DESCRIPTIONS.put("combo", "Consecutive hits");
        DESCRIPTIONS.put("reach", "Distance to crosshair entity");
        DESCRIPTIONS.put("direction", "Cardinal direction");
        DESCRIPTIONS.put("potion", "Active potion effects");
        DESCRIPTIONS.put("coords", "Coordinates, chunk and facing");
        DESCRIPTIONS.put("keystrokes", "WASD and CPS overlay");
        DESCRIPTIONS.put("armor", "Armor and held item status");
        DESCRIPTIONS.put("healthbar", "Vertical health bar");
        DESCRIPTIONS.put("enemyhealth", "Health bars above players");
        DESCRIPTIONS.put("bedfinder", "Nearby beds and block counts");
        DESCRIPTIONS.put("crosshair", "Custom crosshair styles");
        DESCRIPTIONS.put("blockoutline", "Block selection outline");
        DESCRIPTIONS.put("chunkborders", "Chunk boundary lines");
        DESCRIPTIONS.put("fov", "Field of view multiplier");
        DESCRIPTIONS.put("autogg", "Sends gg after games");
        DESCRIPTIONS.put("chattimestamps", "Chat timestamps");
        DESCRIPTIONS.put("gldebug", "OpenGL debug controls");
    }

    private final NeptuneConfig config;
    private final ModuleManager modules;
    private final Set<String> expanded = new HashSet<>();
    private final Map<BKModule, List<Setting>> settingsCache = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();
    private final List<CategoryHit> categoryHits = new ArrayList<>();

    private String category = "Movement";
    private String search = "";
    private boolean searchFocused;
    private int scroll;
    private int totalHeight;
    private int guiLeft;
    private int guiTop;
    private int panelW;
    private int panelH;
    private int contentLeft;
    private int contentTop;
    private int contentRight;
    private int contentBottom;
    private int modeLunarX;
    private int modeVerticalX;
    private int modeY;
    private int searchX;
    private int searchY;
    private int searchW;
    private int keybindsX;
    private int keybindsY;

    private Setting active;
    private int activeX;
    private int activeY;
    private int activeW;
    private long openedAt;

    private static final class Hit {
        BKModule module;
        int x;
        int y;
        int w;
        int h;
        int toggleX;
        int toggleY;
        int toggleW;
        int toggleH;
        int expandX;
        int expandY;
        int expandW;
        int expandH;
        int keyX;
        int keyY;
        int keyW;
        int keyH;
        final List<SettingHit> settings = new ArrayList<>();
    }

    private static final class SettingHit {
        Setting setting;
        int x;
        int y;
        int w;
    }

    private static final class CategoryHit {
        final String name;
        final int x;
        final int y;
        final int w;
        final int h;

        CategoryHit(String name, int x, int y, int w, int h) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

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
        searchFocused = false;
    }

    private boolean lunarMode() {
        return config.guiMode == 0;
    }

    private List<Setting> settingsOf(BKModule module) {
        List<Setting> cached = settingsCache.get(module);
        if (cached == null) {
            cached = module.settings();
            settingsCache.put(module, cached);
        }
        return cached;
    }

    private String description(BKModule module) {
        String description = DESCRIPTIONS.get(module.id());
        return description == null ? module.category() + " module" : description;
    }

    private boolean matches(BKModule module) {
        if (search.isEmpty()) return true;
        String query = search.toLowerCase();
        return module.name().toLowerCase().contains(query)
                || module.id().contains(query)
                || module.category().toLowerCase().contains(query)
                || description(module).toLowerCase().contains(query);
    }

    private List<BKModule> visibleModules() {
        List<BKModule> list = new ArrayList<>();
        if (!search.isEmpty()) {
            for (BKModule module : modules.modules()) {
                if (matches(module)) list.add(module);
            }
            return list;
        }
        return modules.byCategory(category);
    }

    private static int categoryIcon(String name) {
        if ("Movement".equals(name)) return IconRenderer.MOVEMENT;
        if ("Render".equals(name)) return IconRenderer.RENDER;
        if ("HUD".equals(name)) return IconRenderer.HUD;
        if ("Utility".equals(name)) return IconRenderer.UTILITY;
        return IconRenderer.CLIENT;
    }

    private static String keyName(int code) {
        if (code == 0) return "-";
        if (code == -100) return "LMB";
        if (code == -99) return "RMB";
        if (code == -98) return "MMB";
        String name = Keyboard.getKeyName(code);
        return name == null ? "-" : name;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int maxW = width - 24;
        int maxH = height - 24;
        panelW = Math.min(lunarMode() ? 470 : 340, maxW);
        panelH = Math.min(320, maxH);
        if (panelW < 260) panelW = 260;
        if (panelH < 220) panelH = 220;
        guiLeft = (width - panelW) / 2;
        guiTop = (height - panelH) / 2;

        float fade = Ease.smooth(Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 170.0F));
        drawRect(0, 0, width, height, ((int) (fade * 150.0F)) << 24);

        Shapes.roundedRect(guiLeft - 2, guiTop - 2, panelW + 4, panelH + 4, 8, Theme.SHADOW);
        Shapes.card(guiLeft, guiTop, panelW, panelH, 7, Theme.BG, Theme.BORDER);

        drawHeader(mouseX, mouseY);
        if (lunarMode()) drawLunarRail(mouseX, mouseY);
        else drawVerticalSidebar(mouseX, mouseY);

        int headerH = 34;
        if (lunarMode()) {
            contentLeft = guiLeft + 52 + 12;
        } else {
            contentLeft = guiLeft + 82 + 8;
        }
        contentRight = guiLeft + panelW - 12;
        contentTop = guiTop + headerH + 8;
        contentBottom = guiTop + panelH - 24;

        hits.clear();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution resolution = new ScaledResolution(mc);
        int scale = resolution.getScaleFactor();
        GL11.glScissor(contentLeft * scale, mc.displayHeight - contentBottom * scale,
                Math.max(0, contentRight - contentLeft) * scale, Math.max(0, contentBottom - contentTop) * scale);

        int startY = contentTop - scroll;
        if (lunarMode()) drawLunarCards(mouseX, mouseY, startY);
        else drawVerticalRows(mouseX, mouseY, startY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        totalHeight = Math.max(0, (lastY - (contentTop - scroll)));
        scroll = clamp(scroll, 0, Math.max(0, totalHeight - (contentBottom - contentTop)));

        drawFooter(mouseX, mouseY);
    }

    private int lastY;

    private void drawHeader(int mouseX, int mouseY) {
        String title = "NEPTUNE";
        mc.fontRendererObj.drawStringWithShadow(title, guiLeft + 12, guiTop + 8, Theme.TEXT);
        mc.fontRendererObj.drawStringWithShadow("v" + NeptuneClient.VERSION,
                guiLeft + 14 + mc.fontRendererObj.getStringWidth(title), guiTop + 10, Theme.MUTED);

        int modeW = 18;
        int modeH = 16;
        modeY = guiTop + 9;
        modeVerticalX = guiLeft + panelW - 12 - modeW;
        modeLunarX = modeVerticalX - modeW - 4;
        drawModeButton(modeLunarX, modeY, lunarMode(), IconRenderer.GRID, mouseX, mouseY);
        drawModeButton(modeVerticalX, modeY, !lunarMode(), IconRenderer.LIST, mouseX, mouseY);

        int searchRight = modeLunarX - 8;
        int searchLeft = guiLeft + Math.max(110, panelW / 3);
        if (searchRight - searchLeft < 60) searchLeft = Math.max(guiLeft + 96, searchRight - 60);
        searchX = searchLeft;
        searchY = guiTop + 10;
        searchW = Math.max(40, searchRight - searchLeft);
        Shapes.card(searchX, searchY, searchW, 14, 4, Theme.SECONDARY, searchFocused ? Theme.MUTED : Theme.BORDER);
        String shown = search.isEmpty() ? "Search" : search;
        String trimmed = mc.fontRendererObj.trimStringToWidth(shown, searchW - 10);
        mc.fontRendererObj.drawString(trimmed, searchX + 5, searchY + 3, search.isEmpty() ? Theme.MUTED : Theme.TEXT);
        if (searchFocused && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursor = searchX + 5 + mc.fontRendererObj.getStringWidth(trimmed);
            drawRect(cursor, searchY + 3, cursor + 1, searchY + 11, Theme.TEXT);
        }

        drawRect(guiLeft + 1, guiTop + 34, guiLeft + panelW - 1, guiTop + 35, Theme.BORDER);
    }

    private void drawModeButton(int x, int y, boolean active, int icon, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, 18, 16);
        Shapes.card(x, y, 18, 16, 4, active ? Theme.SECONDARY : (hover ? Theme.HOVER : Theme.PANEL),
                active ? Theme.BORDER : Theme.PANEL);
        IconRenderer.draw(icon, x + 5, y + 4, 8, active ? Theme.TEXT : Theme.MUTED);
    }

    private void drawLunarRail(int mouseX, int mouseY) {
        int railW = 52;
        drawRect(guiLeft + 1, guiTop + 35, guiLeft + railW, guiTop + panelH - 20, Theme.PANEL);
        drawRect(guiLeft + railW, guiTop + 35, guiLeft + railW + 1, guiTop + panelH - 20, Theme.BORDER);
        int y = guiTop + 35 + 12;
        for (String name : CATEGORIES) {
            boolean selected = name.equals(category) && search.isEmpty();
            boolean hover = inside(mouseX, mouseY, guiLeft + 8, y - 2, 36, 26);
            if (selected) {
                Shapes.card(guiLeft + 8, y - 2, 36, 26, 5, Theme.SECONDARY, Theme.BORDER);
            } else if (hover) {
                Shapes.roundedRect(guiLeft + 8, y - 2, 36, 26, 5, Theme.HOVER);
            }
            IconRenderer.draw(categoryIcon(name), guiLeft + 16, y + 3, 12, selected ? Theme.TEXT : Theme.MUTED);
            categoryHits.add(new CategoryHit(name, guiLeft + 8, y - 2, 36, 26));
            y += 34;
        }
    }

    private void drawVerticalSidebar(int mouseX, int mouseY) {
        int sidebarW = 82;
        drawRect(guiLeft + 1, guiTop + 35, guiLeft + sidebarW, guiTop + panelH - 20, Theme.PANEL);
        drawRect(guiLeft + sidebarW, guiTop + 35, guiLeft + sidebarW + 1, guiTop + panelH - 20, Theme.BORDER);
        int y = guiTop + 35 + 8;
        for (String name : CATEGORIES) {
            boolean selected = name.equals(category) && search.isEmpty();
            boolean hover = inside(mouseX, mouseY, guiLeft + 6, y, sidebarW - 12, 20);
            if (selected) {
                Shapes.roundedRect(guiLeft + 6, y, sidebarW - 12, 20, 4, Theme.SECONDARY);
                drawRect(guiLeft + 6, y + 4, guiLeft + 8, y + 16, Theme.TEXT);
            } else if (hover) {
                Shapes.roundedRect(guiLeft + 6, y, sidebarW - 12, 20, 4, Theme.HOVER);
            }
            IconRenderer.draw(categoryIcon(name), guiLeft + 12, y + 6, 8, selected ? Theme.TEXT : Theme.MUTED);
            mc.fontRendererObj.drawString(name, guiLeft + 26, y + 6, selected ? Theme.TEXT : Theme.MUTED);
            categoryHits.add(new CategoryHit(name, guiLeft + 6, y, sidebarW - 12, 20));
            y += 22;
        }
    }

    private void drawLunarCards(int mouseX, int mouseY, int y) {
        lastY = y;
        int cardW = contentRight - contentLeft;
        int cardH = 54;
        for (BKModule module : visibleModules()) {
            List<Setting> settings = settingsOf(module);
            boolean open = !settings.isEmpty() && expanded.contains(module.name());
            int extra = 0;
            if (open) {
                extra = 6;
                for (Setting setting : settings) extra += setting.height();
            }
            int x = contentLeft;
            boolean hover = inside(mouseX, mouseY, x, y, cardW, cardH);
            boolean on = module.isEnabled();
            Shapes.card(x, y, cardW, cardH + extra, 6,
                    hover ? Theme.SECONDARY : Theme.PANEL, hover ? Theme.BORDER : Theme.PANEL);
            if (on) drawRect(x + 1, y + 10, x + 3, y + cardH - 10, Theme.TEXT);

            IconRenderer.draw(categoryIcon(module.category()), x + 12, y + 11, 10, on ? Theme.TEXT : Theme.MUTED);
            mc.fontRendererObj.drawString(module.name(), x + 28, y + 9, on ? Theme.TEXT : Theme.MUTED);
            String desc = mc.fontRendererObj.trimStringToWidth(description(module), cardW - 130);
            mc.fontRendererObj.drawString(desc, x + 28, y + 21, Theme.MUTED);

            int toggleX = x + cardW - 34;
            int toggleY = y + 11;
            drawToggle(toggleX, toggleY, on);

            String key = keyName(modules.bindingCode("toggle." + module.id()));
            int keyW = Math.max(22, mc.fontRendererObj.getStringWidth(key) + 10);
            int keyX = x + cardW - 26 - keyW;
            int keyY = y + cardH - 17;
            boolean keyHover = inside(mouseX, mouseY, keyX, keyY, keyW, 12);
            Shapes.card(keyX, keyY, keyW, 12, 3, keyHover ? Theme.ACTIVE : Theme.SECONDARY, Theme.BORDER);
            mc.fontRendererObj.drawString(key, keyX + 5, keyY + 2, keyHover ? Theme.TEXT : Theme.MUTED);

            int expandX = x + cardW - 20;
            int expandY = y + cardH - 18;
            int expandW = 14;
            int expandH = 15;
            IconRenderer.draw(open ? IconRenderer.CHEVRON_DOWN : IconRenderer.CHEVRON_RIGHT,
                    expandX + 3, expandY + 4, 8, inside(mouseX, mouseY, expandX, expandY, expandW, expandH) ? Theme.TEXT : Theme.MUTED);

            Hit hit = new Hit();
            hit.module = module;
            hit.x = x;
            hit.y = y;
            hit.w = cardW;
            hit.h = cardH + extra;
            hit.toggleX = toggleX;
            hit.toggleY = toggleY;
            hit.toggleW = 22;
            hit.toggleH = 10;
            hit.expandX = expandX;
            hit.expandY = expandY;
            hit.expandW = expandW;
            hit.expandH = expandH;
            hit.keyX = keyX;
            hit.keyY = keyY;
            hit.keyW = keyW;
            hit.keyH = 12;

            if (open) {
                drawRect(x + 12, y + cardH, x + cardW - 12, y + cardH + 1, Theme.BORDER);
                int settingY = y + cardH + 6;
                for (Setting setting : settings) {
                    setting.render(x + 12, settingY, cardW - 24, mouseX, mouseY);
                    SettingHit settingHit = new SettingHit();
                    settingHit.setting = setting;
                    settingHit.x = x + 12;
                    settingHit.y = settingY;
                    settingHit.w = cardW - 24;
                    hit.settings.add(settingHit);
                    settingY += setting.height();
                }
            }
            hits.add(hit);
            y += cardH + extra + 6;
        }
        lastY = y - 6;
    }

    private void drawVerticalRows(int mouseX, int mouseY, int y) {
        lastY = y;
        int rowW = contentRight - contentLeft;
        int rowH = 22;
        for (BKModule module : visibleModules()) {
            List<Setting> settings = settingsOf(module);
            boolean open = !settings.isEmpty() && expanded.contains(module.name());
            int extra = 0;
            if (open) {
                extra = 4;
                for (Setting setting : settings) extra += setting.height();
            }
            boolean hover = inside(mouseX, mouseY, contentLeft, y, rowW, rowH);
            boolean on = module.isEnabled();
            if (hover || open) {
                Shapes.roundedRect(contentLeft, y, rowW, rowH, 3, Theme.SECONDARY);
            }
            if (on) drawRect(contentLeft, y + 5, contentLeft + 2, y + rowH - 5, Theme.TEXT);

            IconRenderer.draw(categoryIcon(module.category()), contentLeft + 8, y + 6, 9, on ? Theme.TEXT : Theme.MUTED);
            String name = mc.fontRendererObj.trimStringToWidth(module.name(), rowW - 84);
            mc.fontRendererObj.drawString(name, contentLeft + 22, y + 7, on ? Theme.TEXT : Theme.MUTED);

            String key = keyName(modules.bindingCode("toggle." + module.id()));
            if (!"-".equals(key)) {
                mc.fontRendererObj.drawString(key, contentLeft + rowW - 44 - mc.fontRendererObj.getStringWidth(key),
                        y + 7, Theme.MUTED);
            }

            int toggleX = contentLeft + rowW - 30;
            int toggleY = y + 6;
            drawToggle(toggleX, toggleY, on);
            int expandX = contentLeft + rowW - 14;
            int expandY = y + 7;
            if (!settings.isEmpty()) {
                IconRenderer.draw(open ? IconRenderer.CHEVRON_DOWN : IconRenderer.CHEVRON_RIGHT,
                        expandX + 3, expandY + 2, 7, inside(mouseX, mouseY, expandX, expandY, 12, 12) ? Theme.TEXT : Theme.MUTED);
            }

            Hit hit = new Hit();
            hit.module = module;
            hit.x = contentLeft;
            hit.y = y;
            hit.w = rowW;
            hit.h = rowH + extra;
            hit.toggleX = toggleX;
            hit.toggleY = toggleY;
            hit.toggleW = 22;
            hit.toggleH = 10;
            hit.expandX = expandX;
            hit.expandY = expandY;
            hit.expandW = 12;
            hit.expandH = 12;
            hit.keyX = contentLeft + rowW - 44;
            hit.keyY = y + 5;
            hit.keyW = 30;
            hit.keyH = 12;

            if (open) {
                int settingY = y + rowH;
                for (Setting setting : settings) {
                    setting.render(contentLeft + 10, settingY, rowW - 20, mouseX, mouseY);
                    SettingHit settingHit = new SettingHit();
                    settingHit.setting = setting;
                    settingHit.x = contentLeft + 10;
                    settingHit.y = settingY;
                    settingHit.w = rowW - 20;
                    hit.settings.add(settingHit);
                    settingY += setting.height();
                }
            }
            hits.add(hit);
            y += rowH + extra;
        }
        lastY = y;
    }

    private void drawToggle(int x, int y, boolean on) {
        Shapes.card(x, y, 22, 10, 5, on ? Theme.ACTIVE : Theme.SECONDARY, on ? Theme.MUTED : Theme.BORDER);
        int knob = on ? x + 13 : x + 2;
        Shapes.roundedRect(knob, y + 2, 7, 6, 3, on ? Theme.TEXT : Theme.MUTED);
    }

    private void drawFooter(int mouseX, int mouseY) {
        int y = guiTop + panelH - 20;
        drawRect(guiLeft + 1, y, guiLeft + panelW - 1, y + 1, Theme.BORDER);
        String hint = searchFocused ? "Type to search · ESC clears" : "RShift closes · Ctrl+F search";
        mc.fontRendererObj.drawString(hint, guiLeft + 12, y + 6, Theme.MUTED);

        keybindsX = guiLeft + panelW - 12 - 70;
        keybindsY = y + 3;
        boolean hover = inside(mouseX, mouseY, keybindsX, keybindsY, 70, 14);
        Shapes.card(keybindsX, keybindsY, 70, 14, 4, hover ? Theme.ACTIVE : Theme.SECONDARY, Theme.BORDER);
        IconRenderer.draw(IconRenderer.KEYBOARD, keybindsX + 7, keybindsY + 3, 8, Theme.MUTED);
        mc.fontRendererObj.drawString("Keybinds", keybindsX + 20, keybindsY + 3, hover ? Theme.TEXT : Theme.MUTED);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private void openKeybinds(String action) {
        mc.displayGuiScreen(new KeybindScreen(config, modules, action));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        if (inside(mouseX, mouseY, modeLunarX, modeY, 18, 16)) {
            config.guiMode = 0;
            config.markDirty();
            scroll = 0;
            searchFocused = false;
            return;
        }
        if (inside(mouseX, mouseY, modeVerticalX, modeY, 18, 16)) {
            config.guiMode = 1;
            config.markDirty();
            scroll = 0;
            searchFocused = false;
            return;
        }
        if (inside(mouseX, mouseY, searchX, searchY, searchW, 14)) {
            searchFocused = true;
            return;
        }
        searchFocused = false;

        for (CategoryHit hit : categoryHits) {
            if (inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h)) {
                category = hit.name;
                search = "";
                scroll = 0;
                return;
            }
        }
        if (inside(mouseX, mouseY, keybindsX, keybindsY, 70, 14)) {
            openKeybinds(null);
            return;
        }

        for (Hit hit : hits) {
            for (SettingHit settingHit : hit.settings) {
                if (inside(mouseX, mouseY, settingHit.x, settingHit.y, settingHit.w, settingHit.setting.height())) {
                    if (settingHit.setting.mouseClicked(mouseX, mouseY, mouseButton, settingHit.x, settingHit.y, settingHit.w)) {
                        active = settingHit.setting;
                        activeX = settingHit.x;
                        activeY = settingHit.y;
                        activeW = settingHit.w;
                    }
                    return;
                }
            }
        }

        for (Hit hit : hits) {
            if (!inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h)) continue;
            if (inside(mouseX, mouseY, hit.toggleX, hit.toggleY, hit.toggleW, hit.toggleH)) {
                hit.module.setEnabled(!hit.module.isEnabled());
                return;
            }
            if (inside(mouseX, mouseY, hit.expandX, hit.expandY, hit.expandW, hit.expandH)
                    && !hit.settings.isEmpty()) {
                if (!expanded.remove(hit.module.name())) expanded.add(hit.module.name());
                return;
            }
            if (inside(mouseX, mouseY, hit.keyX, hit.keyY, hit.keyW, hit.keyH)) {
                openKeybinds("toggle." + hit.module.id());
                return;
            }
            hit.module.setEnabled(!hit.module.isEnabled());
            return;
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
            int view = contentBottom - contentTop;
            scroll = clamp(scroll - Integer.signum(wheel) * 18, 0, Math.max(0, totalHeight - view));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (searchFocused && !search.isEmpty()) {
                search = "";
                return;
            }
            if (searchFocused) {
                searchFocused = false;
                return;
            }
            mc.displayGuiScreen(mc.theWorld == null ? new GuiMainMenu() : null);
            return;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            if (searchFocused && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                scroll = 0;
            }
            return;
        }
        if (keyCode == Keyboard.KEY_F && isCtrlKeyDown()) {
            searchFocused = true;
            return;
        }
        if (searchFocused && typedChar >= 32 && typedChar != 127 && search.length() < 32) {
            search += typedChar;
            scroll = 0;
        }
    }
}
