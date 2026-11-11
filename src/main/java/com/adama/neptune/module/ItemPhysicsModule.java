package com.adama.neptune.module;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.gui.setting.BoolSetting;
import com.adama.neptune.gui.setting.Setting;
import com.adama.neptune.gui.setting.SliderSetting;
import com.adama.neptune.render.RenderFlatItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

import java.util.Arrays;
import java.util.List;

public class ItemPhysicsModule extends BKModule {
    private final List<Setting> settings;
    private Render<? extends Entity> original;

    public ItemPhysicsModule(NeptuneConfig config) {
        super(config);
        this.settings = Arrays.asList(
                new BoolSetting("Enabled", () -> config.itemPhysicsEnabled, value -> {
                    config.itemPhysicsEnabled = value;
                    config.markDirty();
                }),
                new SliderSetting("Rotation speed", 0.1D, 4.0D, 0.1D, 1,
                        () -> config.itemPhysicsRotation,
                        value -> {
                            config.itemPhysicsRotation = (float) value;
                            config.markDirty();
                        })
        );
    }

    @Override
    public String name() {
        return "Item Physics";
    }

    @Override
    public String category() {
        return "Render";
    }

    @Override
    public int icon() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
        return config.itemPhysicsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.itemPhysicsEnabled = enabled;
        config.markDirty();
    }

    @Override
    public List<Setting> settings() {
        return settings;
    }

    @SuppressWarnings("unchecked")
    public void sync() {
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager manager = mc.getRenderManager();
        if (manager == null) return;
        Render<? extends Entity> current = manager.entityRenderMap.get(EntityItem.class);
        if (config.itemPhysicsEnabled) {
            if (!(current instanceof RenderFlatItem)) {
                original = current;
                manager.entityRenderMap.put(EntityItem.class,
                        new RenderFlatItem(manager, mc.getRenderItem(), config));
            }
        } else if (current instanceof RenderFlatItem) {
            if (original != null) {
                manager.entityRenderMap.put(EntityItem.class, original);
            } else {
                manager.entityRenderMap.put(EntityItem.class,
                        new RenderEntityItem(manager, mc.getRenderItem()));
            }
            original = null;
        }
    }
}
