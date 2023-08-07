package com.sab.littleh.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.DynamicCamera;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LevelEditor {
    private static DynamicCamera camera = LittleH.program.dynamicCamera;
    private Level level;
    public boolean saved;
    private Set<Point> fillTiles;
    public LevelEditor(Level level) {
        this.level = level;
        setSaved(true);
        fillTiles = new HashSet<>();
    }

    public void setSaved(boolean value) {
        saved = value;
        String title = LittleH.TITLE + " | Editing level: " + level.mapData.getValue("name");
        if (!saved)
            title += " (Unsaved)";
        Gdx.graphics.setTitle(title);
    }

    public Point addTile(Tile tileToCopy, int x, int y) {
        Tile tile = tileToCopy;
        if (!tileToCopy.image.equals("delete"))
            tile = tileToCopy.copy();
        tile.x = x;
        tile.y = y;

        Point amountNegativeResize = resizeAround(x, y);

        tile.x = Math.max(0, x);
        tile.y = Math.max(0, y);

        level.removeTile(tile.x, tile.y);

        if (!tileToCopy.image.equals("delete"))
            level.addTile(tile);

        if (tile.image.equals("delete")) {
            level.tileMap.get(tile.x).set(tile.y, null);
        } else {
            level.tileMap.get(tile.x).set(tile.y, tile);
        }

        Tile[][] neighbors = getNeighbors(tile.x, tile.y);

        for (Tile[] tiles : neighbors)
            for (Tile neighborTile : tiles)
                if (neighborTile != null)
                    checkTiling(getNeighbors(neighborTile.x, neighborTile.y), neighborTile);
        setSaved(false);

        return amountNegativeResize;
    }

    public Point resizeAround(int x, int y) {
        int widthToAdd = 0;
        int heightToAdd = 0;

        if (x < 0) {
            widthToAdd = Math.abs(x);
            for (int i = 0; i < widthToAdd; i++) {
                List<Tile> tiles = new ArrayList<>(level.getHeight());
                for (int j = 0; j < level.getHeight(); j++)
                    tiles.add(null);
                level.tileMap.add(0, tiles);
                camera.targetPosition.x += 64;
                camera.position.x += 64;
            }
        } else if (x >= level.getWidth()) {
            int expandX = x - level.getWidth() + 1;
            for (int i = 0; i < expandX; i++) {
                List<Tile> tiles = new ArrayList<>(level.getHeight());
                for (int j = 0; j < level.getHeight(); j++)
                    tiles.add(null);
                level.tileMap.add(tiles);
            }
        }

        if (y < 0) {
            heightToAdd = Math.abs(y);
            boolean cameraShifted = false;
            for (List<Tile> tiles : level.tileMap) {
                for (int i = 0; i < heightToAdd; i++) {
                    tiles.add(0, null);
                    if (!cameraShifted) {
                        camera.targetPosition.y += 64;
                        camera.position.y += 64;
                    }
                }
                cameraShifted = true;
            }
        } else if (y >= level.getHeight()) {
            int expandY = y - level.getHeight() + 1;
            for (List<Tile> tiles : level.tileMap) {
                for (int i = 0; i < expandY; i++) {
                    tiles.add(null);
                }
            }
        }

        if (widthToAdd > 0 || heightToAdd > 0) {
            for (Tile tile : level.allTiles) {
                tile.x += widthToAdd;
                tile.y += heightToAdd;
            }
        }

        return new Point(widthToAdd, heightToAdd);
    }

    public Tile[][] getNeighbors(int tileX, int tileY) {
        // Magic
        Tile[][] neighbors = new Tile[3][3];
        for (int i = tileX - 1; i < tileX + 2; i++) {
            for (int j = tileY - 1; j < tileY + 2; j++) {
                if (!(i < 0 || j < 0 || i >= level.getWidth() || j >= level.getHeight()) && level.getTileAt(i, j) != null && !level.getTileAt(i, j).ignoreTiling) {
                    neighbors[tileX - i + 1][tileY - j + 1] = level.getTileAt(i, j);
                } else {
                    neighbors[tileX - i + 1][tileY - j + 1] = null;
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
        int dX = endX - startX;
        int dY = endY - startY;

        Vector2 delta = new Vector2(dX, dY);
        Vector2 point = new Vector2(startX, startY);

        while (delta.len() > 0) {
            Vector2 step = delta.cpy().limit(1);

            Point resize = addTile(tile, (int) point.x, (int) point.y);

            point.x += resize.x;
            point.y += resize.y;

            point.add(step);
            delta.sub(step);
        }
        addTile(tile, (int) point.x, (int) point.y);
    }

    public void fill(Tile fillTile, int originX, int originY) {
        // Don't let people fill out of bounds
        if (!(originX >= 0 && originY >= 0 && originX < level.getWidth() && originY < level.getHeight())) return;
        // Don't fill tiles of the same type
        if (Tile.tilesEqual(fillTile, level.getTileAt(originX, originY)) || fillTile.image.equals("delete") && level.getTileAt(originX, originY) == null) return;
        fillTiles.clear();

        List<Point> open = new ArrayList<>();
        Set<Point> closed = new HashSet<>();

        Point origin = new Point(originX, originY);
        Tile tileToFill = level.getTileAt(originX, originY);
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

            if (x + 1 < level.getWidth() && !Tile.tilesEqual(level.getTileAt(x + 1, y), fillTile) && Tile.tilesEqual(level.getTileAt(x + 1, y), tileToFill) && !closed.contains(new Point(x + 1, y))) {
                open.add(new Point(x + 1, y));
                closed.add(new Point(x + 1, y));
            }
            if (x - 1 >= 0 && !Tile.tilesEqual(level.getTileAt(x - 1, y), fillTile) && Tile.tilesEqual(level.getTileAt(x - 1, y), tileToFill) && !closed.contains(new Point(x - 1, y))) {
                open.add(new Point(x - 1, y));
                closed.add(new Point(x - 1, y));
            }
            if (y + 1 < level.getHeight() && !Tile.tilesEqual(level.getTileAt(x, y + 1), fillTile) && Tile.tilesEqual(level.getTileAt(x, y + 1), tileToFill) && !closed.contains(new Point(x, y + 1))) {
                open.add(new Point(x, y + 1));
                closed.add(new Point(x, y + 1));
            }
            if (y - 1 >= 0 && !Tile.tilesEqual(level.getTileAt(x, y - 1), fillTile) && Tile.tilesEqual(level.getTileAt(x, y - 1), tileToFill) && !closed.contains(new Point(x, y - 1))) {
                open.add(new Point(x, y - 1));
                closed.add(new Point(x, y - 1));
            }
        }

//        // Add a "fill" action to the undo queue
//        if (tileToFill == null) {
//            // Save undo actions of null instead as tiles with the tag "delete"
//            tileToFill = new Tile(originX, originY, "delete", 0);
//            tileToFill.setTags(new String[]{ "delete" });
//        }
//        fillTile = fillTile.copy();

        // Add/delete all tiles
        for (Point point : fillTiles) {
            addTile(fillTile, point.x, point.y);
        }

        // Empty the list for good measure
        fillTiles.clear();
    }
}
