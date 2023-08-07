package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.util.HashMap;

public class Images {
    public static boolean inArchive;
    public static HashMap<String, Texture> cache = new HashMap<>();

    public static void load() {
        inArchive = Images.class.getResourceAsStream("Images.class").toString().contains("jar");
    }

    public static Texture getImage(String key) {
        key = "assets/images/" + key;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Texture image;

        FileHandle handle = Gdx.files.internal(key);
        if (!handle.exists()) return null;
        image = new Texture(handle);

        cache.put(key, image);
        return image;
    }

    public static void updateCache() {
        cache.clear();
    }

    public static void drawImage(Graphics g, Texture image, Rectangle drawTo, Rectangle drawFrom) {
         g.draw(image, drawTo.x, drawTo.y, drawTo.width, drawTo.height, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
        //         g.drawImage(image, drawTo.x, drawTo.y, drawTo.x + drawTo.width, drawTo.y + drawTo.height, drawFrom.x, drawFrom.y, drawFrom.x + drawFrom.width, drawFrom.y + drawFrom.height, TileEditor.window);
    }

    public static void drawImage(Graphics g, Texture image, Rectangle drawTo, Rectangle drawFrom, float rotation) {
        Vector2 origin = new Vector2(drawTo.width / 2, drawTo.height / 2);
         g.draw(image, drawTo.x, drawTo.y, origin.x, origin.y, drawTo.width, drawTo.height, 1, 1, rotation, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
        //         g.drawImage(image, drawTo.x, drawTo.y, drawTo.x + drawTo.width, drawTo.y + drawTo.height, drawFrom.x, drawFrom.y, drawFrom.x + drawFrom.width, drawFrom.y + drawFrom.height, TileEditor.window);
    }
}