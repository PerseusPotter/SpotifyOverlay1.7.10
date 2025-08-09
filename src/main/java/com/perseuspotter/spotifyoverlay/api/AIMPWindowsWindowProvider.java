package com.perseuspotter.spotifyoverlay.api;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

// descriptive naming i think
public class AIMPWindowsWindowProvider extends SongProvider {
    public static final AIMPWindowsWindowProvider INSTANCE = new AIMPWindowsWindowProvider();

    private State state = State.NotOpen;
    private String song = "Unknown Song";
    private String artist = "Unknown Artist";
    protected State getStateImpl() {
        return state;
    }
    protected String getSongImpl() {
        return song;
    }
    protected String getArtistImpl() {
        return artist;
    }

    protected void pollDataImpl() {
        try {
            state = State.NotOpen;
            song = "Unknown Song";
            artist = "Unknown Artist";

            Process proc = new ProcessBuilder(
                "cmd.exe", "/s", "/c",
                "chcp", "65001",
                "&&",
                "tasklist.exe",
                "/fo", "csv",
                "/nh",
                "/v",
                "/fi", "\"IMAGENAME eq AIMP.exe\""
            ).start();
            Scanner sc = new Scanner(proc.getInputStream(), "utf-8");
            // Active code page: 65001
            sc.nextLine();
            processFinder:
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.equals("INFO: No tasks are running which match the specified criteria.")) break;

                String[] parts = line.split("\",\"");
                String name = Arrays.stream(parts).skip(8L).collect(Collectors.joining("\",\""));
                name = name.substring(0, name.length() - 1);

                switch (name) {
                    case "N/A": continue;
                    case "AIMP":
                        state = State.Paused;
                        break processFinder;
                    default:
                        int i = name.indexOf(" - ");
                        if (i >= 0) {
                            state = State.Song;
                            song = name.substring(i + 3);
                            artist = name.substring(0, i);
                            break processFinder;
                        } else state = State.Paused;
                }
            }
        } catch (Exception ignored) {}
    }
}
