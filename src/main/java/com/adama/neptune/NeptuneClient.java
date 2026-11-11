package com.adama.neptune;

import com.adama.neptune.config.NeptuneConfig;
import com.adama.neptune.event.ClientEvents;
import com.adama.neptune.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = NeptuneClient.MODID, name = NeptuneClient.NAME, version = NeptuneClient.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class NeptuneClient {
    public static final String MODID = "neptune";
    public static final String NAME = "Neptune Client";
    public static final String VERSION = "1.1.0";

    @Mod.Instance(MODID)
    public static NeptuneClient instance;

    public NeptuneConfig config;
    public ModuleManager modules;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = NeptuneConfig.load(event.getModConfigurationDirectory());
        Runtime.getRuntime().addShutdownHook(new Thread(config::save));
        modules = new ModuleManager(config);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        modules.registerKeybinds();
        ClientEvents handler = new ClientEvents(this);
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }
}
