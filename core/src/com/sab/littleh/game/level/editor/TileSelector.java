package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.screen.ImageButton;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.util.ArrayList;
import java.util.List;

public class TileSelector extends Selector<TileButton> {
    private Selector<TileButton> stateSelection;

    public TileSelector(float x, float y, float width, float height, boolean vertical, boolean inverted) {
        super(x, y, width, height, vertical, inverted);

        // Incredibly scuffed, but not worth overcomplicating ImageButton for this single use, if I end up with more reasons to fix it, I will.
        revealButton = new ImageButton("tile_button", "ui/menu_dots.png", new Rectangle(0, 0, 60, 60),
                6, 6, 48, 48, () -> {
            setFocused(true);
        }) {
            @Override
            public void render(Graphics g, int patchScale, float fontScale) {
                if (patchString != null)
                    g.drawPatch(getPatch(), this, patchScale);
                float xOff = 0;
                float yOff = 0;
                if (imageWidth < 0) {
                    xOff = -imageWidth;
                }
                if (imageHeight < 0) {
                    yOff = -imageHeight;
                }
                Tile tile = getSelectedItem().getTile();
                g.draw(new TextureRegion(tile.getImage(), (int) tile.getDrawSection().x, (int) tile.getDrawSection().y,
                        (int) tile.getDrawSection().width, (int) tile.getDrawSection().height),
                        x + imageOffsetX + xOff, y + getTextOffsetY(patchScale) + yOff, imageWidth, imageHeight);
            }
        };
        toggleButton = revealButton;
    }
    public TileSelector(float x, float y, float width, float height) {
        this(x, y, width, height, false, false);
    }

    @Override
    public void updateBounds(Rectangle bounds) {
        super.updateBounds(bounds);
        if (stateSelection != null)
            updateStateBounds(bounds);
    }
    private void updateStateBounds(Rectangle bounds) {
        stateSelection.updateBounds(new Rectangle(x + width, bounds.y,
                60, bounds.height), false);

    }

    @Override
    public void onFocusedUpdate() {
        if (stateSelection != null) {
            stateSelection.update();
            if (!stateSelection.isFocused())
                closeStateSelector();
        }
        super.onFocusedUpdate();
    }

    // Tile state stuff (This shit's important)
    private void createStateSelector(Tile tile) {
        stateSelection = new Selector<>(x + width, getAdjustedY(), 256, 256, true, false);
        stateSelection.addNewSelection(new ArrayList<>());
        for (int i = 0; i < tile.getStateCount(); i++) {
            Tile propertyTile = tile.copy();
            propertyTile.setTileType(i);
            stateSelection.uncheckedAddItem(0, new TileButton(propertyTile, elementSize).hideGear());
        }
        stateSelection.setSelection(0);
        stateSelection.setFocused(true);
        updateStateBounds(assignedRect);
        toggleButton.setDisabled(true);
    }

    public boolean selectorClicked() {
        boolean selectorUsed = super.selectorClicked();
        if (!selectorUsed && stateSelection != null) {
            selectorUsed = stateSelection.selectorClicked();
            if (selectorUsed) {
                Tile tile = stateSelection.getSelectedItem().getTile();
                getSelectedItem().getTile().setTileType(tile.tileType);
            }
        }
        return selectorUsed;
    }
    @Override
    public void select(int index) {
        super.select(index);
        Tile selected = getSelectedItem().getTile();
        if (selected.hasTag("states"))
            createStateSelector(selected);
        else
            closeStateSelector();
    }

    private void uncheckedAddTile(int selectionIndex, Tile tile) {
        getSelection(selectionIndex).add(new TileButton(tile, elementSize));
    }
    public void addTile(Tile tile) {
        getCurrentSelection().add(new TileButton(tile, elementSize));
        formatItem(getCurrentSelection().size() - 1);
        checkRectBounds();
    }
    public void addNewTileSelection(List<Tile> tiles) {
        getSelections().add(new ArrayList<>());
        if (!tiles.isEmpty())
            addAllTiles(getPageCount() - 1, tiles);
    }
    public void addAllTiles(int index, List<Tile> tiles) {
        for (Tile tile : tiles)
            uncheckedAddTile(index, tile);
        updateBounds(this);
    }

    public void addTileSelections(List<List<Tile>> selections) {
        for (List<Tile> tiles : selections) {
            addNewTileSelection(tiles);
        }
    }
    public void resetTileSelections(List<List<Tile>> selections) {
        this.getSelections().clear();
        for (int i = 0; i < getPageCount(); i++) {
            addNewSelection(new ArrayList<>());
            resetTo(i, getSelections().get(i));
        }
    }
    public void resetTilesTo(int index, List<Tile> tiles) {
        if (index >= getPageCount()) {
            addNewSelection(new ArrayList<>());
        } else {
            getSelection(index).clear();
        }
        addAllTiles(index, tiles);
    }
    public void closeStateSelector() {
        stateSelection = null;
        toggleButton.setDisabled(false);
    }
    public boolean contains(Vector2 point) {
        boolean contains = super.contains(point);
        if (!contains && stateSelection != null) contains = stateSelection.contains(point);
        return contains;
    }

    @Override
    public void render(Graphics g, int patchScale, float fontScale) {
        super.render(g, patchScale, fontScale);
        if (stateSelection != null)
            stateSelection.render(g, patchScale, fontScale);
    }
    @Override
    public void renderItem(Graphics g, int patchScale, float fontScale, int index, TileButton tileButton) {
        super.renderItem(g, patchScale, fontScale, index, tileButton);
        if (isSelectedIndex(index))
            g.draw(Images.getImage("ui/selected_tile.png"), tileButton.x, tileButton.y, tileButton.width, tileButton.height);
    }
}
