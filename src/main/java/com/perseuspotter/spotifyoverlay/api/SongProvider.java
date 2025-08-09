package com.perseuspotter.spotifyoverlay.api;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SongProvider {
    public enum State {
        Paused,
        NotOpen,
        Advertisement,
        Song
    }

    private State cachedState = State.NotOpen;
    private String cachedSong = "Unknown Song";
    private String cachedArtist = "Unknown Artist";
    protected abstract State getStateImpl();
    protected abstract String getSongImpl();
    protected abstract String getArtistImpl();

    protected abstract void pollDataImpl();

    private final AtomicBoolean isPolling = new AtomicBoolean(false);
    public State getState() {
        if (isPolling.get()) return cachedState;
        return cachedState = getStateImpl();
    }
    public String getSong() {
        if (isPolling.get()) return cachedSong;
        return cachedSong = getSongImpl();
    }
    public String getArtist() {
        if (isPolling.get()) return cachedArtist;
        return cachedArtist = getArtistImpl();
    }
    public void pollData() {
        isPolling.set(true);
        pollDataImpl();
        isPolling.set(false);
    }
}
