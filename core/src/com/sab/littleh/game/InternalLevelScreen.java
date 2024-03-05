package com.sab.littleh.game;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.util.Cursors;
import com.sab.littleh.util.SoundEngine;

public class InternalLevelScreen extends GameScreen {
    private Screen screenToReturnTo;
    public InternalLevelScreen(Screen screenToReturnTo, Level level, boolean ignoreDialogue) {
        super(null, level, ignoreDialogue);
        this.screenToReturnTo = screenToReturnTo;
    }

    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            if (level.escapePressed())
                program.switchScreen(new InternalLevelPauseScreen(this));
        } else if (ControlInput.localControls.isJustPressed("select")) {
            level.enterPressed();
        } else if (ControlInput.localControls.isJustPressed("suicide")) {
            level.suicide();
        } else if (ControlInput.localControls.isJustPressed("quick_restart")) {
            level.player.trueKill();
        }
    }

    @Override
    public void stop() {
        LittleH.program.switchScreen(screenToReturnTo);
        Cursors.switchCursor("cursor");
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    @Override
    public void update() {
        level.update();
        if (!level.inGame()) {
           stop();
        }
    }
}
