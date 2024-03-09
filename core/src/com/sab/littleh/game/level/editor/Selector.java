package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.screen.ImageButton;
import com.sab.littleh.screen.ScreenButton;
import com.sab.littleh.screen.ScreenElement;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.MouseUtil;
import com.sab.littleh.util.Patch;

import java.util.ArrayList;
import java.util.List;

public class Selector<E extends Rectangle> extends ScreenElement {
    public final Rectangle elementSize;
    private List<E> selection;
    private final List<List<E>> selections;
    private final boolean vertical;
    private final boolean inverted;
    private boolean isScreenElements;
    protected ScreenButton revealButton;
    protected ScreenButton hideButton;
    protected ScreenButton previousPageButton;
    protected ScreenButton nextPageButton;
    protected ScreenButton toggleButton;
    private int selectedIndex;
    private int selectionIndex;
    private boolean focused;
    protected Rectangle assignedRect;

    public Selector(float x, float y, float width, float height, boolean vertical, boolean inverted) {
        super(x, y, width, height);

        elementSize = new Rectangle(0, 0, 60, 60);

        selections = new ArrayList<>();
        selectedIndex = 0;
        this.vertical = vertical;
        this.inverted = inverted;
        revealButton = new ImageButton("tile_button", "ui/menu_dots.png", new Rectangle(0, 0, 60, 60),
                6, 6, 48, 48, () -> {
            setFocused(true);
        });
        hideButton = new ImageButton("tile_button", "ui/back_arrow.png", new Rectangle(0, 0, 42, 60),
                -6, 6, 48, 48, () -> {
            setFocused(false);
        });

        previousPageButton = new ImageButton("square_button", "ui/buttons/arrow.png", new Rectangle(0, 0, 36, 42),
                6, 12, 24, 24, this::previousPage);
        nextPageButton = new ImageButton("square_button", "ui/buttons/arrow.png", new Rectangle(0, 0, 36, 42),
                6, 12, -24, 24, this::nextPage);

        setFocused(false);

        assignedRect = new Rectangle(x, y, width, height);
    }
    public Selector(float x, float y, float width, float height) {
        this(x, y, width, height, false, false);
    }

    public void updateBounds(Rectangle bounds) {
        updateBounds(bounds, false);
    }
    public void updateBounds(Rectangle bounds, boolean fromTop) {
//        bounds.width = (int) (bounds.width / elementSize.width) * elementSize.width;
//        float oldHeight = bounds.height;
//        bounds.height = (int) (bounds.height / elementSize.height) * elementSize.height;
//        bounds.y += oldHeight - bounds.height;
        set(bounds);
        for (int i = 0; i < size(); i++) {
            formatItem(i);
        }
        checkRectBounds();
        if (fromTop) {
            y -= height;
            for (int i = 0; i < size(); i++) {
                formatItem(i);
            }
            checkRectBounds();
        }
        assignedRect = bounds;
    }
    @Override
    public final void update() {
        if (!focused) {
            onUnfocusedUpdate();
        } else {
            onFocusedUpdate();
        }
        postUpdate();
    }
    public void onUnfocusedUpdate() {

    }
    public void onFocusedUpdate() {
        if (isMultiPage()) {
            previousPageButton.update();
            nextPageButton.update();
        }

        if (isScreenElements)
            for (E e : selection)
                ((ScreenElement) e).update();

        if (ControlInput.localControls.isJustPressed("prev_selection"))
            previousPage();
        if (ControlInput.localControls.isJustPressed("next_selection"))
            nextPage();
    }
    public void postUpdate() {
        toggleButton.update();
    }

    public void checkRectBounds() {
        float xMax = vertical ? 0 : width;
        float yMax = vertical ? height : 0;
        for (int i = 0; i < size(); i++) {
            Rectangle rect = getItem(i);
            xMax = vertical ? Math.max(xMax, rect.x + rect.width - x) : xMax;
            yMax = vertical ? yMax : Math.max(yMax, rect.y + rect.height - y);
//            if (!(this instanceof TileSelector)) {
//                System.out.println(rect.y + ", " + rect.height + ", " + y);
//            }
        }
        width = Math.max(xMax, isMultiPage() ? elementSize.width * 2 : elementSize.width) + (vertical ? 6 : 0);
        height = Math.max(yMax, elementSize.height) + (vertical ? 0 : 6);

//        if (!isMultiPage()) {
//            float gHeight = (int) (getNonPaddedHeight() / elementSize.height) * elementSize.height;
//            float dHeight = getNonPaddedHeight() - gHeight;
//
//            height = gHeight + 12;
//            y += dHeight - 6;
//        }

        revealButton.x = x;
        revealButton.y = y + height - revealButton.height;
        hideButton.x = x + width - 12;
        hideButton.y = y + height / 2 - hideButton.height / 2;

        previousPageButton.x = x + 12;
        previousPageButton.y = y + 12;
        nextPageButton.x = x + width - nextPageButton.width - 12;
        nextPageButton.y = y + 12;
    }
    public void recheckBounds() {
        updateBounds(assignedRect);
    }
    public void setFocused(boolean value) {
        if (value) {
            toggleButton = hideButton;
        } else {
            toggleButton = revealButton;
        }
        focused = value;
    }

    @Override
    public void mouseClicked() {
        selectorClicked();
        if (isScreenElements)
            for (E e : selection)
                ((ScreenElement) e).mouseClicked();
    }

    public boolean selectorClicked() {
        boolean selectorUsed = false;
        if (!focused) {
        } else {
            for (int i = 0; i < selection.size(); i++) {
                if (selection.get(i).contains(MouseUtil.getMousePosition())) {
                    select(i);
                    selectorUsed = true;
                }
            }
            if (isMultiPage()) {
                previousPageButton.mouseClicked();
                nextPageButton.mouseClicked();
            }
        }
        toggleButton.mouseClicked();
        return selectorUsed;
    }

    protected void select(int index) {
        selectedIndex = index;
    }
    public void nextPage() {
        setSelection((selectionIndex + 1) % getPageCount());
    }
    public void previousPage() {
        setSelection(Math.floorMod(selectionIndex - 1, getPageCount()));
    }
    public void setSelection(int index) {
        selection = getSelection(index);
        selectionIndex = index;
        recheckBounds();
        select(0);
        if (!isScreenElements && size() > 0 && getSelectedItem() instanceof ScreenElement) {
            isScreenElements = true;
        }
    }

    public void removeToggle() {
        setFocused(true);
        toggleButton.setDisabled(true);
    }

    protected void uncheckedAddItem(int selectionIndex, E e) {
        e.set(elementSize);
        getSelection(selectionIndex).add(e);
    }
    public void addItem(E e) {
        e.set(elementSize);
        selection.add(e);
        formatItem(selection.size() - 1);
        checkRectBounds();
    }
    public void addNewSelection(List<E> list) {
        selections.add(new ArrayList<>());
        if (!list.isEmpty())
            addAll(getPageCount() - 1, list);
    }
    public void addAll(int index, List<E> list) {
        for (E e : list)
            uncheckedAddItem(index, e);
        updateBounds(this);
    }

    public void addSelections(List<List<E>> selections) {
        for (List<E> list : selections) {
            addNewSelection(list);
        }
    }
    public void resetSelections(List<List<E>> selections) {
        this.selections.clear();
        for (int i = 0; i < getPageCount(); i++) {
            addNewSelection(new ArrayList<>());
            resetTo(i, selections.get(i));
        }
    }
    public void resetTo(int index, List<E> list) {
        if (index >= getPageCount()) {
            addNewSelection(new ArrayList<>());
        } else {
            getSelection(index).clear();
        }
        addAll(index, list);
    }
    protected void formatItem(int index) {
        E button = getItem(index);
        int capacity = getCapacity();
        int layers = getLayerCount();

        if (vertical && isMultiPage() && layers == 1) capacity = (int) Math.ceil(size() / 2.0);

        int gridX = vertical ? index / capacity : index % capacity;
        int gridY = vertical ? index % capacity : index / capacity;

        float bX = x + gridX * elementSize.width + 6;
        float bY = -6 + (inverted ?
                y - gridY * elementSize.height + layers * elementSize.height :
                y + height - elementSize.height - gridY * elementSize.height);

        button.setPosition(bX, bY);
    }

    public boolean isSelectedIndex(int index) {
        return selectedIndex == index;
    }
    public int size() {
        if (selection == null) return 0;
        return selection.size();
    }
    public E getSelectedItem() {
        return selection.get(selectedIndex);
    }
    public E getItem(int index) {
        return selection.get(index);
    }
    public int getCapacity() {
        return vertical ? getYCapacity() : getXCapacity();
    }
    public int getXCapacity() {
        return Math.max(1, (int) (width - 6) / (int) elementSize.width);
    }
    public int getYCapacity() {
        return Math.max(1, (int) (getNonPaddedHeight() - (isMultiPage() ? 48 : 0)) / (int) elementSize.height);
    }
    public float getAdjustedY() {
        return y + height;
    }
    public float getNonPaddedWidth() {
        return width - 6;
    }
    public float getNonPaddedHeight() {
        return height - 6;
    }
    public List<E> getSelection(int index) {
        return selections.get(index);
    }
    public List<E> getCurrentSelection() {
        return selection;
    }
    public List<List<E>> getSelections() {
        return selections;
    }
    public int getSelectedIndex() {
        return selectedIndex;
    }
    public int getPageCount() {
        return selections.size();
    }
    public int getLayerCount() {
        return (size() - 1) / getCapacity() + 1;
    }
    public boolean isVertical() {
        return vertical;
    }
    public boolean isFocused() {
        return focused;
    }
    public boolean isMultiPage() {
        return getPageCount() > 1;
    }

    @Override
    public void render(Graphics g, int patchScale, float fontScale) {
        if (!toggleButton.isDisabled())
        toggleButton.render(g, patchScale);
        if (!focused) {
        } else {
            g.drawPatch(Patch.get("menu_flat"), this, patchScale);
            for (int i = 0; i < selection.size(); i++) {
                E e = selection.get(i);
                renderItem(g, patchScale, fontScale, i, e);
            }

            if (isMultiPage()) {
                previousPageButton.render(g, patchScale);
                nextPageButton.render(g, patchScale);
            }
        }
    }
    public void renderItem(Graphics g, int patchScale, float fontScale, int index, E e) {
        if (isScreenElements)
            ((ScreenElement) e).render(g, patchScale, fontScale);
        if (isSelectedIndex(index))
            g.draw(Images.getImage("ui/selected_property.png"), e.x, e.y, e.width, e.height);
    }
}
