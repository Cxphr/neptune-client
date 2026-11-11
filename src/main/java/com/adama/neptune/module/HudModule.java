package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;

public abstract class HudModule extends BKModule {
    protected HudModule(NeptuneConfig config) {
        super(config);
    }

    @Override
    public String category() {
        return "HUD";
    }

    public abstract void render();

    public abstract int width();

    public abstract int height();
}
