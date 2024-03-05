package com.sab.littleh.game;

import com.sab.littleh.game.level.editor.LevelEditorScreen;
import com.sab.littleh.screen.PauseScreen;
import com.sab.littleh.screen.ScreenButton;

public class GamePauseScreen extends PauseScreen {
    private GameScreen gameScreen;

    public GamePauseScreen(GameScreen gameScreen) {
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
        buttons.add(3, new ScreenButton("button", "Open in Editor", buttonRect, () -> {
            gameScreen.level.endGame();
            program.switchScreen(new LevelEditorScreen(gameScreen.file, gameScreen.level));
        }));
        buttons.add(new ScreenButton("button", "To Level Select", buttonRect, () -> {
            gameScreen.stop();
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
