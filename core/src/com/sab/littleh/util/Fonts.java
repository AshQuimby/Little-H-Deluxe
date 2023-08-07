package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

public class Fonts {
    private static final Map<String, BitmapFont> cache = new HashMap<>();
    private static final GlyphLayout fontMeasurement = new GlyphLayout();

    public static boolean loadFont(String path, int size) {
        path = "assets/fonts/" + path;
        if (!cache.containsKey(path)) {
            FileHandle f = Gdx.files.internal(path);
            if (f != null) {
                FreeTypeFontGenerator font = new FreeTypeFontGenerator(f);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = size;
                cache.put(f.name().substring(0, f.name().length() - 4), font.generateFont(parameter));
                return true;
            }
        }
        return false;
    }

    public static boolean loadFont(String path, int size, Color borderColor, int borderWidth) {
        path = "assets/fonts/" + path;
        if (!cache.containsKey(path)) {
            FileHandle f = Gdx.files.internal(path);
            if (f != null) {
                FreeTypeFontGenerator font = new FreeTypeFontGenerator(f);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = size;
                parameter.color = borderColor;
                parameter.borderWidth = borderWidth;
                cache.put(f.name().substring(0, f.name().length() - 4), font.generateFont(parameter));
                return true;
            }
        }
        return false;
    }

    public static BitmapFont getFont(String key) {
        return cache.get(key);
    }

    public static Rectangle getStringBounds(String text, BitmapFont font, float x, float y, float size, int anchor) {
        font.getData().setScale(size);
        fontMeasurement.setText(font, text);
        Rectangle bounds = new Rectangle(x, y, fontMeasurement.width, fontMeasurement.height);
        bounds.setCenter(new Vector2(x, y + bounds.height / 2));
        bounds.y -= bounds.height;
        if (anchor == 1) {
            bounds.x -= bounds.width / 2;
        } else if (anchor == -1) {
            bounds.x += bounds.width / 2;
        }
        return bounds;
    }

    public static Rectangle getMultiStringBounds(String[] textArray, BitmapFont font, float x, float y, float size, int anchor) {
        Rectangle rect = new Rectangle(2147483647, 2147483647, 0, 0);
        for (String string : textArray) {
            Rectangle textRect = getStringBounds(string, font, x, y, size, anchor);
            if (rect.y == 2147483647) rect.y = textRect.y;
            if (textRect.x < rect.x) rect.x = textRect.x;
            if (textRect.width > rect.width) rect.width = textRect.width;
            rect.height += textRect.height + 4;
            rect.y -= textRect.height - 4;
        }
        return rect;
    }

    public static float getStringWidth(String text, BitmapFont font, float size) {
        font.getData().setScale(size);
        fontMeasurement.setText(font, text);
        return fontMeasurement.width;
    }

    public static float chainedGetStringWidth(String text, BitmapFont font, float size) {
        fontMeasurement.setText(font, text);
        return fontMeasurement.width;
    }
}
