package com.perseuspotter.lib.textgui;

import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

public class GLBufferedImageUploader {
    public static int mode = -1;

    public int w;
    public int h;
    public int texId = -1;
    public int pboId = -1;

    private void create(BufferedImage img) {
        if (mode == -1) {
            ContextCapabilities cap = GLContext.getCapabilities();
            mode = cap.OpenGL21 ? 1 : 0;
        }

        w = img.getWidth();
        h = img.getHeight();
        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_LOD, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 0);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        if (mode == 1) {
            pboId = glGenBuffers();
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);
            glBufferData(GL_PIXEL_UNPACK_BUFFER, (long) w * h * 4, GL_STREAM_DRAW);
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        }
    }

    public void upload(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        if (texId == -1) create(img);
        else if (w != this.w || h != this.h) {
            destroy();
            create(img);
        }

        byte[] pixels = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
        if (mode == 1) {
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);

            ByteBuffer buf = glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY, null);
            if (buf != null) {
                buf.put(pixels);
                buf.flip();
                glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
            }

            glBindTexture(GL_TEXTURE_2D, texId);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, this.w, this.h, GL_RGBA, GL_UNSIGNED_BYTE, 0);

            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        } else {
            ByteBuffer buf = BufferUtils.createByteBuffer(pixels.length);
            buf.put(pixels);
            buf.flip();

            glBindTexture(GL_TEXTURE_2D, texId);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, this.w, this.h, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        }
    }

    private void destroy() {
        if (texId == -1) return;
        glDeleteTextures(texId);
        if (pboId != -1) glDeleteBuffers(pboId);
        texId = pboId = -1;
    }

    public void draw(double x, double y) {
        draw(x, y, w);
    }
    public void draw(double x, double y, double w) {
        draw(x, y, w, h * w / this.w);
    }
    public void draw(double x, double y, double w, double h) {
        glBindTexture(GL_TEXTURE_2D, texId);

        Tessellator tess = Tessellator.instance;
        tess.startDrawing(7);
        tess.addVertexWithUV(x, y + h, 0.0, 0.0, 1.0);
        tess.addVertexWithUV(x + w, y + h, 0.0, 1.0, 1.0);
        tess.addVertexWithUV(x + w, y, 0.0, 1.0, 0.0);
        tess.addVertexWithUV(x, y, 0.0, 0.0, 0.0);
        tess.draw();
    }
}
