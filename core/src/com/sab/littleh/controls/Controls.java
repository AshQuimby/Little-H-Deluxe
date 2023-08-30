package com.sab.littleh.controls;

import static com.badlogic.gdx.Input.Keys.*;
import com.badlogic.gdx.Input;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.sab_format.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controls {
    public static final Map<String, Control> controlMap = new LinkedHashMap<>();
    public static final Control UP = new Control("Up", false, Input.Keys.UP, Input.Keys.W);
    public static final Control DOWN = new Control("Down", false, Input.Keys.DOWN, Input.Keys.S);
    public static final Control LEFT = new Control("Left", false ,Input.Keys.LEFT, Input.Keys.A);
    public static final Control RIGHT = new Control("Right", false, Input.Keys.RIGHT, Input.Keys.D);
    public static final Control JUMP = new Control("Jump", false, Input.Keys.SPACE);

    public static void load() {
        // Set defaults
        resetControls();

        File settingsFile = new File((Images.inArchive ? "controls.sab" : "../controls.sab"));
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Program does not have sufficient permissions to create settings file.");
            }
        }
        try {
            SabData data = SabReader.read(settingsFile);
            setFrom(data);
        } catch (SabParsingException e) {
            e.printStackTrace();
        }
        save();
    }

    private static void setFrom(SabData data) {
        for (String key : data.getValues().keySet()) {
            SabValue[] values = data.getValue(key).asArray();
            int[] keys = new int[values.length];
            for (int i = 0; i < values.length; i++) keys[i] = values[i].asInt();
            controlMap.get(key).replaceKeys(keys);
        }
    }

    public static void save() {
        try {
            SabWriter.write(new File((Images.inArchive ? "controls.sab" : "../controls.sab")), toSabData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SabData toSabData() {
        SabData data = new SabData();
        for (String key : controlMap.keySet()) {
            data.insertValue(key, controlMap.get(key).valueArray());
        }
        return data;
    }

    public static Collection<Control> getControls() {
        return controlMap.values();
    }

    public static void resetControls() {
        controlMap.clear();
        UP.replaceKeys(Input.Keys.UP, W);
        DOWN.replaceKeys(Input.Keys.DOWN, S);
        LEFT.replaceKeys(Input.Keys.LEFT, A);
        RIGHT.replaceKeys(Input.Keys.RIGHT, D);
        JUMP.replaceKeys(SPACE);
        controlMap.put("up", UP);
        controlMap.put("down", DOWN);
        controlMap.put("left", LEFT);
        controlMap.put("right", RIGHT);
        controlMap.put("jump", JUMP);
        controlMap.put("suicide", new Control("To Checkpoint", false, K));
        controlMap.put("quick_restart", new Control("Quick Reset", false, R));
        controlMap.put("return", new Control("Return/Back", false, ESCAPE));
        controlMap.put("select", new Control("Select/Accept", false, ENTER));
        controlMap.put("pencil", new Control("Pencil", false, NUM_1, R));
        controlMap.put("eraser", new Control("Eraser", false, NUM_2, X));
        controlMap.put("pen", new Control("Pen", false, NUM_3, G));
        controlMap.put("fill", new Control("Fill", false, NUM_4, F));
        controlMap.put("eyedropper", new Control("Tile Picker", false, NUM_5, C));
        controlMap.put("selection", new Control("Selection", false, NUM_6, V));
        controlMap.put("quick_test", new Control("Quick Test", false, NUM_7, T));
        controlMap.put("playtest", new Control("Quick Test", false, ENTER));
        controlMap.put("toggle_layer", new Control("Toggle Edit Background", false, TAB));
        controlMap.put("prev_selection", new Control("Previous Tile Menu", false, Q));
        controlMap.put("next_selection", new Control("Next Tile Menu", false, E));
        controlMap.put("drag_camera", new Control("Drag Camera", false, SHIFT_LEFT));
        controlMap.put("move_selection", new Control("Move Selection", false, ALT_LEFT));
        controlMap.put("drag_selection", new Control("Drag Selection", false, CONTROL_LEFT));
        controlMap.put("end_selection", new Control("End Selection", true, D));
        controlMap.put("delete_selection", new Control("Delete Selection", false, BACKSPACE));
        controlMap.put("copy", new Control("Copy", true, C));
        controlMap.put("cut", new Control("Cut", true, X));
        controlMap.put("paste", new Control("Paste", true, V));
        controlMap.put("save", new Control("Save Level", true, S));
        controlMap.put("undo", new Control("Undo", true, Z));
        controlMap.put("redo", new Control("Redo", true, Y));
    }

    public static Control get(String key) {
        return controlMap.get(key);
    }
}
