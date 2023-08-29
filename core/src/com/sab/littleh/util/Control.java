package com.sab.littleh.util;

import com.badlogic.gdx.Input.Keys;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Control {
    public static final Map<String, Control> controlMap = new HashMap<>();
    public static final Control UP = new Control(Keys.UP, Keys.W);
    public static final Control DOWN = new Control(Keys.DOWN, Keys.S);
    public static final Control LEFT = new Control(Keys.LEFT, Keys.A);
    public static final Control RIGHT = new Control(Keys.RIGHT, Keys.D);
    public static final Control JUMP = new Control(Keys.SPACE);
    private int[] validKeys;
    public Control(int... keycodes) {
        this.validKeys = keycodes;
    }

    public static void load() {
        controlMap.put("up", UP);
        controlMap.put("down", DOWN);
        controlMap.put("left", LEFT);
        controlMap.put("right", RIGHT);
        controlMap.put("jump", JUMP);
    }

    public static Collection<Control> getControls() {
        return controlMap.values();
    }

    public boolean containsKey(int keycode) {
        for (int i : validKeys) {
            if (i == keycode)
                return true;
        }
        return false;
    }

    public void replaceKeys(int[] keycodes) {
        this.validKeys = keycodes;
    }

    public boolean sharesKey(Control control) {
        for (int i : validKeys) {
            if (control.containsKey(i))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
