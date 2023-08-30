package com.sab.littleh.controls;

import com.badlogic.gdx.Input.Keys;
import com.sab.littleh.util.sab_format.SabValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Control {
    private int[] validKeys;
    private String controlName;
    private boolean command;
    public Control(String controlName, boolean isCommand, int... defaultKeycodes) {
        this.validKeys = defaultKeycodes;
        this.command = isCommand;
        this.controlName = controlName;
    }

    public boolean containsKey(int keycode) {
        for (int i : validKeys) {
            if (i == keycode)
                return true;
        }
        return false;
    }

    public String getName() {
        return controlName + (command ? ": Ctrl + " : ": ");
    }

    public boolean isCommand() {
        return command;
    }

    public void replaceKeys(int... keycodes) {
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

    public String valueArray() {
        return Arrays.toString(validKeys);
    }

    public int[] getInputs() {
        return validKeys;
    }
}
