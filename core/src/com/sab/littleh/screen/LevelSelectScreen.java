package com.sab.littleh.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.settings.PercentageSetting;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.settings.SettingsScreen;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LevelSelectScreen extends Screen {
    private SettingButton slider;
    private Menu<ImageButton> optionButtons;
    private Menu<LevelButton> mapButtons;
    private TypingQuery createLevelQuery;
    private float levelScroll;

    @Override
    public void start() {
        levelScroll = 0;
        LittleH.setTitle(" | Browsing Levels");
        Cursors.switchCursor("cursor");
        File[] foundMaps = LittleH.findMaps();
        LevelButton[] levelButtons = new LevelButton[foundMaps.length];
        for (int i = 0; i < foundMaps.length; i++) {
            File file = foundMaps[i];
            try {
                levelButtons[i] = new LevelButton("button", file, SabReader.read(file), 0, 0, 256, 64);
            } catch (SabParsingException e) {
                // TODO: Handle errors with .sab parsing
            }
        }

        ImageButton optionButton = new ImageButton("square_button", "ui/buttons/icons/create.png", 0, 0, 80, 88, 8, 16, 64, 64, () -> {
            createLevelQuery = new TypingQuery("Choose a name for your level: \n ", "My Level", new Rectangle(-384, -128, 384 * 2, 128 * 2));
        });
        optionButton.setHoverText("Create new level");

        if (Settings.localSettings.debugMode.value) {
            optionButtons = new Menu<>(new ImageButton[]{
                    optionButton,
                    optionButton.quickCreate("ui/buttons/icons/settings_gear.png", "Settings", () -> {
                        LittleH.program.switchScreen(new SettingsScreen(this));
                    }),
                    optionButton.quickCreate("ui/buttons/icons/help.png", "Help", () -> {
                        LittleH.program.switchScreen(new HelpScreen());
                    }),
                    optionButton.quickCreate("ui/buttons/icons/folder.png", "Open maps folder", () -> {
                        try {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(LittleH.mapsFolder);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }),
                    optionButton.quickCreate("ui/jukebox/loop.png", "Reload maps", () -> {
                        LittleH.program.switchScreen(new LevelSelectScreen());
                    }),
                    optionButton.quickCreate("ui/buttons/icons/jukebox_note.png", "Jukebox", () -> {
                        LittleH.program.switchScreen(new JukeboxScreen());
                    }),
                    optionButton.quickCreate("ui/jukebox/back_arrow.png", "To Title Menu", () -> {
                        LittleH.program.switchScreen(TitleScreen.titleScreen);
                    }),
                    optionButton.quickCreate("ui/buttons/icons/loading.png", "View fake loading screen", () -> {
                        LittleH.program.switchScreen(new FakeLoadingScreen());
                    })
            }, 80, 80, 16);
        } else {
            optionButtons = new Menu<>(new ImageButton[]{
                    optionButton,
                    optionButton.quickCreate("ui/buttons/icons/settings_gear.png", "Settings", () -> {
                        LittleH.program.switchScreen(new SettingsScreen(this));
                    }),
                    optionButton.quickCreate("ui/buttons/icons/help.png", "Help", () -> {
                        LittleH.program.switchScreen(new HelpScreen());
                    }),
                    optionButton.quickCreate("ui/buttons/icons/folder.png", "Open maps folder", () -> {
                        try {
                            Desktop.getDesktop().open(LittleH.mapsFolder);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }),
                    optionButton.quickCreate("ui/jukebox/loop.png", "Reload maps", () -> {
                        LittleH.program.switchScreen(new LevelSelectScreen());
                    }),
                    optionButton.quickCreate("ui/buttons/icons/jukebox_note.png", "Jukebox", () -> {
                        LittleH.program.switchScreen(new JukeboxScreen());
                    }),
                    optionButton.quickCreate("ui/jukebox/back_arrow.png", "To Title Menu", () -> {
                        LittleH.program.switchScreen(TitleScreen.titleScreen);
                    })
            }, 80, 80, 16);
        }

        this.mapButtons = new Menu<>(levelButtons, 256, 64, 16);

        mapButtons.setScreenRectangle(relZeroX() + 32 + (int) levelScroll, program.getHeight() / 2 - 128 - 8, program.getHeight() - 128 - 32 - 96, false);

        if ((int) (mapButtons.getScreenRectangle().width - program.getWidth() + 64) > 0) {
            slider = new SettingButton(new PercentageSetting("slide", "Scroll bar :)", 0, 0,
                    (int) (mapButtons.getScreenRectangle().width - program.getWidth() + 64)), -320, relZeroY() + 40, 640, false);
        }
    }

    @Override
    public void update() {
        if (createLevelQuery != null) {
            createLevelQuery.update();
            if (createLevelQuery.complete) {
                if (createLevelQuery.accepted) {
                    program.switchScreen(new CreateLevelScreen(createLevelQuery.getQuery()));
                } else {
                    createLevelQuery = null;
                }
            }
            return;
        }

        if (slider != null)
            slider.update();

        if (slider != null)
            levelScroll = -((PercentageSetting) slider.setting).value;

        mapButtons.setScreenRectangle(relZeroX() + 32 + (int) levelScroll, program.getHeight() / 2 - 128 - 8, program.getHeight() - 128 - 32 - 64, false);
        optionButtons.setScreenRectangle(relZeroX() + 16, -relZeroY() - 16, 0, false);

        mapButtons.forEach(ScreenButton::update);

        optionButtons.forEach(ScreenButton::update);
    }

    @Override
    public void mouseScrolled(float amountY) {
        levelScroll += amountY * 64f;
        levelScroll = Math.min(0, Math.max(-(mapButtons.getScreenRectangle().width - program.getWidth() + 64), levelScroll));
        if (slider != null)
            slider.setting.value = (int) -levelScroll;
    }

    @Override
    public void mouseDown(int button) {
        if (slider != null)
            slider.mouseDown();
    }

    @Override
    public void mouseUp(int button) {
        if (createLevelQuery != null) {
            createLevelQuery.mouseClicked();
            return;
        }

        if (slider != null)
            slider.mouseClicked();

        mapButtons.forEach(ScreenButton::mouseClicked);

        optionButtons.forEach(ScreenButton::mouseClicked);
    }

    @Override
    public void close() {
    }

    @Override
    public void keyDown(int keycode) {
        if (createLevelQuery != null)
            createLevelQuery.updateQueryKey(keycode, 20, false);
        else if (ControlInput.localControls.isJustPressed(Controls.get("return")))
            program.switchScreen(TitleScreen.titleScreen);
    }

    @Override
    public void keyTyped(char character) {
        if (createLevelQuery != null)
            createLevelQuery.updateQueryChar(character, 20);
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle rectangle = new Rectangle(relZeroX() + 32, relZeroY() + 32 + 64, program.getWidth() - 64, program.getHeight() - 128 - 32 - 64);

        g.drawPatch(Patch.get("menu"), rectangle, 8);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        Rectangle mask = new Rectangle();
        ScissorStack.calculateScissors(LittleH.program.staticCamera, g.getTransformMatrix(), rectangle, mask);
        boolean pop = ScissorStack.pushScissors(mask);

        Rectangle[] itemButtons = mapButtons.getItemButtons();

        for (int i = 0; i < mapButtons.items.length; i++) {
            LevelButton button = mapButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
        }

        g.drawPatch(Patch.get("menu_hollow"), rectangle, 8);

        if (pop)
            ScissorStack.popScissors();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        itemButtons = optionButtons.getItemButtons();

        for (int i = 0; i < optionButtons.items.length; i++) {
            ImageButton button = optionButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
        }

        if (createLevelQuery != null) {
            createLevelQuery.render(g);
        }

        if (slider != null)
            slider.render(g);
    }
}
