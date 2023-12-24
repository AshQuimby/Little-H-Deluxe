package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelEditor;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabValue;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LevelEditorMenu extends MainMenu {
    public boolean canPlaceTiles;
    private List<List<Tile>> tileSelections;
    private TypingQuery timeQuery;
    private TypingQuery extraQuery;
    private ImageButton tileMenuButton;
    private ImageButton settingsMenuButton;
    private Menu<MenuButton> tileButtons;
    private Menu<ImageButton> toolButtons;
    private Menu<ImageButton> settingsButtons;
    private Menu<MenuButton> backgroundMenu;
    private Menu<MenuButton> severeConfirmationMenu;
    private Menu<MenuButton> severeSevereConfirmationMenu;
    private Menu<? extends MenuButton> currentMenu;
    private static DynamicCamera camera = LittleH.program.dynamicCamera;
    private File file;
    private boolean editingBackground;
    private Level level;
    private LevelEditor editor;
    private LevelEditor levelEditor;
    private LevelEditor backgroundEditor;
    private Vector2 mousePosition;
    private Vector2 mouseWorldPosition;
    private Vector2 previousMousePosition;
    private Point tiledMousePosition;
    private Player lastPlayer;
    private boolean backgroundVisible;
    private Tile modifiedExtraTile;
    private int tileIndex;
    private int tileSelectionIndex;

    public LevelEditorMenu(File file, Level level) {
        this.file = file;
        this.level = level;
        backgroundVisible = Settings.localSettings.backgroundVisibility.value;
        levelEditor = new LevelEditor(level, "normal");
        backgroundEditor = new LevelEditor(level, "background");
        editor = levelEditor;
        mousePosition = new Vector2();
        mouseWorldPosition = new Vector2();
        tiledMousePosition = new Point();

        currentMenu = tileButtons;
        tileMenuButton = new ImageButton("tile_button", "settings_dots.png", relZeroX(), -relZeroY() - 80, 80, 80, 0, 0, 0, 0, () -> {
            currentMenu = tileButtons;
        });
        settingsMenuButton = new ImageButton("tile_button", "ui/buttons/icons/gear.png", relZeroX(), -relZeroY() - 80, 80, 80, 8, 8, 64, 64, () -> {
            currentMenu = settingsButtons;
        });

        severeConfirmationMenu = new Menu<>(new MenuButton[]{
                new MenuButton("button", "Save & Exit", 0, 0, 256, 96,
                        () -> {
                            LevelLoader.saveLevel(file, level);
                            LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
                        }),
                new MenuButton("button", "Exit", 0, 0, 256, 96,
                        () -> stop()),
                new MenuButton("button", "Cancel", 0, 0, 256, 96,
                        () -> currentMenu = null)
        }, 256, 96, 16);

        severeSevereConfirmationMenu = new Menu<>(new MenuButton[]{
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

        toolButtons = new Menu<>(new ImageButton[]{
                imageButton,
                imageButton.quickCreate("ui/buttons/icons/eraser.png", "Eraser", () -> setToolIndex(1)),
                imageButton.quickCreate("ui/buttons/icons/pen.png", "Pen", () -> setToolIndex(2)),
                imageButton.quickCreate("ui/buttons/icons/paint_can.png", "Paint Can", () -> setToolIndex(3)),
                imageButton.quickCreate("ui/buttons/icons/color_picker.png", "Color Picker", () -> setToolIndex(4)),
                imageButton.quickCreate("ui/buttons/icons/selector.png", "Selector", () -> setToolIndex(5)),
                imageButton.quickCreate("ui/buttons/icons/h.png", "H", () -> setToolIndex(6)),
        }, 64, 64, 8);

        settingsButtons = new Menu<>(new ImageButton[]{
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
        SoundEngine.playMusic(Settings.localSettings.buildingSong.value);
    }

    public void stop() {
        LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
        setToolIndex(5);
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    public void resetTileMenu() {
        List<Tile> tileSelection = getTileSelection();

        MenuButton[] buttons = new MenuButton[tileSelection.size() + (Settings.localSettings.dividedTileSelection.value ? 3 : 1)];
        buttons[0] = new ImageButton(null, "settings_dots.png", 0, 0, 64, 64,
                0, 0, 64, 64, null);
        for (int i = 1; i < buttons.length - (Settings.localSettings.dividedTileSelection.value ? 2 : 0); i++) {
            tileSelection.get(i - 1).setTags();
            buttons[i] = new TileButton(tileSelection.get(i - 1), 0, 0);
        }

        if (Settings.localSettings.dividedTileSelection.value) {
            buttons[buttons.length - 2] = new ImageButton(null, "back_arrow.png", 0, 0, 64, 64,
                    0, 0, 64, 64, null);

            buttons[buttons.length - 1] = new ImageButton(null, "forward_arrow.png", 0, 0, 64, 64,
                    0, 0, 64, 64, null);
        }

        tileButtons = new Menu<MenuButton>(buttons, 64, 64, 8) {
            @Override
            public MenuButton setItemIndex(int i) {
                if (i > 0) {
                    setTileIndex(i - 1);
                    return super.setItemIndex(i);
                }
                return getSelectedItem();
            }
        };
        tileButtons.setItemIndex(1);
        currentMenu = tileButtons;
    }

    public List<Tile> getTileSelection() {
        return tileSelections.get(tileSelectionIndex);
    }

    public Tile getEditorTile() {
        return getTileSelection().get(tileIndex);
    }

    @Override
    public void start() {
        setToolIndex(toolButtons.itemIndex);
        level.init();

        tileSelections = new ArrayList<>();
        String[] selections = new String[]{
                "level_editor/ground.sab",
                "level_editor/level.sab",
                "level_editor/danger.sab",
                "level_editor/pickups.sab",
                "level_editor/special.sab",
                "level_editor/decoration.sab",
                "level_editor/developer.sab"
        };
        List<Tile> selection = new ArrayList<>();
        for (int i = 0; i < selections.length; i++) {
            if (Settings.localSettings.dividedTileSelection.value)
                selection = new ArrayList<>();
            SabData data = SabReader.read(LevelEditorMenu.class.getResourceAsStream("/scripts/" + selections[i]));
            for (String string : data.getValues().keySet()) {
                selection.add(new Tile(data.getRawValue(string)));
            }
            if (Settings.localSettings.dividedTileSelection.value)
                tileSelections.add(selection);
        }
        if (!Settings.localSettings.dividedTileSelection.value)
            tileSelections.add(selection);

        resetTileMenu();
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
                if (ControlInputs.isPressed("drag_camera"))
                    scalar = 4;
                else if (ControlInputs.isPressed("move_selection"))
                    scalar = 0.25f;
                if (ControlInputs.isJustPressed(Controls.UP) || ControlInputs.getPressedFor(Controls.UP) > 30) {
                    camera.targetPosition.y += 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Controls.DOWN) || ControlInputs.getPressedFor(Controls.DOWN) > 30) {
                    camera.targetPosition.y -= 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Controls.LEFT) || ControlInputs.getPressedFor(Controls.LEFT) > 30) {
                    camera.targetPosition.x -= 64 * scalar;
                }
                if (ControlInputs.isJustPressed(Controls.RIGHT) || ControlInputs.getPressedFor(Controls.RIGHT) > 30) {
                    camera.targetPosition.x += 64 * scalar;
                }
            } else {
                if (ControlInputs.isJustPressed("undo") || ControlInputs.getPressedFor("undo") > 30) {
                    editor.undo();

                } else if (ControlInputs.isJustPressed("redo") || ControlInputs.getPressedFor("redo") > 30) {
                    editor.redo();
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
            if (ControlInputs.isPressed("drag_camera")) {
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
            } else if (currentMenu == severeSevereConfirmationMenu) {
                severeSevereConfirmationMenu.forEach(button -> button.update());
                severeSevereConfirmationMenu.setMenuRectangle(0, 32 + 16 - 64, 0, false);
                severeSevereConfirmationMenu.setCenterX(0);
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
                case 0:
                    Tile tileAt = editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                    Tile newTile = getEditorTile();
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
                        if (tile.hasTag("string_picker") && (tile.extra == null || tile.extra.isEmpty())) {
                            startExtraQuery(tile);
                        }
                    }
                    break;
                // Eraser (just simulates a right click (erases)
                case 1:
                    useTool(false);
                    break;
                // Pen
                case 2:
                    Point tiledPreviousMousePosition = new Point();
                    tiledPreviousMousePosition.x = (int) (previousMousePosition.x / 64);
                    tiledPreviousMousePosition.y = (int) (previousMousePosition.y / 64);
                    editor.drawLine(getEditorTile(), tiledMousePosition.x, tiledMousePosition.y,
                            tiledPreviousMousePosition.x, tiledPreviousMousePosition.y);
                    break;
                // Fill tool
                case 3:
                    if (MouseUtil.leftMouseJustPressed()) {
                        Tile fillTile = getEditorTile();
                        if (!Tile.tilesEqual(editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y), fillTile))
                            editor.fill(fillTile, tiledMousePosition.x, tiledMousePosition.y);
                    }
                    break;
                // Color picker
                case 4:
                    tileAt = editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                    for (List<Tile> selection : tileSelections) {
                        for (Tile tile : selection) {
                            if (Tile.imagesEqual(tile, tileAt)) {
                                tileSelectionIndex = tileSelections.indexOf(selection);
                                setTileIndex(selection.indexOf(tile));
                                tileButtons.setItemIndex(tileIndex + 1);
                                if (tileAt.ignoreTiling)
                                    tile.tileType = tileAt.tileType;
                                if (tile.hasTag("property_set")) {
                                    MenuButton[] buttons = new MenuButton[tile.getPropertyCount() + 1];
                                    buttons[0] = new ImageButton(null, "back_arrow.png", 0, 0, 64, 64, 0, 0, 64, 64, null);
                                    for (int i = 1; i < buttons.length; i++) {
                                        newTile = tile.copy();
                                        newTile.setTileType(i - 1);
                                        buttons[i] = new TileButton(newTile, 0, 0, false);
                                    }
                                    Menu<MenuButton> propertyMenu = new Menu<>(buttons, 64, 64, 8);
                                    propertyMenu.itemIndex = tile.tileType + 1;
                                    propertyMenu.setMenuRectangle((int) (tileButtons.getMenuRectangle().x + tileButtons.getMenuRectangle().width), -relZeroY() - 72, program.getHeight() - 64, false);
                                    tileButtons.setSubMenu(propertyMenu);

                                    TileButton tileButton = (TileButton) tileButtons.getSelectedItem();
                                    tileButton.getTile().setTileType(tileAt.tileType);
                                } else {
                                    tileButtons.subMenu = null;
                                }
                                if (tileAt.extra != null && !tileAt.extra.isEmpty())
                                    tile.extra = tileAt.extra;
                                break;
                            }
                        }
                    }
                    break;
                // Selection tool
                case 5:
                    if (ControlInputs.isPressed("move_selection") || editor.isMoving()) {
                        editor.setSelectionPosition(mouseWorldPosition);
                    } else if (ControlInputs.isPressed("drag_selection") || editor.isNudging()) {
                        editor.updateSelectionPosition(mouseWorldPosition);
                    } else {
                        editor.select(tiledMousePosition);
                    }
                    break;
                // Quick test
                case 6:
                    level.startGame(new Point(tiledMousePosition));
                    lastPlayer = level.player;
                    canPlaceTiles = false;
                    break;
            }
        } else {
            switch (toolButtons.itemIndex) {
                case 2 :
                    Point tiledPreviousMousePosition = new Point();
                    tiledPreviousMousePosition.x = (int) (previousMousePosition.x / 64);
                    tiledPreviousMousePosition.y = (int) (previousMousePosition.y / 64);
                    editor.drawLine(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y,
                            tiledPreviousMousePosition.x, tiledPreviousMousePosition.y);
                    break;
                case 3:
                    editor.fill(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y);
                    break;
                default:
                    editor.addTile(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y, true);
                    break;
            }
        }
    }

    private void setTileIndex(int index) {
        tileIndex = index;
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
            regex = "([A-F]|[0-9]|[a-f])";
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
            case 0 :
                Cursors.switchCursor("pencil");
                break;
            case 1 :
                Cursors.switchCursor("eraser");
                break;
            case 2 :
                Cursors.switchCursor("pen");
                break;
            case 3 :
                Cursors.switchCursor("paint_can");
                break;
            case 4 :
                Cursors.switchCursor("color_picker");
                break;
            case 5 :
                Cursors.switchCursor("cursor");
                break;
            case 6 :
                Cursors.switchCursor("h");
        }
    }

    @Override
    public void keyUp(int keycode) {
        // Cursors for selection mode
        if (ControlInputs.isJustReleased("move_selection") || ControlInputs.isJustReleased("drag_selection")) {
            if (toolButtons.itemIndex == 5) {
                Cursors.switchCursor("cursor");
            }
        }
    }

    @Override
    public void keyDown(int keycode) {
        // Cursors for selection mode
        if (!level.inGame()) {
            if (ControlInputs.isJustPressed("move_selection") && Cursors.cursorIsNot("drag_hand")) {
                if (toolButtons.itemIndex == 5) {
                    Cursors.switchCursor("move_arrow");
                }
            } else if (ControlInputs.isJustPressed("drag_selection") && Cursors.cursorIsNot("move_arrow")) {
                if (toolButtons.itemIndex == 5) {
                    Cursors.switchCursor("drag_hand");
                }
            }
        }
        if (extraQuery != null) {
            if (ControlInputs.isJustPressed("select")) {
                extraQuery.complete(true);
            } else if (ControlInputs.isJustPressed("return")) {
                extraQuery.complete(false);
            } else {
                extraQuery.updateQueryKey(keycode, 256, false);
            }
            return;
        }
        if (timeQuery != null) {
            if (ControlInputs.isJustPressed("select")) {
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
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (!level.inGame()) {
                // Tool Hotkeys
                if (ControlInputs.isJustPressed("pencil")) {
                    setToolIndex(0);
                } else if (ControlInputs.isJustPressed("eraser")) {
                    setToolIndex(1);
                } else if (ControlInputs.isJustPressed("pen")) {
                    setToolIndex(2);
                } else if (ControlInputs.isJustPressed("fill")) {
                    setToolIndex(3);
                } else if (ControlInputs.isJustPressed("eyedropper")) {
                    setToolIndex(4);
                } else if (ControlInputs.isJustPressed("selection")) {
                    setToolIndex(5);
                } else if (ControlInputs.isJustPressed("quick_test")) {
                    setToolIndex(6);
                } else if (Settings.localSettings.dividedTileSelection.value) {
                    if (ControlInputs.isJustPressed("prev_selection")) {
                        tileSelectionIndex = (tileSelectionIndex - 1) % tileSelections.size();
                        SoundEngine.playSound("blip.ogg");
                        resetTileMenu();
                    } else if (ControlInputs.isJustPressed("next_selection")) {
                        tileSelectionIndex = (tileSelectionIndex + 1) % tileSelections.size();
                        SoundEngine.playSound("blip.ogg");
                        resetTileMenu();
                    }
                }
            } else {
                if (ControlInputs.isJustPressed("suicide"))
                    level.suicide();
            }
        }

        // Commands
        if (ControlInputs.isJustPressed("save")) {
            LevelLoader.saveLevel(file, level);
            editor.setSaved(true);

        } else if (ControlInputs.isJustPressed("copy")) {
            editor.copySelection();

        } else if (ControlInputs.isJustPressed("paste")) {
            editor.pasteSelection();

        } else if (ControlInputs.isJustPressed("cut")) {
            editor.copySelection();
            editor.deleteSelection();

        } else if (ControlInputs.isJustPressed("end_selection")) {
            editor.endSelection();

        } else if (ControlInputs.isJustPressed("delete_selection")) {
            editor.deleteSelection();

        } else if (ControlInputs.isJustPressed("return")) {
            if (editor.hasSelection()) {
                editor.endSelection();
            } else if (!level.inGame() && level.escapePressed()) {
                program.switchMenu(new EditorPauseMenu(this));
            } else if (level.escapePressed()) {
                level.endGame();
                setToolIndex(toolButtons.itemIndex);
                SoundEngine.playMusic(Settings.localSettings.buildingSong.value);
            }
            editor.endSelection();

        } else if (ControlInputs.isJustPressed("playtest")) {
            level.enterPressed();
            if (level.hasDialogue())
                return;
            if (!level.inGame())
                startTesting();

        } else if (ControlInputs.isJustPressed("toggle_layer")) {
            editingBackground = !editingBackground;
            editor = editingBackground ? backgroundEditor : levelEditor;

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
                    if (newIndex == 0) {
                        currentMenu = null;
                    } else if (Settings.localSettings.dividedTileSelection.value) {
                        if (newIndex == tileButtons.items.length - 2) {
                            tileSelectionIndex = (tileSelectionIndex - 1) % tileSelections.size();
                            SoundEngine.playSound("blip.ogg");
                            resetTileMenu();
                        } else if (newIndex == tileButtons.items.length - 1) {
                            tileSelectionIndex = (tileSelectionIndex + 1) % tileSelections.size();
                            SoundEngine.playSound("blip.ogg");
                            resetTileMenu();
                        }
                    } else {
                        tileButtons.setItemIndex(newIndex);
                        Tile tile = getEditorTile();
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
        currentMenu = severeSevereConfirmationMenu;
    }

    public void startTesting() {
        Point startPos = null;
        for (Tile tile : level.getBaseLayer().allTiles) {
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
            } else if (currentMenu == severeSevereConfirmationMenu) {
                severeSevereConfirmationMenu.forEach(MenuButton::mouseClicked);
            }

            if (!level.inGame()) {
                level.mouseUp();
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
                Tile tileAt = level.getTileAt("normal", tiledMousePosition.x, tiledMousePosition.y);
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
        g.drawImage(getEditorTile().getImage(), tileMenuButton.x + 8, tileMenuButton.y + 8, 64, 64, getEditorTile().getDrawSection());
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
        } else if (currentMenu == severeSevereConfirmationMenu) {
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
