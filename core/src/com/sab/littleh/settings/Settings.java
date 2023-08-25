package com.sab.littleh.settings;

import com.badlogic.gdx.math.RandomXS128;
import com.sab.littleh.util.SoundEngine;
import com.sab.littleh.util.sab_format.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Settings {
    public static final Settings localSettings = new Settings();
    
    // Gameplay
    public final BoolSetting debugMode = new BoolSetting("debug_mode", "Developer Mode", false);
    public final BoolSetting backgroundVisibility = new BoolSetting("background_visibility", "Default Background Visibility", true);
    public final StringSetting authorName = new StringSetting("author_name", "Change Author Name", getRandomName());
    public final PercentageSetting hColor = new PercentageSetting("h_color", "Player Color", 0, -1, 360, " hue");

    // Video
    public final ListSetting fullscreen = new ListSetting("fullscreen", "Default Display Mode", 1, new String[] {
            "windowed",
            "windowed_fullscreen",
            "fullscreen"
    }, new String[] {
            "Windowed",
            "Windowed Fullscreen",
            "Fullscreen"
    });
    public final BoolSetting screenShake = new BoolSetting("screen_shake", "Screen Shake", true);
    public final BoolSetting rainbowTitle = new BoolSetting("rainbow_title", "Rainbow Title", false);
    public final PercentageSetting zoomScalar = new PercentageSetting("zoom_scalar", "In-Game Zoom Multiplier", 100, 50, 200);
    public final FontSetting font = new FontSetting("font", "Default Font", 0, new String[] {
            "minecraft",
            "sab_font",
            "shitfont23",
            "arial",
            "comic_snas"
    }, new String[] {
            "Minecraft",
            "SAB Font",
            "shitfont23",
            "Arial",
            "Comic Sans"
    }, new float[] {
            0.3f,
            0.2f,
            0.2f,
            0.36f,
            0.3f
    });

    // Audio
    public final BoolSetting muteGame = new BoolSetting("mute_game", "Mute Game", false) {
        @Override
        public void next() {
            super.next();
            SoundEngine.resetCurrentMusicVolume();
        }
    };
    public final PercentageSetting masterVolume = new PercentageSetting("master_volume", "Master Volume", 50);
    public final PercentageSetting musicVolume = new PercentageSetting("music_volume", "Music Volume", 100);
    public final PercentageSetting sfxVolume = new PercentageSetting("sfx_volume", "SFX Volume", 100);

    public final Setting<?>[] settings = new Setting[] {
            // Gameplay
            debugMode, backgroundVisibility, authorName, hColor, rainbowTitle,
            // Video
            fullscreen, screenShake, zoomScalar, font,
            // Audio
            muteGame, masterVolume, musicVolume, sfxVolume
    };

    public static String getRandomName() {
        Random random = new RandomXS128();
        String[] prefixes = new String[]{ "The Big", "The Little", "The Medium", "The Colossal", "The Microscopic", "The Minuscule" };
        String[] bases = new String[]{ "EnemyA", "B", "C", "D", "EnemyE", "EnemyF", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        String[] suffixes = new String[]{ "Fan", "Enjoyer", "Appreciator", "Stan", "Hater", "Critic", "Scorner", "Murderer", "Elater", "Skeptic", "Actualizer" };
        return prefixes[random.nextInt(prefixes.length)] + " " + bases[random.nextInt(bases.length)] + " " + suffixes[random.nextInt(suffixes.length)];
    }

    public void resetAll() {
        for (Setting<?> setting : settings) {
            setting.reset();
        }
    }

    public void save() {
        try {
            SabWriter.write(new File("../settings.sab"), toSabData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File settingsFile = new File("../settings.sab");
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Program does not have sufficient permissions to create settings file.");
            }
        }
        try {
            SabData data = SabReader.read(settingsFile);

            List<Setting<?>> unloaded = setFrom(data);
            for (Setting<?> setting : unloaded) {
                data.insertValue(setting.id, new SabValue(setting.asRawValue()));
            }

            if (unloaded.size() > 0) {
                save();
            }
        } catch (SabParsingException e) {
            e.printStackTrace();
            resetAll();
            save();
        }
    }

    private SabData toSabData() {
        SabData data = new SabData();
        for (Setting<?> setting : settings) {
            data.insertValue(setting.id, new SabValue(setting.asRawValue()));
        }
        return data;
    }

    private List<Setting<?>> setFrom(SabData data) {
        List<Setting<?>> unloaded = new ArrayList<>();

        for (Setting<?> setting : settings) {
            if (data.hasValue(setting.id) && setting.isValid(data.getValue(setting.id).getRawValue())) {
                setting.set(data.getValue(setting.id).getRawValue());
            } else {
                unloaded.add(setting);
            }
        }

        return unloaded;
    }
}
