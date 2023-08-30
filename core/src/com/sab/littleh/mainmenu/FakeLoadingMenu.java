package com.sab.littleh.mainmenu;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.controls.Controls;

public class FakeLoadingMenu extends LoadingMenu {
    @Override
    public void keyDown(int keycode) {
        if (ControlInputs.isJustPressed("return")) {
            LittleH.program.switchMenu(new LevelSelectMenu());
        } else {
            super.keyDown(keycode);
        }
    }
}
