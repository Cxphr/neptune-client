package com.adama.neptune.gui.setting;

public abstract class Setting {
    protected final String label;

    protected Setting(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public abstract int height();

    public abstract void render(int x, int y, int width, int mouseX, int mouseY);

    public abstract boolean mouseClicked(int mouseX, int mouseY, int button, int x, int y, int width);

    public void mouseDragged(int mouseX, int mouseY, int x, int y, int width) {
    }

    public void mouseReleased() {
    }
}
