package com.sab.littleh.game.tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.Prop;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.level.wiring.Wiring;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.util.*;

public class Tile {
    public static final Rectangle drawRect = new Rectangle(0, 0, 8, 8);
    // Store position for efficiency both in-game and for saving files
    public int x;
    public int y;
    // An image identifier used to find the image
    public String image;
    public String extra;
    // Byte to save RAM, it should never be bigger than 63 in reality otherwise other things would break first
    public byte tileType;
    // Tells the tile if it's a tileset or a bunch of individual images
    public boolean ignoreTiling;
    private Rectangle cachedDrawRect;
    private Object arbDat;
    // Set because we only use it to check if one is present
    public TileTags tags;

    public Tile(int x, int y, String image, int tileType, TileTags tags, String extra, boolean ignoreTiling, Rectangle drawRect) {
        this.x = x;
        this.y = y;
        this.image = image;
        // Very important to create a new TileTags, not just reference the argument
        this.tags = new TileTags(tags);
        if (extra != null)
            this.extra = extra.trim();
        this.tileType = (byte) tileType;
        this.ignoreTiling = ignoreTiling;
        this.cachedDrawRect = drawRect;
    }

    public Tile(int x, int y, String image, int tileType, TileTags tags, String extra) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.tags = new TileTags(tags);
        if (extra != null)
            this.extra = extra.trim();
        setTileType(tileType);
    }

    public Tile(int x, int y, String image) {
        this.image = image;
        this.x = x;
        this.y = y;
        if (!image.equals("delete")) {
            setTags();
            setTileType(0);
        } else {
            tags = TileTags.EMPTY;
        }
    }

    public Tile(String image) {
        this(0, 0, image);
    }

    public static boolean imagesEqual(Tile tileAt, Tile newTile) {
        return tileAt != null && newTile != null && tileAt.image.equals(newTile.image);
    }

    public static boolean extrasEqual(Tile tile, Tile other) {
        return tile == null && other == null || tile != null && tile.extrasEqual(other);
    }

    public void setTags() {
        tags = new TileTags(LevelLoader.getTileTags(image));
    }

    public void setTags(String[] tags) {
        if (tags == null) return;
        for (String tag: tags) {
            this.tags.addTag(tag);
        }
    }

    public boolean tileEquals(Tile other) {
        return other != null && image.equals(other.image) && (tileType == other.tileType || !ignoreTiling) && extrasEqual(other);
    }

    public boolean extrasEqual(Tile other) {
        return extra == null && (other != null && other.extra == null) || extra != null && (other != null && extra.equals(other.extra));
    }

    public static boolean tilesEqual(Tile tile, Tile other) {
        return tile == null && other == null || tile != null && tile.tileEquals(other);
    }

    public boolean isSolid() {
        if (hasTag("coin_box") || hasTag("enemy_box")) return tileType % 2 == 0;
        return hasTag("solid");
    }

    public int getOrientation() {
        if (tileType == 0) return (x + y / 2) % 4;
        return tileType / 15 % 4;
    }

    public void setTileType(int tileType) {
        if (hasTag("tileset")) {
            this.tileType = (byte) tileType;
        } else {
            ignoreTiling = true;
            this.tileType = (byte) tileType;
            updateDrawSection();
            return;
        }
        ignoreTiling = false;
        updateDrawSection();
    }

    public void updateDrawSection() {
        int localType = tileType % (hasTag("tileset") ? 15 : 16);
        int column = localType % 4;
        int row = localType / 4;
        Rectangle region = new Rectangle(1 + column * 10, 1 + row * 10, 8, 8);
        cacheDrawRect(region);
    }

    public void cacheDrawRect(Rectangle rectangle) {
        cachedDrawRect = rectangle;
    }

    public Rectangle getDrawSection() {
        return cachedDrawRect;
    }

    public int getStateCount() {
        if (hasTag("states")) {
            return Integer.parseInt(tags.getTag("states"));
        }
        return 0;
    }

    public int getStateIndex() {
        if (hasTag("states")) {
            return tileType;
        }
        return 0;
    }

    /*
    public void notify(Level game, String broadcastTag, int[] data) {
        switch (broadcastTag) {
            case "notify_all_coins" : {
                if (tileType / 2 == data[0] && hasTag("coin_box")) {
                    cycleProperties(tileType % 2 == 0);
                }
                break;
            }
            case "notify_collect_checkpoint" : {
                if (hasTag("notified_reset_checkpoint") && hasTag("used")) {
                    removeTag("used");
                    image = "tiles/strong_checkpoint";
                }
                break;
            }
            case "notify_update" : {
                if (hasTag("notified_spawn_enemy")) {
                    game.createEnemy(this);
                }
                break;
            }
        }
    }
     */

    public String getImageString() {
        if (ignoreTiling) return image + ".png";
        return image + "_rotation_" + getOrientation() + ".png";
    }

    public Texture getImage() {
        return Images.getImage(getImageString());
    }

    public void render(boolean playerExists, Graphics g) {
        if (hasTag("invisible") && playerExists) {
            return;
        }
        if (hasTag("render_color")) {
            g.setColor(extra == null ? Color.WHITE : Color.valueOf("#" + extra.toUpperCase().trim()));
            g.drawImage(getImage(), x * 64, y * 64, 64, 64, getDrawSection());
            g.resetColor();
        } else {
            g.drawImage(getImage(), x * 64, y * 64, 64, 64, getDrawSection());
        }
    }

    public Rectangle toRectangle() {
        Rectangle tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
        if (hasTag("half")) {
            switch (tileType % 4) {
                case 2 :
                    tileHitbox.height = 32;
                    tileHitbox.y += 32;
                    break;
                case 1 :
                    tileHitbox.width = 32;
                    break;
                case 0 :
                    tileHitbox.height = 32;
                    break;
                case 3 :
                    tileHitbox.width = 32;
                    tileHitbox.x += 32;
                    break;
                default :
                    break;
            }
        } else if (hasTag("quarter")) {
            switch (tileType) {
                case 2 :
                    tileHitbox.height = 16;
                    tileHitbox.y += 48;
                    break;
                case 1 :
                    tileHitbox.width = 16;
                    break;
                case 0 :
                    tileHitbox.height = 16;
                    break;
                case 3 :
                    tileHitbox.width = 16;
                    tileHitbox.x += 48;
                    break;
                default :
                    break;
            }
        } else if (hasTag("small")) {
            tileHitbox.x += 8;
            tileHitbox.y += 8;
            tileHitbox.width -= 16;
            tileHitbox.height -= 16;
        }

        return tileHitbox;
    }

    public void toRectangles(List<Rectangle> hitboxes) {
        if (!hasTag("multi_hitbox")) {
            hitboxes.add(toRectangle());
            return;
        }
        Rectangle tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
        if (hasTag("small_triangle")) {
            switch (tileType) {
                case 0 :
                    tileHitbox.height = 8;
                    hitboxes.add(tileHitbox);
                    tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
                    tileHitbox.height = 32;
                    tileHitbox.width = 16;
                    tileHitbox.x += 16;
                    hitboxes.add(tileHitbox);
                    break;
                case 3 :
                    tileHitbox.width = 8;
                    tileHitbox.x += 56;
                    hitboxes.add(tileHitbox);
                    tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
                    tileHitbox.width = 32;
                    tileHitbox.x += 32;
                    tileHitbox.height = 16;
                    tileHitbox.y += 16;
                    hitboxes.add(tileHitbox);
                    break;
                case 2 :
                    tileHitbox.height = 8;
                    tileHitbox.y += 56;
                    hitboxes.add(tileHitbox);
                    tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
                    tileHitbox.height = 32;
                    tileHitbox.y += 32;
                    tileHitbox.width = 16;
                    tileHitbox.x += 16;
                    hitboxes.add(tileHitbox);
                    break;
                case 1 :
                    tileHitbox.width = 8;
                    hitboxes.add(tileHitbox);
                    tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
                    tileHitbox.width = 32;
                    tileHitbox.height = 16;
                    tileHitbox.y += 16;
                    hitboxes.add(tileHitbox);
                    break;
                default :
                    break;
            }
        }
    }

    public void setImage(String image) {
        this.image = image;
        setTileType(tileType);
    }

    @Override
    public String toString() {
        return image + ", " + tileType + ". " + x + ", " + y;
    }

    public boolean hasTag(String tag) {
        return tags.hasTag(tag);
    }

    public String[] getTags() {
        return tags.getTags();
    }

    public void removeTag(String tag) {
        tags.removeTag(tag);
    }

    public Tile copy() {
        if (image.equals("delete")) return new Tile(x, y, "delete");
        return new Tile(x, y, image, tileType, tags, extra, ignoreTiling, cachedDrawRect);
    }

    public void notify(Level game, String notification, int[] data) {
        if (hasTag("notified_reset_type")) {
            setTileType(0);
        }
        if (hasTag("notified_alternate_type")) {
            if (!hasTag("coin_box") || hasTag("coin_box") && data[0] == tileType / 2)
                setTileType(tileType % 2 == 0 ? tileType + 1 : tileType - 1);
        }
        if (hasTag("respawn_power_fruit")) {
            Tile powerFruit = game.getTileAt("normal", x, y);
            if (powerFruit.hasTag("powerup")) {
                arbDat = new Object[2];
                ((Object[]) arbDat)[0] = powerFruit;
                ((Object[]) arbDat)[1] = 120;
            }
        }
        if (hasTag("prop")) {
            game.addMiscGameObject(new Prop("props/" + extra + ".png", x * 64, y * 64));
        }
        if (hasTag("notified_spring_bounce")) {
            arbDat = tileType % 2 + 30;
        }

        if (hasTag("actuator")) {
            // Set the actuatable tiles to have their states saved at checkpoints
            Tile tile = game.getTileAt("normal", x, y);
            if (tile != null) {
                tile.tags.addTag("checkpoint_saved");
                game.inGameSetTile("normal", x, y, tile);
            } else {
                game.inGameSetTile("normal", x, y, new Tile(x, y, "tiles/actuator_air"));
            }
            tile = game.getTileAt("background", x, y);
            if (tile != null) {
                tile.tags.addTag("checkpoint_saved");
                game.inGameSetTile("background", x, y, tile);
            } else {
                game.inGameSetTile("background", x, y, new Tile(x, y, "tiles/actuator_air"));
            }
        }

        if (hasTag("decimator")) {
            // Set the actuatable tiles to have their states saved at checkpoints
            Tile tile = game.getTileAt("normal", x, y);
            if (tile != null) {
                tile.tags.addTag("checkpoint_saved");
                game.inGameSetTile("normal", x, y, tile);
            }
        }
        if (hasTag("observer")) {
            Tile observing = game.getTileAt("normal", x, y);
            if (observing == null) {
                tileType = -1;
            } else {
                tileType = observing.tileType;
            }
        }
    }

    public void update(Level game) {
        if (hasTag("observer")) {
            Tile observing = game.getTileAt("normal", x, y);
            byte observedTileType = observing == null ? -1 : observing.tileType;

            if (tileType != observedTileType) {
//                List<Tile> poweredTiles = game.wiring.getPoweredTiles(this);
//                if (poweredTiles != null) {
//                    for (Tile powered : poweredTiles) {
//                        powered.signalReceived(game);
//                    }
//                }
//
//                tileType = observedTileType;
            }
        }

        if (hasTag("camera_focus")) {
            Vector2 tileCenter = new Vector2(x * 64 + 32, y * 64 + 32);
            float playerDist = game.player.getCenter().dst2(tileCenter);
            float focusDist;
            if (extra == null || extra.isBlank())
                focusDist = 1720;
            else
                focusDist = Float.parseFloat(extra);

            if (playerDist < focusDist * focusDist) {
                game.setCameraFocus(tileCenter);
            }
        }

        if (hasTag("enemy")) {
            for (Enemy enemy : game.getEnemies()) {
                if (enemy.getParent().equals(this)) return;
            }

            if (game.player.isFresh()) {
                game.addEnemy(Enemy.createEnemy(x, y, game.player, this, tileType));
            } else {
                Vector2 tileCenter = new Vector2(x * 64 + 32, y * 64 + 32);
                float playerDist = game.player.getCenter().dst2(tileCenter);
                if (playerDist > 1112 * 1112 && playerDist < 1720 * 1720) {
                    game.addEnemy(Enemy.createEnemy(x, y, game.player, this, tileType));
                }
            }
        }

        if (hasTag("respawn_power_fruit")) {
            if (arbDat != null) {
                Object[] data = (Object[]) arbDat;
                if (game.getTileAt("normal", x, y) == null) {
                    int timer = (Integer) data[1];
                    if (timer <= 0) {
                        Tile powerFruit = ((Tile) data[0]).copy();
                        if (!toRectangle().overlaps(game.player.toRectangle())) {
                            game.addTileToMap("normal", powerFruit);
                            timer = 90;
                        }
                    }
                    timer--;
                    data[1] = timer;
                }
            }
        }

        if (hasTag("notified_spring_bounce")) {
            if (arbDat != null) {
                int data = (Integer) arbDat;
                if (data > 0) data--;

                tileType = (byte) (tileType % 2 + data / 10);
            } else {
                arbDat = 0;
            }
        }

        if (hasTag("ambient_particles")) {
            if (MathUtils.random() < 0.02f) {
                if (hasTag("end")) {
                    game.addParticle(new Particle(x * 64 + MathUtils.random() * 64, y * 64 + MathUtils.random() * 64,
                            (float) ((MathUtils.random() - 0.5) * 2), (float) ((MathUtils.random() - 0.5) * 2),
                            24, 24, 3, 3, 1, 0.98f, 0f,
                            (int) (MathUtils.random() * 2), 0, "particles/twinkle.png", 60, 0.02f));
                }

                if (hasTag("evil") && hasTag("key")) {
                    game.addParticle(new Particle(x * 64 + MathUtils.random() * 64, y * 64 + MathUtils.random() * 64,
                            (float) ((MathUtils.random() - 0.5) * 2), (float) ((MathUtils.random() - 0.5) * 2),
                            32, 32, 4, 4, 1, 0.98f, 0f,
                            (int) (MathUtils.random() * 4), 0, "particles/evil_smoke.png", 60, 0.02f));
                }

                if (hasTag("powerup")) {
                    game.addParticle(new Particle(x * 64 + MathUtils.random() * 64, y * 64 + MathUtils.random() * 64,
                            (float) ((MathUtils.random() - 0.5) * 2), (float) ((MathUtils.random() - 0.5) * 2),
                            24, 24, 3, 3, 1, 0.98f, 0f,
                            (int) (MathUtils.random() * 3), 0, "particles/shine.png", 60, 0.02f));
                }
            }
        }
    }

    public void wiringUpdate(Wiring wiring) {
        if (hasTag("repeater")) {
            int poweredInputs = wiring.poweredInputCount(this);
            if (poweredInputs > 0) {
                if (wiring.isInGroup(x + Wiring.dx(tileType), y + Wiring.dy(tileType))) {
                    wiring.power(wiring.getGroup(x + Wiring.dx(tileType), y + Wiring.dy(tileType)).id);
                }
            }
        }

        if (hasTag("and_gate")) {
            int poweredInputs = wiring.poweredInputCount(this);
            if (poweredInputs == 2) {
                if (wiring.isInGroup(x + Wiring.dx(tileType), y + Wiring.dy(tileType))) {
                    wiring.power(wiring.getGroup(x + Wiring.dx(tileType), y + Wiring.dy(tileType)).id);
                }
            }
        }
    }

    public void signalReceived(Level game) {
        if (hasTag("actuator")) {
            Tile tileToSwap = game.getTileAt("normal", x, y);
            Tile backgroundTile = game.getTileAt("background", x, y);

            game.inGameSetTile("normal", x, y, backgroundTile);
            game.inGameSetTile("background", x, y, tileToSwap);
        }
        if (hasTag("decimator")) {
            game.inGameSetTile("normal", x, y, null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tile) {
            Tile other = (Tile) o;
            if (imagesEqual(this, other)) {
                if (ignoreTiling && !hasTag("wiring_component")) {
                    return x == other.x && y == other.y && tileType == other.tileType;
                } else {
                    return x == other.x && y == other.y;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return image.hashCode() + (ignoreTiling && !hasTag("wiring_component") ? tileType * tileType * tileType : 0) + x * x + y;
    }
}