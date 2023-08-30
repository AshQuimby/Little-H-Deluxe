package com.sab.littleh.mainmenu;

import com.sab.littleh.game.level.LevelEditor;

public class EditorPauseMenu extends PauseMenu {
    private LevelEditorMenu editorMenu;

    public EditorPauseMenu(LevelEditorMenu editorMenu) {
        super(editorMenu);
        this.editorMenu = editorMenu;
        buttons.add(1, new MenuButton("button", "Playtest", buttonRect, () -> {
            editorMenu.startTesting();
            program.switchMenu(menuBehind);
        }));
        buttons.add(new MenuButton("button", "To Level Select", buttonRect, () -> {
            if (LevelEditor.saved) {
                editorMenu.stop();
            } else {
                program.switchMenu(menuBehind);
                editorMenu.confirmExit();
            }
        }));
        editorMenu.canPlaceTiles = false;
    }

    @Override
    public void start() {
        super.start();
    }
}
