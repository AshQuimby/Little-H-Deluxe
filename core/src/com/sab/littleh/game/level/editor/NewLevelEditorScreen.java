package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.screen.*;
import com.sab.littleh.util.Cursors;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.sab_format.SabReader;

import java.util.ArrayList;
import java.util.List;

public class NewLevelEditorScreen extends Screen {
    private final ScreenGui<ScreenElement> elements;
    private final TileSelector tileSelector;
    private final Selector<ImageButton> toolSelector;
    private final ImageButton settingsButton;
    private OptionsPanel optionsPanel;
    private boolean canPlaceTiles;

    public NewLevelEditorScreen() {
        Rectangle button = new Rectangle(0, 0, 60, 60);

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

        settingsButton = new ImageButton("tile_button", "ui/buttons/icons/gear.png", new Rectangle(0, 0, 72, 72),
                6, 6, 60, 60, () -> {
            if (optionsPanel == null) {
                optionsPanel = new OptionsPanel(SabReader.read(LittleH.getInternalLevel("meadows.map")));
                elements.queueAdd(optionsPanel);
            } else {
                optionsPanel.close(elements);
                optionsPanel = null;
            }
        });

        elements.add(tileSelector);
        elements.add(toolSelector);
        elements.add(settingsButton);
    }

    @Override
    public void update() {
        // TODO: Make screens detect window size changes to allow for this to only be called when necessary
        tileSelector.updateBounds(new Rectangle(relZeroX(), relZeroY(), program.getWidth(), program.getHeight()));
        toolSelector.updateBounds(new Rectangle(-relZeroX() - 72, relZeroY() + 72, 72, program.getHeight() - 72));
        settingsButton.setPosition(-relZeroX() - 72, relZeroY());

        if (ControlInput.localControls.isJustPressed("return")) {
            if (optionsPanel != null) {
                optionsPanel.close(elements);
                optionsPanel = null;
            }
        }

        elements.update();
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

    @Override
    public void mouseUp(int button) {
        elements.mouseClicked();
    }

    @Override
    public void render(Graphics g) {
        program.useStaticCamera();
        elements.render(g, 6, 1);
    }
}
