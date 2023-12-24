package com.sab.littleh.game.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Collisions;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.awt.Point;
import java.awt.Graphics2D;
import java.util.*;

public class Entity {
    public float x;
    public float y;
    public int width;
    public int height;
    public int direction;
    public float rotation;
    public float velocityX;
    public float velocityY;
    public int frame;
    public boolean slippery;
    public boolean touchingGround;
    public boolean touchingWater;
    public String image;
    public Set<Tile> lastTouchedTiles = new HashSet<>();

    public void update(Level game) {
        touchingWater = false;
        for (Tile tile : lastTouchedTiles) {
            if (tile.hasTag("water")) {
                touchingWater = true;
                break;
            }
        }
        lastTouchedTiles = new HashSet<>();
        collide(game);
        if (touchingWater) {
            velocityX *= 0.96f;
            velocityY *= 0.96f;
        }
    }

    // Return false to prevent the entity from having their velocity set to 0
    public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
        return true;
    }

    public Tile getTile(float relX, float relY, List<List<Tile>> map) {
        int absX = (int) Math.round((x - 6) / 64 + relX);
        int absY = (int) Math.round((y) / 64 + relY);
        if (absX >= map.size() || absX <= 0 || absY >= map.get(0).size() || absY <= 0) return null;
        return map.get(absX).get(absY);
    }

    public List<Tile> getNearbyTiles(List<List<Tile>> map) {
        int minX = (int) Math.floor((x) / 64) - 1;
        int maxX = minX + 3;
        int minY = (int) Math.floor((y) / 64) - 1;
        int maxY = minY + 3;
        List<Tile> tiles = new ArrayList<>();
        for (int i = minX; i < maxX; i++) {
            for (int j = minY; j < maxY; j++) {
                if (i >= 0 && j >= 0 && i < map.size() && j < map.get(0).size() && map.get(i).get(j) != null) {
                    tiles.add(map.get(i).get(j));
                }
            }
        }
        return tiles;
    }

    public void set(Rectangle setTo) {
        x = setTo.x;
        y = setTo.y;
        width = (int) setTo.width;
        height = (int) setTo.height;
    }

    public void collide(Level game) {
        List<Tile> collisions = new ArrayList<Tile>();
        Rectangle entityHitbox = new Rectangle(x, y, width, height);

        solidInteractions(entityHitbox, collisions, game);
        collisions = getNearbyTiles(game.getBaseLayer().tileMap);
        List<Tile> backgroundTiles = getNearbyTiles(game.getBackgroundLayer().tileMap);
        backgroundTiles.removeIf(tile -> !tile.hasTag("ignore_background"));
        collisions.addAll(backgroundTiles);
        tileInteractions(entityHitbox, collisions, game);

        set(entityHitbox);
    }

    public void onCollision(boolean horizontal, boolean vertical) {
        if (horizontal) {
            velocityX = 0;
        }
        if (vertical) {
            velocityY = 0;
        }
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public void solidInteractions(Rectangle entityHitbox, List<Tile> collisions, Level game) {
        entityHitbox.x += velocityX;
        x = entityHitbox.x;

        boolean stopX = false;
        boolean stopY = false;

        collisions = getNearbyTiles(game.getBaseLayer().tileMap);
        for (Tile tile : collisions) {
            if (!tile.isSolid()) continue;
            Rectangle tileHitbox = tile.toRectangle();
            if (tile.hasTag("one_way") && (Math.signum(tile.tileType - 2) == Math.signum(velocityX) || velocityX == 0 || tile.tileType % 2 == 0) || lastTouchedTiles.contains(tile)) continue;
            if (Collisions.resolveX(entityHitbox, tileHitbox, velocityX)) {
                if (onCollide(game, entityHitbox, tileHitbox, tile, false)) stopX = true;
            }
        }

        entityHitbox.y += velocityY;
        y = entityHitbox.y;

        collisions = getNearbyTiles(game.getBaseLayer().tileMap);
        for (Tile tile : collisions) {
            if (!tile.isSolid()) continue;
            Rectangle tileHitbox = tile.toRectangle();
            if (tile.hasTag("one_way") && (Math.signum(tile.tileType - 1) == Math.signum(velocityY) || tile.tileType % 2 != 0) || lastTouchedTiles.contains(tile)) continue;
            if (Collisions.resolveY(entityHitbox, tileHitbox, velocityY)) {
                if (onCollide(game, entityHitbox, tileHitbox, tile, true)) {
                    stopY = true;
                    if (velocityY < 0) {
                        touchingGround = true;
                    }
                }
            }
        }

        if (stopX || stopY) onCollision(stopX, stopY);
    }

    public void kill() {
    }

    public void touchingTile(Level game, Tile tile) {

    }

    public void render(Graphics g, Level game) {
        g.drawImage(Images.getImage(image), new Rectangle((int) x - 8, (int) y, 64, 64), new Rectangle((direction == 1 ? 0 : 8), 8 * frame, (direction == 1 ? 8 : -8), 8), -MathUtils.radiansToDegrees * rotation);
    }

    public float velocityMagnitude() {
        return (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }

    public void tileInteractions(Rectangle entityHitbox, List<Tile> collisions, Level game) {
        Set<Tile> newLastTouchedTiles = new HashSet<>();
        for (Tile tile : collisions) {
            if (tile.hasTag("multi_hitbox")) {
                List<Rectangle> tileHitboxes = tile.toRectangles();
                for (Rectangle tileHitbox : tileHitboxes) {
                    if (entityHitbox.overlaps(tileHitbox)) {
                        touchingTile(game, tile);
                        newLastTouchedTiles.add(tile);
                        break;
                    }
                }
            } else {
                Rectangle tileHitbox = tile.toRectangle();
                if (entityHitbox.overlaps(tileHitbox)) {
                    touchingTile(game, tile);
                    newLastTouchedTiles.add(tile);
                }
            }
        }
        lastTouchedTiles = newLastTouchedTiles;
    }
}