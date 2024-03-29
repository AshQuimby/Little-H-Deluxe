package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.screen.*;
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

public class LevelEditorScreen extends Screen {
    public boolean canPlaceTiles;
    private List<List<Tile>> tileSelections;
    private List<Tile> wireSelection;
    private TypingQuery timeQuery;
    private TypingQuery extraQuery;
    private final ImageButton tileScreenButton;
    private final ImageButton settingsScreenButton;
    private Menu<ScreenButton> tileButtons;
    private Menu<ScreenButton> normalTileButtons;
    private Menu<ScreenButton> wiringTileButtons;
    private final Menu<ImageButton> toolButtons;
    private Menu<ImageButton> settingsButtons;
    private Menu<ScreenButton> backgroundMenu;
    private final Menu<ScreenButton> severeConfirmationMenu;
    private final Menu<ScreenButton> severeSevereConfirmationMenu;
    private Menu<? extends ScreenButton> currentMenu;
    private static final DynamicCamera camera = LittleH.program.dynamicCamera;
    private final File file;
    private boolean editingBackground;
    private boolean wiringMode;
    private final Level level;
    private LevelEditor editor;
    private final LevelEditor levelEditor;
    private final LevelEditor backgroundEditor;
    private final LevelEditor wiringEditor;
    private Vector2 mousePosition;
    private Vector2 mouseWorldPosition;
    private Vector2 previousMousePosition;
    private final Point tiledMousePosition;
    private Point lineToolOrigin;
    private boolean deleteLine;
    private Player lastPlayer;
    private boolean backgroundVisible;
    private Tile modifiedExtraTile;
    private int tileIndex;
    private int tileSelectionIndex;

    public LevelEditorScreen(File file, Level level) {
        this.file = file;
        this.level = level;
        backgroundVisible = Settings.localSettings.backgroundVisibility.value;
        levelEditor = new LevelEditor(level, "normal");
        backgroundEditor = new LevelEditor(level, "background");
        wiringEditor = new LevelEditor(level, "wiring");
        editor = levelEditor;
        mousePosition = new Vector2();
        mouseWorldPosition = new Vector2();
        tiledMousePosition = new Point();

        currentMenu = tileButtons;
        tileScreenButton = new ImageButton("tile_button", "settings_dots.png", relZeroX(), -relZeroY() - 80, 80, 80, 0, 0, 0, 0, () -> {
            currentMenu = tileButtons;
        });
        settingsScreenButton = new ImageButton("tile_button", "ui/buttons/icons/gear.png", relZeroX(), -relZeroY() - 80, 80, 80, 8, 8, 64, 64, () -> {
            currentMenu = settingsButtons;
        });

        severeConfirmationMenu = new Menu<>(new ScreenButton[]{
                new ScreenButton("button", "Save & Exit", 0, 0, 256, 96,
                        () -> {
                            LevelLoader.saveLevel(file, level);
                            LittleH.program.switchScreen(new LevelOptionsScreen(file, level.mapData));
                        }),
                new ScreenButton("button", "Exit", 0, 0, 256, 96,
                        () -> stop()),
                new ScreenButton("button", "Cancel", 0, 0, 256, 96,
                        () -> currentMenu = null)
        }, 256, 96, 16);

        severeSevereConfirmationMenu = new Menu<>(new ScreenButton[]{
                new ScreenButton("button", "Save & Close", 0, 0, 256, 96,
                        () -> {
                            LevelLoader.saveLevel(file, level);
                            LittleH.program.dispose();
                            System.exit(0);
                        }),
                new ScreenButton("button", "Close", 0, 0, 256, 96,
                        () -> {
                            LittleH.program.dispose();
                            System.exit(0);
                        }),
                new ScreenButton("button", "Cancel", 0, 0, 256, 96,
                        () -> currentMenu = null)
        }, 256, 96, 16);

        ImageButton imageButton = new ImageButton(null, "ui/buttons/icons/pencil.png", new Rectangle(0, 0, 64, 64), 0, 0, 64, 64, () -> setToolIndex(0));
        imageButton.setHoverText("Pencil");

        toolButtons = new Menu<>(new ImageButton[]{
                imageButton,
                imageButton.quickCreate("ui/buttons/icons/eraser.png", "Eraser", () -> setToolIndex(1)),
                imageButton.quickCreate("ui/buttons/icons/pen.png", "Pen", () -> setToolIndex(2)),
                imageButton.quickCreate("ui/buttons/icons/paint_can.png", "Paint Can", () -> setToolIndex(3)),
                imageButton.quickCreate("ui/buttons/icons/line.png", "Line Tool", () -> setToolIndex(4)),
                imageButton.quickCreate("ui/buttons/icons/color_picker.png", "Color Picker", () -> setToolIndex(5)),
                imageButton.quickCreate("ui/buttons/icons/selector.png", "Selector", () -> setToolIndex(6)),
                imageButton.quickCreate("ui/buttons/icons/h.png", "H", () -> setToolIndex(7)),
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
                imageButton.quickCreate("ui/buttons/icons/edit_background.png", "Toggle background editor", () -> {
                    editingBackground = !editingBackground;
                    if (editingBackground)
                        editor = backgroundEditor;
                    else
                        editor = levelEditor;
                }),
                imageButton.quickCreate("ui/buttons/icons/wiring_mode.png", "Toggle wiring mode", () -> {
                    wiringMode = !wiringMode;
                    if (wiringMode)
                        enterWiringMode();
                    else
                        exitWiringMode();
                }),
                imageButton.quickCreate("ui/buttons/icons/floppy_disc.png", "Save", () -> {
                    LevelLoader.saveLevel(file, level);
                    editor.setSaved(true);
                }),
                new ImageButton(null, "ui/buttons/icons/gear.png", 0, 0, 64, 64, 0, 0, 64, 64, () -> currentMenu = null),
        }, 64, 64, 16);

        imageButton = new ImageButton("square_button", null, new Rectangle(0, 0, 256 + 16, 144 + 24),
                8, 16, 256, 144, () -> setToolIndex(0));

        backgroundMenu = new Menu<>(new ScreenButton[]{
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

    @Override
    public void onLoad() {
        level.init();
    }

    private void enterWiringMode() {
        editor = wiringEditor;
        tileButtons = wiringTileButtons;
        resetTileScreen();
    }

    private void exitWiringMode() {
        if (editingBackground)
            editor = backgroundEditor;
        else
            editor = levelEditor;
        tileButtons = normalTileButtons;
        resetTileScreen();
    }

    public void stop() {
        LittleH.program.switchScreen(new LevelOptionsScreen(file, level.mapData));
        setToolIndex(5);
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    public void resetTileScreen() {
        if (wiringMode) {
            List<Tile> tileSelection = wireSelection;

            ScreenButton[] buttons = new ScreenButton[tileSelection.size() + 1];
            buttons[0] = new ImageButton(null, "settings_dots.png", 0, 0, 64, 64,
                    0, 0, 64, 64, null);

            for (int i = 1; i < buttons.length; i++) {
                tileSelection.get(i - 1).setTags();
                buttons[i] = new TileButton(tileSelection.get(i - 1), 0, 0);
            }

            wiringTileButtons = new Menu<>(buttons, 64, 64, 8) {
                @Override
                public ScreenButton setItemIndex(int i) {
                    if (i > 0) {
                        setTileIndex(i - 1);
                        return super.setItemIndex(i);
                    }
                    return getSelectedItem();
                }
            };
            wiringTileButtons.setItemIndex(1);
            tileButtons = wiringTileButtons;
        } else {
            List<Tile> tileSelection = getTileSelection();

            ScreenButton[] buttons = new ScreenButton[tileSelection.size() + (Settings.localSettings.dividedTileSelection.value ? 3 : 1)];
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

            normalTileButtons = new Menu<>(buttons, 64, 64, 8) {
                @Override
                public ScreenButton setItemIndex(int i) {
                    if (i > 0) {
                        setTileIndex(i - 1);
                        return super.setItemIndex(i);
                    }
                    return getSelectedItem();
                }
            };
            normalTileButtons.setItemIndex(1);
            tileButtons = normalTileButtons;
        }

        currentMenu = tileButtons;
    }

    public List<Tile> getTileSelection() {
        if (wiringMode) return wireSelection;
        return tileSelections.get(tileSelectionIndex);
    }

    public Tile getEditorTile() {
        return getTileSelection().get(tileIndex);
    }

    @Override
    public void start() {
        setToolIndex(toolButtons.itemIndex);

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
            SabData data = SabReader.read(LevelEditorScreen.class.getResourceAsStream("/scripts/" + selections[i]));
            for (String string : data.getValues().keySet()) {
                selection.add(new Tile(data.getRawValue(string)));
            }
            if (Settings.localSettings.dividedTileSelection.value)
                tileSelections.add(selection);
        }
        if (!Settings.localSettings.dividedTileSelection.value)
            tileSelections.add(selection);

        wireSelection = new ArrayList<>();
        SabData data = SabReader.read(LevelEditorScreen.class.getResourceAsStream("/scripts/level_editor/wiring.sab"));
        for (String string : data.getValues().keySet()) {
            wireSelection.add(new Tile(data.getRawValue(string)));
        }

        resetTileScreen();
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
                if (ControlInput.localControls.isPressed("drag_camera"))
                    scalar = 4;
                else if (ControlInput.localControls.isPressed("move_selection"))
                    scalar = 0.25f;
                if (ControlInput.localControls.isJustPressed(Controls.UP) || ControlInput.localControls.getPressedFor(Controls.UP) > 30) {
                    camera.targetPosition.y += 64 * scalar;
                }
                if (ControlInput.localControls.isJustPressed(Controls.DOWN) || ControlInput.localControls.getPressedFor(Controls.DOWN) > 30) {
                    camera.targetPosition.y -= 64 * scalar;
                }
                if (ControlInput.localControls.isJustPressed(Controls.LEFT) || ControlInput.localControls.getPressedFor(Controls.LEFT) > 30) {
                    camera.targetPosition.x -= 64 * scalar;
                }
                if (ControlInput.localControls.isJustPressed(Controls.RIGHT) || ControlInput.localControls.getPressedFor(Controls.RIGHT) > 30) {
                    camera.targetPosition.x += 64 * scalar;
                }
            } else {
                if (ControlInput.localControls.isJustPressed("undo") || ControlInput.localControls.getPressedFor("undo") > 30) {
                    editor.undo();

                } else if (ControlInput.localControls.isJustPressed("redo") || ControlInput.localControls.getPressedFor("redo") > 30) {
                    editor.redo();
                }
            }
            settingsScreenButton.setPosition(-relZeroX() - 80, relZeroY());
            tileScreenButton.setPosition(relZeroX(), -relZeroY() - 80);
            previousMousePosition = mouseWorldPosition.cpy();
            mousePosition = MouseUtil.getMousePosition();
            mouseWorldPosition = MouseUtil.getDynamicMousePosition();
            tiledMousePosition.x = (int) Math.floor(mouseWorldPosition.x / 64);
            tiledMousePosition.y = (int) Math.floor(mouseWorldPosition.y / 64);

            if (!MouseUtil.isLeftMouseDown() && !MouseUtil.isRightMouseDown()) {
                canPlaceTiles = true;
                if (lineToolOrigin != null) {
                    if (deleteLine)
                        editor.drawLine(new Tile("delete"), tiledMousePosition.x, tiledMousePosition.y, lineToolOrigin.x, lineToolOrigin.y);
                    else
                        editor.drawLine(getEditorTile(), tiledMousePosition.x, tiledMousePosition.y, lineToolOrigin.x, lineToolOrigin.y);
                    lineToolOrigin = null;
                }
            }

            if (ControlInput.localControls.isPressed("drag_camera")) {
                canPlaceTiles = false;
                if (MouseUtil.isLeftMouseDown()) {
                    camera.targetPosition.sub(MouseUtil.getMouseDelta().scl(camera.zoom));
                }
            }
            if (currentMenu == tileButtons) {
                tileButtons.setScreenRectangle(relZeroX(), -relZeroY(), program.getHeight(), false);
                if (tileButtons.contains(mousePosition)) {
                    canPlaceTiles = false;
                }

                if (tileButtons.subMenu != null) {
                    Rectangle tileScreenRect = tileButtons.getScreenRectangle();
                    tileButtons.subMenu.setScreenRectangle((int) (tileScreenRect.x + tileScreenRect.width), -relZeroY() - 72, program.getHeight() - 64, false);
                }
            } else if (currentMenu == settingsButtons) {
                settingsButtons.forEach(button -> button.update());
                settingsButtons.setScreenRectangle(-relZeroX(), relZeroY() + 88, 0, true);
                if (settingsButtons.contains(mousePosition)) {
                    canPlaceTiles = false;
                }
            } else if (currentMenu == backgroundMenu) {
                backgroundMenu.forEach(button -> button.update());
                backgroundMenu.setScreenRectangle(relZeroX() + 32, -relZeroY() - 32, program.getHeight() - 128, false);
                if (backgroundMenu.contains(mousePosition)) {
                    canPlaceTiles = false;
                }
            } else if (currentMenu == severeConfirmationMenu) {
                severeConfirmationMenu.forEach(button -> button.update());
                severeConfirmationMenu.setScreenRectangle(0, 32 + 16 - 64, 0, false);
                severeConfirmationMenu.setCenterX(0);
                canPlaceTiles = false;
            } else if (currentMenu == severeSevereConfirmationMenu) {
                severeSevereConfirmationMenu.forEach(button -> button.update());
                severeSevereConfirmationMenu.setScreenRectangle(0, 32 + 16 - 64, 0, false);
                severeSevereConfirmationMenu.setCenterX(0);
                canPlaceTiles = false;
            }

            tileScreenButton.update();
            settingsScreenButton.update();
            if (tileScreenButton.contains(mousePosition) || settingsScreenButton.contains(mousePosition)) {
                canPlaceTiles = false;
            }

            toolButtons.setScreenRectangle(-relZeroX(), -relZeroY(), program.getHeight() - 72, true);

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
        if (!canPlaceTiles)
            lineToolOrigin = null;
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
                // Eraser (just simulates a right click (erases))
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
                // Line tool
                case 4:
                    if (MouseUtil.leftMouseJustPressed()) {
                        lineToolOrigin = new Point(tiledMousePosition);
                        deleteLine = false;
                    }
                    break;
                // Color picker
                case 5:
                    tileAt = editor.getTileAt(tiledMousePosition.x, tiledMousePosition.y);
                    for (List<Tile> selection : tileSelections) {
                        for (Tile tile : selection) {
                            if (Tile.imagesEqual(tile, tileAt)) {
                                tileSelectionIndex = tileSelections.indexOf(selection);
                                setTileIndex(selection.indexOf(tile));
                                tileButtons.setItemIndex(tileIndex + 1);
                                if (tileAt.ignoreTiling)
                                    tile.tileType = tileAt.tileType;
                                if (tile.hasTag("states")) {
                                    ScreenButton[] buttons = new ScreenButton[tile.getStateCount() + 1];
                                    buttons[0] = new ImageButton(null, "back_arrow.png", 0, 0, 64, 64, 0, 0, 64, 64, null);
                                    for (int i = 1; i < buttons.length; i++) {
                                        newTile = tile.copy();
                                        newTile.setTileType(i - 1);
                                        buttons[i] = new TileButton(newTile, 0, 0);
                                    }
                                    Menu<ScreenButton> propertyMenu = new Menu<>(buttons, 64, 64, 8);
                                    propertyMenu.itemIndex = tile.tileType + 1;
                                    propertyMenu.setScreenRectangle((int) (tileButtons.getScreenRectangle().x + tileButtons.getScreenRectangle().width), -relZeroY() - 72, program.getHeight() - 64, false);
                                    tileButtons.setSubScreen(propertyMenu);

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
                case 6:
                    if (ControlInput.localControls.isPressed("move_selection") || editor.isMoving()) {
                        editor.setSelectionPosition(mouseWorldPosition);
                    } else if (ControlInput.localControls.isPressed("drag_selection") || editor.isNudging()) {
                        editor.updateSelectionPosition(mouseWorldPosition);
                    } else {
                        editor.select(tiledMousePosition);
                    }
                    break;
                // Quick test
                case 7:
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
                // Line tool
                case 4:
                    if (lineToolOrigin == null) {
                        lineToolOrigin = new Point(tiledMousePosition);
                        deleteLine = true;
                    }
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
        String[] parameters = tileAt.tags.getTagParameters("string_picker");
        String prompt = parameters[0];
        String regex = parameters[1];
        System.out.println(regex);
        int maxSize = Integer.parseInt(parameters[2]);

        prompt = prompt.replace("\\n", "\n");
        extraQuery = new TypingQuery(prompt, tileAt.extra == null ? "" : tileAt.extra, new Rectangle(-384, -256, 384 * 2, 256 * 2), true);
        if (regex.length() > 0)
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
                Cursors.switchCursor("line");
                break;
            case 5 :
                Cursors.switchCursor("color_picker");
                break;
            case 6 :
                Cursors.switchCursor("cursor");
                break;
            case 7 :
                Cursors.switchCursor("h");
        }
    }

    @Override
    public void keyUp(int keycode) {
        // Cursors for selection mode
        if (ControlInput.localControls.isJustReleased("move_selection") || ControlInput.localControls.isJustReleased("drag_selection")) {
            if (toolButtons.itemIndex == 6) {
                Cursors.switchCursor("cursor");
            }
        }
    }

    @Override
    public void keyDown(int keycode) {
        // Cursors for selection mode
        if (!level.inGame()) {
            if (ControlInput.localControls.isJustPressed("move_selection") && Cursors.cursorIsNot("drag_hand")) {
                if (toolButtons.itemIndex == 6) {
                    Cursors.switchCursor("move_arrow");
                }
            } else if (ControlInput.localControls.isJustPressed("drag_selection") && Cursors.cursorIsNot("move_arrow")) {
                if (toolButtons.itemIndex == 6) {
                    Cursors.switchCursor("drag_hand");
                }
            }
        }
        if (extraQuery != null) {
            if (ControlInput.localControls.isJustPressed("select")) {
                extraQuery.complete(true);
            } else if (ControlInput.localControls.isJustPressed("return")) {
                extraQuery.complete(false);
            } else {
                extraQuery.updateQueryKey(keycode, 256, false);
            }
            return;
        }
        if (timeQuery != null) {
            if (ControlInput.localControls.isJustPressed("select")) {
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
                if (ControlInput.localControls.isJustPressed("pencil")) {
                    setToolIndex(0);
                } else if (ControlInput.localControls.isJustPressed("eraser")) {
                    setToolIndex(1);
                } else if (ControlInput.localControls.isJustPressed("pen")) {
                    setToolIndex(2);
                } else if (ControlInput.localControls.isJustPressed("fill")) {
                    setToolIndex(3);
                } else if (ControlInput.localControls.isJustPressed("line")) {
                    setToolIndex(4);
                }  else if (ControlInput.localControls.isJustPressed("eyedropper")) {
                    setToolIndex(5);
                } else if (ControlInput.localControls.isJustPressed("selection")) {
                    setToolIndex(6);
                } else if (ControlInput.localControls.isJustPressed("quick_test")) {
                    setToolIndex(7);
                } else if (Settings.localSettings.dividedTileSelection.value) {
                    if (ControlInput.localControls.isJustPressed("prev_selection")) {
                        tileSelectionIndex = (tileSelectionIndex - 1) % tileSelections.size();
                        SoundEngine.playSound("blip.ogg");
                        resetTileScreen();
                    } else if (ControlInput.localControls.isJustPressed("next_selection")) {
                        tileSelectionIndex = (tileSelectionIndex + 1) % tileSelections.size();
                        SoundEngine.playSound("blip.ogg");
                        resetTileScreen();
                    }
                }
            } else {
                if (ControlInput.localControls.isJustPressed("suicide"))
                    level.suicide();
            }
        }

        // Commands
        if (ControlInput.localControls.isJustPressed("save")) {
            LevelLoader.saveLevel(file, level);
            editor.setSaved(true);

        } else if (ControlInput.localControls.isJustPressed("copy")) {
            editor.copySelection();
        } else if (ControlInput.localControls.isJustPressed("paste")) {
            editor.pasteSelection();

        } else if (ControlInput.localControls.isJustPressed("cut")) {
            editor.copySelection();
            editor.deleteSelection();

        } else if (ControlInput.localControls.isJustPressed("end_selection")) {
            editor.endSelection();

        } else if (ControlInput.localControls.isJustPressed("delete_selection")) {
            editor.deleteSelection();

        } else if (ControlInput.localControls.isJustPressed("return")) {
            if (editor.hasSelection()) {
                editor.endSelection();
            } else if (!level.inGame() && level.escapePressed()) {
//                program.switchScreen(new EditorPauseScreen(this));
            } else if (level.escapePressed()) {
                level.endGame();
                setToolIndex(toolButtons.itemIndex);
                SoundEngine.playMusic(Settings.localSettings.buildingSong.value);
                level.timeLimit = level.mapData.getValue("time_limit").asInt();
            }
            editor.endSelection();

        } else if (ControlInput.localControls.isJustPressed("playtest")) {
            level.enterPressed();
            if (level.hasDialogue())
                return;
            if (!level.inGame())
                startTesting();

        } else if (ControlInput.localControls.isJustPressed("toggle_wiring")) {
            wiringMode = !wiringMode;
            if (wiringMode)
                enterWiringMode();
            else
                exitWiringMode();
        } else if (ControlInput.localControls.isJustPressed("toggle_layer")) {
            if (!wiringMode) {
                editingBackground = !editingBackground;
                editor = editingBackground ? backgroundEditor : levelEditor;
            }
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
            if (tileButtons.getScreenRectangle().contains(mousePosition)) {
                if (button == 0) {
                    int newIndex = tileButtons.getOverlappedElement(MouseUtil.getMousePosition());
                    if (newIndex == 0) {
                        currentMenu = null;
                    } else if (Settings.localSettings.dividedTileSelection.value) {
                        if (newIndex == tileButtons.items.length - 2) {
                            tileSelectionIndex = (tileSelectionIndex - 1) % tileSelections.size();
                            SoundEngine.playSound("blip.ogg");
                            resetTileScreen();
                        } else if (newIndex == tileButtons.items.length - 1) {
                            tileSelectionIndex = (tileSelectionIndex + 1) % tileSelections.size();
                            SoundEngine.playSound("blip.ogg");
                            resetTileScreen();
                        }
                    } else {
                        tileButtons.setItemIndex(newIndex);
                        Tile tile = getEditorTile();
                        if (tile.hasTag("states")) {
                            ScreenButton[] buttons = new ScreenButton[tile.getStateCount() + 1];
                            buttons[0] = new ImageButton(null, "back_arrow.png", 0, 0, 64, 64, 0, 0, 64, 64, null);
                            for (int i = 1; i < buttons.length; i++) {
                                Tile newTile = tile.copy();
                                newTile.setTileType(i - 1);
                                buttons[i] = new TileButton(newTile, 0, 0);
                            }
                            Menu<ScreenButton> propertyMenu = new Menu<>(buttons, 64, 64, 8);
                            propertyMenu.itemIndex = tile.tileType + 1;
                            tileButtons.setSubScreen(propertyMenu);
                        } else {
                            tileButtons.subMenu = null;
                        }
                    }
                }
                canPlaceTiles = false;
            }
            if (tileButtons.subMenu != null) {
                if (tileButtons.subMenu.getScreenRectangle().contains(mousePosition)) {
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
            tileScreenButton.mouseClicked();
        }

        if (currentMenu == settingsButtons) {
            if (settingsButtons.getScreenRectangle().contains(mousePosition)) {
                if (button == 0) {
                    int index = settingsButtons.getOverlappedElement(mousePosition);
                    if (index != -1) {
                        settingsButtons.items[index].mouseClicked();
                    }
                }
                canPlaceTiles = false;
            }
        } else {
            settingsScreenButton.mouseClicked();
        }

        if (currentMenu == backgroundMenu) {
            backgroundMenu.forEach((screenButton) -> {
                screenButton.mouseClicked();
            });
        }

        if (toolButtons.getScreenRectangle().contains(mousePosition)) {
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
            if (ControlInput.localControls.isPressed("shift"))
                level.ignoreDialogue();
            else
                level.resumeDialogue();
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
                severeConfirmationMenu.forEach(ScreenButton::mouseClicked);
            } else if (currentMenu == severeSevereConfirmationMenu) {
                severeSevereConfirmationMenu.forEach(ScreenButton::mouseClicked);
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

        mousePosition = MouseUtil.getMousePosition();
        mouseWorldPosition = MouseUtil.getDynamicMousePosition();
        tiledMousePosition.x = (int) Math.floor(mouseWorldPosition.x / 64);
        tiledMousePosition.y = (int) Math.floor(mouseWorldPosition.y / 64);

        if (backgroundVisible) level.renderBackground(g);
        else {
            program.useStaticCamera();
            g.setColor(0, 0, 0, 1);
            g.draw(Images.getImage("pixel.png"), Screen.relZeroX(), Screen.relZeroY(), program.getWidth(), program.getHeight());
            g.resetColor();
        }

        program.useDynamicCamera();

        level.render(g, editingBackground, wiringMode, getEditorTile().hasTag("wiring_component"));

        program.useDynamicCamera();

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
                Tile tileAt = level.getTileAt(editingBackground ? "background" : "normal", tiledMousePosition.x, tiledMousePosition.y);
                if (tileAt != null && tileAt.extra != null) {
                    if (tileAt.hasTag("prop")) {
                        Texture tex = Images.getImage("props/" + tileAt.extra + ".png");
                        g.draw(tex, tileAt.x * 64, tileAt.y * 64, tex.getWidth() * 8, tex.getHeight() * 8);
                    }
                    g.drawString(tileAt.extra, LittleH.font, tileAt.x * 64 + 32, tileAt.y * 64 + 96, LittleH.defaultFontScale * 0.85f, 0);
                }
                g.draw(Images.getImage("ui/selector.png"), tiledMousePosition.x * 64, tiledMousePosition.y * 64, 64, 64);
                if (lineToolOrigin != null) {
                    List<Point> line = editor.getLinePoints(tiledMousePosition.x, tiledMousePosition.y, lineToolOrigin.x, lineToolOrigin.y);
                    for (Point point : line)
                        g.draw(Images.getImage("ui/selector.png"), point.x * 64, point.y * 64, 64, 64);
                }
            }
        }

        // Draw selection
        editor.renderSelection(g);

        LittleH.program.useStaticCamera();


        // Don't draw screens while in-game
        if (level.inGame()) {
            level.renderHUD(g);
            return;
        }

        // Draw screens
        Rectangle[] itemButtons;

        toolButtons.renderScreenRectangle(g, Patch.get("menu_flat"));

        g.setColor(Images.quickAlpha(0.5f));
        if (!editor.saved)
            g.draw(Images.getImage("ui/buttons/icons/floppy_disc.png"), -relZeroX() - 128 - 24, -relZeroY() - 8 - 64, 64, 64);
        g.resetColor();

        itemButtons = toolButtons.getItemButtons();

        for (int i = 0; i < toolButtons.items.length; i++) {
            ScreenButton button = toolButtons.getItem(i);
            button.setPosition(itemButtons[i].getPosition(new Vector2()));
            button.render(g);
            Rectangle item = itemButtons[i];
            if (toolButtons.itemIndex == i) {
                g.draw(Images.getImage("ui/selected_tile.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
            }
        }

        tileScreenButton.render(g);
        g.drawImage(getEditorTile().getImage(), tileScreenButton.x + 8, tileScreenButton.y + 8, 64, 64, getEditorTile().getDrawSection());
        settingsScreenButton.render(g);

        if (currentMenu == tileButtons) {
            tileButtons.renderScreenRectangle(g, Patch.get("menu_flat"));
            itemButtons = tileButtons.getItemButtons();

            for (int i = 0; i < tileButtons.items.length; i++) {
                ScreenButton button = tileButtons.getItem(i);
                button.setPosition(itemButtons[i].getPosition(new Vector2()));
                button.render(g);
                Rectangle item = itemButtons[i];
                if (tileButtons.itemIndex == i) {
                    g.draw(Images.getImage("ui/selected_tile.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                }
            }

            if (tileButtons.subMenu != null) {
                tileButtons.subMenu.renderScreenRectangle(g, Patch.get("menu_flat"));
                itemButtons = tileButtons.subMenu.getItemButtons();

                for (int i = 0; i < tileButtons.subMenu.items.length; i++) {
                    ScreenButton button = tileButtons.subMenu.getItem(i);
                    button.setPosition(itemButtons[i].getPosition(new Vector2()));
                    button.render(g);
                    Rectangle item = itemButtons[i];
                    if (tileButtons.subMenu.itemIndex == i) {
                        g.draw(Images.getImage("ui/selected_property.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                }
            }
        } else if (currentMenu == settingsButtons) {
            settingsButtons.renderScreenRectangle(g, Patch.get("menu_flat"));
            itemButtons = settingsButtons.getItemButtons();

            String[] levelOptions = {
                    "double_jumping",
                    "wall_sliding",
                    "crouching",
                    "look_around"
            };

            for (int i = 0; i < settingsButtons.items.length; i++) {
                ScreenButton button = settingsButtons.getItem(i);
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
                } else if (i == 8) {
                    if (wiringMode) {
                        g.draw(Images.getImage("ui/level_setting_on.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    } else {
                        g.draw(Images.getImage("ui/level_setting_off.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                }  else if (i > 2 && i < 7) {
                    int levelSettingIndex = i - 3;
                    if (level.mapData.getValue(levelOptions[levelSettingIndex]).asBool()) {
                        g.draw(Images.getImage("ui/level_setting_on.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    } else {
                        g.draw(Images.getImage("ui/level_setting_off.png"), item.x - 8, item.y - 8, item.width + 16, item.height + 16);
                    }
                }
            }
        } else if (currentMenu == backgroundMenu) {
            backgroundMenu.renderScreenRectangle(g, Patch.get("menu_flat"));
            itemButtons = backgroundMenu.getItemButtons();

            for (int i = 0; i < backgroundMenu.items.length; i++) {
                ScreenButton button = backgroundMenu.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
            }
        } else if (currentMenu == severeConfirmationMenu) {
            Rectangle rect = new Rectangle(severeConfirmationMenu.getScreenRectangle());
            rect.height += 128;
            g.drawPatch(Patch.get("menu_flat"), rect, 8);

            g.drawString("You are about to exit without saving!", LittleH.font,
                    rect.x + rect.width / 2, rect.y + rect.height - 64, LittleH.defaultFontScale, 0);

            itemButtons = severeConfirmationMenu.getItemButtons();

            for (int i = 0; i < severeConfirmationMenu.items.length; i++) {
                ScreenButton button = severeConfirmationMenu.getItem(i);
                Rectangle item = itemButtons[i];
                button.setPosition(item.getPosition(new Vector2()));
                button.render(g);
            }
        } else if (currentMenu == severeSevereConfirmationMenu) {
            Rectangle rect = new Rectangle(severeConfirmationMenu.getScreenRectangle());
            rect.height += 128;
            g.drawPatch(Patch.get("menu_flat"), rect, 8);

            g.drawString("You are about to close the program without saving!", LittleH.font,
                    rect.x + rect.width / 2, rect.y + rect.height - 64, LittleH.defaultFontScale, 0);

            itemButtons = severeConfirmationMenu.getItemButtons();

            for (int i = 0; i < severeConfirmationMenu.items.length; i++) {
                ScreenButton button = severeConfirmationMenu.getItem(i);
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