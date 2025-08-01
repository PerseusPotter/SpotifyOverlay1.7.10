package com.perseuspotter.lib.textgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public class EditGui extends GuiScreen {
    public static TextGui curr;
    public static TextGui dummy = new TextGui(null) {
        @Override
        public String[] getEditText() {
            return new String[0];
        }
    };

    public EditGui() {
        super();
        allowUserInput = true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        dummy.setLines(curr.getEditText());
        dummy.location = curr.location;
        dummy.setFont(curr.getFont());
        curr.editRenderHookPre();
        dummy.render();
        curr.editRenderHookPost();

        int x = 50;
        int y = 20;
        for (String s : new String[] {
            "§7[§21§7] §fReset",
            "§7[§22§7] §fChange Anchor",
            "§7[§23§7] §fChange Alignment",
            "§7[§24§7] §fToggle Shadow",
            "§7[§2Scroll§7] §fResize",
            "§7[§2Drag§7] §fMove"
        }) {
            Minecraft.getMinecraft().fontRenderer.drawString(s, x, y, 0xFFFFFFFF, true);
            y += 10;
        }

        drawRect((int) (curr.location.x - 2), (int) (curr.location.y - 2), (int) (curr.location.x + 2), (int) (curr.location.y + 2), 0xFF0000FF);
    }

    public int lastX = -1;
    public int lastY = -1;

    @Override
    public void onGuiClosed() {
        curr.isEdit = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        switch (keyCode) {
            case 2:
                curr.location.x = 50.0;
                curr.location.y = 50.0;
                curr.location.s = 1.0;
                curr.location.a = 0;
                curr.location.b = false;
                curr.location.c = 0;
                break;
            case 3:
                curr.location.a = (curr.location.a + 1) & 3;
                break;
            case 4:
                curr.location.c = (curr.location.c + 1) & 3;
                break;
            case 5:
                curr.location.b = !curr.location.b;
                break;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (clickedMouseButton != 0) return;
        if (lastX != -1) {
            curr.location.x += (mouseX - lastX);
            curr.location.y += (mouseY - lastY);
        }
        lastX = mouseX;
        lastY = mouseY;
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);

        if (state == 0) {
            lastX = -1;
            lastY = -1;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) curr.location.s = Math.max(0.1, curr.location.s + Integer.signum(wheel) * 0.1);
    }
}
