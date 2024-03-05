package com.sab.littleh.screen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.GameScreen;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.editor.LevelEditorScreen;
import com.sab.littleh.game.level.LevelErrorScreen;
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

public class LevelOptionsScreen extends Screen {
    private Menu<ScreenButton> confirmationButtons;
    private Menu<ScreenButton> optionButtons;
    private TypingQuery typingQuery;
    private File file;
    private SabData mapData;

    public LevelOptionsScreen(File file, SabData mapData) {
        this.file = file;
        this.mapData = mapData;
        ScreenButton[] buttons = new ScreenButton[5];
        buttons[0] = new ScreenButton("square_button", "Play", 0, 0, 160, 80, () -> {
            LoadingUtil.startLoading(() -> {
                try {
                    Level level = LevelLoader.readLevel(mapData, file);
                    if (level == null) {
                        LittleH.pendingScreen = new LevelErrorScreen("Error reading level file. File is corrupted!");
                        return;
                    }
                    GameScreen gameScreen = new GameScreen(file, level, ControlInput.localControls.isPressed("shift"));
                    if (gameScreen.failedPlaying) {
                        LittleH.pendingScreen = new LevelErrorScreen("Cannot play a level without a spawn point!");
                    } else {
                        LittleH.pendingScreen = gameScreen;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LittleH.pendingScreen = new LevelErrorScreen("Error reading level file. File is corrupted!");
                }
            });
        });
        buttons[1] = new ScreenButton("square_button", "Edit", 0, 0, 160, 80, () -> {
            LoadingUtil.startLoading(() -> {
                try {
                    Level level = LevelLoader.readLevel(mapData, file);
                    if (level == null) {
                        LittleH.pendingScreen = new LevelErrorScreen("Error reading level file. File is corrupted!");
                        return;
                    }
                    LevelEditorScreen editorScreen = new LevelEditorScreen(file, level);
                    LittleH.pendingScreen = editorScreen;
                } catch (IOException e) {
                    e.printStackTrace();
                    LittleH.pendingScreen = new LevelErrorScreen("Error reading level file. File is corrupted!");
                }
            });
        });
        buttons[2] = new ScreenButton("square_button", "Rename", 0, 0, 160, 80, () -> {
            askForRename();
        });
        buttons[3] = new ScreenButton("square_button", "Delete", 0, 0, 160, 80, () -> {
            askDeleteConfirmation();
        });
        buttons[4] = new ScreenButton("square_button", "Back", 0, 0, 160, 80, () -> {
            program.switchScreen(new LevelSelectScreen());
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
        optionButtons.setScreenRectangle(relZeroX() + 16, 0 - 64, 0, true);
        optionButtons.setCenterX(0);

        optionButtons.forEach(mapButton -> {
            mapButton.update();
        });

        if (confirmationButtons != null) {
            confirmationButtons.setScreenRectangle(relZeroX() + 16, 0 - 64, 0, true);
            confirmationButtons.setCenterX(0);

            confirmationButtons.forEach(mapButton -> {
                mapButton.update();
            });
        }
    }

    public void askDeleteConfirmation() {
        optionButtons.forEach(screenButton -> {
            screenButton.setDisabled(true);
        });
        confirmationButtons = new Menu<>(new ScreenButton[] {
                new ScreenButton("square_button", MathUtils.random() > 0.25f ? "Yes" : "Absolutely", 0, 0, 320, 80, () -> {
                    file.delete();
                    LittleH.program.switchScreen(new LevelSelectScreen());
                }),
                new ScreenButton("square_button", MathUtils.random() > 0.25f ? "No" : "Take me back!", 0, 0, 320, 80, () -> {
                    confirmationButtons = null;
                    optionButtons.forEach(screenButton -> {
                        screenButton.setDisabled(false);
                    });
                })
        }, 320, 80, 16);
    }

    public void askForRename() {
        optionButtons.forEach(screenButton -> {
            screenButton.setDisabled(true);
        });
        typingQuery = new TypingQuery("Level will be renamed: \n ", mapData.getValue("name").getRawValue(),
                new Rectangle(-256, 0, 512, 192));
        confirmationButtons = new Menu<>(new ScreenButton[] {
                new ScreenButton("square_button", "Yes", 0, 0, 320, 80, () -> {
                    confirmationButtons = null;
                    optionButtons.forEach(screenButton -> {
                        screenButton.setDisabled(false);
                    });
                    changeName(typingQuery.getQuery());
                    typingQuery = null;
                }),
                new ScreenButton("square_button", "No", 0, 0, 320, 80, () -> {
                    typingQuery = null;
                    confirmationButtons = null;
                    optionButtons.forEach(screenButton -> {
                        screenButton.setDisabled(false);
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
        else if (ControlInput.localControls.isJustPressed("return"))
            LittleH.program.switchScreen(new LevelSelectScreen());

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
                ScreenButton button = confirmationButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
            }
        } else {
            for (int i = 0; i < optionButtons.items.length; i++) {
                itemButtons = optionButtons.getItemButtons();
                ScreenButton button = optionButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
            }
        }
    }
}
