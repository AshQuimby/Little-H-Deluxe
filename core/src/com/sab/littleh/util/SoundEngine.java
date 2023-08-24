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
    private static boolean musicQueued;
    private static boolean jukebox;
    private static boolean looping;

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
        try {
            if (getTotalSfxVolume() == 0f) return;
            String playFilePath = "assets/sounds/effects/" + filePath;
            if (soundCache.containsKey(playFilePath)) {
                soundCache.get(playFilePath).play(getTotalSfxVolume());
            } else {
                FileHandle handle = Gdx.files.internal(playFilePath);
                soundCache.put(playFilePath, Gdx.audio.newSound(handle));
                playSound(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playMusic(String filePath) {
        looping = true;
        try {
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
                stopMusic();
                currentMusic = musicCache.get(playFilePath);
                musicQueued = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jukebox = false;
    }

    public static void playJukeboxMusic(String filePath, boolean loops) {
        looping = loops;
        String playFilePath = "assets/sounds/music/" + filePath;
        if (SoundEngine.jukebox && currentMusic == musicCache.get(playFilePath)) {
            currentMusic.play();
        } else try {
            if (musicCache.containsKey(playFilePath)) {
                stopMusic();
                currentMusic = musicCache.get(playFilePath);
                currentMusic.play();
                currentMusic.setVolume(getTotalMusicVolume());
                currentMusic.setLooping(looping);
            } else {
                FileHandle handle = Gdx.files.internal(playFilePath);
                musicCache.put(playFilePath, Gdx.audio.newMusic(handle));
                stopMusic();
                currentMusic = musicCache.get(playFilePath);
                musicQueued = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jukebox = true;
    }


    public static void update() {
        if (musicQueued) {
            currentMusic.play();
            currentMusic.setVolume(getTotalMusicVolume());
            currentMusic.setLooping(looping);
            musicQueued = false;
        }
    }

    public static void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
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

    public static boolean isJukeboxPlaying() {
        return jukebox && isMusicPlaying();
    }

    public static void resetCurrentMusicVolume() {
        if (isMusicPlaying())
            currentMusic.setVolume(getTotalMusicVolume());
    }

    public static void setLooping(boolean looping) {
        currentMusic.setLooping(looping);
    }
}