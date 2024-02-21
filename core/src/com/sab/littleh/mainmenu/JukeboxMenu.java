package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.settings.ListSetting;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabValue;

import java.util.*;

public class JukeboxMenu extends MainMenu {
    private static List<JukeboxSong> jukeboxSongs = new ArrayList<>();
    private static int oldSongIndex;
    private static int songIndex;
    private static int playing;
    private static int hFrame;
    private static int jukeboxFrame;
    private static float hFrameTimer;
    private static float jukeboxFrameTimer;
    private static float currentDanceRate;
    private static boolean looping = true;
    private List<MenuButton> buttons;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton loopButton;
    private ImageButton stopLoopButton;
    private ParallaxBackground background;
    private SettingButton settingButton;

    static {
        Collection<SabValue> values = SabReader.read(LevelLoader.class.getResourceAsStream("/sounds/music/manifest.sab")).getValues().values();
        for (SabValue value : values) {
            jukeboxSongs.add(new JukeboxSong(value.asArray()));
        }
    }


    public JukeboxMenu() {
        buttons = new ArrayList<>();
        String[] songNames = new String[jukeboxSongs.size()];
        int i = 0;
        for (JukeboxSong song : jukeboxSongs) {
            songNames[i] = song.name;
            i++;
        }
        settingButton = new SettingButton(new ListSetting("song", "Jukebox Song", playing, songNames, songNames), -512 / 2, -222, 512);
        buttons.add(settingButton);
        playButton = new ImageButton("square_button", "ui/jukebox/play.png",
                new Rectangle(MainMenu.relZeroX() + 16, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            playSong();
        });
        playButton.setHoverText("Play");
        pauseButton = playButton.quickCreate("ui/jukebox/pause.png", "Pause", () -> {
            pauseSong();
        });
        buttons.add(playButton);
        buttons.add(new ImageButton("square_button", "ui/jukebox/stop.png",
                new Rectangle(MainMenu.relZeroX() + 16 + 88, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            stopSong();
        }).setHoverText("Stop"));

        loopButton = new ImageButton("square_button", "ui/jukebox/loop.png",
                new Rectangle(MainMenu.relZeroX() + 16 + 88 * 2, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            toggleLooping();
        });
        loopButton.setHoverText("Loop");

        stopLoopButton = new ImageButton("square_button", "ui/jukebox/no_loop.png",
                new Rectangle(MainMenu.relZeroX() + 16 + 88 * 2, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            toggleLooping();
        });
        stopLoopButton.setHoverText("Stop Looping");

        buttons.add(stopLoopButton);

        buttons.add(new ImageButton("square_button", "ui/jukebox/building_song.png",
                new Rectangle(MainMenu.relZeroX() + 16 + 88 * 3, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            Settings.localSettings.buildingSong.set(jukeboxSongs.get(songIndex).path);
        }).setHoverText("Set as Building Song"));

        buttons.add(new ImageButton("square_button", "ui/jukebox/back_arrow.png",
                new Rectangle(MainMenu.relZeroX() + 16 + 88 * 4, MainMenu.relZeroY() + 16, 80, 88), 8, 16, 64, 64, () -> {
            LittleH.program.switchMenu(new LevelSelectMenu());
        }).setHoverText("Back"));

        changedSongIndex();
    }

    @Override
    public void start() {
        LittleH.program.dynamicCamera.reset();
        LittleH.program.dynamicCamera.targetPosition.y = 300;
        LittleH.setTitle(" | Listening to bangers");
    }

    public void changedSongIndex() {
        background = new ParallaxBackground(jukeboxSongs.get(songIndex).background);
        oldSongIndex = songIndex;
    }

    public void toggleLooping() {
        looping = !looping;
        SoundEngine.setLooping(looping);
        buttons.remove(3);
        if (!looping) {
            buttons.add(3, loopButton);
        } else {
            buttons.add(3, stopLoopButton);
        }
    }

    public void playSong() {
        if (playing != songIndex) {
            resetDancing();
        }
        jukeboxSongs.get(songIndex).play();
        playing = songIndex;
    }

    public void pauseSong() {
        SoundEngine.pauseMusic();
    }

    public void stopSong() {
        resetDancing();
        SoundEngine.stopMusic();
    }

    private void resetDancing() {
        hFrame = 0;
        hFrameTimer = 0;
        jukeboxFrame = 0;
        jukeboxFrameTimer = 0;
    }

    @Override
    public void update() {
        if (ControlInput.localControls.isJustPressed(Controls.RIGHT)) {
            SoundEngine.playSound("blip.ogg");
            settingButton.setting.next();
        }
        if (ControlInput.localControls.isJustPressed(Controls.LEFT)) {
            SoundEngine.playSound("blip.ogg");
            settingButton.setting.previous();
        }

        LittleH.program.dynamicCamera.targetPosition.x += SoundEngine.isJukeboxPlaying() ? jukeboxSongs.get(playing).tempo / 32f : 0;

        // Divide by 4, so it does one full loop for every 4 beats instead of every beat
        if (SoundEngine.isJukeboxPlaying()) {
            currentDanceRate = (60 / (14f / 8f)) / jukeboxSongs.get(playing).tempo * 60f;
            if (++hFrameTimer >= currentDanceRate) {
                hFrame++;
                hFrame = hFrame % 14;
                hFrameTimer -= currentDanceRate;
            }
            currentDanceRate = (60 / (6f / 2f)) / jukeboxSongs.get(playing).tempo * 60f;
            if (++jukeboxFrameTimer >= currentDanceRate) {
                if (jukeboxFrame == 2)
                    if (LittleH.program.dynamicCamera.targetPosition.y > 300)
                        LittleH.program.dynamicCamera.targetPosition.y = 300;
                    else
                        LittleH.program.dynamicCamera.targetPosition.y = 1300;
                jukeboxFrame++;
                jukeboxFrame = jukeboxFrame % 6;
                jukeboxFrameTimer -= currentDanceRate;
            }
        }

        if (songIndex != oldSongIndex) {
            changedSongIndex();
        }
        if (songIndex != playing && buttons.get(1) == pauseButton || !SoundEngine.isJukeboxPlaying()) {
            buttons.remove(1);
            buttons.add(1, playButton);
        } else if (songIndex == playing && buttons.get(1) == playButton && SoundEngine.isJukeboxPlaying()) {
            buttons.remove(1);
            buttons.add(1, pauseButton);
        }
        buttons.forEach(MenuButton::update);
        songIndex = ((ListSetting) settingButton.setting).value;
    }

    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return"))
            LittleH.program.switchMenu(new LevelSelectMenu());
        else if (ControlInput.localControls.isJustPressed(Controls.JUMP))
            buttons.get(1).onPress.run();
    }

    @Override
    public void mouseUp(int button) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).mouseClicked();
        }
    }

    @Override
    public void close() {
        LittleH.program.dynamicCamera.reset();
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle menuPanel = new Rectangle(-1024 / 2, -576 / 2, 1024, 576);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        Rectangle mask = new Rectangle();
        ScissorStack.calculateScissors(LittleH.program.staticCamera, g.getTransformMatrix(), menuPanel, mask);
        boolean pop = ScissorStack.pushScissors(mask);

        g.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        background.render(g);

        if (pop)
            ScissorStack.popScissors();
        g.flush();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        LittleH.program.useStaticCamera();

        g.resetColor();
        g.drawPatch(Patch.get("menu_hollow"), menuPanel, 8);

        if (SoundEngine.isJukeboxPlaying()) {
            g.drawImage(Images.getImage("ui/jukebox/dancing_h.png"), new Rectangle(-256 - 128 - 64, -576 / 2 + 8, 128, 128), new Rectangle(0, 32 * hFrame, 32, 32));
            g.setColor(Images.getHColor());
            g.drawImage(Images.getImage("ui/jukebox/dancing_h_color.png"), new Rectangle(-256 - 128 - 64, -576 / 2 + 8, 128, 128), new Rectangle(0, 32 * hFrame, 32, 32));
            g.resetColor();
            g.drawImage(Images.getImage("ui/jukebox/dancing_jukebox.png"), new Rectangle(256 + 64, -576 / 2 + 16, 128, 128), new Rectangle(0, 32 * jukeboxFrame, 32, 32));
        } else {
            g.drawImage(Images.getImage("ui/jukebox/idling_h.png"), new Rectangle(-256 - 128 - 64, -576 / 2 + 8, 128, 128), new Rectangle(0, 32 * (LittleH.getTick() / 30 % 2), 32, 32));
            g.setColor(Images.getHColor());
            g.drawImage(Images.getImage("ui/jukebox/idling_h_color.png"), new Rectangle(-256 - 128 - 64, -576 / 2 + 8, 128, 128), new Rectangle(0, 32 * (LittleH.getTick() / 30 % 2), 32, 32));
            g.resetColor();
            g.drawImage(Images.getImage("ui/jukebox/idling_jukebox.png"), new Rectangle(256 + 64, -576 / 2 + 16, 128, 128), new Rectangle(0, 0, 32, 32));
        }

        g.drawPatch(Patch.get("menu_globbed"), -512 + 32, -MainMenu.relZeroY() - 178, 512 * 2 - 64, 162, 8);

        g.drawString(jukeboxSongs.get(songIndex).name, LittleH.font, 0, -MainMenu.relZeroY() - 64, LittleH.defaultFontScale * 1.25f, 0);
        if (jukeboxSongs.get(songIndex).placeholder) {
            g.drawString("-" + jukeboxSongs.get(songIndex).artist, LittleH.font, 0, -MainMenu.relZeroY() - 102, LittleH.defaultFontScale * 0.9f, 0);
            g.drawString("(Placeholder)", LittleH.font, 0, -MainMenu.relZeroY() - 136, LittleH.defaultFontScale * 0.7f, 0);
        } else {
            g.drawString("-" + jukeboxSongs.get(songIndex).artist, LittleH.font, 0, -MainMenu.relZeroY() - 102 - 20, LittleH.defaultFontScale * 0.9f, 0);
        }

        buttons.forEach(button -> button.render(g));
    }

    private static class JukeboxSong {
        public final String path;
        public final String name;
        public final String artist;
        public final String background;
        public final float tempo;
        public final boolean placeholder;

        public JukeboxSong(String path, String name, String artist, String background, float tempo, boolean placeholder) {
            this.path = path;
            this.name = name;
            this.artist = artist;
            this.background = background;
            this.tempo = tempo;
            this.placeholder = placeholder;
        }

        public JukeboxSong(SabValue[] values) {
            this(values[0].getRawValue(), values[1].getRawValue(), values[2].getRawValue(), values[3].getRawValue(), values[4].asFloat(), values[5].asBool());
        }

        public void play() {
            SoundEngine.playJukeboxMusic(path, looping);
        }
    }
}
