package com.sab.littleh.game.level.editor;

import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.level.editor.UndoAction;
import com.sab.littleh.game.tile.Tile;

import java.awt.*;

public class UndoTile extends UndoAction {
    private final Tile undo;
    private final Tile redo;
    public UndoTile(Tile redo, Tile undo) {
        this.undo = undo.copy();
        this.redo = redo.copy();
    }
    @Override
    public void undo(LevelEditor editor) {
        editor.addTile(undo, undo.x, undo.y, false);
    }
    @Override
    public void redo(LevelEditor editor) {
        editor.addTile(redo, redo.x, redo.y, false);
    }
    @Override
    public void resize(Point size) {
        undo.x += size.x;
        undo.y += size.y;
        redo.x += size.x;
        redo.y += size.y;
    }
}
