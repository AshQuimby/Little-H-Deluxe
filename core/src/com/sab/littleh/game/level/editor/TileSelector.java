package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.MouseUtil;
import com.sab.littleh.util.Patch;

import java.util.ArrayList;
import java.util.List;

public class TileSelector extends Rectangle {
    private final ArrayList<TileButton> tileSelection;
    private final boolean vertical;
    private int selectedIndex;
    private boolean focused;

    public TileSelector(float x, float y, float width, float height, boolean vertical) {
        super(x, y, width, height);
        tileSelection = new ArrayList<>();
        selectedIndex = 0;
        this.vertical = vertical;

        // THIS IS FOR DEBUGGING, REMOVE IT
        focused = true;
    }
    public TileSelector(float x, float y, float width, float height) {
        this(x, y, width, height, false);
    }

    public void updateBounds(Rectangle bounds) {
        set(bounds);
        float xMax = 0;
        float yMax = 0;
        for (int i = 0; i < tileSelection.size(); i++) {
            Rectangle rect = formatItem(i);
            xMax = Math.max(xMax, rect.x + rect.width - x);
            yMax = Math.max(yMax, rect.y + rect.height - y);
        }
        width = xMax;
        height = yMax;
    }
    public void update() {
        if (!focused) return;
        tileSelection.forEach(TileButton::update);
    }
    public void checkBounds() {
        float xMax = 0;
        float yMax = 0;
        for (int i = 0; i < tileSelection.size(); i++) {
            Rectangle rect = getItem(i);
            xMax = Math.max(xMax, rect.x + rect.width - x);
            yMax = Math.max(yMax, rect.y + rect.height - y);
        }
        width = xMax;
        height = yMax;
    }

    public void mouseClicked() {
        for (int i = 0; i < tileSelection.size(); i++)
            if (tileSelection.get(i).contains(MouseUtil.getMousePosition())) select(i);
    }

    private void select(int index) {
        selectedIndex = index;
    }
    public void addItem(Tile tile) {
        tileSelection.add(new TileButton(tile, 1, 1));
        formatItem(tileSelection.size() - 1);
        checkBounds();
    }
    public void addAll(List<Tile> tiles) {
        for (Tile tile : tiles)
            uncheckedAddItem(tile);
        updateBounds(this);
    }
    public void resetTo(List<Tile> tiles) {
        tileSelection.clear();
        addAll(tiles);
    }
    private void uncheckedAddItem(Tile tile) {
        tileSelection.add(new TileButton(tile, 1, 1));
    }
    private Rectangle formatItem(int index) {
        TileButton button = tileSelection.get(index);
        int gridX = vertical ? index / getCapacity() : index % getCapacity();
        int gridY = vertical ? index % getCapacity() : index / getCapacity();
        button.setPosition(x + gridX * 60, getAdjustedY() - 60 - gridY * 60);
        return button;
    }

    public boolean isSelectedIndex(int index) {
        return selectedIndex == index;
    }
    public int size() {
        return tileSelection.size();
    }
    public TileButton getSelectedItem() {
        return tileSelection.get(selectedIndex);
    }
    public TileButton getItem(int index) {
        return tileSelection.get(index);
    }
    public int getCapacity() {
        return vertical ? getYCapacity() : getXCapacity();
    }
    public int getXCapacity() {
        return (int) width / 60;
    }
    public int getYCapacity() {
        return (int) height / 60;
    }
    public float getAdjustedY() {
        return y + height;
    }

    public void render(Graphics g) {
        g.drawPatch(Patch.get("menu_flat"), this, 6);
        for (int i = 0; i < tileSelection.size(); i++) {
            TileButton tile = tileSelection.get(i);
            tile.render(g);
            if (isSelectedIndex(i))
                g.draw(Images.getImage("ui/selected_tile.png"), tile.x, tile.y, tile.width, tile.height);
        }
    }
}
