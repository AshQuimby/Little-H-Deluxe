package com.sab.littleh.game.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Collisions;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.util.*;

public class Entity extends GameObject {
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
    public Set<Tile> lastTouchedTiles = new HashSet<>();

    @Override
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
        Set<Tile> oldCollisions = new HashSet<>();
        List<Tile> allCollisions = new ArrayList<>();
        List<Tile> collisions;
        Rectangle entityHitbox = new Rectangle(x, y, width, height);

        Vector2 vel = new Vector2(velocityX, velocityY);

        while (vel.len() > 0) {
            Vector2 step = vel.cpy().limit(8);
            collisions = getNearbyTiles(game.getBaseLayer().tileMap);
            collisions.removeAll(oldCollisions);
            allCollisions.addAll(collisions);
            Vector2 stop = new Vector2(1, 1);
            solidInteractions(step, stop, entityHitbox, collisions, game);
            vel.scl(stop.x, stop.y);
            step.scl(stop.x, stop.y);
            oldCollisions.addAll(collisions);
            vel.sub(step);
        }
        List<Tile> backgroundTiles = getNearbyTiles(game.getBackgroundLayer().tileMap);
        backgroundTiles.removeIf(tile -> !tile.hasTag("ignore_background"));
        allCollisions.addAll(backgroundTiles);
        tileInteractions(entityHitbox, allCollisions, game);

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

    public void solidInteractions(Vector2 step, Vector2 stop, Rectangle entityHitbox, List<Tile> collisions, Level game) {
        entityHitbox.x += step.x;
        x = entityHitbox.x;

        boolean stopX = false;
        boolean stopY = false;

        collisions = getNearbyTiles(game.getBaseLayer().tileMap);
        List<Rectangle> hitboxes = new ArrayList<>();
        for (Tile tile : collisions) {
            if (!tile.isSolid()) continue;
            tile.toRectangles(hitboxes);
            for (Rectangle tileHitbox : hitboxes) {
                if (tile.hasTag("slope")) {
                    slopeCollision(step, stop, entityHitbox, tile, game, false);
                } else {
                    if (tile.hasTag("one_way") && (Math.signum(tile.tileType - 2) == Math.signum(velocityX) || velocityX == 0 || tile.tileType % 2 == 0) || lastTouchedTiles.contains(tile))
                        continue;
                    if (Collisions.resolveX(entityHitbox, tileHitbox, step.x)) {
                        if (onCollide(game, entityHitbox, tileHitbox, tile, false)) stopX = true;
                        stop.x = 0;
                    }
                }
            }
            hitboxes.clear();
        }

        entityHitbox.y += step.y;
        y = entityHitbox.y;

        collisions = getNearbyTiles(game.getBaseLayer().tileMap);
        for (Tile tile : collisions) {
            if (!tile.isSolid()) continue;
            tile.toRectangles(hitboxes);
            for (Rectangle tileHitbox : hitboxes) {
                if (tile.hasTag("slope")) {
                    slopeCollision(step, stop, entityHitbox, tile, game, true);
                } else {
                    if (tile.hasTag("one_way") && (Math.signum(tile.tileType - 1) == Math.signum(velocityY) || tile.tileType % 2 != 0) || lastTouchedTiles.contains(tile))
                        continue;
                    if (Collisions.resolveY(entityHitbox, tileHitbox, step.y)) {
                        if (onCollide(game, entityHitbox, tileHitbox, tile, true)) {
                            stopY = true;
                            if (step.y < 0 && !dontRefreshTouchingGround()) {
                                touchingGround = true;
                            }
                        }
                        stop.y = 0;
                    }
                }
            }
            hitboxes.clear();
        }

        if (stopX || stopY) onCollision(stopX, stopY);
    }

    private void slopeCollision(Vector2 step, Vector2 stop, Rectangle entityHitbox, Tile tile, Level game, boolean yCollision) {
        boolean stopX = false;
        boolean stopY = false;

        if (yCollision) {
            Rectangle tileHitbox = tile.toRectangle();
            if (tile.tileType == 0) {
                if (y < tileHitbox.y - Math.abs(velocityX)) {
                    if (Collisions.resolveY(entityHitbox, tileHitbox, step.y)) {
                        if (onCollide(game, entityHitbox, tileHitbox, tile, true)) {
                            if (step.y < 0 && !dontRefreshTouchingGround()) {
                                touchingGround = true;
                            }
                            stopY = true;
                        }
                        stop.y = 0;
                    }
                } else {
                    tileHitbox.y += Math.abs(velocityX);
                    if (step.y < 0 && tileHitbox.overlaps(entityHitbox)) {
                        if (tile.hasTag("normal_slope")) {
                            float slopeY = Math.min(64, Math.max(0, (entityHitbox.x + entityHitbox.width) - tileHitbox.x));
                            if (entityHitbox.y <= tileHitbox.y + slopeY) {
                                entityHitbox.y = slopeY + tileHitbox.y;
                                velocityY = 0;
                                stop.y = 0;
                                touchingGround = true;
                            }
                        }
                    }
                }
            } else
            if (tile.tileType == 1) {
                if (y < tileHitbox.y - Math.abs(velocityX)) {
                    if (Collisions.resolveY(entityHitbox, tileHitbox, step.y)) {
                        if (onCollide(game, entityHitbox, tileHitbox, tile, true)) {
                            if (step.y < 0 && !dontRefreshTouchingGround()) {
                                touchingGround = true;
                            }
                            stopY = true;
                        }
                        stop.y = 0;
                    }
                } else {
                    tileHitbox.y += Math.abs(velocityX);
                    if (step.y < 0 && tileHitbox.overlaps(entityHitbox)) {
                        if (tile.hasTag("normal_slope")) {
                            float slopeY = Math.min(64, Math.max(0, tileHitbox.height - (entityHitbox.x - tileHitbox.x)));
                            if (entityHitbox.y <= tileHitbox.y + slopeY) {
                                entityHitbox.y = slopeY + tileHitbox.y;
                                velocityY = 0;
                                stop.y = 0;
                                touchingGround = true;
                            }
                        }
                    }
                }
            }
        }

        if (stopX || stopY) onCollision(stopX, stopY);
    }

    protected boolean dontRefreshTouchingGround() {
        return false;
    }

    public void kill() {
    }

    public void touchingTile(Level game, Tile tile) {

    }

    @Override
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
                List<Rectangle> tileHitboxes = new ArrayList<>();
                tile.toRectangles(tileHitboxes);
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