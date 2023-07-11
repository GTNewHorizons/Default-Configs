package net.blay09.mods.defaultkeys;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    acceptableRemoteVersions = "*",
    acceptedMinecraftVersions = "[1.7.10]",
    modid = DefaultKeys.MOD_ID,
    name = "Default Configs",
    version = Tags.VERSION)
public class DefaultKeys {

    public static final String MOD_ID = "defaultkeys";
    public static final Logger logger = LogManager.getLogger();

    @Mod.Instance
    public static DefaultKeys instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide()
            .isClient()) {
            ClientCommandHandler.instance.registerCommand(new CommandDefaultOptions());
            MinecraftForge.EVENT_BUS.register(new EventHandler());
        }
    }

    public static void preStartGame() {
        EventHandler.preStartGame();
    }

    public boolean saveDefaultOptionsOptiFine() {
        return EventHandler.saveDefaultOptionsOptiFine();
    }

    public boolean saveDefaultMappings() {
        return EventHandler.saveDefaultMappings();
    }

    public boolean saveDefaultOptions() {
        return EventHandler.saveDefaultOptions();
    }

    public void reloadDefaultMappings() {
        EventHandler.reloadDefaultMappings();
    }
}
