package com.sab.littleh.game.level;

import com.sab.littleh.game.tile.Tile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UndoSelection extends UndoAction {
    private List<Tile> undo;
    private List<Tile> redo;
    public UndoSelection(List<Tile> before, List<Tile> after) {
        this.undo = before;
        this.redo = after;
    }
    public UndoSelection() {
        this.undo = new ArrayList<>();
        this.redo = new ArrayList<>();
    }
    public void add(Tile undo, Tile redo) {
        this.undo.add(undo.copy());
        this.redo.add(redo.copy());
    }
    @Override
    public void undo(LevelEditor editor) {
        for (Tile tile : undo) {
            editor.addTile(tile, tile.x, tile.y, false);
        }
    }
    @Override
    public void redo(LevelEditor editor) {
        for (Tile tile : redo) {
            editor.addTile(tile, tile.x, tile.y, false);
        }
    }
    @Override
    public void resize(Point size) {
        for (Tile tile : undo) {
            tile.x += size.x;
            tile.y += size.y;
        }
        for (Tile tile : redo) {
            tile.x += size.x;
            tile.y += size.y;
        }
    }
}