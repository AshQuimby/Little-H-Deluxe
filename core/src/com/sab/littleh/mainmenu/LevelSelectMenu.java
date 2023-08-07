package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.Cursors;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.io.File;

public class LevelSelectMenu extends MainMenu {
    private Menu<ImageButton> optionButtons;
    private Menu<LevelButton> mapButtons;
    @Override
    public void start() {
        Gdx.graphics.setTitle(LittleH.TITLE + " | Browsing Levels");
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
            // Trollge :)
        });
        optionButton.setHoverText("Create new level");

        optionButtons = new Menu<>(new ImageButton[] {
                optionButton,
                optionButton.quickCreate("ui/buttons/icons/settings_gear.png", "Settings", () -> {
                    LittleH.program.switchMenu(new SettingsMenu());
                }),
                optionButton.quickCreate("ui/buttons/icons/help.png", "Help", () -> {

                })
        }, 80, 80, 16);

        this.mapButtons = new Menu<>(levelButtons, 256, 64, 16);
    }

    @Override
    public void update() {
        mapButtons.setMenuRectangle(relZeroX() + 32, program.getHeight() / 2 - 128, program.getHeight() - 128 - 32, false);
        optionButtons.setMenuRectangle(relZeroX() + 16, -relZeroY() - 16, 0, false);

        mapButtons.forEach(levelButton -> {
            levelButton.update();
        });

        optionButtons.forEach(mapButton -> {
            mapButton.update();
        });
    }

    @Override
    public void mouseUp(int button) {
        mapButtons.forEach(levelButton -> {
            levelButton.mouseClicked();
        });

        optionButtons.forEach(mapButton -> {
            mapButton.mouseClicked();
        });
    }

    @Override
    public void close() {
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        mapButtons.renderMenuRectangle(g, Patch.get("menu"));

        Rectangle[] itemButtons = mapButtons.getItemButtons();

        for (int i = 0; i < mapButtons.items.length; i++) {
            LevelButton button = mapButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
        }

        itemButtons = optionButtons.getItemButtons();

        for (int i = 0; i < optionButtons.items.length; i++) {
            ImageButton button = optionButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
        }
    }
}
