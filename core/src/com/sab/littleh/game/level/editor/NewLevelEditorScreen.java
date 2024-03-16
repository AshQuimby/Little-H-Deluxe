package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.screen.*;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.sab_format.SabReader;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewLevelEditorScreen extends Screen {
    private final ScreenGui<ScreenElement> elements;
    private final TileSelector tileSelector;
    private final Selector<ImageButton> toolSelector;
    private final ImageButton settingsButton;
    private final DynamicCamera camera;
    private OptionsPanel optionsPanel;
    private final File file;
    private final Level level;
    private final Vector2 mouseWorldPos;
    private final Vector2 oldMouseWorldPos;
    private final Point mouseGridPos;
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
    public void update() {
        if (!MouseUtil.isLeftMouseDown())
            canPlaceTiles = true;

        // TODO: Make screens detect window size changes to allow for this to only be called when necessary
        tileSelector.updateBounds(new Rectangle(relZeroX(), relZeroY(), program.getWidth(), program.getHeight()));
        toolSelector.updateBounds(new Rectangle(-relZeroX() - 72, relZeroY() + 72, 72, program.getHeight() - 72));
        settingsButton.setPosition(-relZeroX() - 72, relZeroY());

        if (ControlInput.localControls.isJustPressed("return")) {
            if (optionsPanel != null) {
                closeOptionsPanel();
            } else {
                program.switchScreen(new EditorPauseScreen(this));
            }
        }
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
            }
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
    public LevelEditor getEditor() {
        return level.getEditor(getLayer());
    }
    public String getLayer() {
        return "normal";
    }
    public Tile getSelectedTile() {
        return tileSelector.getSelectedItem().getTile();
    }

    @Override
    public void render(Graphics g) {
        program.useStaticCamera();
        level.renderBackground(g);
        program.useDynamicCamera();
        level.render(g, false, false, false);
        program.useDynamicCamera();
        if (canPlaceTiles) {
            g.draw(Images.getImage("ui/selector.png"), mouseGridPos.x * 64, mouseGridPos.y * 64, 64, 64);
            getEditor().render(g, mouseGridPos.x, mouseGridPos.y);
        }
        program.useStaticCamera();
        elements.render(g, 6, 1);
    }
}
