package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.BackgroundEditor;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelEditor;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.sab_format.SabValue;

import java.awt.*;
import java.io.File;
import java.util.Optional;

public class LevelEditorMenu extends MainMenu {
    private static Tile[] tileSelection = {
            new Tile("tiles/sets/grass"),
            new Tile("tiles/sets/stone"),
            new Tile("tiles/sets/sandstone"),
            new Tile("tiles/sets/rock"),
            new Tile("tiles/sets/snowy_turf"),
            new Tile("tiles/sets/location_bricks"),
            new Tile("tiles/sets/ice"),
            new Tile("tiles/sets/slick_block"),
            new Tile("tiles/sets/bounce"),
            new Tile("tiles/sets/malice"),
            new Tile("tiles/sets/water"),
            new Tile("tiles/half_spike"),
            new Tile("tiles/spawn"),
            new Tile("tiles/checkpoint"),
            new Tile("tiles/strong_checkpoint"),
            new Tile("tiles/end"),
            new Tile("tiles/invisiblock"),
            new Tile("tiles/invisible_death"),
            new Tile("tiles/one_way"),
            new Tile("tiles/coin"),
            new Tile("tiles/coin_box"),
            new Tile("tiles/power_fruit"),
            new Tile("tiles/timer"),
            new Tile("tiles/key"),
            new Tile("tiles/key_box"),
            new Tile("tiles/evil_key"),
            new Tile("tiles/evil_key_box"),
            new Tile("tiles/enemy"),
            new Tile("tiles/enemy_box"),
            new Tile("tiles/color_cube"),
            new Tile("tiles/dynamic_color_cube"),
            new Tile("tiles/statues"),
            new Tile("tiles/text")
    };
    private static Tile[] developerTileSelection;
    private TypingQuery timeQuery;
    private TypingQuery extraQuery;
    private ImageButton tileMenuButton;
    private ImageButton settingsMenuButton;
    private Menu<MenuButton> tileButtons;
    private Menu<ImageButton> toolButtons;
    private Menu<ImageButton> settingsButtons;
    private Menu<MenuButton> backgroundMenu;
    private Menu<MenuButton> severeConfirmationMenu;
    private Menu<MenuButton> severesevereConfirmationMenu;
    private Menu<? extends MenuButton> currentMenu;
    private static DynamicCamera camera = LittleH.program.dynamicCamera;
    private File file;
    private boolean editingBackground;
    private Level level;
    private LevelEditor editor;
    private LevelEditor levelEditor;
    private BackgroundEditor backgroundEditor;
    private Vector2 mousePosition;
    private Vector2 mouseWorldPosition;
    private Vector2 previousMousePosition;
    private Point tiledMousePosition;
    private Player lastPlayer;
    private boolean canPlaceTiles;
    private boolean backgroundVisible;
    private Tile modifiedExtraTile;

    static {
        if (Settings.localSettings.debugMode.value) {
            developerTileSelection = new Tile[tileSelection.length + 2];
            int i = 0;
            for (Tile tile : tileSelection) {
                developerTileSelection[i] = tile;
                i++;
            }
            developerTileSelection[i] = new Tile("tiles/h_fragment");
            i++;
            developerTileSelection[i] = new Tile("tiles/dialogue_trigger");
            tileSelection = developerTileSelection;
        }
    }

    public LevelEditorMenu(File file, Level level) {
        this.file = file;
        this.level = level;
        backgroundVisible = Settings.localSettings.backgroundVisibility.value;
        levelEditor = new LevelEditor(level);
        backgroundEditor = new BackgroundEditor(level);
        editor = levelEditor;
        mousePosition = new Vector2();
        mouseWorldPosition = new Vector2();
        tiledMousePosition = new Point();
        MenuButton[] buttons = new MenuButton[tileSelection.length + 1];
        buttons[0] = new ImageButton(null, "settings_dots.png", 0, 0, 64, 64, 0, 0, 64, 64, null);
        for (int i = 1; i < buttons.length; i++) {
            tileSelection[i - 1].setTags();
            buttons[i] = new TileButton(tileSelection[i - 1], 0, 0);
        }
        tileButtons = new Menu<>(buttons, 64, 64, 8);
        tileButtons.setItemIndex(1);

        currentMenu = tileButtons;
        tileMenuButton = new ImageButton("tile_button", "settings_dots.png", relZeroX(), -relZeroY() - 80, 80, 80, 0, 0, 0, 0, () -> {
            currentMenu = tileButtons;
        });
        settingsMenuButton = new ImageButton("tile_button", "ui/buttons/icons/gear.png", relZeroX(), -relZeroY() - 80, 80, 80, 8, 8, 64, 64, () -> {
            currentMenu = settingsButtons;
        });

        severeConfirmationMenu = new Menu<>(new MenuButton[] {
                new MenuButton("button", "Save & Exit", 0, 0, 256, 96,
                        () -> {
                            LevelLoader.saveLevel(file, level);
                            LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
                        }),
                new MenuButton("button", "Exit", 0, 0, 256, 96,
                        () -> LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData))),
                new MenuButton("button", "Cancel", 0, 0, 256, 96,
                        () -> currentMenu = null)
        }, 256, 96, 16);

        severesevereConfirmationMenu = new Menu<>(new MenuButton[] {
                new MenuButton("button", "Save & Close", 0, 0, 256, 96,
                        () -> {
                            LevelLoader.saveLevel(file, level);
                            LittleH.program.dispose();
                            System.exit(0);
                        }),
                new MenuButton("button", "Close", 0, 0, 256, 96,
                        () -> {
                            LittleH.program.dispose();
                            System.exit(0);
                        }),
                new MenuButton("button", "Cancel", 0, 0, 256, 96,
                        () -> currentMenu = null)
        }, 256, 96, 16);

        ImageButton imageButton = new ImageButton(null, "ui/buttons/icons/pencil.png", new Rectangle(0, 0, 64, 64), 0, 0, 64, 64, () -> setToolIndex(0));
        imageButton.setHoverText("Pencil");

        toolButtons = new Menu<>(new ImageButton[] {
                imageButton,
                imageButton.quickCreate("ui/buttons/icons/eraser.png", "Eraser", () -> setToolIndex(1)),
                imageButton.quickCreate("ui/buttons/icons/pen.png", "Pen", () -> setToolIndex(2)),
                imageButton.quickCreate("ui/buttons/icons/paint_can.png", "Paint Can", () -> setToolIndex(3)),
                imageButton.quickCreate("ui/buttons/icons/color_picker.png", "Color Picker", () -> setToolIndex(4)),
                imageButton.quickCreate("ui/buttons/icons/selector.png", "Selector", () -> setToolIndex(5)),
                imageButton.quickCreate("ui/buttons/icons/h.png", "H", () -> setToolIndex(6)),
        }, 64, 64, 8);

        settingsButtons = new Menu<>(new ImageButton[] {
                imageButton.quickCreate("ui/buttons/icons/background.png", "Change background", () -> {
                    currentMenu = backgroundMenu;
                }),
                imageButton.quickCreate("ui/buttons/icons/clock.png", "Set time limit", () -> {
                    timeQuery = new TypingQuery("Time limit (leave blank for infinite): ", this.level.timeLimit == -1 ? "" : "" + this.level.timeLimit, new Rectangle(-384, -96, 768, 192));
                }),
                imageButton.quickCreate("ui/buttons/icons/background_visible.png", "Background visibility", () -> backgroundVisible = !backgroundVisible),
                imageButton.quickCreate("ui/buttons/icons/double_jump.png", "Toggle double jumping", () -> {
                    level.mapData.insertValue("double_jumping", SabValue.fromBool(!level.mapData.getValue("double_jumping").asBool()));
                    editor.setSaved(false);
                }),
                imageButton.quickCreate("ui/buttons/icons/wall_jump.png", "Toggle wall sliding", () -> {
                    level.mapData.insertValue("wall_sliding", SabValue.fromBool(!level.mapData.getValue("wall_sliding").asBool()));
                    editor.setSaved(false);
                }),
                imageButton.quickCreate("ui/buttons/icons/slide.png", "Toggle crouching", () -> {
                    level.mapData.insertValue("crouching", SabValue.fromBool(!level.mapData.getValue("crouching").asBool()));
                    editor.setSaved(false);
                }),
                imageButton.quickCreate("ui/buttons/icons/magnifying_glass.png", "Toggle magnifying glass", () -> {
                    level.mapData.insertValue("look_around", SabValue.fromBool(!level.mapData.getValue("look_around").asBool()));
                    editor.setSaved(false);
                }),
                imageButton.quickCreate("ui/buttons/icons/edit_background.png", "Toggle edit background", () -> {
                    editingBackground = !editingBackground;
                    if (editingBackground)
                        editor = backgroundEditor;
                    else
                        editor = levelEditor;
                }),
                imageButton.quickCreate("ui/buttons/icons/floppy_disc.png", "Save", () -> {
                    LevelLoader.saveLevel(file, level);
                    editor.setSaved(true);
                }),
                new ImageButton(null, "ui/buttons/icons/gear.png", 0, 0, 64, 64, 0, 0, 64, 64, () -> currentMenu = null),
        }, 64, 64, 16);

        imageButton = new ImageButton("square_button", null, new Rectangle(0, 0, 256 + 16, 144 + 24),
                8, 16, 256, 144, () -> setToolIndex(0));

        backgroundMenu = new Menu<>(new MenuButton[]{
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[0] + "/whole.png", "Mountains", () -> changeLevelBackground("mountains")),
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[1] + "/whole.png", "Cold Mountains", () -> changeLevelBackground("cold_mountains")),
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[2] + "/whole.png", "Desert", () -> changeLevelBackground("desert")),
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[3] + "/whole.png", "Cave", () -> changeLevelBackground("cave")),
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[4] + "/whole.png", "Tundra", () -> changeLevelBackground("tundra")),
                imageButton.quickCreate("backgrounds/" + Level.backgrounds[5] + "/whole.png", "Hyperspace", () -> changeLevelBackground("hyperspace"))
        }, 256 + 16, 144 + 24, 16);

        setToolIndex(0);
        SoundEngine.playMusic("menu/building_song.ogg");
    }

    @Override
    public void start() {
        level.init();
    }

    private void changeLevelBackground(String background) {
        editor.setSaved(false);
        level.changeBackground(background);
    }

    @Override
    public void update() {
        if (extraQuery != null) {
            extraQuery.update();
            if (extraQuery.complete) {
                if (extraQuery.accepted) {
                    modifiedExtraTile.extra = extraQuery.getQuery();
                    editor.setSaved(false);
                }
                extraQuery = null;
            }
            return;
        }
        if (timeQuery != null) {
            return;
        }

        if (!level.inGame()) {
            if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    float scalar = 1;
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                    scalar = 4;
                else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
                    scalar = 0.25f;
                if (ControlInputs.isJustPressed(Control.UP) || ControlInputs.getPressedFor(Control.UP) > 30) {
                    camera.targetPosition.y += 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Control.DOWN) || ControlInputs.getPressedFor(Control.DOWN) > 30) {
                    camera.targetPosition.y -= 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Control.LEFT) || ControlInputs.getPressedFor(Control.LEFT) > 30) {
                    camera.targetPosition.x -= 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Control.RIGHT) || ControlInputs.getPressedFor(Control.RIGHT) > 30) {
                    camera.targetPosition.x += 64 * scalar;
                }
            }

            if (!MouseUtil.isLeftMouseDown()) {
                canPlaceTiles = true;
            }
            settingsMenuButton.setPosition(-relZeroX() - 80, relZeroY());
            tileMenuButton.setPosition(relZeroX(), -relZeroY() - 80);
            previousMousePosition = mouseWorldPosition.cpy();
            mousePosition = MouseUtil.getMousePosition();
            mouseWorldPosition = MouseUtil.getDynamicMousePosition();
            tiledMousePosition.x = (int) Math.floor(mouseWorldPosition.x / 64);
            tiledMousePosition.y = (int) Math.floor(mouseWorldPosition.y / 64);
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                canPlaceTiles = false;
                if (MouseUtil.isLeftMouseDown()) {
                    camera.targetPosition.sub(MouseUtil.getMouseDelta().scl(camera.zoom));
                }
            }
            if (currentMenu == tileButtons) {
                tileButtons.setMenuRectangle(relZeroX(), -relZeroY(), program.getHeight(), false);
                if (tileButtons.contains(mousePosition)) {
                    canPlaceTiles = false;
                }

                if (tileButtons.subMenu != null) {
                    Rectangle tileMenuRect = tileButtons.getMenuRectangle();
                    tileButtons.subMenu.setMenuRectangle((int) (tileMenuRect.x + tileMenuRect.width), -relZeroY() - 72, program.getHeight() - 64, false);
                }
            } else if (currentMenu == settingsButtons) {
                settingsButtons.forEach(button -> button.update());
                settingsButtons.setMenuRectangle(-relZeroX(), relZeroY() + 88, 0, true);
                if (settingsButtons.contains(mousePosition)) {
                    canPlaceTiles = false;
                }
            } else if (currentMenu == backgroundMenu) {
                backgroundMenu.forEach(button -> button.update());
                backgroundMenu.setMenuRectangle(relZeroX() + 32, -relZeroY() - 32, program.getHeight() - 128, false);
                if (backgroundMenu.contains(mousePosition)) {
                    canPlaceTiles = false;
                }
            } else if (currentMenu == severeConfirmationMenu) {
                severeConfirmationMenu.forEach(button -> button.update());
                severeConfirmationMenu.setMenuRectangle(0, 32 + 16 - 64, 0, false);
                severeConfirmationMenu.setCenterX(0);
                canPlaceTiles = false;
            } else if (currentMenu == severesevereConfirmationMenu) {
                severesevereConfirmationMenu.forEach(button -> button.update());
                severesevereConfirmationMenu.setMenuRectangle(0, 32 + 16 - 64, 0, false);
                severesevereConfirmationMenu.setCenterX(0);
                canPlaceTiles = false;
            }

            tileMenuButton.update();
            settingsMenuButton.update();
            if (tileMenuButton.contains(mousePosition) || settingsMenuButton.contains(mousePosition)) {
                canPlaceTiles = false;
            }

            toolButtons.setMenuRectangle(-relZeroX(), -relZeroY(), program.getHeight() - 72, true);

            if (toolButtons.contains(mousePosition)) {
                canPlaceTiles = false;
            }

            if (canPlaceTiles) {
                if (MouseUtil.isLeftMouseDown()) {
                    useTool(true);
                } else if (MouseUtil.isRightMouseDown()) {
                    useTool(false);
                }
            }
        } else {
            level.update();
        }
    }

    public void useTool(boolean leftMouse) {
        if (leftMouse) {
            switch (toolButtons.itemIndex) {
                // Pencil
                case 0 -> {
                    Tile tileAt = editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                    Tile newTile = tileSelection[tileButtons.itemIndex - 1];
                    if (Tile.imagesEqual(tileAt, newTile)) {
                        if (tileAt.hasTag("string_picker")) {
                            if (MouseUtil.leftMouseJustPressed()) {
                                if (tileAt.ignoreTiling && tileAt.tileType != newTile.tileType) {
                                    newTile.extra = tileAt.extra;
                                    editor.addTile(newTile, tileAt.x, tileAt.y, true);
                                }
                                startExtraQuery(editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y));
                            }
                        } else {
                            if (tileAt.ignoreTiling && tileAt.tileType != newTile.tileType) {
                                editor.addTile(newTile, tileAt.x, tileAt.y, true);
                            }
                        }
                    }

                    if (!Tile.imagesEqual(tileAt, newTile)) {
                        Point offset = editor.addTile(newTile, tiledMousePosition.x, tiledMousePosition.y, true);
                        Tile tile = editor.getTileAt(tiledMousePosition.x + offset.x, tiledMousePosition.y + offset.y);
                        if (tile.hasTag("string_picker") && (tile.extra == null || tile.extra.isBlank())) {
                            startExtraQuery(tile);
                        }
                    }
                }
                // Eraser (just simulates a right click (erases)
                case 1 -> {
                    useTool(false);
                }
                // Color picker
                case 2 -> {
                    Point tiledPreviousMousePosition = new Point();
                    tiledPreviousMousePosition.x = (int) (previousMousePosition.x / 64);
                    tiledPreviousMousePosition.y = (int) (previousMousePosition.y / 64);
                    editor.drawLine(tileSelection[tileButtons.itemIndex - 1], tiledMousePosition.x, tiledMousePosition.y,
                            tiledPreviousMousePosition.x, tiledPreviousMousePosition.y);
                }
                // Fill tool
                case 3 -> {
                    if (MouseUtil.leftMouseJustPressed()) {
                        Tile fillTile = tileSelection[tileButtons.itemIndex - 1];
                        if (!Tile.tilesEqual(editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y), fillTile))
                            editor.fill(fillTile, tiledMousePosition.x, tiledMousePosition.y);
                    }
                }
                // Color picker
                case 4 -> {
                    int tileCount = tileSelection.length;
                    Tile tileAt = editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                    if (tileAt != null) {
                        for (int i = 0; i < tileCount; i++) {
                            if (tileSelection[i].image.equals(tileAt.image)) {
                                tileButtons.itemIndex = i + 1;
                                Tile buttonTile = ((TileButton) tileButtons.getSelectedItem()).getTile();
                                if (buttonTile.ignoreTiling)
                                    buttonTile.setTileType(tileAt.tileType);
                                buttonTile.extra = tileAt.extra;
                            }
                        }
                    }
                }
                // Selection tool
                case 5 -> {
                    if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || editor.isMoving()) {
                        editor.setSelectionPosition(mouseWorldPosition);
                    } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || editor.isNudging()) {
                        editor.updateSelectionPosition(mouseWorldPosition);
                    } else {
                        editor.select(tiledMousePosition);
                    }
                }
                //
                case 6 -> {
                    level.startGame(new Point(tiledMousePosition));
                    lastPlayer = level.player;
                    canPlaceTiles = false;
                }
            }
        } else {
            switch (toolButtons.itemIndex) {
                case 2 -> {
                    Point tiledPreviousMousePosition = new Point();
                    tiledPreviousMousePosition.x = (int) (previousMousePosition.x / 64);
                    tiledPreviousMousePosition.y = (int) (previousMousePosition.y / 64);
                    editor.drawLine(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y,
                            tiledPreviousMousePosition.x, tiledPreviousMousePosition.y);
                }
                case 3 -> {
                    editor.fill(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y);
                }
                default -> {
                    editor.addTile(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y, true);
                }
            }
        }
    }

    public void negativeResize(int x, int y) {
        if (lastPlayer != null) {
            for (Point point : lastPlayer.previousPositions) {
                point.x += x * 64;
                point.y += y * 64;
            }
        }
    }

    private void startExtraQuery(Tile tileAt) {
        String prompt = "Set the arbitrary extra data for this tile \n ";
        String regex = null;
        int maxSize = -1;
        if (tileAt.hasTag("text")) {
            prompt = "Set the text for this tile: \n ";
        } else if (tileAt.hasTag("dialogue")) {
            prompt = "Set the path to the dialogue file \n ";
        } else if (tileAt.hasTag("render_color")) {
            prompt = "Set the hex code for this tile \n #";
            regex = "([EnemyA-EnemyF]|[0-9]|[a-f])";
            maxSize = 6;
        }
        extraQuery = new TypingQuery(prompt, tileAt.extra == null ? "" : tileAt.extra, new Rectangle(-384, -256, 384 * 2, 256 * 2), true);
        if (regex != null)
            extraQuery.setRegex(regex);
        if (maxSize > 0)
            extraQuery.setAbsoluteMaxSize(maxSize);
        modifiedExtraTile = tileAt;
    }

    public void setToolIndex(int index) {
        toolButtons.setItemIndex(index);
        switch (index) {
            case 0 -> {
                Cursors.switchCursor("pencil");
            }
            case 1 -> {
                Cursors.switchCursor("eraser");
            }
            case 2 -> {
                Cursors.switchCursor("pen");
            }
            case 3 -> {
                Cursors.switchCursor("paint_can");
            }
            case 4 -> {
                Cursors.switchCursor("color_picker");
            }
            case 5 -> {
                Cursors.switchCursor("cursor");
            }
            case 6 -> {
                Cursors.switchCursor("h");
            }
        }
    }

    @Override
    public void keyUp(int keycode) {
        // Cursors for selection mode
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.ALT_LEFT) {
            if (toolButtons.itemIndex == 5) {
                Cursors.switchCursor("cursor");
            }
        }
    }

    @Override
    public void keyDown(int keycode) {
        // Cursors for selection mode
        if (!level.inGame()) {
            if (keycode == Input.Keys.ALT_LEFT && Cursors.cursorIsNot("drag_hand")) {
                if (toolButtons.itemIndex == 5) {
                    Cursors.switchCursor("move_arrow");
                }
            } else if (keycode == Input.Keys.CONTROL_LEFT && Cursors.cursorIsNot("move_arrow")) {
                if (toolButtons.itemIndex == 5) {
                    Cursors.switchCursor("drag_hand");
                }
            }
        }
        if (extraQuery != null) {
            if (keycode == Input.Keys.ENTER) {
                extraQuery.complete(true);
            } else if (keycode == Input.Keys.ESCAPE) {
                extraQuery.complete(false);
            } else {
                extraQuery.updateQueryKey(keycode, 256, false);
            }
            return;
        }
        if (timeQuery != null) {
            if (keycode == Input.Keys.ENTER) {
                String timeLimit = timeQuery.getQuery();
                if (timeLimit.equals("")) timeLimit = "-1";
                level.mapData.insertValue("time_limit", timeLimit);
                timeQuery = null;
                editor.setSaved(false);
            } else {
                timeQuery.updateQueryKey(keycode, 4, false);
            }
            return;
        }
        if (!level.inGame()) {
            if (keycode > 7 && keycode < 17) {
                int keyNum = keycode - 8;
                if (keyNum < toolButtons.items.length) {
                    setToolIndex(keyNum);
                }
            } else if (keycode == Input.Keys.R) {
                setToolIndex(0);
            } else if (keycode == Input.Keys.E) {
                setToolIndex(1);
            } else if (keycode == Input.Keys.G) {
                setToolIndex(2);
            } else if (keycode == Input.Keys.F) {
                setToolIndex(3);
            } else if (keycode == Input.Keys.T) {
                setToolIndex(4);
            } else if (keycode == Input.Keys.C) {
                setToolIndex(5);
            }
        } else {
            if (keycode == Input.Keys.K)
                level.suicide();
        }
        switch (keycode) {
            case Input.Keys.S :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    LevelLoader.saveLevel(file, level);
                    editor.setSaved(true);
                }
                break;
            case Input.Keys.C :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && editor.hasSelection()) {
                    editor.copySelection();
                }
                break;
            case Input.Keys.V :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && editor.hasSelection() && editor.hasTileSelection()) {
                    editor.pasteSelection();
                }
                break;
            case Input.Keys.X :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && editor.hasSelection()) {
                    editor.copySelection();
                    editor.deleteSelection();
                }
                break;
            case Input.Keys.D :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    editor.endSelection();
                }
                break;
            case Input.Keys.Z :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    editor.undo();
                }
                break;
            case Input.Keys.Y :
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    editor.redo();
                }
                break;
            case Input.Keys.BACKSPACE:
                editor.deleteSelection();
                break;
            case Input.Keys.ESCAPE :
                if (editor.hasSelection()) {
                    editor.endSelection();
                } else if (!level.inGame() && level.escapePressed()) {
                    if (!editor.saved)
                        confirmExit();
                    else
                        LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
                } else if (level.escapePressed()) {
                    level.endGame();
                    setToolIndex(toolButtons.itemIndex);
                    SoundEngine.playMusic("menu/building_song.ogg");
                }
                break;
            case Input.Keys.ENTER :
                level.enterPressed();
                if (level.hasDialogue())
                    return;
                if (!level.inGame())
                    startTesting();
                break;
            case Input.Keys.TAB :
                editingBackground = !editingBackground;
                editor = editingBackground ? backgroundEditor : levelEditor;
                break;
        }
    }

    @Override
    public void keyTyped(char character) {
        if (extraQuery != null) {
            extraQuery.updateQueryChar(character, 256);
            return;
        }
        if (timeQuery != null) {
            timeQuery.updateQueryChar(character, 4, "([0-9])");
            return;
        }
    }

    @Override
    public void mouseDown(int button) {
        if (currentMenu == tileButtons) {
            if (tileButtons.getMenuRectangle().contains(mousePosition)) {
                if (button == 0) {
                    int newIndex = tileButtons.getOverlappedElement(MouseUtil.getMousePosition());
                    if (newIndex > 0 && newIndex != tileButtons.itemIndex) {
                        tileButtons.setItemIndex(newIndex);
                        Tile tile = tileSelection[tileButtons.itemIndex - 1];
                        if (tile.hasTag("property_set")) {
                            MenuButton[] buttons = new MenuButton[tile.getPropertyCount() + 1];
                            buttons[0] = new ImageButton(null, "back_arrow.png", 0, 0, 64, 64, 0, 0, 64, 64, null);
                            for (int i = 1; i < buttons.length; i++) {
                                Tile newTile = tile.copy();
                                newTile.setTileType(i - 1);
                                buttons[i] = new TileButton(newTile, 0, 0, false);
                            }
                            Menu<MenuButton> propertyMenu = new Menu<>(buttons, 64, 64, 8);
                            propertyMenu.itemIndex = tile.tileType + 1;
                            tileButtons.setSubMenu(propertyMenu);
                        } else {
                            tileButtons.subMenu = null;
                        }
                    } else if (newIndex == 0) {
                        currentMenu = null;
                    }
                }
                canPlaceTiles = false;
            }
            if (tileButtons.subMenu != null) {
                if (tileButtons.subMenu.getMenuRectangle().contains(mousePosition)) {
                    if (button == 0) {
                        int newIndex = tileButtons.subMenu.getOverlappedElement(MouseUtil.getMousePosition());
                        if (newIndex > 0 && newIndex != tileButtons.subMenu.itemIndex) {
                            tileButtons.subMenu.setItemIndex(newIndex);
                            TileButton tileButton = (TileButton) tileButtons.getSelectedItem();
                            tileButton.getTile().setTileType(newIndex - 1);
                        } else if (newIndex == 0) {
                            tileButtons.subMenu = null;
                        }
                    }
                }
            }
        } else {
            tileMenuButton.mouseClicked();
        }

        if (currentMenu == settingsButtons) {
            if (settingsButtons.getMenuRectangle().contains(mousePosition)) {
                if (button == 0) {
                    int index = settingsButtons.getOverlappedElement(mousePosition);
                    if (index != -1) {
                        settingsButtons.items[index].mouseClicked();
                    }
                }
                canPlaceTiles = false;
            }
        } else {
            settingsMenuButton.mouseClicked();
        }

        if (currentMenu == backgroundMenu) {
            backgroundMenu.forEach((menuButton) -> {
                menuButton.mouseClicked();
            });
        }

        if (toolButtons.getMenuRectangle().contains(mousePosition)) {
            if (button == 0) {
                int newIndex = toolButtons.getOverlappedElement(MouseUtil.getMousePosition());
                if (newIndex != -1) {
                    toolButtons.setItemIndex(newIndex);
                    setToolIndex(toolButtons.itemIndex);
                }
            }
        }
    }

    public void confirmExit() {
        currentMenu = severeConfirmationMenu;
    }

    public void confirmProgramExit() {
        currentMenu = severesevereConfirmationMenu;
    }

    public void startTesting() {
        Point startPos = null;
        for (Tile tile : level.allTiles) {
            if (tile.hasTag("start")) {
                startPos = new Point(tile.x, tile.y);
                break;
            }
        }
        if (startPos != null) {
            level.startGame(startPos);
            lastPlayer = level.player;
        } else level.startPopup("Please add a spawn point (the red triangle)", 240);
    }

    @Override
    public void mouseUp(int button) {
        editor.selectionReleased();
        if (extraQuery != null) {
            extraQuery.mouseClicked();
        } else {
            if (currentMenu == severeConfirmationMenu) {
                severeConfirmationMenu.forEach(MenuButton::mouseClicked);
            } else if (currentMenu == severesevereConfirmationMenu) {
                severesevereConfirmationMenu.forEach(MenuButton::mouseClicked);
            }
        }
    }

    @Override
    public void mouseScrolled(float amountY) {
        camera.targetPosition.sub(mousePosition.cpy().scl(amountY).scl(addZoom(amountY / 2f)));
    }

    // Returns by how much zoom changed
    public float addZoom(float zoom) {
        float zoomBefore = camera.targetZoom;
        camera.targetZoom += zoom;
        camera.targetZoom = Math.max(Math.min(camera.targetZoom, 16f), 1f);
        return Math.abs(zoomBefore - camera.targetZoom);
    }

    @Override
    public void close() {
        setToolIndex(5);
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    public void resetToolCursor() {
        setToolIndex(toolButtons.itemIndex);
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        // Draw level

        camera.updateCamera();

        mousePosition = MouseUtil.getMousePosition();
        mouseWorldPosition = MouseUtil.getDynamicMousePosition();
        tiledMousePosition.x = (int) Math.floor(mouseWorldPosition.x / 64);
        tiledMousePosition.y = (int) Math.floor(mouseWorldPosition.y / 64);

        if (backgroundVisible) level.renderBackground(g);

        LittleH.program.useDynamicCamera();

        level.render(g, editingBackground);

        LittleH.program.useDynamicCamera();

        if (!level.inGame()) {
            if (lastPlayer != null) {
                for (int i = 0; i < lastPlayer.previousPositions.size(); i++) {
                    Point point = lastPlayer.previousPositions.get(i);
                    if (i % 5 == 0) {
                        g.draw(Images.getImage("h_trace.png"), point.x - 8, point.y, 64, 64);
                    }
                }
            }
            if (canPlaceTiles && Cursors.cursorIsNot("move_arrow", "drag_hand")) {
                Tile tileAt = level.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                if (tileAt != null && tileAt.hasTag("string_picker")) {
                    if (tileAt.extra != null) {
                        g.drawString(tileAt.extra, LittleH.font, tileAt.x * 64 + 32, tileAt.y * 64 + 96, LittleH.defaultFontScale * 0.85f, 0);
                    }
                }
                g.draw(Images.getImage("tiles/selector.png"), tiledMousePosition.x * 64, tiledMousePosition.y * 64, 64, 64);
            }
        }

        // Draw selection
        editor.renderSelection(g);

        LittleH.program.useStaticCamera();


        // Don't draw menus while in-game
        if (level.inGame()) {
            level.renderHUD(g);
            return;
        }

        // Draw menus
        Rectangle[] itemButtons;

        toolButtons.renderMenuRectangle(g, Patch.get("menu_flat"));

        g.setColor(Images.quickAlpha(0.5f));
        if (!editor.saved)
            g.draw(Images.getImage("ui/buttons/icons/floppy_disc.png"), -relZeroX() - 128 - 24, -relZeroY() - 8 - 64, 64, 64);
        g.resetColor();

        itemButtons = toolButtons.getItemButtons();

        for (int i = 0; i < toolButtons.items.length; i++) {
            MenuButton button = toolButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
            Rectangle item = itemButtons[i];
            if (toolButtons.itemIndex == i) {
                g.draw(Images.getImage("ui/selected_tile.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
            }
        }

        tileMenuButton.render(g);
        g.drawImage(tileSelection[tileButtons.itemIndex - 1].getImage(), tileMenuButton.x + 8, tileMenuButton.y + 8, 64, 64, tileSelection[tileButtons.itemIndex - 1].getDrawSection());
        settingsMenuButton.render(g);

        if (currentMenu == tileButtons) {
            tileButtons.renderMenuRectangle(g, Patch.get("menu_flat"));
            itemButtons = tileButtons.getItemButtons();

            for (int i = 0; i < tileButtons.items.length; i++) {
                MenuButton button = tileButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
                Rectangle item = itemButtons[i];
                if (tileButtons.itemIndex == i) {
                    g.draw(Images.getImage("ui/selected_tile.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                }
            }

            if (tileButtons.subMenu != null) {
                tileButtons.subMenu.renderMenuRectangle(g, Patch.get("menu_flat"));
                itemButtons = tileButtons.subMenu.getItemButtons();

                for (int i = 0; i < tileButtons.subMenu.items.length; i++) {
                    MenuButton button = tileButtons.subMenu.getItem(i);
                    button.setPosition(itemButtons[i].getPosition(new Vector2()));
                    button.render(g);
                    Rectangle item = itemButtons[i];
                    if (tileButtons.subMenu.itemIndex == i) {
                        g.draw(Images.getImage("ui/selected_property.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                }
            }
        } else if (currentMenu == settingsButtons) {
            settingsButtons.renderMenuRectangle(g, Patch.get("menu_flat"));
            itemButtons = settingsButtons.getItemButtons();

            String[] levelOptions = {
                    "double_jumping",
                    "wall_sliding",
                    "crouching",
                    "look_around"
            };

            for (int i = 0; i < settingsButtons.items.length; i++) {
                MenuButton button = settingsButtons.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
                if (i == 2) {
                    if (backgroundVisible) {
                        g.draw(Images.getImage("ui/level_setting_on.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    } else {
                        g.draw(Images.getImage("ui/level_setting_off.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                } else if (i == 7) {
                    if (editingBackground) {
                        g.draw(Images.getImage("ui/level_setting_on.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    } else {
                        g.draw(Images.getImage("ui/level_setting_off.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                } else if (i > 2 && i < 7) {
                    int levelSettingIndex = i - 3;
                    if (level.mapData.getValue(levelOptions[levelSettingIndex]).asBool()) {
                        g.draw(Images.getImage("ui/level_setting_on.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    } else {
                        g.draw(Images.getImage("ui/level_setting_off.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                }
            }
        } else if (currentMenu == backgroundMenu) {
            backgroundMenu.renderMenuRectangle(g, Patch.get("menu_flat"));
            itemButtons = backgroundMenu.getItemButtons();

            for (int i = 0; i < backgroundMenu.items.length; i++) {
                MenuButton button = backgroundMenu.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
            }
        } else if (currentMenu == severeConfirmationMenu) {
            Rectangle rect = new Rectangle(severeConfirmationMenu.getMenuRectangle());
            rect.height += 128;
            g.drawPatch(Patch.get("menu_flat"), rect, 8);

            g.drawString("You are about to exit without saving!", LittleH.font,
                    rect.x + rect.width / 2, rect.y + rect.height - 64, LittleH.defaultFontScale, 0);

            itemButtons = severeConfirmationMenu.getItemButtons();

            for (int i = 0; i < severeConfirmationMenu.items.length; i++) {
                MenuButton button = severeConfirmationMenu.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
            }
        } else if (currentMenu == severesevereConfirmationMenu) {
            Rectangle rect = new Rectangle(severeConfirmationMenu.getMenuRectangle());
            rect.height += 128;
            g.drawPatch(Patch.get("menu_flat"), rect, 8);

            g.drawString("You are about to close the program without saving!", LittleH.font,
                    rect.x + rect.width / 2, rect.y + rect.height - 64, LittleH.defaultFontScale, 0);

            itemButtons = severeConfirmationMenu.getItemButtons();

            for (int i = 0; i < severeConfirmationMenu.items.length; i++) {
                MenuButton button = severeConfirmationMenu.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
            }
        }

        if (timeQuery != null) {
            timeQuery.render(g);
        }

        if (extraQuery != null) {
            extraQuery.render(g);
        }
    }
}
