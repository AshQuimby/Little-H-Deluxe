package com.sab.littleh.mainmenu;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;

public class FakeLoadingMenu extends LoadingMenu {
    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            LittleH.program.switchMenu(new LevelSelectMenu());
        } else {
            super.keyDown(keycode);
        }
    }
}
