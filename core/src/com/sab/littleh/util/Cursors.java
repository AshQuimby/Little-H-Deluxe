package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.HashMap;

public class Cursors {
    private static final String[] cursors = {
            "none",
            "color_picker",
            "cursor",
            "eraser",
            "h",
            "magnifier",
            "paint_can",
            "pen",
            "pencil"
    };
    private static final HashMap<String, Cursor> cache = new HashMap<>();

    public static void loadCursors() {
        for (String key : cursors) {
            String filePath = "assets/images/ui/cursor/" + key + ".png";
            cache.put(key, Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal(filePath)), 0, 0));
        }
    }

    public static void switchCursor(String identifier) {
        Gdx.graphics.setCursor(cache.get(identifier));
    }
}
