package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Localization;

public class FakeLoadingMenu extends LoadingMenu {
    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            LittleH.program.switchMenu(new LevelSelectMenu());
        } else {
            super.keyDown(keycode);
        }
    }
}
