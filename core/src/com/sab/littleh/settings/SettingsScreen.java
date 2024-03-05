package com.sab.littleh.settings;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.screen.ControlsScreen;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.screen.ScreenButton;
import com.sab.littleh.screen.VisualButton;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private List<ScreenButton> screenButtons;
    private List<ScreenButton> currentScreen;
    private List<ScreenButton> gameSettings;
    private List<ScreenButton> videoSettings;
    private List<ScreenButton> audioSettings;
    private List<ScreenButton> miscSettings;
    private TypingQuery typingQuery;
    private Screen cameFrom;

    public SettingsScreen(Screen cameFrom) {
        this.cameFrom = cameFrom;
        screenButtons = new ArrayList<>();
        screenButtons.add(new ScreenButton("square_button", "Game Settings", -360 / 2 - 360 / 2 - 8, 576 / 2 - 32, 360, 64, () -> {
            currentScreen = gameSettings;
            screenButtons.forEach(screenButton -> {
                screenButton.setDisabled(screenButton.text.equals("Game Settings"));
            });
        }));
        screenButtons.add(new ScreenButton("square_button", "Video Settings", -360 / 2 - 360 * 1.5f - 24, 576 / 2 - 32, 360, 64, () -> {
            currentScreen = videoSettings;
            screenButtons.forEach(screenButton -> {
                screenButton.setDisabled(screenButton.text.equals("Video Settings"));
            });
        }));
        screenButtons.add(new ScreenButton("square_button", "Audio Settings", 360 / 2 - 360 / 2 + 8, 576 / 2 - 32, 360, 64, () -> {
            currentScreen = audioSettings;
            screenButtons.forEach(screenButton -> {
                screenButton.setDisabled(screenButton.text.equals("Audio Settings"));
            });
        }));
        screenButtons.add(new ScreenButton("square_button", "Misc Settings", 360 / 2 + 24 + 360 / 2, 576 / 2 - 32, 360, 64, () -> {
            currentScreen = miscSettings;
            screenButtons.forEach(screenButton -> {
                screenButton.setDisabled(screenButton.text.equals("Misc Settings"));
            });
        }));
        screenButtons.add(new ScreenButton("square_button", "Save & Return", -384 / 2 - 384 - 16, -640 / 2 + 16, 384, 64, () -> {
            Settings.localSettings.save();
            LittleH.program.switchScreen(cameFrom);
            LittleH.program.resetWindow();
        }));
        screenButtons.add(new ScreenButton("square_button", "Don't Save", -384 / 2, -640 / 2 + 16, 384, 64, () -> {
            Settings.loadSettings();
            LittleH.program.switchScreen(cameFrom);
            LittleH.program.resetWindow();
        }));
        screenButtons.add(new ScreenButton("square_button", "Reset Settings", 384 / 2 + 16, -640 / 2 + 16, 384, 64, () -> {
            Settings.localSettings.resetAll();
            SoundEngine.resetCurrentMusicVolume();
        }));
        videoSettings = new ArrayList<>();
        audioSettings = new ArrayList<>();
        gameSettings = new ArrayList<>();
        miscSettings = new ArrayList<>();

        audioSettings.add(new SettingButton(Settings.localSettings.muteGame, -72 / 2, 64));
        audioSettings.add(new SettingButton(Settings.localSettings.masterVolume, -256 / 2, -64));
        audioSettings.add(new SettingButton(Settings.localSettings.sfxVolume, -256 / 2 - 272, -64));
        audioSettings.add(new SettingButton(Settings.localSettings.musicVolume, -256 / 2 + 272, -64));
        audioSettings.add(new ScreenButton("square_button", "Test SFX", -272 / 2, -96 * 2, 272, 64, this::playTestSound));

        gameSettings.add(new SettingButton(Settings.localSettings.debugMode, -72 / 2 - 96 * 2 - 32, 96));
        gameSettings.add(new SettingButton(Settings.localSettings.backgroundVisibility, -72 / 2 + 96 * 2 + 32, 96));
        gameSettings.add(new SettingButton(Settings.localSettings.dividedTileSelection, -72 / 2 - 96 * 2 - 32, -16));
        gameSettings.add(new SettingButton(Settings.localSettings.upEqualsJump, -72 / 2 + 96 * 2 + 32, -16));
        gameSettings.add(new ScreenButton("square_button", "Change Name", -272 / 2 - 196, -128 - 64, 272, 64, this::askForRename));
        gameSettings.add(new ScreenButton("square_button", "Controls", -272 / 2 + 196, -128 - 64, 272, 64,
                () -> program.switchScreen(new ControlsScreen(this))));

        videoSettings.add(new SettingButton(Settings.localSettings.font, -280 / 2 + 256, -128 - 64));
        videoSettings.add(new SettingButton(Settings.localSettings.fullscreen, -280 / 2 - 256, -96,
                "Windowed Fullscreen currently does \n not work libGDX hates me :) \n (F11 can be pressed at any time)"));
        videoSettings.add(new SettingButton(Settings.localSettings.screenShake, 72 + 64, 64 - 18 + 96));
        videoSettings.add(new SettingButton(Settings.localSettings.grid, 72 + 64, -18 + 32));
        videoSettings.add(new SettingButton(Settings.localSettings.selectionContrast, 72 + 256, -18 + 96));
        videoSettings.add(new SettingButton(Settings.localSettings.useShaders, 72 + 256, -18 - 32));
//        videoSettings.add(new SettingButton(Settings.localSettings.retroMode, 72 + 64, -18 - 96) {
//            @Override
//            public void mouseClicked() {
//                super.mouseClicked();
//                if (hovered) {
//                    LittleH.program.recheckShaderStack();
//                }
//            }
//        });
        // Hidden offscreen by default >:3
        videoSettings.add(new SettingButton(Settings.localSettings.rainbowTitle, 824, -384));

        videoSettings.add(new SettingButton(Settings.localSettings.zoomScalar, -256 / 2 - 160, 128 - 16,
                "Altering this could be considered cheating \n or make some levels more challenging"));

        miscSettings.add(new SettingButton(Settings.localSettings.hColor, -361 / 2 - 40 + 8, -16, 361));
        miscSettings.add(new VisualButton("menu_flat", new Rectangle(-361 / 2 + 361 - 8, -48 + 16, 64, 64)) {
            @Override
            public void render(Graphics g, int patchScale) {
                super.render(g, patchScale);
                g.setColor(Images.getHColor());
                g.draw(Images.getImage("pixel.png"), x + 16, y + 16, 32, 32);
                g.resetColor();
            }
        });
        miscSettings.add(new SettingButton(Settings.localSettings.dialogueSpeed, -100, -128, 200));
        miscSettings.add(new SettingButton(Settings.localSettings.autoDialogue, -72 / 2 + 160, 96));
        miscSettings.add(new SettingButton(Settings.localSettings.controllerVibration, -72 / 2 - 160, 96));

        currentScreen = gameSettings;
        screenButtons.get(0).setDisabled(true);
    }

    private void playTestSound() {
        SoundEngine.playSound("coin.ogg");
    }

    @Override
    public void start() {
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        if (typingQuery != null) {
            typingQuery.update();
            if (typingQuery.complete) {
                if (typingQuery.accepted) {
                    changeName(typingQuery.getQuery());
                }
                typingQuery = null;
            }
        } else {
            screenButtons.forEach(ScreenButton::update);
            currentScreen.forEach(ScreenButton::update);
        }
    }

    public void askForRename() {
        typingQuery = new TypingQuery("Your new name will be: \n ", Settings.localSettings.authorName.value,
                new Rectangle(-320, -128, 640, 320), true);
    }

    public void changeName(String newName) {
        Settings.localSettings.authorName.set(newName);
    }

    @Override
    public void keyDown(int keycode) {
        if (typingQuery != null)
            typingQuery.updateQueryKey(keycode, 64, false);
    }

    @Override
    public void keyTyped(char character) {
        if (typingQuery != null)
            typingQuery.updateQueryChar(character, 64);
    }

    @Override
    public void mouseDown(int button) {
        if (typingQuery == null) {
            currentScreen.forEach(ScreenButton::mouseDown);
        }
    }

    @Override
    public void mouseUp(int button) {
        if (typingQuery != null) {
            typingQuery.mouseClicked();
        } else {
            screenButtons.forEach(ScreenButton::mouseClicked);
            currentScreen.forEach(ScreenButton::mouseClicked);
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle screenPanel = new Rectangle(-1024 / 2, -576 / 2, 1024, 576);
        g.drawPatch(Patch.get("menu"), screenPanel, 3);

        screenButtons.forEach(screenButton -> {
            screenButton.render(g, 3);
        });
        currentScreen.forEach(screenButton -> {
            screenButton.render(g, 3);
        });

        if (typingQuery != null) {
            typingQuery.render(g, 3);
        }
    }
}
