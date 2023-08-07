package com.sab.littleh.mainmenu;

import com.sab.littleh.LittleH;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;

public abstract class MainMenu {
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

    public void drawBackground(Graphics g) {
        g.drawPatch(Patch.get("menu_indented"), relZeroX(), relZeroY(), program.getWidth(), program.getHeight(), 8);
        g.draw(Images.getImage("title.png"), -568 / 2, -424 / 2, 568, 424);
    }

    public void render(Graphics g) {
        drawBackground(g);
    }

    public void mouseScrolled(float amountY) {
    }
}
