package com.sab.littleh.game.level;

import com.badlogic.gdx.graphics.Color;
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
    public TileSelection(Rectangle rectangle, Level level, boolean copyTiles, boolean background) {
        this.rectangle = rectangle;
        x = rectangle.x * 64;
        y = rectangle.y * 64;
        tiles = new ArrayList<>();
        tilePositions = new ArrayList<>();
        if (copyTiles) {
            for (int i = 0; i < rectangle.width; i++) {
                for (int j = 0; j < rectangle.height; j++) {
                    Tile tileAt = background ? level.getBackgroundTileAt(rectangle.x + i, rectangle.y + j)
                            : level.getTileAt(rectangle.x + i, rectangle.y + j);
                    if (tileAt != null) {
                        tiles.add(tileAt.copy());
                        tilePositions.add(new Point(tileAt.x - rectangle.x, tileAt.y - rectangle.y));
                    }
                }
            }
        }
    }
    public TileSelection(Rectangle rectangle, Level level) {
        this(rectangle, level, true, false);
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
        g.setColor(Images.quickAlpha(0.5f));
        for (Tile tile : tiles) {
            tile.render(false, g);
        }
        g.resetColor();
    }

    public void paste(LevelEditor editor) {
        UndoSelection undoSelection = new UndoSelection();
        tiles.forEach(tile -> {
            Tile tileAt = editor.getLevel().getTileAt(tile.x, tile.y);
            if (tileAt == null) {
                tileAt = new Tile("delete");
                tileAt.x = tile.x;
                tileAt.y = tile.y;
            }
            undoSelection.add(tileAt, tile);
            editor.addTile(tile, tile.x, tile.y, false);
        });
        editor.addUndoAction(undoSelection);
    }

    public void setRect(Rectangle selection) {
        rectangle = selection;
    }
}
