package com.sab.littleh.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.HashMap;
import java.util.Map;

public class ControlInput {
    public static final ControlInput localControls = new ControlInput();
    private final Map<Control, Boolean> pressed = new HashMap<>();
    private final Map<Control, Boolean> justPressed = new HashMap<>();
    private final Map<Control, Boolean> justReleased = new HashMap<>();
    private final Map<Control, Integer> pressedFor = new HashMap<>();

    public void press(int keycode) {
        for (Control input : Controls.getControls()) {
            if (input.containsKey(keycode) && (!input.isCommand() || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))) {
                pressed.put(input, true);
                justPressed.put(input, true);
            }
        }
    }

    public void release(int keycode) {
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

    public boolean isPressed(String key) {
        return isPressed(Controls.get(key));
    }

    public boolean isPressed(Control input) {
        if (!pressed.containsKey(input)) return false;
        return pressed.get(input);
    }

    public boolean isJustPressed(String key) {
        return isJustPressed(Controls.get(key));
    }

    public boolean isJustPressed(Control input) {
        if (!justPressed.containsKey(input)) return false;
        return justPressed.get(input);
    }

    public boolean isJustReleased(String key) {
        return isJustReleased(Controls.get(key));
    }

    public boolean isJustReleased(Control input) {
        if (!justReleased .containsKey(input)) return false;
        return justReleased.get(input);
    }

    public int getPressedFor(String key) {
        return getPressedFor(Controls.get(key));
    }

    public int getPressedFor(Control input) {
        if (!pressedFor .containsKey(input)) return 0;
        return pressedFor.get(input);
    }

    public void update() {
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

    public void pressControl(Control input) {
        if (pressed.get(input) == null || !pressed.get(input)) {
            pressed.put(input, true);
            justPressed.put(input, true);
        }
    }

    public void releaseControl(Control input) {
        if (pressed.get(input) == null || pressed.get(input)) {
            pressed.put(input, false);
            justReleased.put(input, true);
        }
    }
}