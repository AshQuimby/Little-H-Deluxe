package com.sab.littleh.campaign;

import com.sab.littleh.LittleH;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabWriter;
import com.sab.littleh.util.sab_format.VirtualSabValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SaveFile {
    public static final HashMap<String, SabData> saveData = new HashMap<>();

    public static void load() {
        try {
            saveData.clear();
            validateFiles();
            saveGame();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to create save files.", e);
        }
    }

    private static void validateFiles() throws IOException {
        if (!LittleH.getFileResource("saves").exists()) {
            LittleH.getFileResource("saves").mkdir();
        }
        checkForFile("unlocked_levels", new VirtualSabValue("meadows", "true"), new VirtualSabValue("forest", "false"));
        checkForFile("clear_times");
    }

    private static void checkForFile(String key, VirtualSabValue... defaultValues) throws IOException {
        File file = LittleH.getFileResource(String.format("saves/%s.sab", key));
        SabData data;
        if (!file.exists()) {
            file.createNewFile();
            data = new SabData();
            for (VirtualSabValue defaultValue : defaultValues) {
                data.addVirtualValue(defaultValue);
            }
        } else {
            data = SabReader.read(file);
            for (VirtualSabValue defaultValue : defaultValues) {
                if (!data.hasValue(defaultValue.key)) {
                    data.addVirtualValue(defaultValue);
                }
            }
        }

        saveData.put(key, data);
    }

    public static void saveGame() {
        try {
            for (String key : saveData.keySet()) {
                File file = LittleH.getFileResource(String.format("saves/%s.sab", key));
                SabWriter.write(file, saveData.get(key));
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while attempting to save the game.");
        }
    }
}
