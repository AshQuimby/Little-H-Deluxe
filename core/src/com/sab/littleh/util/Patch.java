package com.sab.littleh.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Patch {
    public static final Map<String, Patch> cache = new HashMap<>();
    public final String imagePath;
    private final int imageWidth, imageHeight, x1, y1;

    public Patch(String imagePath, int imageWidth, int imageHeight, int x1, int y1) {
        this.imagePath = imagePath;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.x1 = x1;
        this.y1 = y1;
    }

    public static Patch get(String key) {
        return cache.get(key);
    }

    public static void cachePatch(String key, Patch patch) {
        cache.put(key, patch);
    }

    public static void cacheButtonPatch(String key, String imageId) {
        Patch.cachePatch(key, new Patch(imageId + ".png", 7, 7, 3, 3));
        Patch.cachePatch(key + "_hovered", new Patch(imageId + "_hovered.png", 7, 7, 3, 3));
        Patch.cachePatch(key + "_pressed", new Patch(imageId + "_pressed.png", 7, 7, 3, 3));
        Patch.cachePatch(key + "_disabled", new Patch(imageId + "_disabled.png", 7, 7, 3, 3));
    }

    public void render(Graphics g, int patchScale, float x, float y, float width, float height) {
        render( g, patchScale, new Rectangle((int) x, (int) y, (int) width, (int) height));
    }

    public void render(Graphics g, int patchScale, Rectangle rect) {
        Texture image = Images.getImage(imagePath);
        Vector2 pos = new Vector2(rect.x, rect.y);
        int w = x1 * patchScale;
        int h = y1 * patchScale;
//        Images.drawImage( g, image, new Rectangle(0, 0, 64, 64),
//                new Rectangle(0, 0, x1, y1));
        // Top left corner
         g.drawImage(image, new Rectangle(pos.x, pos.y + rect.height - h, w, h),
                new Rectangle(0, 0, x1, y1));
        // Bottom left corner
        g.drawImage(image, new Rectangle(pos.x, pos.y, w, h),
                new Rectangle(0, imageHeight - y1, x1, y1));
        // Top right corner
        g.drawImage(image, new Rectangle(pos.x + rect.width - w, pos.y + rect.height - h, w, h),
                new Rectangle(imageWidth - x1, 0, x1, y1));
        // Bottom right corner
        g.drawImage(image, new Rectangle(pos.x + rect.width - w, pos.y, w, h),
                new Rectangle(imageWidth - x1, imageHeight - y1, x1, y1));
        // Left side
        g.drawImage(image, new Rectangle(pos.x, pos.y + h, w, rect.height - h * 2),
                new Rectangle(0, y1, x1, imageHeight - y1 * 2));
        // Top side
        g.drawImage(image, new Rectangle(pos.x + w, pos.y + rect.height - h, rect.width - w * 2, h),
                new Rectangle(x1, 0, imageWidth - x1 * 2, y1));
        // Middle
        g.drawImage(image, new Rectangle(pos.x + w, pos.y + h, rect.width - w * 2, rect.height - h * 2),
                new Rectangle(x1, y1, imageWidth - x1 * 2, imageHeight - y1 * 2));
        // Right side
        g.drawImage(image, new Rectangle(pos.x + rect.width - w, pos.y + h, w, rect.height - h * 2),
                new Rectangle(imageWidth - x1, y1, x1, imageHeight - y1 * 2));
        // Bottom side
        g.drawImage(image, new Rectangle(pos.x + w, pos.y, rect.width - w * 2, h),
                new Rectangle(x1, imageHeight - y1, imageWidth - x1 * 2, y1));
    }
}