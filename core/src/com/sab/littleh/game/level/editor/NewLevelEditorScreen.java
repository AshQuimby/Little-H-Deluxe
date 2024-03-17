package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.screen.*;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewLevelEditorScreen extends Screen {
    private final String[] layers = { "normal", "background", "wiring_components" };
    private final ScreenGui<ScreenElement> elements;
    private final TileSelector tileSelector;
    private final TileSelector wiringSelector;
    private final Selector<ImageButton> toolSelector;
    private final ImageButton settingsButton;
    private final DynamicCamera camera;
    private OptionsPanel optionsPanel;
    private final File file;
    private final Level level;
    private final Vector2 mouseWorldPos;
    private final Vector2 oldMouseWorldPos;
    private final Point mouseGridPos;
    private int layerIndex;
    private boolean canPlaceTiles;
    private boolean saved;

    public NewLevelEditorScreen(File file, Level level) {
        Rectangle button = new Rectangle(0, 0, 60, 60);
        camera = LittleH.program.dynamicCamera;

        elements = new ScreenGui<>();
        tileSelector = new TileSelector(0, 0, 256, 256, true, false);
        tileSelector.resetTilesTo(0, TileSelections.allTiles);
        tileSelector.addTileSelections(TileSelections.selections);
        tileSelector.setSelection(0);
        wiringSelector = new TileSelector(0, 0, 256, 256, true, false);
        wiringSelector.resetTilesTo(0, TileSelections.wiringTiles);
        wiringSelector.setSelection(0);

        toolSelector = new Selector<>(0, 0, 256, 256, true, false);
        List<ImageButton> tools = new ArrayList<>();
        ImageButton toolButton = new ImageButton(null, "ui/buttons/icons/pencil.png", button, 6, 6, 48, 48, null);
        tools.add(toolButton.quickCreate("ui/buttons/icons/pencil.png", "Pencil", () -> switchCursor("pencil")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/eraser.png", "Eraser", () -> switchCursor("eraser")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/pen.png", "Pen", () -> switchCursor("pen")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/paint_can.png", "Fill Tool", () -> switchCursor("paint_can")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/line.png", "Line Tool", () -> switchCursor("line")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/color_picker.png", "Tile Picker", () -> switchCursor("color_picker")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/selector.png", "Select", () -> switchCursor("selector")));
        tools.add(toolButton.quickCreate("ui/buttons/icons/h.png", "Quick Test", () -> switchCursor("h")));
        toolSelector.resetTo(0, tools);
        toolSelector.setSelection(0);
        toolSelector.removeToggle();
        toolSelector.getItem(0).run();
        toolSelector.select(0);

        settingsButton = new ImageButton("tile_button", "ui/buttons/icons/gear.png", new Rectangle(0, 0, 72, 72),
                6, 6, 60, 60, () -> {
            if (optionsPanel == null) {
                optionsPanel = new OptionsPanel(level.mapData);
                elements.queueAdd(optionsPanel);
            } else {
                closeOptionsPanel();
            }
        });

        elements.add(tileSelector);
        elements.add(toolSelector);
        elements.add(settingsButton);

        this.file = file;
        this.level = level;

        mouseWorldPos = MouseUtil.getDynamicMousePosition();
        oldMouseWorldPos = MouseUtil.getDynamicMousePosition();
        mouseGridPos = new Point((int) Math.floor(mouseWorldPos.x / 64), (int) Math.floor(mouseWorldPos.y / 64));
    }
    @Override
    public void onLoad() {
        level.init();
    }
    @Override
    public void start() {
        resize(0, 0);
    }

    @Override
    public void update() {
        if (!MouseUtil.isLeftMouseDown())
            canPlaceTiles = true;

        checkHotkeys();
        if (ControlInput.localControls.isPressed("drag_camera")) {
            canPlaceTiles = false;
            if (MouseUtil.isLeftMouseDown()) {
                camera.targetPosition.sub(MouseUtil.getMouseDelta().scl(camera.zoom));
            }
        }
        if (optionsPanel != null)
            canPlaceTiles = false;

        elements.update();
        for (ScreenElement element : elements) {
            if (element.contains(MouseUtil.getMousePosition())) {
                canPlaceTiles = false;
                break;
            }
        }

        if (level.inGame()) {
            level.update();
            canPlaceTiles = false;
        }

        getEditor().update(canPlaceTiles);

        if (canPlaceTiles) {
            if (MouseUtil.isRightMouseDown())
                useTool(true);
            else if (MouseUtil.isLeftMouseDown())
                useTool(false);
        }

        oldMouseWorldPos.set(mouseWorldPos);
        mouseWorldPos.set(MouseUtil.getDynamicMousePosition());
        mouseGridPos.setLocation((int) Math.floor(mouseWorldPos.x / 64), (int) Math.floor(mouseWorldPos.y / 64));
    }

    public void startTesting() {
        Point startPos = new Point();
        for (Tile tile : level.getBaseLayer().allTiles)
            if (tile.hasTag("start")) startPos = new Point(tile.x, tile.y);
        level.startGame(startPos);
    }
    public void stopTesting() {
        level.endGame();
        toolSelector.getSelectedItem().run();
    }

    public void useTool(boolean rightClick) {
        LevelEditor editor = getEditor();
        int x = mouseGridPos.x, y = mouseGridPos.y;
        if (rightClick) {
            switch (toolSelector.getSelectedIndex()) {
                case 0:
                case 1:
                    editor.useEraser(x, y);
                    break;
                case 2:
                    int pX = (int) Math.floor(oldMouseWorldPos.x / 64), pY = (int) Math.floor(oldMouseWorldPos.y / 64);
                    editor.usePen(new Tile("delete"), x, y, pX, pY);
                    break;
                case 3:
                    editor.useFillTool(new Tile("delete"), x, y);
                    break;
                case 4:
                    editor.useLineTool(new Tile("delete"), x, y);
                    break;
            }
        } else {
            Tile tile = getSelectedTile();
            switch (toolSelector.getSelectedIndex()) {
                case 0:
                    editor.usePencil(tile, x, y);
                    break;
                case 1:
                    editor.useEraser(x, y);
                    break;
                case 2:
                    int pX = (int) Math.floor(oldMouseWorldPos.x / 64), pY = (int) Math.floor(oldMouseWorldPos.y / 64);
                    editor.usePen(tile, x, y, pX, pY);
                    break;
                case 3:
                    editor.useFillTool(tile, x, y);
                    break;
                case 4:
                    editor.useLineTool(tile, x, y);
                    break;
                case 7:
                    level.startGame(mouseGridPos);
                    break;
            }
        }
    }

    public void checkHotkeys() {
        ControlInput input = ControlInput.localControls;
        if (input.isJustPressed("return")) {
            if (optionsPanel != null) {
                closeOptionsPanel();
            } else if (level.inGame()) {
                stopTesting();
            } else {
                program.switchScreen(new EditorPauseScreen(this));
            }
        }
        if (input.isJustPressed("playtest")) {
            startTesting();
        }
        if (input.isJustPressed("save")) {
            save();
        }
        if (input.isJustPressed("suicide")) {
            level.suicide();
        }
        if (input.isJustPressed("quick_reset")) {
            level.reset();
        }
        if (input.isJustPressed("undo") || input.getPressedFor("undo") > 30) {
            getEditor().undo();
        }
        if (input.isJustPressed("redo") || input.getPressedFor("redo") > 30) {
            getEditor().redo();
        }

        if (!level.inGame() && !input.commandMode()) {
            if (input.isJustPressed("up") || input.getPressedFor("up") > 30) {
                if (input.isPressed("drag_camera")) camera.targetPosition.y += 64 * 4;
                else camera.targetPosition.y += 64;
            }
            if (input.isJustPressed("down") || input.getPressedFor("down") > 30) {
                if (input.isPressed("drag_camera")) camera.targetPosition.y -= 64 * 4;
                else camera.targetPosition.y -= 64;
            }
            if (input.isJustPressed("left") || input.getPressedFor("left") > 30) {
                if (input.isPressed("drag_camera")) camera.targetPosition.x -= 64 * 4;
                else camera.targetPosition.x -= 64;
            }
            if (input.isJustPressed("right") || input.getPressedFor("right") > 30) {
                if (input.isPressed("drag_camera")) camera.targetPosition.x += 64 * 4;
                else camera.targetPosition.x += 64;
            }
        }

        if (input.isJustPressed("reverse_cycle_layer")) {
            cycleLayer(-1);
        } else if (input.isJustPressed("cycle_layer")) {
            cycleLayer(1);
        }
    }
    public void cycleLayer(int amount) {
        int oldLayer = layerIndex;
        layerIndex = Math.floorMod(layerIndex + amount, layers.length);
        if (oldLayer != 2 && layerIndex == 2) {
            elements.remove(0);
            elements.add(0, wiringSelector);
            wiringSelector.setFocused(tileSelector.isFocused());
        } else if (oldLayer == 2) {
            elements.remove(0);
            elements.add(0, tileSelector);
            tileSelector.setFocused(wiringSelector.isFocused());
        }
    }

    public void closeOptionsPanel() {
        optionsPanel.close(elements);
        level.init();
        optionsPanel = null;
    }

    public void switchCursor(String cursorString) {
        if (cursorString.equals("selector"))
            Cursors.switchCursor("cursor");
        else
            Cursors.switchCursor(cursorString);
    }
    @Override
    public void keyDown(int keycode) {
        if (optionsPanel != null) optionsPanel.keyDown(keycode);
    }
    @Override
    public void keyTyped(char character) {
        if (optionsPanel != null) optionsPanel.keyTyped(character);
    }
    public void blockTilePlacement() {
        canPlaceTiles = false;
    }
    public void setSaved(boolean value) {
        saved = value;
    }
    public boolean isSaved() {
        return saved;
    }

    @Override
    public void mouseUp(int button) {
        elements.mouseClicked();
        getEditor().mouseReleased(mouseGridPos.x, mouseGridPos.y);
    }
    @Override
    public void mouseScrolled(float amountY) {
        camera.targetPosition.sub(MouseUtil.getMousePosition().cpy().scl(amountY).scl(addZoom(amountY / 2f)));
    }
    // Returns by how much zoom changed
    public float addZoom(float zoom) {
        float zoomBefore = camera.targetZoom;
        camera.targetZoom += zoom;
        camera.targetZoom = Math.max(Math.min(camera.targetZoom, 16f), 1f);
        return Math.abs(zoomBefore - camera.targetZoom);
    }
    @Override
    public void resize(int width, int height) {
        tileSelector.updateBounds(new Rectangle(relZeroX(), relZeroY(), program.getWidth(), program.getHeight()));
        wiringSelector.updateBounds(new Rectangle(relZeroX(), relZeroY(), program.getWidth(), program.getHeight()));
        toolSelector.updateBounds(new Rectangle(-relZeroX() - 72, relZeroY() + 72, 72, program.getHeight() - 72));
        settingsButton.setPosition(-relZeroX() - 72, relZeroY());
    }

    public LevelEditor getEditor() {
        if (getSelectedTile().hasTag("wiring"))
            return level.getEditor("wiring");
        return level.getEditor(getLayer());
    }
    public String getLayer() {
        return layers[layerIndex];
    }
    public Tile getSelectedTile() {
        return layerIndex == 2 ? wiringSelector.getSelectedItem().getTile() : tileSelector.getSelectedItem().getTile();
    }
    public void save() {
        if (file == null)
            throw new RuntimeException("THIS IS NOT A REAL LEVEL DONT SAVE IT");
        LevelLoader.saveLevel(file, level);
        getEditor().setSaved(true);
    }
    public void stop() {
        program.switchScreen(new LevelSelectScreen());
    }

    @Override
    public void render(Graphics g) {
        program.useStaticCamera();
        level.renderBackground(g);
        program.useDynamicCamera();
        level.render(g, layerIndex == 1, layerIndex == 2, getSelectedTile().hasTag("wiring_component"));
        program.useDynamicCamera();
        if (canPlaceTiles) {
            g.draw(Images.getImage("ui/selector.png"), mouseGridPos.x * 64, mouseGridPos.y * 64, 64, 64);
            getEditor().render(g, mouseGridPos.x, mouseGridPos.y);
        }
        program.useStaticCamera();
        if (!level.inGame())
            elements.render(g, 6, 1);
        if (!LevelEditor.saved) {
            g.setColor(Images.quickAlpha(0.5f));
            g.draw(Images.getImage("ui/buttons/icons/floppy_disc.png"), -relZeroX() - 96 - 36, -relZeroY() - 12 - 48, 48, 48);
            g.resetColor();
        }
    }
}
