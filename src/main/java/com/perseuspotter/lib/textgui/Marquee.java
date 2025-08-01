package com.perseuspotter.lib.textgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;

public class Marquee extends TextGui {
    private String text = "";
    private int maxLen = 100;
    private int scrollSpeed = 20;
    private long freezeTime = 1_500_000_000L;
    private long scrollTime = 0L;
    private long cycleTime = 0L;
    private boolean alternate = false;
    private long startTime = System.nanoTime();

    public Marquee() {
        super(new GuiLocation(0.0, 0.0, 1.0, 0, true, 0));
    }

    @Override
    public String[] getEditText() {
        return new String[] { text };
    }

    public void reset() {
        startTime = System.nanoTime();
        scrollTime = maxLen * 1_000_000_000L / scrollSpeed;
        cycleTime = freezeTime + scrollTime + freezeTime + (alternate ? scrollTime : 0L);
    }

    public void setText(String text) {
        if (this.text.equals(text)) return;
        this.text = text;
        setLine(text);
        reset();
    }
    public void setMaxLen(int maxLen) {
        if (this.maxLen == maxLen) return;
        this.maxLen = maxLen;
        reset();
    }
    public void setScrollSpeed(int scrollSpeed) {
        if (this.scrollSpeed == scrollSpeed) return;
        this.scrollSpeed = scrollSpeed;
        reset();
    }
    public void setFreezeTime(int freezeTime) {
        long freezeTimeL = freezeTime * 1_000_000L;
        if (this.freezeTime == freezeTimeL) return;
        this.freezeTime = freezeTimeL;
        reset();
    }
    public void setAlternate(boolean alternate) {
        if (this.alternate == alternate) return;
        this.alternate = alternate;
        reset();
    }

    public void render(double x, double y, double scale, boolean shadow) {
        long time = System.nanoTime() - startTime;
        // long cycleCount = time / cycleTime;
        long cycleOffset = time % cycleTime;

        double pos = cycleOffset < freezeTime ? 0.0 :
            cycleOffset < freezeTime + scrollTime ?
                (double) (cycleOffset - freezeTime) / scrollTime :
                cycleOffset < freezeTime + scrollTime + freezeTime ?
                    1.0 :
                    1.0 - (double) (cycleOffset - freezeTime - scrollTime - freezeTime) / scrollTime;

        location.x = maxLen <= 0 ? x : x - Math.max(getVisibleWidth() - maxLen, 0.0) * pos;
        location.y = y;
        location.s = scale;
        location.b = shadow;
        location.a = location.c = 0;

        if (maxLen > 0) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int ss = sr.getScaleFactor();

            glEnable(GL_SCISSOR_TEST);
            glScissor(
                (int) (x * ss),
                (int) ((sr.getScaledHeight() - y - 15 * scale) * ss),
                maxLen * ss,
                (int) (20 * scale * ss)
            );
        }
        super.render();
        if (maxLen > 0) glDisable(GL_SCISSOR_TEST);
    }
}
