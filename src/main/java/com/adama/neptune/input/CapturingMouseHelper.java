package com.adama.neptune.input;

import com.adama.neptune.module.ModuleManager;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class CapturingMouseHelper extends MouseHelper {
    private final ModuleManager modules;

    public CapturingMouseHelper(ModuleManager modules) {
        this.modules = modules;
    }

    @Override
    public void mouseXYChange() {
        int dx = Mouse.getDX();
        int dy = Mouse.getDY();
        if (modules.freelook().isActive()) {
            modules.freelook().onMouseDelta(dx, dy, modules.zoom().sensitivityScale());
            this.deltaX = 0;
            this.deltaY = 0;
            return;
        }
        float scale = modules.zoom().sensitivityScale();
        this.deltaX = Math.round(dx * scale);
        this.deltaY = Math.round(dy * scale);
    }
}
