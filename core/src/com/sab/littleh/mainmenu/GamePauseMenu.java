package com.sab.littleh.mainmenu;

public class GamePauseMenu extends PauseMenu {
    private GameMenu gameMenu;

    public GamePauseMenu(GameMenu gameMenu) {
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
        buttons.add(new MenuButton("button", "To Level Select", buttonRect, () -> {
            gameMenu.stop();
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
