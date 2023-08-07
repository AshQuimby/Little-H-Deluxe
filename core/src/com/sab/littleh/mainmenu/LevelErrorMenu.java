package com.sab.littleh.mainmenu;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.io.File;

public class LevelErrorMenu extends MainMenu {
    private String errorMessage;
    public LevelErrorMenu(String message) {
        errorMessage = message;
    }
    public void mouseUp(int button) {
        LittleH.program.switchMenu(new LevelSelectMenu());
    }

    public void keyUp(int keycode) {
        LittleH.program.switchMenu(new LevelSelectMenu());
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
