package com.sab.littleh.net;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.mainmenu.GameMenu;
import com.sab.littleh.mainmenu.LevelEditorMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.mainmenu.PauseMenu;

public class OnlinePauseMenu extends PauseMenu {
    private GameMenu gameMenu;

    public OnlinePauseMenu(GameMenu gameMenu) {
        super(gameMenu);
        this.gameMenu = gameMenu;
        buttons.add(1, new MenuButton("button", "To Checkpoint", buttonRect, () -> {
            gameMenu.level.suicide();
            program.switchMenu(menuBehind);
        }));
        buttons.add(2, new MenuButton("button", "Start Over", buttonRect, () -> {
            gameMenu.level.reset();
            program.switchMenu(menuBehind);
        }));
        buttons.add(3, new MenuButton("button", "Open in Editor", buttonRect, () -> {
            gameMenu.level.endGame();
            program.switchMenu(new LevelEditorMenu(gameMenu.file, gameMenu.level));
        }));
        buttons.add(new MenuButton("button", "To Level Select", buttonRect, gameMenu::stop));
    }

    @Override
    public void update() {
        buttonMenu.setMenuRectangle(-256 - 32, (int) buttonMenu.getMenuRectangle().height / 2, 96 * buttons.size(), false);
        Rectangle[] itemButtons = buttonMenu.getItemButtons();
        for (int i = 0; i < buttonMenu.items.length; i++) {
            buttonMenu.items[i].setPosition(itemButtons[i].x, itemButtons[i].y);
        }
        buttonMenu.forEach(MenuButton::update);
        super.update();
    }

    @Override
    public void start() {
        super.start();
    }
}
