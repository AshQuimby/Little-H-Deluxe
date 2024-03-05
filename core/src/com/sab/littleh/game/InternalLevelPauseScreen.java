package com.sab.littleh.game;

import com.sab.littleh.game.InternalLevelScreen;
import com.sab.littleh.screen.PauseScreen;
import com.sab.littleh.screen.ScreenButton;

public class InternalLevelPauseScreen extends PauseScreen {
    private InternalLevelScreen gameScreen;

    public InternalLevelPauseScreen(InternalLevelScreen gameScreen) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        buttons.add(1, new ScreenButton("button", "To Checkpoint", buttonRect, () -> {
            gameScreen.level.suicide();
            program.switchScreen(screenBehind);
        }));
        buttons.add(2, new ScreenButton("button", "Start Over", buttonRect, () -> {
            gameScreen.level.reset();
            program.switchScreen(screenBehind);
        }));
        buttons.add(new ScreenButton("button", "Stop playing", buttonRect, () -> {
            gameScreen.stop();
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
