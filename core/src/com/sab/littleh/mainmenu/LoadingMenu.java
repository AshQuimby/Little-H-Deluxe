package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.*;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.io.File;

public class LoadingMenu extends MainMenu {
    public static final String[] splashes = Localization.getLocalizedTextFile("splashes.txt").getText();
    private static final RandomXS128 splashRandomizer = new RandomXS128();
    private int splash;
    private int newSplash;
    private float splashTimer;
    @Override
    public void start() {
        newSplash();
        splash = newSplash;
        splashTimer = -1;
    }

    @Override
    public void update() {
        if (splashTimer < -10) {
            newSplash();
        }
    }

    @Override
    public void keyDown(int keycode) {
        if (splashTimer < -1) {
            newSplash();
        }
    }

    public void newSplash() {
        do {
            newSplash = splashRandomizer.nextInt(splashes.length);
        } while (newSplash == splash);
        splashTimer = 1;
    }

    public String getSplash() {
        return splashes[splash];
    }

    @Override
    public void mouseUp(int button) {
    }

    @Override
    public void close() {
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        splashTimer -= 0.02f;
        if (Math.abs(splashTimer) < 0.02f) {
            splash = newSplash;
        }
        LittleH.font.setColor(new Color(1, 1, 1, Math.min(1, Math.abs(splashTimer))));
        g.drawString("    " + getSplash().trim(), LittleH.font, new Rectangle(relZeroX() + 24, relZeroY() + 24, 720, 128), 12, LittleH.defaultFontScale * 0.75f, -1, -1);

        String loading = "Loading";
        for (int i = 0; i < System.currentTimeMillis() / 500 % 4; i++) {
            loading += ".";
        }
        LittleH.font.setColor(Color.WHITE);
        g.drawString(loading, LittleH.font, -relZeroX() - 256 - 64, relZeroY() + 48, LittleH.defaultFontScale, -1);
        g.drawImage(Images.getImage("ui/loading_h.png"), -relZeroX() - 128 - 8, relZeroY() + 8, 128, 128, new Rectangle(0, 16 * (System.currentTimeMillis() / 250 % 4), 16, 16));
    }
}
