package com.sab.littleh.controls;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class ControlTypingBox extends TypingBox {
    private final Control control;
    private final List<Integer> keyCodes;

    public ControlTypingBox(Control control, Rectangle box) {
        super(control.getName(), "", box);
        this.control = control;
        keyCodes = new ArrayList<>();
        for (int i : control.getInputs())
            keyCodes.add(i);
        headerPosition = getQuery().length();
    }

    @Override
    public void mouseClicked() {
        focused = rectangle.contains(MouseUtil.getMousePosition());
    }

    @Override
    public void updateQueryKey(int keycode, int max, boolean enterIsNewline) {
        if (isFocused()) {
            if (keycode == Input.Keys.BACKSPACE && keyCodes.size() > 0) {
                keyCodes.remove(keyCodes.size() - 1);
                headerPosition = getQuery().length();
            } else {
                if (!keyCodes.contains(keycode) && keyCodes.size() < max) {
                    keyCodes.add(keycode);
                    headerPosition = getQuery().length();
                }
            }
        }
    }

    @Override
    public String getQuery() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < keyCodes.size(); i++) {
            b.append(Input.Keys.toString(keyCodes.get(i)));
            if (i == keyCodes.size() - 1)
                return b.toString();
            b.append(", ");
        }
        return b.toString();
    }

    public void save() {
        control.replaceKeys(getInputs());
    }

    @Override
    public void updateQueryChar(char character, int max) {
    }

    public int[] getInputs() {
        int[] keys = new int[keyCodes.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = keyCodes.get(i);
        }
        return keys;
    }
}
