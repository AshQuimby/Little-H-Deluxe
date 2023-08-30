package com.sab.littleh.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class ControlInputs {
    private static final Map<Control, Boolean> pressed = new HashMap<>();
    private static final Map<Control, Boolean> justPressed = new HashMap<>();
    private static final Map<Control, Boolean> justReleased = new HashMap<>();
    private static final Map<Control, Integer> pressedFor = new HashMap<>();

    public static void press(int keycode) {
        for (Control input : Controls.getControls()) {
            if (input.containsKey(keycode) && (!input.isCommand() || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))) {
                pressed.put(input, true);
                justPressed.put(input, true);
            }
        }
    }

    public static void release(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT) {
            for (Control input : Controls.getControls()) {
                if (input.isCommand()) {
                    pressed.put(input, false);
                    justReleased.put(input, true);
                }
            }
        }
        for (Control input : Controls.getControls()) {
            if (input.containsKey(keycode)) {
                pressed.put(input, false);
                justReleased.put(input, true);
            }
        }
    }

    public static boolean isPressed(String key) {
        return isPressed(Controls.get(key));
    }

    public static boolean isPressed(Control input) {
        if (!pressed.containsKey(input)) return false;
        return pressed.get(input);
    }

    public static boolean isJustPressed(String key) {
        return isJustPressed(Controls.get(key));
    }

    public static boolean isJustPressed(Control input) {
        if (!justPressed.containsKey(input)) return false;
        return justPressed.get(input);
    }

    public static boolean isJustReleased(String key) {
        return isJustReleased(Controls.get(key));
    }

    public static boolean isJustReleased(Control input) {
        if (!justReleased .containsKey(input)) return false;
        return justReleased.get(input);
    }

    public static int getPressedFor(String key) {
        return getPressedFor(Controls.get(key));
    }

    public static int getPressedFor(Control input) {
        if (!pressedFor .containsKey(input)) return 0;
        return pressedFor.get(input);
    }

    public static void update() {
        justPressed.replaceAll((k, v) -> false);
        justReleased.replaceAll((k, v) -> false);
        for (Control key : pressed.keySet()) {
            if (pressed.get(key)) {
                if (pressedFor.containsKey(key)) {
                    pressedFor.put(key, pressedFor.get(key) + 1);
                } else {
                    pressedFor.put(key, 1);
                }
            } else {
                pressedFor.put(key, 0);
            }
        }
    }

    public static void pressControl(Control input) {
        if (pressed.get(input) == null || !pressed.get(input)) {
            pressed.put(input, true);
            justPressed.put(input, true);
        }
    }

    public static void releaseControl(Control input) {
        if (pressed.get(input) == null || pressed.get(input)) {
            pressed.put(input, false);
            justReleased.put(input, true);
        }
    }
}