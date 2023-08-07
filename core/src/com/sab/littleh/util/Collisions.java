package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;

public class Collisions {
    public static boolean resolveY(Rectangle a, Rectangle b, float dy) {
        if (a.overlaps(b)) {
            if (dy > 0) {
                a.y = b.y - a.height;
            } else {
                a.y = b.y + b.height;
            }
            return true;
        }
        return false;
    }

    public static boolean resolveX(Rectangle a, Rectangle b, float dX) {
        if (a.overlaps(b)) {
            if (dX > 0) {
                a.x = b.x - a.width;
            } else {
                a.x = b.x + b.width;
            }
            return true;
        }
        return false;
    }
}
