package com.perseuspotter.lib.textgui;

import com.perseuspotter.spotifyoverlay.SpotifyOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.List;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class TextGui {
    public static final int MC_FONT_SIZE = 10;
    public static final Font MOJANGLES_FONT;
    public static final Map<String, Font> FONT_NAMES = new HashMap<>();
    static {
        Font mojanglesFont1;
        try {
            mojanglesFont1 = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(TextGui.class.getResourceAsStream("/Mojangles.ttf")));
        } catch (Exception e) {
            mojanglesFont1 = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()[0];
        }
        MOJANGLES_FONT = mojanglesFont1;
        for (Font f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            FONT_NAMES.put(f.getFamily().replace(" ", ""), f);
        }
        FONT_NAMES.put("Mojangles", MOJANGLES_FONT);
    }
    public static Map<Character, Color> COLORS = new HashMap<>();
    public static Map<Character, Color> COLORS_SHADOW = new HashMap<>();
    private static Color toAWTColor(int c) {
        return new Color(c >>> 24, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF);
    }
    static {
        char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int[] cols = {
            0x000000FF,
            0x0000AAFF,
            0x00AA00FF,
            0x00AAAAFF,
            0xAA0000FF,
            0xAA00AAFF,
            0xFFAA00FF,
            0xAAAAAAFF,
            0x555555FF,
            0x5555FFFF,
            0x55FF55FF,
            0x55FFFFFF,
            0xFF5555FF,
            0xFF55FFFF,
            0xFFFF55FF,
            0xFFFFFFFF
        };
        for (int i = 0; i < chars.length; i++) {
            COLORS.put(chars[i], toAWTColor(cols[i]));
            COLORS_SHADOW.put(chars[i], toAWTColor(((cols[i] >>> 2) & 0x3F3F3F00) | 0xFF));
        }
    }
    private static final ColorModel COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBufferByte.TYPE_BYTE);

    public GuiLocation location;
    public boolean isEdit = false;
    protected boolean cb = false;
    protected int cc = 0;
    protected boolean dirty = false;
    protected boolean hasImg = false;
    protected GLBufferedImageUploader img = new GLBufferedImageUploader();
    protected double rx = 0.0;
    protected double ry = 0.0;
    protected double rs = 1.0;
    protected double actW = 0.0;
    protected double actH = 0.0;
    protected int imgW = 0;
    protected int imgH = 0;
    protected String font = "Mojangles";
    protected Font fontMain;
    protected Font fontMono;
    protected Font fontBackup;
    protected boolean fontsDirty = true;
    protected int fontSize;

    public TextGui(GuiLocation location) {
        this.location = location;
    }

    public String[] getEditText() {
        return lines.stream().map(v -> v.s).toArray(String[]::new);
    }

    protected void mark() {
        dirty = true;
        for (Line l : lines) l.dirty = true;
    }

    public void setFont(String font) {
        if (this.font.equals(font)) return;
        this.font = font;
        fontsDirty = true;
        mark();
    }

    public String getFont() {
        return font;
    }

    public void edit() {
        isEdit = true;
        EditGui.curr = this;

        SpotifyOverlay.INSTANCE.openGui(new EditGui());
    }

    public static class Line {
        public final String s;
        public FontHelper.LineData data;
        public boolean dirty = true;

        public Line(String s) {
            this.s = s;
        }
    }

    protected List<Line> lines = new ArrayList<>();
    protected double lineW = 0.0;
    protected double lineVW = 0.0;
    protected boolean hasObf = false;

    public TextGui clearLines() {
        dirty = false;
        lines.clear();
        hasObf = false;
        lineW = 0.0;
        lineVW = 0.0;
        return this;
    }

    public TextGui setLine(String str) {
        if (lines.size() != 1 || !lines.get(0).s.equals(str)) {
            clearLines();
            addLine(str);
        }
        return this;
    }
    public TextGui setLines(String[] strs) {
        if (strs.length == 0) return clearLines();
        if (strs.length == 1) return setLine(strs[0]);
        if (strs.length < lines.size()) {
            lines.subList(strs.length, lines.size()).clear();
            dirty = true;
        }
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            if (i < lines.size() && s.equals(lines.get(i).s)) continue;
            dirty = true;
            Line l = new Line(s);
            if (i < lines.size()) lines.add(l);
            else lines.set(i, l);
        }
        return this;
    }

    public TextGui addLine(String str) {
        dirty = true;
        lines.add(new Line(str));
        return this;
    }
    public TextGui addLines(String[] strs) {
        for (String s : strs) addLine(s);
        return this;
    }

    public TextGui removeLine(int i) {
        dirty = true;
        lines.remove(i);
        return this;
    }
    public TextGui insertLine(String str, int i) {
        if (i == lines.size()) return addLine(str);
        dirty = true;
        lines.add(i, new Line(str));
        return this;
    }
    public TextGui replaceLine(String str, int i) {
        if (i == lines.size()) return addLine(str);
        if (!lines.get(i).s.equals(str)) {
            dirty = true;
            lines.set(i, new Line(str));
        }
        return this;
    }

    protected void updateCache() {
        if (cb != location.b) {
            mark();
            cb = location.b;
        }
        if (cc != location.c) {
            mark();
            cc = location.c;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int renderSize = (int)Math.round(MC_FONT_SIZE * sr.getScaleFactor() * location.s);
        if (renderSize != fontSize) {
            fontsDirty = true;
            mark();
        }
        if (fontsDirty) {
            fontsDirty = false;
            fontSize = renderSize;
            fontMain = FONT_NAMES.getOrDefault(font, MOJANGLES_FONT).deriveFont(Font.PLAIN, renderSize);
            fontMono = new Font(Font.MONOSPACED, Font.PLAIN, renderSize);
            fontBackup = new Font(Font.SANS_SERIF, Font.PLAIN, renderSize);
        }

        GuiLocation tl = getTrueLocation();
        rx = tl.x;
        ry = tl.y;
        rs = (double) MC_FONT_SIZE / renderSize * location.s;
    }

    protected void updateLines() {
        Graphics2D g = null;
        lineW = 0.0;
        lineVW = 0.0;
        hasObf = false;

        for (Line l : lines) {
            if (l.dirty) {
                if (g == null) {
                    WritableRaster raster = Raster.createInterleavedRaster(DataBufferByte.TYPE_BYTE, 1, 1, 4, 4, new int[] { 0, 1, 2, 3 }, null);
                    BufferedImage tmpImg = new BufferedImage(COLOR_MODEL, raster, false, null);
                    g = tmpImg.createGraphics();
                    g.setFont(fontMain);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }

                l.data = FontHelper.processString(l.s, location.b, g, fontMain, fontMono, fontBackup, fontSize);
                l.dirty = false;
            }

            if (l.data.o.length > 0) hasObf = true;
            lineW = Math.max(lineW, l.data.w);
            lineVW = Math.max(lineVW, l.data.vw);
        }

        if (g != null) g.dispose();
    }

    private int ceilPow2(int num, int bits) {
        int mask = (1 << Math.max(0, 31 - Integer.numberOfLeadingZeros(num) - bits)) - 1;
        return (num + mask) & ~mask;
    }
    protected BufferedImage renderImage() {
        actW = lineW + (location.b ? fontSize * 0.1 : 0.0);
        actH = fontSize * (lines.size() + 1) + (location.b ? fontSize * 0.1 : 0.0);
        imgW = ceilPow2((int) actW, 1);
        imgH = ceilPow2((int) actH, 2);
        WritableRaster raster = Raster.createInterleavedRaster(DataBufferByte.TYPE_BYTE, imgW, imgH, imgW * 4, 4, new int[] { 0, 1, 2, 3 }, null);
        BufferedImage bimg = new BufferedImage(COLOR_MODEL, raster, false, null);
        Graphics2D g = bimg.createGraphics();
        g.setFont(fontMain);
        int ascent = g.getFontMetrics().getAscent();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ListIterator<Line> iter = lines.listIterator();
        while (iter.hasNext()) {
            int i = iter.nextIndex();
            Line l = iter.next();

            double y = i * fontSize + ascent;
            double x = location.c == 0 ? 0.0 :
                location.c == 1 ? lineVW - l.data.vw :
                (lineVW - l.data.vw) * 0.5;
            if (location.b && l.data.b != null) {
                l.data.tylB.draw(g, (float) (x + fontSize * 0.1), (float) (y + fontSize * 0.1));
            }
            l.data.tylA.draw(g, (float) x, (float) y);
        }

        g.dispose();
        return bimg;
    }

    public void render() {
        if (isEdit) return;
        if (lines.isEmpty()) return;

        updateCache();
        if (hasImg) {
            glDepthMask(false);
            glEnable(GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 771);
            glColor4f(1f, 1f, 1f, 1f);
            img.draw(rx, ry, imgW * rs, imgH * rs);
            glDepthMask(true);
            // glDisable(GL_BLEND);
        }

        if (!dirty) return;

        updateLines();
        img.upload(renderImage());

        dirty = hasObf;
        hasImg = true;
    }

    public void editRenderHookPre() {}
    public void editRenderHookPost() {}

    public double getVisibleWidth() {
        if (isEdit) return EditGui.dummy.getVisibleWidth();
        return (lineVW + (location.b ? fontSize * 0.1 : 0.0)) * rs;
    }
    public double getWidth() {
        if (isEdit) return EditGui.dummy.getWidth();
        return lineW * rs;
    }
    public double getLineHeight() {
        if (isEdit) return EditGui.dummy.getLineHeight();
        return MC_FONT_SIZE * location.s;
    }
    public double getHeight() {
        if (isEdit) return EditGui.dummy.getHeight();
        return getLineHeight() * lines.size();
    }
    public GuiLocation getTrueLocation() {
        if (isEdit) return EditGui.dummy.getTrueLocation();
        double w = getWidth();
        double h = getHeight();
        double x = location.x;
        double y = location.y;
        return new GuiLocation(
            location.c == 3 ? x - w * 0.5 : (location.a & 1) != 0 ? x - w : x,
            (location.a & 2) != 0 ? y - h : y,
            location.s,
            0, location.b, 0
        );
    }
}