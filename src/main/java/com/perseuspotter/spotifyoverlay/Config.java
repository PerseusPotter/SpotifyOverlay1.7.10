package com.perseuspotter.spotifyoverlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.perseuspotter.lib.textgui.GuiLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    public static final File SAVE_LOCATION = new File("./config/SpotifyOverlay.json");
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Config INSTANCE = null;
    public static Config getInstance() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static Config load() {
        try {
            return INSTANCE = gson.fromJson(new FileReader(SAVE_LOCATION), Config.class);
        } catch (Exception e) {
            return new Config();
        }
    }

    public static void save() {
        if (INSTANCE == null) return;
        try (FileWriter writer = new FileWriter(SAVE_LOCATION)) {
            gson.toJson(INSTANCE, writer);
        } catch (Exception e) {
            System.out.println("failed to save Spotify Overlay config");
            e.printStackTrace();
        }
    }

    public boolean enabled = true;
    public GuiLocation location = new GuiLocation(50.0, 50.0, 1.0, 0, true, 0);
    public int refreshDelay = 1_000;
    public String prefix = "&2Spotify &7>&r ";
    public String songFormat = "&a%ARTIST% &7-&b %SONG%";
    public boolean hideWhenNotOpen = true;
    public int scrollSpeed = 80;
    public int freezeTime = 1_500;
    public int maxLength = 100;
    public boolean alternate = false;
    public String font = "Mojangles";
}
