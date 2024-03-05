package com.sab.littleh.game.level;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.screen.LevelSelectScreen;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Patch;

public class LevelErrorScreen extends Screen {
    private String errorMessage;
    public LevelErrorScreen(String message) {
        errorMessage = message;
    }

    @Override
    public void mouseUp(int button) {
        LittleH.program.switchScreen(new LevelSelectScreen());
    }

    @Override
    public void keyUp(int keycode) {
        LittleH.program.switchScreen(new LevelSelectScreen());
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle titleBox = new Rectangle(-384, -128, 768, 256);
        g.drawPatch(Patch.get("menu"), titleBox, 8);
        titleBox.x += 16;
        titleBox.width -= 32;

        g.drawString(errorMessage, LittleH.font, titleBox, 4, LittleH.defaultFontScale * 1.2f, 0, 0);
    }
}
