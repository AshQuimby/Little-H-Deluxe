package com.sab.littleh.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.mainmenu.LevelEditorMenu;
import com.sab.littleh.util.DynamicCamera;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BackgroundEditor extends LevelEditor {
    public BackgroundEditor(Level level) {
        super(level);
        setSaved(true);
        fillTiles = new HashSet<>();
        undoQueue = new ArrayList<>();
    }

    @Override
    public Point addTile(Tile tileToCopy, int x, int y, boolean addToUndoQueue) {
        Tile tile = tileToCopy;
        if (!tileToCopy.image.equals("delete"))
            tile = tileToCopy.copy();
        tile.x = x;
        tile.y = y;

        Point amountNegativeResize = resizeAround(x, y);

        tile.x = Math.max(0, x);
        tile.y = Math.max(0, y);

        level.removeBackgroundTile(tile.x, tile.y);

        if (!tileToCopy.image.equals("delete"))
            level.addBackgroundTile(tile);

        if (amountNegativeResize.x > 0 || amountNegativeResize.y > 0) {
            undoQueue.forEach(undoAction -> {
                undoAction.resize(amountNegativeResize);
            });
        }

        if (addToUndoQueue) {
            Tile tileAt = level.backgroundMap.get(tile.x).get(tile.y);
            if (tileAt == null) {
                tileAt = new Tile("delete");
                tileAt.x = tile.x;
                tileAt.y = tile.y;
            } else {
                tileAt = tileAt.copy();
            }
            if (!Tile.tilesEqual(tileAt, tile))
                addUndoAction(new UndoTile(tile.copy(), tileAt));
        }

        if (tile.image.equals("delete")) {
            level.backgroundMap.get(tile.x).set(tile.y, null);
        } else {
            level.backgroundMap.get(tile.x).set(tile.y, tile);
        }

        Tile[][] neighbors = getNeighbors(tile.x, tile.y);

        for (Tile[] tiles : neighbors)
            for (Tile neighborTile : tiles)
                if (neighborTile != null)
                    checkTiling(getNeighbors(neighborTile.x, neighborTile.y), neighborTile);
        setSaved(false);

        return amountNegativeResize;
    }

    @Override
    public Tile[][] getNeighbors(int tileX, int tileY) {
        // Magic
        Tile[][] neighbors = new Tile[3][3];
        for (int i = tileX - 1; i < tileX + 2; i++) {
            for (int j = tileY - 1; j < tileY + 2; j++) {
                if (!(i < 0 || j < 0 || i >= level.getWidth() || j >= level.getHeight()) && level.getBackgroundTileAt(i, j) != null && !level.getBackgroundTileAt(i, j).ignoreTiling) {
                    neighbors[tileX - i + 1][tileY - j + 1] = level.getBackgroundTileAt(i, j);
                } else {
                    neighbors[tileX - i + 1][tileY - j + 1] = null;
                }
            }
        }
        return neighbors;
    }

    @Override
    public void fill(Tile fillTile, int originX, int originY) {
        // Don't let people fill out of bounds
        if (!(originX >= 0 && originY >= 0 && originX < level.getWidth() && originY < level.getHeight())) return;
        // Don't fill tiles of the same type
        if (Tile.tilesEqual(fillTile, level.getBackgroundTileAt(originX, originY)) || fillTile.image.equals("delete") && level.getBackgroundTileAt(originX, originY) == null) return;
        fillTiles.clear();

        List<Point> open = new ArrayList<>();
        Set<Point> closed = new HashSet<>();

        Point origin = new Point(originX, originY);
        Tile tileToFill = level.getBackgroundTileAt(originX, originY);
        open.add(origin);
        closed.add(origin);

        int tilesFilled = 0;
        while (open.size() > 0) {
            Point current = open.get(0);
            open.remove(0);

            fillTiles.add(current);
            tilesFilled++;

            int x = current.x;
            int y = current.y;

            if (x + 1 < level.getWidth() && !Tile.tilesEqual(level.getBackgroundTileAt(x + 1, y), fillTile) && Tile.tilesEqual(level.getBackgroundTileAt(x + 1, y), tileToFill)
                    && !closed.contains(new Point(x + 1, y)) && Tile.extrasEqual(tileToFill, level.getBackgroundTileAt(x + 1, y))) {
                open.add(new Point(x + 1, y));
                closed.add(new Point(x + 1, y));
            }
            if (x - 1 >= 0 && !Tile.tilesEqual(level.getBackgroundTileAt(x - 1, y), fillTile) && Tile.tilesEqual(level.getBackgroundTileAt(x - 1, y), tileToFill)
                    && !closed.contains(new Point(x - 1, y)) && Tile.extrasEqual(tileToFill, level.getBackgroundTileAt(x - 1, y))) {
                open.add(new Point(x - 1, y));
                closed.add(new Point(x - 1, y));
            }
            if (y + 1 < level.getHeight() && !Tile.tilesEqual(level.getBackgroundTileAt(x, y + 1), fillTile) && Tile.tilesEqual(level.getBackgroundTileAt(x, y + 1), tileToFill)
                    && !closed.contains(new Point(x, y + 1)) && Tile.extrasEqual(tileToFill, level.getBackgroundTileAt(x, y + 1))) {
                open.add(new Point(x, y + 1));
                closed.add(new Point(x, y + 1));
            }
            if (y - 1 >= 0 && !Tile.tilesEqual(level.getBackgroundTileAt(x, y - 1), fillTile) && Tile.tilesEqual(level.getBackgroundTileAt(x, y - 1), tileToFill)
                    && !closed.contains(new Point(x, y - 1)) && Tile.extrasEqual(tileToFill, level.getBackgroundTileAt(x, y - 1))) {
                open.add(new Point(x, y - 1));
                closed.add(new Point(x, y - 1));
            }
        }

        // Add a "fill" action to the undo queue
        if (tileToFill == null) {
            // Save undo actions of null instead as tiles with the tag "delete"
            tileToFill = new Tile("delete");
        }
//        fillTile = fillTile.copy();

        // Add/delete all tiles
        for (Point point : fillTiles) {
            addTile(fillTile, point.x, point.y, false);
        }

        addUndoAction(new UndoFill(fillTile.copy(), tileToFill.copy(), Set.copyOf(fillTiles)));

        // Empty the list for good measure
        fillTiles.clear();
    }

    @Override
    public void deleteSelection() {
        if (selection != null && !selectionAlive) {
            UndoSelection undoSelection = new UndoSelection();
            for (int i = 0; i < selection.width; i++) {
                for (int j = 0; j < selection.height; j++) {
                    Tile tileAt = level.getBackgroundTileAt(selection.x + i, selection.y + j);
                    if (tileAt != null) {
                        undoSelection.add(tileAt.copy(), new Tile("delete"));
                        addTile(new Tile("delete"), selection.x + i, selection.y + j, false);
                    }
                }
            }
            addUndoAction(undoSelection);
            setSaved(false);
        }
    }

    @Override
    public void copySelection() {
        tileSelection = new TileSelection(selection, level, true, true);
    }

    @Override
    public Tile getTileAt(int x, int y) {
        return level.getBackgroundTileAt(x, y);
    }
}
