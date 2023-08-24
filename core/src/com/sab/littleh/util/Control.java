package com.sab.littleh.util;

import com.badlogic.gdx.Input.Keys;

public class Control {
    public static final Control UP = new Control(Keys.UP, Keys.W);
    public static final Control DOWN = new Control(Keys.DOWN, Keys.S);
    public static final Control LEFT = new Control(Keys.LEFT, Keys.A);
    public static final Control RIGHT = new Control(Keys.RIGHT, Keys.D);
    public static final Control JUMP = new Control(Keys.SPACE);
    public static final Control[] controls = {
            UP,
            DOWN,
            LEFT,
            RIGHT,
            JUMP
    };
    private int[] validKeys;
    public Control(int... keycodes) {
        this.validKeys = keycodes;
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
