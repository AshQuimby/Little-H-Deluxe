package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.*;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.io.File;

public class LoadingMenu extends MainMenu {
    @Override
    public void start() {
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
        String loading = "Loading";
        for (int i = 0; i < System.currentTimeMillis() / 500 % 4; i++) {
            loading += ".";
        }
        g.drawString(loading, LittleH.font, -relZeroX() - 256 - 64, relZeroY() + 64 + 16, LittleH.defaultFontScale, -1);
        g.drawImage(Images.getImage("ui/loading_h.png"), -relZeroX() - 128 - 8, relZeroY() + 8, 128, 128, new Rectangle(0, 16 * (System.currentTimeMillis() / 150 % 4), 16, 16));
    }
}
