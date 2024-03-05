package com.sab.littleh.game.level.editor;

import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.level.editor.UndoAction;
import com.sab.littleh.game.tile.Tile;

import java.awt.*;
import java.util.Set;

public class UndoFill extends UndoAction {
    private Tile undo;
    private Set<Point> points;
    private Tile redo;
    public UndoFill(Tile redo, Tile undo, Set<Point> points) {
        this.undo = undo;
        this.redo = redo;
        this.points = points;
    }
    @Override
    public void undo(LevelEditor editor) {
        for (Point point : points) {
            undo.x = point.x;
            undo.y = point.y;
            editor.addTile(undo, undo.x, undo.y, false);
        }
    }
    @Override
    public void redo(LevelEditor editor) {
        for (Point point : points) {
            redo.x = point.x;
            redo.y = point.y;
            editor.addTile(redo, redo.x, redo.y, false);
        }
    }
    @Override
    public void resize(Point size) {
        for (Point point : points) {
            point.x += size.x;
            point.y += size.y;
        }
    }
}
