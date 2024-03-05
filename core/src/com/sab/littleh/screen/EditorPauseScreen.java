package com.sab.littleh.screen;

import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.level.editor.LevelEditorScreen;

public class EditorPauseScreen extends PauseScreen {
    private LevelEditorScreen editorScreen;

    public EditorPauseScreen(LevelEditorScreen editorScreen) {
        super(editorScreen);
        this.editorScreen = editorScreen;
        buttons.add(1, new ScreenButton("button", "Playtest", buttonRect, () -> {
            editorScreen.startTesting();
            program.switchScreen(screenBehind);
        }));
        buttons.add(new ScreenButton("button", "To Level Select", buttonRect, () -> {
            if (LevelEditor.saved) {
                editorScreen.stop();
            } else {
                program.switchScreen(screenBehind);
                editorScreen.confirmExit();
            }
        }));
        editorScreen.canPlaceTiles = false;
    }

    @Override
    public void start() {
        super.start();
    }
}
