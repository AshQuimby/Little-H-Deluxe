package com.sab.littleh.mainmenu;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.settings.PercentageSetting;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.util.Graphics;

public class VibrationMenu extends LoadingMenu {

    private PercentageSetting strength = new PercentageSetting("s", "s", 0);
    private SettingButton slider = new SettingButton(strength, -128, 32, 256);

    @Override
    public void update() {
        super.update();
        slider.update();
        LittleH.getControllers().forEach(controller -> controller.startVibration(1, strength.asFloat()));
    }

    @Override
    public void mouseDown(int button) {
        super.mouseDown(button);
        slider.mouseDown();
    }

    @Override
    public void mouseUp(int button) {
        super.mouseUp(button);
        slider.mouseClicked();
    }

    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            LittleH.program.switchMenu(new LevelSelectMenu());
        } else {
            super.keyDown(keycode);
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        slider.render(g);
    }
}
