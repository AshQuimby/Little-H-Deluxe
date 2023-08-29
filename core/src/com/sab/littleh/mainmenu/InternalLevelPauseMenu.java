package com.sab.littleh.mainmenu;

public class InternalLevelPauseMenu extends PauseMenu {
    private InternalLevelMenu gameMenu;

    public InternalLevelPauseMenu(InternalLevelMenu gameMenu) {
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
        buttons.add(new MenuButton("button", "Stop playing", buttonRect, () -> {
            gameMenu.stop();
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
