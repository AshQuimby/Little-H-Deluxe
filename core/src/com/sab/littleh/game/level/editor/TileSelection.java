package com.sab.littleh.game.level.editor;

import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TileSelection {
    private List<Tile> tiles;
    private List<Point> tilePositions;
    private Rectangle rectangle;
    float x;
    float y;
    public TileSelection(Rectangle rectangle, Level level, boolean copyTiles, String layer) {
        if (rectangle == null) rectangle = new Rectangle(0, 0, 1, 1);
        this.rectangle = rectangle;
        x = rectangle.x * 64;
        y = rectangle.y * 64;
        tiles = new ArrayList<>();
        tilePositions = new ArrayList<>();
        if (copyTiles) {
            for (int i = 0; i < rectangle.width; i++) {
                for (int j = 0; j < rectangle.height; j++) {
                    Tile tileAt = level.getTileAt(layer, rectangle.x + i, rectangle.y + j);
                    if (tileAt != null) {
                        tiles.add(tileAt.copy());
                        tilePositions.add(new Point(tileAt.x - rectangle.x, tileAt.y - rectangle.y));
                    }
                }
            }
        }
    }
    public TileSelection(Rectangle rectangle, Level level, String layer) {
        this(rectangle, level, true, layer);
    }

    public void translate() {
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            Point point = tilePositions.get(i);
            tile.x = point.x + rectangle.x;
            tile.y = point.y + rectangle.y;
        }
    }

    public void render(Graphics g) {
        g.setTint(Images.quickAlpha(0.5f));
        for (Tile tile : tiles) {
            tile.render(false, g);
        }
        g.resetTint();
    }

    public void paste(LevelEditor editor) {
        UndoSelection undoSelection = new UndoSelection();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            Tile tileAt = editor.getLevel().getTileAt(editor.layer, tile.x, tile.y);
            if (tileAt == null) {
                tileAt = new Tile("delete");
                tileAt.x = tile.x;
                tileAt.y = tile.y;
            }
            undoSelection.add(tileAt, tile);
            Point resize = editor.addTile(tile, tile.x, tile.y, false);
            if (resize.x > 0 || resize.y > 0) {
                for (int j = i + 1; j < tiles.size(); j++) {
                    tiles.get(j).x += resize.x;
                    tiles.get(i).x += resize.x;
                }
            }
        }
        editor.addUndoAction(undoSelection);
    }

    public void setRect(Rectangle selection) {
        rectangle = selection;
    }

    public void resize(int widthToAdd, int heightToAdd) {
        for (Tile tile : tiles) {
            tile.x += widthToAdd;
            tile.y += heightToAdd;
        }
    }
}
