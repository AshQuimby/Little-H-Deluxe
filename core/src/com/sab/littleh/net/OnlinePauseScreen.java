package com.sab.littleh.net;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.GameScreen;
import com.sab.littleh.game.level.editor.LevelEditorScreen;
import com.sab.littleh.screen.ScreenButton;
import com.sab.littleh.screen.PauseScreen;

public class OnlinePauseScreen extends PauseScreen {
    private GameScreen gameScreen;

    public OnlinePauseScreen(GameScreen gameScreen) {
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
        buttons.add(new ScreenButton("button", "To Level Select", buttonRect, gameScreen::stop));
    }

    @Override
    public void update() {
        buttonMenu.setScreenRectangle(-256 - 32, (int) buttonMenu.getScreenRectangle().height / 2, 96 * buttons.size(), false);
        Rectangle[] itemButtons = buttonMenu.getItemButtons();
        for (int i = 0; i < buttonMenu.items.length; i++) {
            buttonMenu.items[i].setPosition(itemButtons[i].x, itemButtons[i].y);
        }
        buttonMenu.forEach(ScreenButton::update);
        super.update();
    }

    @Override
    public void start() {
        super.start();
    }
}
