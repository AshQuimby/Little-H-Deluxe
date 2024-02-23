package com.sab.littleh.util;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
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
        String imageKey = "images/" + key;
        if (cache.containsKey(imageKey)) {
            return cache.get(imageKey);
        }
        Texture image;

        FileHandle handle = Gdx.files.internal(imageKey);
        if (!handle.exists()) return getImage("missing.png");
        try {
            image = new Texture(handle);
            // If the image is a tile add procedural padding so that way the .png still looks nice
            if (key.contains("tiles/")) {
                // Fetch the data of the original image
                TextureData data = image.getTextureData();
                data.prepare();
                // Get a pixmap of the original
                Pixmap originalImage = data.consumePixmap();
                // Set up a pixmap to add the buffer to
                Pixmap paddedPixmap = new Pixmap(40, 40, Pixmap.Format.RGBA8888);
                // Go through every frame on the 4x4 tilesheet and copy over each frame (this is why every tile has to be on a 16x16 image)
                for (int i = 0; i < 16; i++) {
                    // Get the frameX and frameY from i in the same way the game would based on tileType
                    int frameX = i % 4;
                    int frameY = i / 4;
                    // Actually put the padding on the simulated image
                    // Pad the top left corner
                    paddedPixmap.drawPixmap(originalImage, frameX * 10, frameY * 10, frameX * 8, frameY * 8, 1, 1);
                    // Pad the top right corner
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 9, frameY * 10, frameX * 8 + 7, frameY * 8, 1, 1);
                    // Pad the bottom left corner
                    paddedPixmap.drawPixmap(originalImage, frameX * 10, frameY * 10 + 9, frameX * 8, frameY * 8 + 7, 1, 1);
                    // Pad the bottom right corner
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 9, frameY * 10 + 9, frameX * 8 + 7, frameY * 8 + 7, 1, 1);
                    // Pad the left
                    paddedPixmap.drawPixmap(originalImage, frameX * 10, frameY * 10 + 1, frameX * 8, frameY * 8, 1, 8);
                    // Pad the right
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 9, frameY * 10 + 1, frameX * 8 + 7, frameY * 8, 1, 8);
                    // Pad the top
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 1, frameY * 10, frameX * 8, frameY * 8, 8, 1);
                    // Pad the bottom
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 1, frameY * 10 + 9, frameX * 8, frameY * 8 + 7, 8, 1);
                    // Add the original frame to the center of the new one
                    paddedPixmap.drawPixmap(originalImage, frameX * 10 + 1, frameY * 10 + 1, frameX * 8, frameY * 8, 8, 8);
                }
                image.dispose();
                image = new Texture(paddedPixmap);
            }
        } catch (Exception e) {
            return getImage("missing.png");
        }

        cache.put(imageKey, image);
        return image;
    }

    public static Color getHColor() {
        return hColor;
    }

    public static float getHHue() {
        if (Settings.localSettings.hColor.value == -1) {
            return LittleH.getTick() % 360;
        } else {
            return Settings.localSettings.hColor.asRelativeFloat() * 360f;
        }
    }

    public static Color getHColor(float hueShift, float addSat, float addValue) {
        if (Settings.localSettings.hColor.value == -1) {
            return new Color(1f, 1f, 1f, 1f).fromHsv(LittleH.getTick() % 360 + hueShift, 1f + addSat, 1f + addValue);
        } else {
            return new Color(1f, 1f, 1f, 1f).fromHsv(Settings.localSettings.hColor.asRelativeFloat() * 360f + hueShift, 1f + addSat, 1f + addValue);
        }
    }

    public static void cacheHColor() {
        if (Settings.localSettings.hColor.value == -1) {
            hColor = getRainbowColor();
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

    public static Color getRainbowColor() {
        return new Color(1f, 1f, 1f, 1f).fromHsv(LittleH.getTick() % 360, 1f, 1f);
    }
}