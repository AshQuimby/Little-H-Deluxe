package com.sab.littleh.mainmenu;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenu extends MainMenu {
    private List<MenuButton> menuButtons;
    private List<MenuButton> currentMenu;
    private List<MenuButton> gameSettings;
    private List<MenuButton> videoSettings;
    private List<MenuButton> audioSettings;
    private TypingQuery typingQuery;

    public SettingsMenu() {
        menuButtons = new ArrayList<>();
        menuButtons.add(new MenuButton("square_button", "Game Settings", -384 / 2, 576 / 2 - 32, 384, 64, () -> {
            currentMenu = gameSettings;
            menuButtons.forEach(menuButton -> {
                menuButton.setDisabled(menuButton.text.equals("Game Settings"));
            });
        }));
        menuButtons.add(new MenuButton("square_button", "Video Settings", -384 / 2 - 384 - 16, 576 / 2 - 32, 384, 64, () -> {
            currentMenu = videoSettings;
            menuButtons.forEach(menuButton -> {
                menuButton.setDisabled(menuButton.text.equals("Video Settings"));
            });
        }));
        menuButtons.add(new MenuButton("square_button", "Audio Settings", 384 / 2 + 16, 576 / 2 - 32, 384, 64, () -> {
            currentMenu = audioSettings;
            menuButtons.forEach(menuButton -> {
                menuButton.setDisabled(menuButton.text.equals("Audio Settings"));
            });
        }));
        menuButtons.add(new MenuButton("square_button", "Save & Return", -384 / 2 - 384 - 16, -640 / 2 + 16, 384, 64, () -> {
            Settings.localSettings.save();
            LittleH.program.switchMenu(new LevelSelectMenu());
            LittleH.program.resetWindow();
        }));
        menuButtons.add(new MenuButton("square_button", "Don't Save", -384 / 2, -640 / 2 + 16, 384, 64, () -> {
            Settings.localSettings.load();
            LittleH.program.switchMenu(new LevelSelectMenu());
            LittleH.program.resetWindow();
        }));
        menuButtons.add(new MenuButton("square_button", "Reset Settings", 384 / 2 + 16, -640 / 2 + 16, 384, 64, () -> {
            Settings.localSettings.resetAll();
            SoundEngine.resetCurrentMusicVolume();
        }));
        videoSettings = new ArrayList<>();
        audioSettings = new ArrayList<>();
        gameSettings = new ArrayList<>();

        audioSettings.add(new SettingButton(Settings.localSettings.muteGame, -72 / 2, 64));
        audioSettings.add(new SettingButton(Settings.localSettings.masterVolume, -152 / 2, -64));
        audioSettings.add(new SettingButton(Settings.localSettings.sfxVolume, -152 / 2 - 256, -64));
        audioSettings.add(new SettingButton(Settings.localSettings.musicVolume, -152 / 2 + 256, -64));
        audioSettings.add(new MenuButton("square_button", "Test SFX", -272 / 2, -96 * 2, 272, 64, this::playTestSound));

        gameSettings.add(new SettingButton(Settings.localSettings.debugMode, -72 / 2 - 96 * 2 - 32, 96));
        gameSettings.add(new SettingButton(Settings.localSettings.backgroundVisibility, -72 / 2 + 96 * 2 + 32, 96));
        gameSettings.add(new SettingButton(Settings.localSettings.hColor, -361 / 2 - 40 + 8, -16, 361));
        gameSettings.add(new VisualButton("menu_flat", new Rectangle(-361 / 2 + 361 - 8, -32, 64, 64)) {
            @Override
            public void render(Graphics g) {
                super.render(g);
                g.setColor(Images.getHColor());
                g.draw(Images.getImage("pixel.png"), x + 16, y + 16, 32, 32);
                g.resetColor();
            }
        });
        gameSettings.add(new MenuButton("square_button", "Change Name", -272 / 2, -128, 272, 64, this::askForRename));


        videoSettings.add(new SettingButton(Settings.localSettings.font, -280 / 2 + 256, -64));
        videoSettings.add(new SettingButton(Settings.localSettings.fullscreen, -280 / 2 - 256, -64,
                "Windowed Fullscreen currently does \n not work libGDX hates me :) \n (F11 can be pressed at any time)"));
        videoSettings.add(new SettingButton(Settings.localSettings.screenShake, 72 + 96, 128 - 18));
        // Hidden offscreen by default >:3
        videoSettings.add(new SettingButton(Settings.localSettings.rainbowTitle, 824, -384));
        videoSettings.add(new SettingButton(Settings.localSettings.zoomScalar, -152 / 2 - 160, 128 - 16,
                "Altering this could be considered cheating \n or make some levels more challenging"));

        currentMenu = gameSettings;
        menuButtons.get(0).setDisabled(true);
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
            menuButtons.forEach(MenuButton::update);
            currentMenu.forEach(MenuButton::update);
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
            currentMenu.forEach(MenuButton::mouseDown);
        }
    }

    @Override
    public void mouseUp(int button) {
        if (typingQuery != null) {
            typingQuery.mouseClicked();
        } else {
            menuButtons.forEach(MenuButton::mouseClicked);
            currentMenu.forEach(MenuButton::mouseClicked);
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle menuPanel = new Rectangle(-1024 / 2, -576 / 2, 1024, 576);
        g.drawPatch(Patch.get("menu"), menuPanel, 8);

        menuButtons.forEach(menuButton -> {
            menuButton.render(g);
        });
        currentMenu.forEach(menuButton -> {
            menuButton.render(g);
        });

        if (typingQuery != null) {
            typingQuery.render(g);
        }
    }
}
