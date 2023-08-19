package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Input;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Cursors;

import java.awt.*;
import java.io.File;

public class InternalLevelMenu extends GameMenu {
    private MainMenu menuToReturnTo;
    public InternalLevelMenu(MainMenu menuToReturnTo, Level level) {
        super(null, level);
        this.menuToReturnTo = menuToReturnTo;
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            if (level.escapePressed())
                LittleH.program.switchMenu(menuToReturnTo);
        } else if (keycode == Input.Keys.ENTER) {
            level.enterPressed();
        } else if (keycode == Input.Keys.K) {
            level.suicide();
        }
    }

    @Override
    public void update() {
        level.update();
        if (!level.inGame()) {
            LittleH.program.switchMenu(menuToReturnTo);
        }
    }
}
