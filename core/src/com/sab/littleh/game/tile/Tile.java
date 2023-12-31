package com.sab.littleh.game.tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import org.w3c.dom.Text;

import java.util.*;

public class Tile {
    public static final Rectangle drawRect = new Rectangle(0, 0, 8, 8);
    // Cache tag sets to save RAM
    public static final Map<Set<String>, Set<String>> tagsCache = new HashMap<>();
    // Store position for efficiency both in-game and for saving files
    public int x;
    public int y;
    // Byte to save RAM, it should never be bigger than 3
    private byte rotation;
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
    public Set<String> tags;

    private Tile(int x, int y, String image, int tileType, Set<String> tags, String extra, boolean ignoreTiling, Rectangle drawRect) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.tags = tags;
        if (extra != null)
            this.extra = extra.trim();
        this.tileType = (byte) tileType;
        this.ignoreTiling = ignoreTiling;
        this.cachedDrawRect = drawRect;
    }

    private Tile(int x, int y, String image, int tileType, Set<String> tags, String extra) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.tags = getCachedTags(tags);
        if (extra != null)
            this.extra = extra.trim();
        setTileType(tileType);
    }

    public Tile(int x, int y, String image, int tileType, String tags, String extra) {
        this(x, y, image, tileType, tags.split(","), extra);
    }

    public Tile(int x, int y, String image, int tileType, String[] tags, String extra) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.tags = new HashSet<>();
        for (String string : tags) {
            this.tags.add(string);
        }
        this.tags = getCachedTags(this.tags);
        if (extra != null)
            this.extra = extra.trim();
        setTileType(tileType);
    }

    public Tile(int x, int y, String image, int tileType, String[] tags) {
        this(x, y, image, tileType, tags, "");
    }

    public Tile(int x, int y, String image, int tileType, String tags) {
        this(x, y, image, tileType, tags, "");
    }

    public Tile(int x, int y, String image, int tileType) {
        this(x, y, image, tileType, "");
    }

    public Tile(String image, int tileType) {
        this(0, 0, image, tileType);
    }

    public Tile(String image, String[] tags) {
        this(0, 0, image, 0, tags, "");
    }

    public Tile(String image) {
        this.image = image;
        setTags();
        if (!image.equals("delete"))
            setTileType(0);
    }

    public static boolean imagesEqual(Tile tileAt, Tile newTile) {
        return tileAt != null && newTile != null && tileAt.image.equals(newTile.image);
    }

    public static boolean extrasEqual(Tile tile, Tile other) {
        return tile == null && other == null || tile != null && tile.extrasEqual(other);
    }

    public void setTags() {
        String identifier = image;
        if (!image.startsWith("."))
            identifier = image.substring(image.lastIndexOf("/") + 1);
        if (LevelLoader.tagsByTile.getValue(identifier) == null)
            return;
        setTags(LevelLoader.tagsByTile.getValue(identifier).asStringArray());
    }
    public void setTags(String[] tags) {
        if (tags == null) return;
        this.tags = new HashSet<>();
        Collections.addAll(this.tags, tags);
        this.tags = getCachedTags(this.tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = getCachedTags(tags);
    }

    public boolean tileEquals(Tile other) {
        return other != null && image.equals(other.image) && (tileType == other.tileType || !ignoreTiling);
    }

    public boolean extrasEqual(Tile other) {
        return extra == null && (other != null && other.extra == null) || extra != null && (other != null && extra.equals(other.extra));
    }

    public static boolean tilesEqual(Tile tile, Tile other) {
        return tile == null && other == null || tile != null && tile.tileEquals(other);
    }

    public static void clearTagsCache() {
        tagsCache.clear();
    }

    public Set<String> getCachedTags(Set<String> tags) {
        if (tagsCache.containsKey(tags)) {
            return tagsCache.get(tags);
        }
        tagsCache.put(tags, tags);
        return tags;
    }

    public boolean isSolid() {
        if (hasTag("coin_box") || hasTag("enemy_box")) return tileType % 2 == 0;
        return hasTag("solid");
    }

    public boolean matches(Tile other) {
        return other != null && other.image.equals(image);
    }

    public int getOrientation() {
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
        Rectangle region = new Rectangle(column * 8, row * 8, 8, 8);
        cacheDrawRect(region);
    }

    public void cacheDrawRect(Rectangle rectangle) {
        cachedDrawRect = rectangle;
    }

    public Rectangle getDrawSection() {
        return cachedDrawRect;
    }

    public int getPropertyCount() {
        if (hasTag("property_set")) {
            for (String tag : tags) {
                if (!tag.contains("_set") || tag.equals("property_set")) continue;
                String count = tag.split("_")[0];
                return Integer.parseInt(count);
            }
        }
        return 0;
    }

    public int getPropertyIndex() {
        if (hasTag("property_set")) {
            return tileType;
        }
        return 0;
    }

    public void setPropertyValue(int value) {
        if (hasTag("property_set")) {
            setTileType(value);
        }
    }

    public int[] getPropertyValues() {
        if (hasTag("property_set")) {
            for (String tag : tags) {
                if (!tag.contains("_set") || tag.equals("property_set")) continue;
                String count = tag.split("_")[0];
                int[] values = new int[Integer.parseInt(count)];
                for (int i = 0; i < values.length; i++) {
                    values[i] = i;
                }
                return values;
            }
        }
//       if (hasTag("3_set")) {
//          return new int[]{ 0, 1, 2 };
//       } else if (hasTag("4_set")) {
//          return new int[]{ 0, 1, 2, 3 };
//       } else if (hasTag("15_set")) {
//          return new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
//       }
        return new int[]{ 0 };
    }

    public void cycleProperties(boolean forward) {
        if (hasTag("property_set")) {
            int max = getPropertyValues().length;
            tileType = (byte) (tileType + (forward ? 1 : -1));
            if (tileType > max) tileType = 0;
            else if (tileType < 0) tileType = (byte) (max - 1)  ;
            updateDrawSection();
        }
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
        if (hasTag("render_color"))
            g.setColor(extra == null ? Color.WHITE : Color.valueOf("#" + extra.toUpperCase().trim()));
        g.drawImage(getImage(), x * 64, y * 64, 64, 64, getDrawSection());
        g.resetColor();
    }

    public Rectangle toRectangle() {
        Rectangle tileHitbox = new Rectangle(x * 64, y * 64, 64, 64);
        if (hasTag("half")) {
            switch (tileType) {
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

    public List<Rectangle> toRectangles() {
        List<Rectangle> hitboxes = new ArrayList<>();
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

        return hitboxes;
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
        return tags.contains(tag);
    }

    public boolean hasTags() {
        return tags.isEmpty();
    }

    public String[] getTags() {
        String[] tagsArray = new String[tags.size()];
        int i = 0;
        for (String string : tags) {
            tagsArray[i] = string;
            i++;
        }
        return tagsArray;
    }

    public void addTag(String tag) {
        String[] tags = getTags();
        List<String> newTags = new ArrayList<>();
        for (String string : tags) {
            newTags.add(string);
        }
        newTags.add(tag);
        tags = new String[newTags.size()];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = newTags.get(i);
        }
        setTags(tags);
    }

    public void removeTag(String tag) {
        String[] tags = getTags();
        String[] newTags = new String[tags.length - 1];
        int n = 0;
        for (int i = 0; i < tags.length; i++) {
            if (!tags[i].equals(tag)) {
                newTags[n] = tags[i];
                n++;
            }
        }
        setTags(newTags);
    }

    public Tile copy() {
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
            Tile powerFruit = game.getTileAt(x, y);
            if (powerFruit.hasTag("powerup")) {
                arbDat = new Object[2];
                ((Object[]) arbDat)[0] = powerFruit;
                ((Object[]) arbDat)[1] = 120;
            }
        }
    }

    public void update(Level game) {
        if (hasTag("enemy")) {
            for (Enemy enemy : game.getEnemies()) {
                if (enemy.getParent().equals(this)) return;
            }

            if (game.player.isFresh()) {
                game.addEnemy(Enemy.createEnemy(x, y, game.player, this, tileType));
            } else {
                Vector2 tileCenter = new Vector2(x * 64 + 32, y * 64 + 32);
                float playerDist = game.player.getCenter().dst2(tileCenter);
                if (playerDist > 1112 * 1112) {
                    game.addEnemy(Enemy.createEnemy(x, y, game.player, this, tileType));
                }
            }
        }

        if (hasTag("respawn_power_fruit")) {
            if (arbDat != null) {
                Object[] data = (Object[]) arbDat;
                if (game.getTileAt(x, y) == null) {
                    int timer = (Integer) data[1];
                    if (timer <= 0) {
                        Tile powerFruit = ((Tile) data[0]).copy();
                        if (!toRectangle().overlaps(game.player.toRectangle())) {
                            game.addTileToMap(powerFruit);
                            timer = 90;
                        }
                    }
                    timer--;
                    data[1] = timer;
                }
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tile) {
            Tile other = (Tile) o;
            if (imagesEqual(this, other)) {
                if (ignoreTiling) {
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
        return image.hashCode() + (ignoreTiling ? tileType * tileType * tileType : 0) + x * x + y;
    }
}