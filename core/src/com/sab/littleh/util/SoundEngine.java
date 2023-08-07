package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.sab.littleh.settings.Settings;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioSystem.getClip;

public class SoundEngine {
    private static HashMap<String, Sound> soundCache = new HashMap<>();
    private static HashMap<String, Music> musicCache = new HashMap<>();
    public static Music currentMusic = null;

    public static void load() {
        resetCurrentMusicVolume();
    }

    public static float getTotalSfxVolume() {
        if (Settings.localSettings.muteGame.value)
            return 0f;
        return Settings.localSettings.sfxVolume.value * Settings.localSettings.masterVolume.value / 10000f;
    }

    public static float getTotalMusicVolume() {
        if (Settings.localSettings.muteGame.value)
            return 0f;
        return Settings.localSettings.musicVolume.value * Settings.localSettings.masterVolume.value / 10000f;
    }

    public static void playSound(String filePath) {
        String playFilePath = "assets/sounds/effects/" + filePath;
        if (getTotalSfxVolume() == 0f) return;
        if (soundCache.containsKey(playFilePath)) {
            soundCache.get(playFilePath).play(getTotalSfxVolume());
        } else {
            FileHandle handle = Gdx.files.internal(playFilePath);
            soundCache.put(playFilePath, Gdx.audio.newSound(handle));
            playSound(filePath);
        }
    }

    public static void playMusic(String filePath) {
        String playFilePath = "assets/sounds/music/" + filePath;
        if (musicCache.containsKey(playFilePath)) {
            stopMusic();
            currentMusic = musicCache.get(playFilePath);
            currentMusic.play();
            currentMusic.setVolume(getTotalMusicVolume());
            currentMusic.setLooping(true);
        } else {
            FileHandle handle = Gdx.files.internal(playFilePath);
            musicCache.put(playFilePath, Gdx.audio.newMusic(handle));
            playMusic(filePath);
        }
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public static boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    public static void resetCurrentMusicVolume() {
        if (isMusicPlaying())
            currentMusic.setVolume(getTotalMusicVolume());
    }
}