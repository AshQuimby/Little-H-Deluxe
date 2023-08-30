package com.sab.littleh.mainmenu;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.util.*;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabValue;
import com.sab.littleh.util.sab_format.SabWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class LevelOptionsMenu extends MainMenu {
    private Menu<MenuButton> confirmationButtons;
    private Menu<MenuButton> optionButtons;
    private TypingQuery typingQuery;
    private File file;
    private SabData mapData;

    public LevelOptionsMenu(File file, SabData mapData) {
        this.file = file;
        this.mapData = mapData;
        MenuButton[] buttons = new MenuButton[5];
        buttons[0] = new MenuButton("square_button", "Play", 0, 0, 160, 80, () -> {
            LoadingUtil.startLoading(() -> {
                try {
                    Level level = LevelLoader.readLevel(mapData, file);
                    GameMenu gameMenu = new GameMenu(file, level);
                    if (gameMenu.failedPlaying) {
                        LittleH.pendingMenu = new LevelErrorMenu("Cannot play a level without a spawn point!");
                    } else {
                        LittleH.pendingMenu = gameMenu;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LittleH.pendingMenu = new LevelErrorMenu("Error reading level file. File is corrupted");
                }
            });
        });
        buttons[1] = new MenuButton("square_button", "Edit", 0, 0, 160, 80, () -> {
            LoadingUtil.startLoading(() -> {
                try {
                    Level level = LevelLoader.readLevel(mapData, file);
                    LevelEditorMenu editorMenu = new LevelEditorMenu(file, level);
                    LittleH.pendingMenu = editorMenu;
                } catch (IOException e) {
                    e.printStackTrace();
                    LittleH.pendingMenu = new LevelErrorMenu("Error reading level file. File is corrupted");
                }
            });
        });
        buttons[2] = new MenuButton("square_button", "Rename", 0, 0, 160, 80, () -> {
            askForRename();
        });
        buttons[3] = new MenuButton("square_button", "Delete", 0, 0, 160, 80, () -> {
            askDeleteConfirmation();
        });
        buttons[4] = new MenuButton("square_button", "Back", 0, 0, 160, 80, () -> {
            program.switchMenu(new LevelSelectMenu());
        });
        optionButtons = new Menu<>(buttons, 160, 80, 16);
    }

    @Override
    public void start() {
        LittleH.setTitle(" | Viewing Level " + mapData.getValue("name"));
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        optionButtons.setMenuRectangle(relZeroX() + 16, 0 - 64, 0, true);
        optionButtons.setCenterX(0);

        optionButtons.forEach(mapButton -> {
            mapButton.update();
        });

        if (confirmationButtons != null) {
            confirmationButtons.setMenuRectangle(relZeroX() + 16, 0 - 64, 0, true);
            confirmationButtons.setCenterX(0);

            confirmationButtons.forEach(mapButton -> {
                mapButton.update();
            });
        }
    }

    public void askDeleteConfirmation() {
        optionButtons.forEach(menuButton -> {
            menuButton.setDisabled(true);
        });
        confirmationButtons = new Menu<>(new MenuButton[] {
                new MenuButton("square_button", MathUtils.random() > 0.25f ? "Yes" : "Absolutely", 0, 0, 320, 80, () -> {
                    file.delete();
                    LittleH.program.switchMenu(new LevelSelectMenu());
                }),
                new MenuButton("square_button", MathUtils.random() > 0.25f ? "No" : "Take me back!", 0, 0, 320, 80, () -> {
                    confirmationButtons = null;
                    optionButtons.forEach(menuButton -> {
                        menuButton.setDisabled(false);
                    });
                })
        }, 320, 80, 16);
    }

    public void askForRename() {
        optionButtons.forEach(menuButton -> {
            menuButton.setDisabled(true);
        });
        typingQuery = new TypingQuery("Level will be renamed: \n ", mapData.getValue("name").getRawValue(),
                new Rectangle(-256, 0, 512, 192));
        confirmationButtons = new Menu<>(new MenuButton[] {
                new MenuButton("square_button", "Yes", 0, 0, 320, 80, () -> {
                    confirmationButtons = null;
                    optionButtons.forEach(menuButton -> {
                        menuButton.setDisabled(false);
                    });
                    changeName(typingQuery.getQuery());
                    typingQuery = null;
                }),
                new MenuButton("square_button", "No", 0, 0, 320, 80, () -> {
                    typingQuery = null;
                    confirmationButtons = null;
                    optionButtons.forEach(menuButton -> {
                        menuButton.setDisabled(false);
                    });
                })
        }, 320, 80, 16);
    }

    public void changeName(String newName) {
        mapData.insertValue("name", new SabValue(newName));
        try {
            String oldFile = "";
            Scanner scanner = SabReader.skipSabPreface(new Scanner(file));
            scanner.useDelimiter("");
            while (scanner.hasNext()) {
                oldFile += scanner.nextLine() + "\n";
            }
            scanner.close();
            file.delete();
            file.createNewFile();
            SabWriter.write(file, mapData);
            FileWriter writer = new FileWriter(file, true);
            writer.write(oldFile);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyDown(int keycode) {
        if (typingQuery != null)
            typingQuery.updateQueryKey(keycode, 64, false);
        else if (ControlInputs.isJustPressed("return"))
            LittleH.program.switchMenu(new LevelSelectMenu());

    }

    @Override
    public void keyTyped(char character) {
        if (typingQuery != null)
            typingQuery.updateQueryChar(character, 64);
    }

    @Override
    public void mouseUp(int button) {
        optionButtons.forEach(mapButton -> {
            mapButton.mouseClicked();
        });

        if (confirmationButtons != null) {
            confirmationButtons.forEach(mapButton -> {
                mapButton.mouseClicked();
            });
        }
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle titleBox = new Rectangle(-256, 0, 512, 192);
        g.drawPatch(Patch.get("menu"), titleBox, 8);
        titleBox.x += 16;
        titleBox.width -= 32;

        String text;
        float size;

        if (typingQuery != null) {
            text = "";
            size = 0.75f;
            typingQuery.render(g);
        } else if (confirmationButtons != null) {
            text = "Are you sure you want to delete: " + mapData.getValue("name") + ". \n (This cannot be undone)";
            size = LittleH.defaultFontScale * 0.75f;
        } else {
            text = "Selected Level: " + mapData.getValue("name");
            size = LittleH.defaultFontScale;
        }

        g.drawString(text, LittleH.font, titleBox, 4, size, 0, 0);

        Rectangle infoBox = new Rectangle(-420 / 2, -64, 420, 64);
        g.drawPatch(Patch.get("menu_globbed"), infoBox, 8);

        g.drawString("Author: " + mapData.getValue("author"), LittleH.font, 0, -32, LittleH.defaultFontScale * 0.75f, 0);

        Rectangle[] itemButtons;

        if (confirmationButtons != null) {
            itemButtons = confirmationButtons.getItemButtons();

            for (int i = 0; i < confirmationButtons.items.length; i++) {
                MenuButton button = confirmationButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
            }
        } else {
            for (int i = 0; i < optionButtons.items.length; i++) {
                itemButtons = optionButtons.getItemButtons();
                MenuButton button = optionButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
            }
        }
    }
}
