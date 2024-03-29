package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.*;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.DynamicCamera;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.MouseUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LevelEditor {
    protected static DynamicCamera camera = LittleH.program.dynamicCamera;
    protected Level level;
    public static boolean saved;
    protected Set<Point> fillTiles;
    protected static Rectangle selection;
    protected static boolean selectionAlive;
    protected static TileSelection tileSelection;
    protected static Vector2 selectionAnchor;
    protected static boolean movingSelection;
    protected static boolean nudgingSelection;
    protected List<UndoAction> undoQueue;
    protected int undoIndex;
    protected String layer;
    protected Point lineToolOrigin;
    protected Tile lineToolTile;

    public LevelEditor(Level level, String layer) {
        this.level = level;
        setSaved(true);
        fillTiles = new HashSet<>();
        undoQueue = new ArrayList<>();
        this.layer = layer;
    }

    public void usePencil(Tile tile, int x, int y) {
        addTile(tile, x, y, true);
    }
    public void useEraser(int x, int y) {
        addTile(new Tile("delete"), x, y, true);
    }
    public void usePen(Tile tile, int startX, int startY, int endX, int endY) {
        drawLine(tile, startX, startY, endX, endY);
    }
    public void useFillTool(Tile tile, int x, int y) {
        fill(tile, x, y);
    }
    public void useLineTool(Tile tile, int x, int y) {
        if (lineToolOrigin == null) {
            lineToolTile = tile;
            lineToolOrigin = new Point(x, y);
        }
    }
    public void mouseReleased(int x, int y) {
        if (lineToolOrigin != null) {
            drawLine(lineToolTile, x, y, lineToolOrigin.x, lineToolOrigin.y);
            lineToolTile = null;
            lineToolOrigin = null;
        }
    }

    public void update(boolean canPlaceTiles) {
        if (!canPlaceTiles)
            lineToolOrigin = null;
    }

    public void setSaved(boolean value) {
        saved = value;
        String title = " | Editing level: " + level.mapData.getValue("name");
        if (!saved)
            title += " (Unsaved)";
        LittleH.setTitle(title);
    }

    public Point addTile(Tile tileToCopy, int x, int y, boolean addToUndoQueue) {
        Tile tile = tileToCopy;
        if (!tileToCopy.image.equals("delete"))
            tile = tileToCopy.copy();
        tile.x = x;
        tile.y = y;

        Point amountNegativeResize = resizeAround(x, y);

        tile.x = Math.max(0, x);
        tile.y = Math.max(0, y);

        level.removeTile(layer, tile.x, tile.y);

        if (!tileToCopy.image.equals("delete"))
            level.addTile(layer, tile);

        if (amountNegativeResize.x > 0 || amountNegativeResize.y > 0) {
            undoQueue.forEach(undoAction -> {
                undoAction.resize(amountNegativeResize);
            });
        }

        if (addToUndoQueue) {
            Tile tileAt = level.mapLayers.get(layer).tileMap.get(tile.x).get(tile.y);
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
            level.mapLayers.get(layer).tileMap.get(tile.x).set(tile.y, null);
        } else {
            level.mapLayers.get(layer).tileMap.get(tile.x).set(tile.y, tile);
        }

        Tile[][] neighbors = getNeighbors(null, tile.x, tile.y);

        for (Tile[] tiles : neighbors)
            for (Tile neighborTile : tiles)
                if (neighborTile != null)
                    checkTiling(getNeighbors(neighborTile, neighborTile.x, neighborTile.y), neighborTile);
        setSaved(false);

        return amountNegativeResize;
    }

    public void select(Point position) {
        if (selectionActive()) {
            updateSelection(position);
        } else {
            startSelection(position);
        }
    }

    public void startSelection(Point point) {
        endSelection();
        selection = new Rectangle(point.x, point.y, 1, 1);
        selectionAlive = true;
    }

    public void updateSelection(Point point) {
        if (selection == null) {
            startSelection(point);
        } else {
            Point range = new Point(point.x - selection.x - selection.width + 1, point.y - selection.y - selection.height + 1);
            selection.width += range.x;
            selection.height += range.y;
        }
    }

    public void selectionReleased() {
        if (selection != null) {
            selection = selectionNormalized();
            if (tileSelection != null)
                tileSelection.setRect(selection);
        }
        selectionAlive = false;
        selectionAnchor = null;
        movingSelection = false;
        nudgingSelection = false;
    }

    public boolean selectionActive() {
        return selection != null && selectionAlive;
    }

    public void endSelection() {
        selection = null;
        tileSelection = null;
    }

    public void startMovingSelection(Vector2 relativeTo) {
        nudgingSelection = true;
        selectionAnchor = new Vector2();
        selectionAnchor.x = selection.x * 64 - relativeTo.x;
        selectionAnchor.y = selection.y * 64 - relativeTo.y;
        if (tileSelection == null)
            tileSelection = new TileSelection(selection, level, false, layer);
    }

    public void updateSelectionPosition(Vector2 mousePosition) {
        if (selection != null) {
            if (selectionAnchor == null)
                startMovingSelection(mousePosition);
            selection.x = (int) (selectionAnchor.x + mousePosition.x) / 64;
            selection.y = (int) (selectionAnchor.y + mousePosition.y) / 64;
            if (tileSelection != null)
                tileSelection.translate();
        }
    }

    public void setSelectionPosition(Vector2 mousePosition) {
        if (selection != null) {
            movingSelection = true;
            selection.x = (int) (mousePosition.x) / 64 - selection.width / 2;
            selection.y = (int) (mousePosition.y) / 64 - selection.height / 2;
            if (tileSelection != null)
                tileSelection.translate();
        }
    }

    public Point resizeAround(int x, int y) {
        int widthToAdd = 0;
        int heightToAdd = 0;

        if (x < 0) {
            widthToAdd = Math.abs(x);
            for (int i = 0; i < widthToAdd; i++) {
                Collection<MapLayer> layers = level.mapLayers.values();
                for (MapLayer mapLayer : layers) {
                    List<Tile> tiles = new ArrayList<>(level.getHeight());
                    for (int j = 0; j < level.getHeight(); j++)
                        tiles.add(null);
                    mapLayer.tileMap.add(0, tiles);
                }
                camera.targetPosition.x += 64;
                camera.position.x += 64;
            }
        } else if (x >= level.getWidth()) {
            int expandX = x - level.getWidth() + 1;
            for (int i = 0; i < expandX; i++) {
                Collection<MapLayer> layers = level.mapLayers.values();
                for (MapLayer mapLayer : layers) {
                    List<Tile> tiles = new ArrayList<>(level.getHeight());
                    for (int j = 0; j < level.getHeight(); j++)
                        tiles.add(null);
                    mapLayer.tileMap.add(tiles);
                }
            }
        }

        if (y < 0) {
            heightToAdd = Math.abs(y);
            boolean cameraShifted = false;

            Collection<MapLayer> layers = level.mapLayers.values();
            for (MapLayer mapLayer : layers) {
                for (List<Tile> tiles : mapLayer.tileMap) {
                    for (int i = 0; i < heightToAdd; i++) {
                        tiles.add(0, null);
                        if (!cameraShifted) {
                            camera.targetPosition.y += 64;
                            camera.position.y += 64;
                        }
                    }
                    cameraShifted = true;
                }
            }
        } else if (y >= level.getHeight()) {
            int expandY = y - level.getHeight() + 1;

            Collection<MapLayer> layers = level.mapLayers.values();
            for (MapLayer mapLayer : layers) {
                for (List<Tile> tiles : mapLayer.tileMap) {
                    for (int i = 0; i < expandY; i++) {
                        tiles.add(null);
                    }
                }
            }
        }

        if (widthToAdd > 0 || heightToAdd > 0) {
            Collection<MapLayer> layers = level.mapLayers.values();
            for (MapLayer mapLayer : layers) {
                for (Tile tile : mapLayer.allTiles) {
                    tile.x += widthToAdd;
                    tile.y += heightToAdd;
                }
            }

            if (selection != null) {
                selection.x += widthToAdd;
                selection.y += heightToAdd;
                if (tileSelection != null) {
                    tileSelection.resize(widthToAdd, heightToAdd);
                }
            }
        }

        if (LittleH.program.getScreen() instanceof LevelEditorScreen) {
            ((LevelEditorScreen) LittleH.program.getScreen()).negativeResize(widthToAdd, heightToAdd);
        }

        return new Point(widthToAdd, heightToAdd);
    }

    public boolean isNudging() {
        return nudgingSelection;
    }

    public boolean isMoving() {
        return movingSelection;
    }

    public Tile[][] getNeighbors(Tile tile, int tileX, int tileY) {
        // Magic
        Tile[][] neighbors = new Tile[3][3];
        for (int i = tileX - 1; i < tileX + 2; i++) {
            for (int j = tileY - 1; j < tileY + 2; j++) {
                Tile otherTile = level.getTileAt(layer, i, j);
                if (otherTile != null) {
                    if (tile != null && !tile.image.equals("delete") && tile.hasTag("exclusive_tiling")) {
                        for (String string : tile.tags.getTagParameters("exclusive_tiling")) {
                            if (otherTile.hasTag(string)) {
                                neighbors[tileX - i + 1][tileY - j + 1] = otherTile;
                                break;
                            }
                        }
                    } else if (!otherTile.ignoreTiling)
                        neighbors[tileX - i + 1][tileY - j + 1] = otherTile;
                }
            }
        }
        return neighbors;
    }

    public int numNeighbors(Tile[][] tileMatrix) {
        int count = 0;
        for (Tile[] tiles : tileMatrix) {
            for (Tile tile : tiles) {
                if (tile != null) count++;
            }
        }
        // Subtract one to remove itself
        return count - 1;
    }

    public void checkTiling(Tile[][] neighbors, Tile tile) {
        if (tile.ignoreTiling) return;
        int tileType = 0;

        int numNeighbors = numNeighbors(neighbors);
        // No neighbors
        if (numNeighbors == 0) {
            tileType = 0;
        // Max neighbors
        } else if (numNeighbors == 8) {
            tileType = 5;
        } else {
            // Seven connecting pieces
            if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 2), new Point(0, 1), new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1)) && numNeighbors == 7) {
                tileType = rotateType(6, 2);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(2, 2), new Point(0, 1), new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1)) && numNeighbors == 7) {
                tileType = rotateType(6, 1);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 2), new Point(0, 1), new Point(2, 2), new Point(1, 0), new Point(2, 0), new Point(2, 1)) && numNeighbors == 7) {
                tileType = 6;
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 2), new Point(0, 1), new Point(0, 0), new Point(1, 0), new Point(2, 2), new Point(2, 1)) && numNeighbors == 7) {
                tileType = rotateType(6, 3);

                // Six connecting pieces
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 2), new Point(2, 1), new Point(2, 0))) {
                tileType = 7;
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2))) {
                tileType = rotateType(7, 3);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(1, 0), new Point(2, 1), new Point(0, 2), new Point(0, 1), new Point(0, 0))) {
                tileType = rotateType(7, 2);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(2, 1), new Point(0, 0), new Point(1, 0), new Point(2, 0))) {
                tileType = rotateType(7, 1);
            } else if (checkNeighbors(neighbors, new Point(0, 2), new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(2, 0))) {
                tileType = 8;
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(2, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(0, 0))) {
                tileType = rotateType(8, 1);

                // Five connecting pieces
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(2, 2), new Point(2, 1), new Point(2, 0), new Point(1, 0)) && neighbors[0][1] == null) {
                tileType = rotateType(4, 2);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1)) && neighbors[1][2] == null) {
                tileType = rotateType(4, 3);
            } else if (checkNeighbors(neighbors, new Point(0, 2), new Point(1, 2), new Point(0, 1), new Point(0, 0), new Point(1, 0)) && neighbors[2][1] == null) {
                tileType = 4;
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(2, 1)) && neighbors[1][0] == null) {
                tileType = rotateType(4, 1);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(2, 2))) {
                tileType = rotateType(9, 1);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(2, 0))) {
                tileType = rotateType(9, 2);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(0, 0))) {
                tileType = rotateType(9, 3);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(0, 2))) {
                tileType = 9;

                // Four connecting pieces
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0), new Point(2, 1))) {
                tileType = 10;
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(1, 0), new Point(2, 1), new Point(2, 0))) {
                tileType = rotateType(11, 2);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(0, 0), new Point(1, 0), new Point(2, 1))) {
                tileType = rotateType(11, 3);
            } else if (checkNeighbors(neighbors, new Point(0, 2), new Point(1, 2), new Point(0, 1), new Point(1, 0))) {
                tileType = 11;
            } else if (checkNeighbors(neighbors, new Point(2, 1), new Point(2, 2), new Point(1, 2), new Point(0, 1))) {
                tileType = rotateType(11, 1);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(1, 0), new Point(2, 1), new Point(2, 0))) {
                tileType = rotateType(12, 3);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(2, 2), new Point(2, 1), new Point(1, 0))) {
                tileType = rotateType(12, 2);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(0, 0), new Point(1, 0))) {
                tileType = 12;
            } else if (checkNeighbors(neighbors, new Point(0, 2), new Point(1, 2), new Point(0, 1), new Point(2, 1))) {
                tileType = rotateType(12, 1);

                // Three connecting pieces

            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(1, 0))) {
                tileType = 13;
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(1, 0), new Point(2, 1))) {
                tileType = rotateType(13, 3);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(1, 0), new Point(2, 1))) {
                tileType = rotateType(13, 2);
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(0, 1), new Point(2, 1))) {
                tileType = rotateType(13, 1);
            } else if (checkNeighbors(neighbors, new Point(0, 2), new Point(1, 2), new Point(0, 1))) {
                tileType = 2;
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(2, 2), new Point(2, 1))) {
                tileType = rotateType(2, 1);
            } else if (checkNeighbors(neighbors, new Point(2, 1), new Point(2, 0), new Point(1, 0))) {
                tileType = rotateType(2, 2);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(0, 0), new Point(1, 0))) {
                tileType = rotateType(2, 3);

                // Two connecting pieces
            } else if (checkNeighbors(neighbors, new Point(1, 2), new Point(1, 0))) {
                tileType = 3;
            } else if (checkNeighbors(neighbors, new Point(2, 1), new Point(0, 1))) {
                tileType = rotateType(3, 1);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(1, 2))) {
                tileType = 14;
            } else if (checkNeighbors(neighbors, new Point(2, 1), new Point(1, 2))) {
                tileType = rotateType(14, 1);
            } else if (checkNeighbors(neighbors, new Point(2, 1), new Point(1, 0))) {
                tileType = rotateType(14, 2);
            } else if (checkNeighbors(neighbors, new Point(0, 1), new Point(1, 0))) {
                tileType = rotateType(14, 3);

                // One connecting piece
            } else if (checkNeighbors(neighbors, new Point(1, 2))) {
                tileType = 1;
            } else if (checkNeighbors(neighbors, new Point(0, 1))) {
                tileType = rotateType(1, 3);
            } else if (checkNeighbors(neighbors, new Point(1, 0))) {
                tileType = rotateType(1, 2);
            } else if (checkNeighbors(neighbors, new Point(2, 1))) {
                tileType = rotateType(1, 1);
            }
        }

        if (!tile.image.equals("delete"))
            tile.setTileType(tileType);
    }

    public int rotateType(int baseType, int rotations) {
        return baseType + rotations * 15;
    }

    public boolean checkNeighbors(Tile[][] neighbors, Point... positions) {
        for (Point relPos : positions) {
            if (neighbors[relPos.x][relPos.y] == null) return false;
        }
        return true;
    }

    public void drawLine(Tile tile, int endX, int endY, int startX, int startY) {
        Point resize = new Point();
        for (Point point : getLinePoints(endX, endY, startX, startY)) {
            Point tileResize = addTile(tile, point.x + resize.x, point.y + resize.y, true);
            resize.x += tileResize.x;
            resize.y += tileResize.y;
        }
        addTile(tile, endX + resize.x, endY + resize.y, true);
    }

    public void fill(Tile fillTile, int originX, int originY) {
        // Don't let people fill out of bounds
        if (!(originX >= 0 && originY >= 0 && originX < level.getWidth() && originY < level.getHeight())) return;
        // Don't fill tiles of the same type
        if (Tile.tilesEqual(fillTile, level.getTileAt(layer, originX, originY)) || fillTile.image.equals("delete") && level.getTileAt(layer, originX, originY) == null) return;
        fillTiles.clear();

        List<Point> open = new ArrayList<>();
        Set<Point> closed = new HashSet<>();

        Point origin = new Point(originX, originY);
        Tile tileToFill = level.getTileAt(layer, originX, originY);
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

            if (x + 1 < level.getWidth() && !Tile.tilesEqual(level.getTileAt(layer, x + 1, y), fillTile) && Tile.tilesEqual(level.getTileAt(layer, x + 1, y), tileToFill)
                    && !closed.contains(new Point(x + 1, y)) && Tile.extrasEqual(tileToFill, level.getTileAt(layer, x + 1, y))) {
                open.add(new Point(x + 1, y));
                closed.add(new Point(x + 1, y));
            }
            if (x - 1 >= 0 && !Tile.tilesEqual(level.getTileAt(layer, x - 1, y), fillTile) && Tile.tilesEqual(level.getTileAt(layer, x - 1, y), tileToFill)
                    && !closed.contains(new Point(x - 1, y)) && Tile.extrasEqual(tileToFill, level.getTileAt(layer, x - 1, y))) {
                open.add(new Point(x - 1, y));
                closed.add(new Point(x - 1, y));
            }
            if (y + 1 < level.getHeight() && !Tile.tilesEqual(level.getTileAt(layer, x, y + 1), fillTile) && Tile.tilesEqual(level.getTileAt(layer, x, y + 1), tileToFill)
                    && !closed.contains(new Point(x, y + 1)) && Tile.extrasEqual(tileToFill, level.getTileAt(layer, x, y + 1))) {
                open.add(new Point(x, y + 1));
                closed.add(new Point(x, y + 1));
            }
            if (y - 1 >= 0 && !Tile.tilesEqual(level.getTileAt(layer, x, y - 1), fillTile) && Tile.tilesEqual(level.getTileAt(layer, x, y - 1), tileToFill)
                    && !closed.contains(new Point(x, y - 1)) && Tile.extrasEqual(tileToFill, level.getTileAt(layer, x, y - 1))) {
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

    public Rectangle selectionAsWorldCoords() {
        Rectangle selection = selectionNormalized();
        return new Rectangle(selection.x * 64, selection.y * 64, selection.width * 64, selection.height * 64);
    }

    public Rectangle selectionNormalized() {
        Rectangle selection = new Rectangle(this.selection);

        if (selection.width <= 0) {
            selection.x += selection.width - 1;
            selection.width = Math.abs(selection.width) + 2;
        }

        if (selection.height <= 0) {
            selection.y += selection.height - 1;
            selection.height = Math.abs(selection.height) + 2;
        }

        return selection;
    }

    public void renderSelection(Graphics g) {
        if (tileSelection != null) {
            tileSelection.render(g);
        }

        Texture selectionImage = Images.getImage(!Settings.localSettings.selectionContrast.value ? "ui/selection.png" : "ui/selection_high_con.png");

        if (selection != null) {
            Rectangle selection = selectionNormalized();
            Rectangle rect = selectionAsWorldCoords();
            int frameY = LittleH.getTick() / 12 % 4 * 8;
            com.badlogic.gdx.math.Rectangle frame = new com.badlogic.gdx.math.Rectangle(0, frameY, 8, 8);
            for (int i = 0; i < selection.width; i++) {
                g.drawImage(selectionImage, rect.x + 64 * i, rect.y, 64, 64,
                        frame, 180);
                g.drawImage(selectionImage, rect.x + 64 * i, rect.y + rect.height - 64, 64, 64,
                        frame);
            }
            for (int i = 0; i < selection.height; i++) {
                g.drawImage(selectionImage, rect.x, rect.y + i * 64, 64, 64,
                        frame, 90);
                g.drawImage(selectionImage, rect.x + rect.width - 64, rect.y + i * 64, 64, 64,
                        frame, 270);
            }
        }
    }

    public void deleteSelection() {
        if (selection != null && !selectionAlive) {
            UndoSelection undoSelection = new UndoSelection();
            for (int i = 0; i < selection.width; i++) {
                for (int j = 0; j < selection.height; j++) {
                    Tile tileAt = level.getTileAt(layer, selection.x + i, selection.y + j);
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

    public void undo() {
        if (undoIndex < undoQueue.size()) {
            undoQueue.get(undoIndex).undo(this);
            undoIndex++;
        }
    }

    public void redo() {
        if (undoIndex > 0) {
            undoIndex--;
            undoQueue.get(undoIndex).redo(this);
        }
    }

    public void trimUndoQueue() {
        for (int i = undoIndex - 1; i >= 0; i--) {
            undoQueue.remove(i);
        }
        undoIndex = 0;
    }

    public void addUndoAction(UndoAction action) {
        trimUndoQueue();
        undoQueue.add(0, action);
    }

    public void pasteSelection() {
        if (tileSelection != null) {
            tileSelection.paste(this);
        }
        setSaved(false);
    }

    public void copySelection() {
        tileSelection = new TileSelection(selection, level, layer);
    }

    public boolean hasSelection() {
        return selection != null;
    }

    public Level getLevel() {
        return level;
    }

    public boolean hasTileSelection() {
        return tileSelection != null;
    }

    public Tile getTileAt(int x, int y) {
        return level.getTileAt(layer, x, y);
    }

    public List<Point> getLinePoints(int endX, int endY, int startX, int startY) {
        List<Point> points = new ArrayList<>();
        int dX = endX - startX;
        int dY = endY - startY;

        dX *= 64;
        dY *= 64;

        float dLen = (float) Math.sqrt(dX * dX + dY * dY);
        float sclX = ((float) dX / dLen);
        float sclY = ((float) dY / dLen);

        int[] delta = new int[]{ dX, dY };
        int[] point = new int[]{ startX * 64 + 32, startY * 64 + 32 };
        Vector2 spillover = new Vector2();

        int len = 0;
        while (true) {
            Vector2 vec = new Vector2(sclX * 64, sclY * 64);
            Point step = new Point((int) (sclX * 64 + spillover.x), (int) (sclY * 64 + spillover.y));
            vec.x -= step.x;
            vec.y -= step.y;
            spillover.add(vec);

            Point linePoint = new Point((point[0]) / 64, (point[1]) / 64);
            points.add(linePoint);
            if (linePoint.x == endX && linePoint.y == endY || Math.abs(delta[0]) < 64 && Math.abs(delta[1]) < 64)
                break;

            point[0] += step.x;
            point[1] += step.y;
            delta[0] -= step.x;
            delta[1] -= step.y;
            len++;
            if (len > 512)
                break;
        }
        return points;
    }

    public void render(Graphics g, int x, int y) {
        if (lineToolOrigin != null) {
            for (Point point : getLinePoints(x, y, lineToolOrigin.x, lineToolOrigin.y)) {
                g.draw(Images.getImage("ui/selector.png"), point.x * 64, point.y * 64, 64, 64);
            }
        }
    }
}
