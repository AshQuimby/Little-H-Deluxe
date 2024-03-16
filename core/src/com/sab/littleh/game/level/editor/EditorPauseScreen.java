package com.sab.littleh.game.level.editor;

import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.level.editor.LevelEditorScreen;
import com.sab.littleh.game.level.editor.NewLevelEditorScreen;
import com.sab.littleh.screen.PauseScreen;
import com.sab.littleh.screen.ScreenButton;

public class EditorPauseScreen extends PauseScreen {

    public EditorPauseScreen(NewLevelEditorScreen editorScreen) {
        super(editorScreen);
        buttons.add(1, new ScreenButton("button", "Playtest", buttonRect, () -> {
            editorScreen.startTesting();
            program.switchScreen(screenBehind);
        }));
        buttons.add(new ScreenButton("button", "To Level Select", buttonRect, () -> {
//            if (editorScreen.isSaved()) {
                editorScreen.stop();
//            } else {
//                program.switchScreen(screenBehind);
//                editorScreen.confirmExit();
//            }
        }));
        editorScreen.blockTilePlacement();
    }
}
