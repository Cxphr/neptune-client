package com.adama.neptune.gui;

import com.adama.neptune.NeptuneClient;
import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.ColorSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.module.BKModule;
import com.adama.neptune.module.ModuleManager;
import com.adama.neptune.util.Ease;
import com.adama.neptune.util.IconRenderer;
import com.adama.neptune.util.Shapes;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
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
    private static final String APPEARANCE = "__appearance";
    private static final ResourceLocation BACKGROUND = new ResourceLocation("neptune:textures/gui/background.png");
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();

    static {
        DESCRIPTIONS.put("freelook", "Camera-only free look");
        DESCRIPTIONS.put("smooth_zoom", "Progressive smooth zoom");
        DESCRIPTIONS.put("toggle_sprint", "Always sprint while moving");
        DESCRIPTIONS.put("motion_blur", "Velocity-based screen blur");
        DESCRIPTIONS.put("hitboxes", "Player hitbox renderer");
        DESCRIPTIONS.put("fullbright", "Maximum client brightness");
        DESCRIPTIONS.put("item_physics", "3D dropped items on the ground");
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
        DESCRIPTIONS.put("enemyhealth", "Health bars on entities");
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
    private final Set<String> collapsed = new HashSet<>();
    private final Map<BKModule, List<Setting>> settingsCache = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();
    private final List<CategoryHit> categoryHits = new ArrayList<>();
    private final List<Setting> appearanceSettings;
    private final List<SettingHit> appearanceHits = new ArrayList<>();

    private String search = "";
    private boolean searchFocused;
    private boolean appearanceOpen;
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
    private int lastY;

    private String capturingAction;
    private String capturingLabel;

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
        final Setting setting;
        final int x;
        final int y;
        final int w;

        SettingHit(Setting setting, int x, int y, int w) {
            this.setting = setting;
            this.x = x;
            this.y = y;
            this.w = w;
        }
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
        this.appearanceSettings = new ArrayList<>();
        appearanceSettings.add(new ColorSetting("Module background",
                () -> config.color("gui.moduleBg", 0xFF181818),
                value -> config.setColor("gui.moduleBg", value)));
        appearanceSettings.add(new ColorSetting("Module border",
                () -> config.color("gui.moduleBorder", 0xFF303030),
                value -> config.setColor("gui.moduleBorder", value)));
        appearanceSettings.add(new ColorSetting("Text",
                () -> config.color("gui.text", 0xFFFFFFFF),
                value -> config.setColor("gui.text", value)));
        appearanceSettings.add(new ColorSetting("Muted text",
                () -> config.color("gui.muted", 0xFF808080),
                value -> config.setColor("gui.muted", value)));
        appearanceSettings.add(new ColorSetting("Accent",
                () -> config.color("gui.accent", 0xFFFFFFFF),
                value -> config.setColor("gui.accent", value)));
        appearanceSettings.add(new BoolSetting("Background image",
                () -> config.toggle("gui.background", true),
                value -> config.setToggle("gui.background", value)));
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
        capturingAction = null;
        capturingLabel = null;
    }

    @Override
    public void onGuiClosed() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private boolean lunarMode() {
        return config.guiMode == 0;
    }

    private List<Setting> settingsOf(BKModule module) {
        List<Setting> list = settingsCache.get(module);
        if (list == null) {
            list = module.settings();
            settingsCache.put(module, list);
        }
        return list;
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

    private List<BKModule> filtered(String category) {
        List<BKModule> list = new ArrayList<>();
        for (BKModule module : modules.byCategory(category)) {
            if (matches(module)) list.add(module);
        }
        return list;
    }

    private int moduleBg() {
        return config.color("gui.moduleBg", 0xFF181818);
    }

    private int moduleBorder() {
        return config.color("gui.moduleBorder", 0xFF303030);
    }

    private int textColor() {
        return config.color("gui.text", 0xFFFFFFFF);
    }

    private int mutedColor() {
        return config.color("gui.muted", 0xFF808080);
    }

    private int accentColor() {
        return config.color("gui.accent", 0xFFFFFFFF);
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
        panelW = Math.min(lunarMode() ? 520 : 400, maxW);
        panelH = Math.min(330, maxH);
        if (panelW < 260) panelW = 260;
        if (panelH < 220) panelH = 220;
        guiLeft = (width - panelW) / 2;
        guiTop = (height - panelH) / 2;

        float fade = Ease.smooth(Math.min(1.0F, (System.currentTimeMillis() - openedAt) / 170.0F));
        if (config.toggle("gui.background", true)) {
            mc.getTextureManager().bindTexture(BACKGROUND);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, width, height, width, height);
        }
        drawRect(0, 0, width, height, ((int) (fade * 150.0F)) << 24);

        Shapes.roundedRect(guiLeft - 2, guiTop - 2, panelW + 4, panelH + 4, 8, Theme.SHADOW);
        Shapes.card(guiLeft, guiTop, panelW, panelH, 7, Theme.BG, Theme.BORDER);

        drawHeader(mouseX, mouseY);

        contentLeft = guiLeft + 12;
        contentRight = guiLeft + panelW - 12;
        contentTop = guiTop + 42;
        contentBottom = guiTop + panelH - 24;

        hits.clear();
        categoryHits.clear();
        appearanceHits.clear();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        try {
            ScaledResolution resolution = new ScaledResolution(mc);
            double sx = mc.displayWidth / (double) resolution.getScaledWidth();
            double sy = mc.displayHeight / (double) resolution.getScaledHeight();
            int px = (int) Math.round(contentLeft * sx);
            int py = (int) Math.round(mc.displayHeight - contentBottom * sy);
            int pw = (int) Math.round((contentRight - contentLeft) * sx);
            int ph = (int) Math.round((contentBottom - contentTop) * sy);
            GL11.glScissor(px, py, Math.max(0, pw), Math.max(0, ph));
            drawContent(mouseX, mouseY);
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        int view = contentBottom - contentTop;
        totalHeight = Math.max(0, lastY - (contentTop - scroll));
        scroll = clamp(scroll, 0, Math.max(0, totalHeight - view));

        drawFooter(mouseX, mouseY);
    }

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
        int searchLeft = guiLeft + Math.max(120, panelW / 3);
        if (searchRight - searchLeft < 60) searchLeft = Math.max(guiLeft + 100, searchRight - 60);
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

    private void drawContent(int mouseX, int mouseY) {
        int x = contentLeft;
        int w = contentRight - contentLeft;
        int y = contentTop - scroll;
        lastY = y;

        boolean appearanceHover = inside(mouseX, mouseY, x, y, w, 22);
        drawBar(APPEARANCE, "Appearance", IconRenderer.SETTINGS, x, y, w, appearanceOpen, appearanceHover, 0);
        categoryHits.add(new CategoryHit(APPEARANCE, x, y, w, 22));
        y += 24;
        if (appearanceOpen) {
            int settingsY = y + 4;
            for (Setting setting : appearanceSettings) {
                setting.render(x + 8, settingsY, w - 16, mouseX, mouseY);
                appearanceHits.add(new SettingHit(setting, x + 8, settingsY, w - 16));
                settingsY += setting.height();
            }
            y = settingsY + 6;
        }

        for (String category : CATEGORIES) {
            List<BKModule> list = filtered(category);
            if (!search.isEmpty() && list.isEmpty()) continue;
            boolean open = !collapsed.contains(category) || !search.isEmpty();
            boolean hover = inside(mouseX, mouseY, x, y, w, 22);
            drawBar(category, category, categoryIcon(category), x, y, w, open, hover, list.size());
            categoryHits.add(new CategoryHit(category, x, y, w, 22));
            y += 24;
            if (open) {
                for (BKModule module : list) {
                    if (lunarMode()) y += drawModuleCard(module, x, y, w, mouseX, mouseY);
                    else y += drawModuleRow(module, x, y, w, mouseX, mouseY);
                }
            }
        }
        lastY = y;
    }

    private void drawBar(String id, String label, int icon, int x, int y, int w, boolean open, boolean hover, int count) {
        Shapes.card(x, y, w, 22, 4, hover ? Theme.HOVER : Theme.SECONDARY, Theme.BORDER);
        if (open) drawRect(x + 1, y + 5, x + 3, y + 17, accentColor());
        IconRenderer.draw(icon, x + 10, y + 6, 9, open ? textColor() : mutedColor());
        mc.fontRendererObj.drawString(label, x + 26, y + 7, open ? textColor() : mutedColor());
        if (count > 0) {
            String text = String.valueOf(count);
            mc.fontRendererObj.drawString(text, x + w - 26 - mc.fontRendererObj.getStringWidth(text), y + 7, mutedColor());
        }
        IconRenderer.draw(open ? IconRenderer.CHEVRON_DOWN : IconRenderer.CHEVRON_RIGHT, x + w - 16, y + 8, 7,
                hover ? textColor() : mutedColor());
    }

    private int drawModuleCard(BKModule module, int x, int y, int w, int mouseX, int mouseY) {
        List<Setting> settings = settingsOf(module);
        boolean open = !settings.isEmpty() && expanded.contains(module.name());
        int extra = 0;
        if (open) {
            extra = 6;
            for (Setting setting : settings) extra += setting.height();
        }
        int cardH = 54;
        boolean hover = inside(mouseX, mouseY, x, y, w, cardH);
        boolean on = module.isEnabled();
        int bg = hover ? Theme.HOVER : moduleBg();
        Shapes.card(x, y, w, cardH + extra, 6, bg, moduleBorder());
        if (on) drawRect(x + 1, y + 10, x + 3, y + cardH - 10, accentColor());

        IconRenderer.draw(categoryIcon(module.category()), x + 12, y + 11, 10, on ? textColor() : mutedColor());
        mc.fontRendererObj.drawString(module.name(), x + 28, y + 9, on ? textColor() : mutedColor());
        String desc = mc.fontRendererObj.trimStringToWidth(description(module), w - 140);
        mc.fontRendererObj.drawString(desc, x + 28, y + 21, mutedColor());

        int toggleX = x + w - 34;
        int toggleY = y + 11;
        drawToggle(toggleX, toggleY, on);

        String key = keyName(modules.bindingCode("toggle." + module.id()));
        int keyW = Math.max(22, mc.fontRendererObj.getStringWidth(key) + 10);
        int keyX = x + w - 26 - keyW;
        int keyY = y + cardH - 17;
        boolean keyHover = inside(mouseX, mouseY, keyX, keyY, keyW, 12);
        Shapes.card(keyX, keyY, keyW, 12, 3, keyHover ? Theme.ACTIVE : Theme.SECONDARY, Theme.BORDER);
        mc.fontRendererObj.drawString(key, keyX + 5, keyY + 2, keyHover ? textColor() : mutedColor());

        int expandX = x + w - 20;
        int expandY = y + cardH - 18;
        IconRenderer.draw(open ? IconRenderer.CHEVRON_DOWN : IconRenderer.CHEVRON_RIGHT,
                expandX + 3, expandY + 4, 8, inside(mouseX, mouseY, expandX, expandY, 14, 15) ? textColor() : mutedColor());

        Hit hit = new Hit();
        hit.module = module;
        hit.x = x;
        hit.y = y;
        hit.w = w;
        hit.h = cardH + extra;
        hit.toggleX = toggleX;
        hit.toggleY = toggleY;
        hit.toggleW = 22;
        hit.toggleH = 10;
        hit.expandX = expandX;
        hit.expandY = expandY;
        hit.expandW = 14;
        hit.expandH = 15;
        hit.keyX = keyX;
        hit.keyY = keyY;
        hit.keyW = keyW;
        hit.keyH = 12;

        if (open) {
            drawRect(x + 12, y + cardH, x + w - 12, y + cardH + 1, moduleBorder());
            int settingY = y + cardH + 6;
            for (Setting setting : settings) {
                setting.render(x + 12, settingY, w - 24, mouseX, mouseY);
                hit.settings.add(new SettingHit(setting, x + 12, settingY, w - 24));
                settingY += setting.height();
            }
        }
        hits.add(hit);
        return cardH + extra + 6;
    }

    private int drawModuleRow(BKModule module, int x, int y, int w, int mouseX, int mouseY) {
        List<Setting> settings = settingsOf(module);
        boolean open = !settings.isEmpty() && expanded.contains(module.name());
        int extra = 0;
        if (open) {
            extra = 4;
            for (Setting setting : settings) extra += setting.height();
        }
        int rowH = 22;
        boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
        boolean on = module.isEnabled();
        Shapes.roundedRect(x, y, w, rowH, 3, hover ? Theme.HOVER : moduleBg());
        if (on) drawRect(x + 1, y + 5, x + 3, y + rowH - 5, accentColor());

        IconRenderer.draw(categoryIcon(module.category()), x + 9, y + 6, 9, on ? textColor() : mutedColor());
        String name = mc.fontRendererObj.trimStringToWidth(module.name(), w - 120);
        mc.fontRendererObj.drawString(name, x + 23, y + 7, on ? textColor() : mutedColor());

        String key = keyName(modules.bindingCode("toggle." + module.id()));
        if (!"-".equals(key)) {
            mc.fontRendererObj.drawString(key, x + w - 50 - mc.fontRendererObj.getStringWidth(key), y + 7, mutedColor());
        }

        int toggleX = x + w - 30;
        int toggleY = y + 6;
        drawToggle(toggleX, toggleY, on);
        int expandX = x + w - 14;
        int expandY = y + 7;
        if (!settings.isEmpty()) {
            IconRenderer.draw(open ? IconRenderer.CHEVRON_DOWN : IconRenderer.CHEVRON_RIGHT,
                    expandX + 3, expandY + 2, 7, inside(mouseX, mouseY, expandX, expandY, 12, 12) ? textColor() : mutedColor());
        }

        Hit hit = new Hit();
        hit.module = module;
        hit.x = x;
        hit.y = y;
        hit.w = w;
        hit.h = rowH + extra;
        hit.toggleX = toggleX;
        hit.toggleY = toggleY;
        hit.toggleW = 22;
        hit.toggleH = 10;
        hit.expandX = expandX;
        hit.expandY = expandY;
        hit.expandW = 12;
        hit.expandH = 12;
        hit.keyX = x + w - 50;
        hit.keyY = y + 5;
        hit.keyW = 30;
        hit.keyH = 12;

        if (open) {
            int settingY = y + rowH;
            for (Setting setting : settings) {
                setting.render(x + 10, settingY, w - 20, mouseX, mouseY);
                hit.settings.add(new SettingHit(setting, x + 10, settingY, w - 20));
                settingY += setting.height();
            }
        }
        hits.add(hit);
        return rowH + extra;
    }

    private void drawToggle(int x, int y, boolean on) {
        Shapes.card(x, y, 22, 10, 5, on ? Theme.ACTIVE : Theme.SECONDARY, on ? Theme.MUTED : Theme.BORDER);
        int knob = on ? x + 13 : x + 2;
        Shapes.roundedRect(knob, y + 2, 7, 6, 3, on ? accentColor() : mutedColor());
    }

    private void drawFooter(int mouseX, int mouseY) {
        int y = guiTop + panelH - 20;
        drawRect(guiLeft + 1, y, guiLeft + panelW - 1, y + 1, Theme.BORDER);
        String hint;
        if (capturingAction != null) hint = "Press a key for " + capturingLabel + " · ESC cancels";
        else hint = searchFocused ? "Type to search · ESC clears" : "Ctrl+R closes · Ctrl+F search";
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

        if (capturingAction != null) {
            if (mouseButton >= 0 && mouseButton <= 2) {
                modules.setBindingCode(capturingAction, -100 - mouseButton);
                capturingAction = null;
                capturingLabel = null;
            }
            return;
        }

        if (inside(mouseX, mouseY, keybindsX, keybindsY, 70, 14)) {
            mc.displayGuiScreen(new KeybindScreen(config, modules, null));
            return;
        }

        for (CategoryHit hit : categoryHits) {
            if (!inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h)) continue;
            if (APPEARANCE.equals(hit.name)) {
                appearanceOpen = !appearanceOpen;
            } else {
                if (!collapsed.remove(hit.name)) collapsed.add(hit.name);
            }
            return;
        }

        for (SettingHit hit : appearanceHits) {
            if (inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.setting.height())) {
                if (hit.setting.mouseClicked(mouseX, mouseY, mouseButton, hit.x, hit.y, hit.w)) {
                    active = hit.setting;
                    activeX = hit.x;
                    activeY = hit.y;
                    activeW = hit.w;
                }
                return;
            }
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
            if (inside(mouseX, mouseY, hit.expandX, hit.expandY, hit.expandW, hit.expandH) && !hit.settings.isEmpty()) {
                if (!expanded.remove(hit.module.name())) expanded.add(hit.module.name());
                return;
            }
            if (inside(mouseX, mouseY, hit.keyX, hit.keyY, hit.keyW, hit.keyH)) {
                capturingAction = "toggle." + hit.module.id();
                capturingLabel = hit.module.name();
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
        if (capturingAction != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                capturingAction = null;
                capturingLabel = null;
            } else {
                modules.setBindingCode(capturingAction, keyCode);
                capturingAction = null;
                capturingLabel = null;
            }
            return;
        }
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
