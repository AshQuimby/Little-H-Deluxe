package com.sab.littleh.screen;

import com.sab.littleh.LittleH;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;

public abstract class Screen {
    public static LittleH program;

    public void start() {
    }

    public void update() {
    }

    public void keyDown(int keycode) {
    }

    public void keyUp(int keycode) {
    }

    public void keyTyped(char character) {
    }

    public void mouseDown(int button) {
    }

    public void mouseUp(int button) {
    }

    public static int relZeroX() {
        return -program.getWidth() / 2;
    }

    public static int relZeroY() {
        return -program.getHeight() / 2;
    }

    public void close() {
    }

    public void drawBackground(Graphics g, int patchScale) {
        g.drawPatch(Patch.get("menu_indented"), relZeroX(), relZeroY(), program.getWidth(), program.getHeight(), patchScale);
        g.setColor(Images.getHColor());
        g.draw(Images.getImage("title_color.png"), -568 / 2, -424 / 2, 568, 424);
        g.resetColor();
        if (Settings.localSettings.rainbowTitle.value)
            g.draw(Images.getImage("title_rainbow.png"), -568 / 2, -424 / 2, 568, 424);
        else
            g.draw(Images.getImage("title.png"), -568 / 2, -424 / 2, 568, 424);
    }

    public void render(Graphics g) {
        drawBackground(g, 8);
    }

    public void mouseScrolled(float amountY) {
    }
}
