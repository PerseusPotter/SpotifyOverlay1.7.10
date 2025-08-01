package com.perseuspotter.spotifyoverlay;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import static cpw.mods.fml.common.Mod.EventHandler;

@Mod(modid = SpotifyOverlay.MODID, version = SpotifyOverlay.VERSION, name = SpotifyOverlay.MODNAME, acceptedMinecraftVersions = "[1.7.10]")
public class SpotifyOverlay {
    public static final String MODID = "spotifyoverlay";
    public static final String VERSION = "1.0.0";
    public static final String MODNAME = "SpotifyOverlay";

    public static SpotifyOverlay INSTANCE;

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @EventHandler
    public void init(FMLInitializationEvent event) {
        INSTANCE = this;
        Config.load();
        Runtime.getRuntime().addShutdownHook(new Thread(Config::save));

        ClientCommandHandler.instance.registerCommand(new SpotifyCommand());
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!Config.getInstance().enabled) return;
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!IS_WINDOWS) {
            Config.getInstance().enabled = false;
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("only compatible with windows"));
            return;
        }

        SpotifyTextGui.INSTANCE.render();
    }

    private GuiScreen pendingGui = null;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (pendingGui == null) return;
        if (Minecraft.getMinecraft().currentScreen != null) return;

        Minecraft.getMinecraft().displayGuiScreen(pendingGui);
        pendingGui = null;
    }

    public void openGui(GuiScreen gui) {
        pendingGui = gui;
    }
}
