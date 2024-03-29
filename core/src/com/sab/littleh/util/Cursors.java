package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.HashMap;

public class Cursors {
    private static String currentCursorKey;
    private static final String[] cursors = {
            "color_picker",
            "cursor",
            "eraser",
            "h",
            "none",
            "magnifier",
            "paint_can",
            "pen",
            "pencil",
            "move_arrow",
            "drag_hand",
            "line"
    };
    private static final HashMap<String, Cursor> cache = new HashMap<>();

    public static void loadCursors() {
        for (String key : cursors) {
            String filePath = "images/ui/cursor/" + key + ".png";
            cache.put(key, Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal(filePath)), 0, 0));
        }
        cache.put("reticle", Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("images/ui/cursor/reticle.png")), 22, 22));
    }

    public static void switchCursor(String identifier) {
        currentCursorKey = identifier;
        Gdx.graphics.setCursor(cache.get(identifier));
    }

    public static boolean cursorIsNot(String... keys) {
        for (String string : keys) {
            if (string.equals(currentCursorKey)) return false;
        }
        return true;
    }

    public static boolean cursorIs(String... keys) {
        for (String string : keys) {
            if (string.equals(currentCursorKey)) return true;
        }
        return false;
    }
}
