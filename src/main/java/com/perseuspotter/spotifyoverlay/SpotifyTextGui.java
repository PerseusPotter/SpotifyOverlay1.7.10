package com.perseuspotter.spotifyoverlay;

import com.google.common.base.Strings;
import com.perseuspotter.lib.textgui.GuiLocation;
import com.perseuspotter.lib.textgui.Marquee;
import com.perseuspotter.lib.textgui.TextGui;
import com.perseuspotter.spotifyoverlay.api.AIMPWindowsWindowProvider;
import com.perseuspotter.spotifyoverlay.api.SongProvider;
import com.perseuspotter.spotifyoverlay.api.SpotifyWindowsWindowProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SpotifyTextGui extends TextGui {
    public static final SpotifyTextGui INSTANCE = new SpotifyTextGui(Config.getInstance().location);

    public SongProvider API;
    {
        switch (Config.getInstance().provider) {
            case "AIMP":
                API = AIMPWindowsWindowProvider.INSTANCE;
                break;
            case "Spotify":
            default:
                API = SpotifyWindowsWindowProvider.INSTANCE;
        }
    }
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public final Marquee infoGui = new Marquee();
    private static class DummyTextGui extends TextGui {
        public DummyTextGui() {
            super(new GuiLocation(0.0, 0.0, 1.0, 0, true, 0));
        }

        public void forceUpdate() {
            updateCache();
            updateLines();
        }
    }
    private final DummyTextGui dummy = new DummyTextGui();
    private final Pattern formattingPattern = Pattern.compile("&([\\da-fk-or])");

    public SpotifyTextGui(GuiLocation location) {
        super(location);

        scheduler.schedule(this::schedule, 0, TimeUnit.MILLISECONDS);
        dummy.setLine(" ");
    }

    private void schedule() {
        if (Config.getInstance().enabled) API.pollData();
        scheduler.schedule(this::schedule, Config.getInstance().refreshDelay, TimeUnit.MILLISECONDS);
    }

    private void updateMarquee(SongProvider.State type, String artist, String song) {
        switch (type) {
            case Song:
                infoGui.setText(
                    formattingPattern.matcher(
                        Config.getInstance().songFormat
                            .replace("%ARTIST%", formattingPattern.matcher(artist).replaceAll("&\u200B$1"))
                            .replace("%SONG%", formattingPattern.matcher(song).replaceAll("&\u200B$1"))
                    ).replaceAll("§$1")
                );
                break;
            case Advertisement:
                infoGui.setText("&aAdvertisement");
                break;
            case Paused:
                infoGui.setText("&cPaused");
                break;
            case NotOpen:
                infoGui.setText("&cNot Opened");
                break;
        }
        infoGui.setScrollSpeed(Config.getInstance().scrollSpeed);
        infoGui.setFreezeTime(Config.getInstance().freezeTime);
        infoGui.setMaxLen(Config.getInstance().maxLength);
        infoGui.setAlternate(Config.getInstance().alternate);
        infoGui.setFont(Config.getInstance().font);
    }

    private void renderMarquee() {
        GuiLocation loc = getTrueLocation();
        infoGui.render(loc.x + getVisibleWidth() + dummy.getWidth(), loc.y, loc.s, loc.b);
    }

    private void updatePrefix() {
        setFont(Config.getInstance().font);
        dummy.location.s = location.s;
        dummy.setFont(font);
        dummy.forceUpdate();
        setLine(Config.getInstance().prefix + Strings.repeat(" ", (int) (Config.getInstance().maxLength / dummy.getWidth())));
    }

    @Override
    public void render() {
        if (isEdit) return;
        if (Config.getInstance().hideWhenNotOpen && API.getState().equals(SongProvider.State.NotOpen)) return;

        updatePrefix();
        updateMarquee(API.getState(), API.getArtist(), API.getSong());

        super.render();
        renderMarquee();
    }

    @Override
    public void editRenderHookPre() {
        updatePrefix();
        updateMarquee(SongProvider.State.Song, "Rick Astley", "Never Gonna Give You Up");
    }

    @Override
    public void editRenderHookPost() {
        renderMarquee();
    }
}
