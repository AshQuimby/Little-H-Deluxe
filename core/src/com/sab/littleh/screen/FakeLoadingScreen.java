package com.sab.littleh.screen;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;

public class FakeLoadingScreen extends LoadingScreen {
    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            LittleH.program.switchScreen(new LevelSelectScreen());
        } else {
            super.keyDown(keycode);
        }
    }
}
