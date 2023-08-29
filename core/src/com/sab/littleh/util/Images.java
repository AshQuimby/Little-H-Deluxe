package com.sab.littleh.util;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.settings.Settings;

import java.io.File;
import java.util.HashMap;

public class Images {
    public static boolean inArchive;
    public static HashMap<String, Texture> cache = new HashMap<>();
    private static Color hColor = new Color(1f, 0f, 0f, 1f);

    static {
        inArchive = Images.class.getResourceAsStream("Images.class").toString().contains("jar");
    }

    public static Texture getImage(String key) {
        key = "images/" + key;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Texture image;

        FileHandle handle = Gdx.files.internal(key);
        if (!handle.exists()) return getImage("missing.png");
        image = new Texture(handle);

        cache.put(key, image);
        return image;
    }

    public static Color getHColor() {
        return hColor;
    }

    public static Color getHColor(float hueShift) {
        return new Color(1f, 1f, 1f, 1f).fromHsv(Settings.localSettings.hColor.asRelativeFloat() * 360f + hueShift, 1f, 1f);
    }

    public static void cacheHColor() {
        if (Settings.localSettings.hColor.value == -1) {
            hColor = new Color(1f, 1f, 1f, 1f).fromHsv(LittleH.getTick() % 360, 1f, 1f);
        } else {
            hColor = new Color(1f, 1f, 1f, 1f).fromHsv(Settings.localSettings.hColor.asRelativeFloat() * 360f, 1f, 1f);
        }
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

    public static Color quickAlpha(float a) {
        return new Color(1f, 1f, 1f, a);
    }

    public static void clearCache() {
        cache.clear();
    }
}