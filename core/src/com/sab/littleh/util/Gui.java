package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.function.Consumer;

public class Gui<E> {
    public List<E> items;
    public int itemIndex;
    // Potential auto rendering and math
    // public Rectangle area;
    // public int popOutAnimation;
    // private Point restingPoint;
    // private Point activePoint;

    // This is pretty pointless
    // private boolean useImages;

    // These are not pointless
    public Gui<? extends E> subMenu;
    private Rectangle rectangle;
    public int elementWidth;
    public int elementHeight;
    private int itemOffset;

    public Gui(E[] items, int elementWidth, int elementHeight, int itemOffset) {
        if (items != null)
            this.items = List.of(items);
        this.elementWidth = elementWidth;
        this.elementHeight = elementHeight;
        // this.useImages = useImages;
        this.itemOffset = itemOffset;
        rectangle = new Rectangle(0, 0, 1, 1);
    }

    public Gui(E[] items, int elementWidth, int elementHeight) {
        this(items, elementWidth, elementHeight, 4);
    }

    public E setItemIndex(int index) {
        itemIndex = index;
        if (itemIndex == -1) return null;
        return getSelectedItem();
    }

    public boolean contains(Vector2 point) {
        if (subMenu != null) {
            return rectangle.contains(point) || subMenu.contains(point);
        }
        return rectangle.contains(point);
    }

    public E setItemIndexToContainer(Vector2 point) {
        int newItemIndex = getOverlappedElement(point);
        if (newItemIndex == -1) return null;
        itemIndex = newItemIndex;
        return getItem(itemIndex);
    }

    public int getLastIndexInBounds(Rectangle bounds) {
        Rectangle[] buttons = getItemButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (bounds.contains(buttons[i])) continue;
            return i--;
        }
        return items.size() - 1;
    }

    public void setSubMenu(Gui<? extends E> menu) {
        subMenu = menu;
    }

    public E getSelectedItem() {
        return items.get(itemIndex);
    }

    public void setPosition(int x, int y) {
        rectangle.x = x;
        rectangle.y = y;
    }

    public void setCenterX(float x) {
        rectangle.x = x - rectangle.width / 2;
    }

    public void setCenterY(float y) {
        rectangle.y = y - rectangle.height / 2;
    }

    public void setMenuRectangle(float x, float y, int maxHeight, boolean expandLeft) {
        int height = Math.max(elementHeight + itemOffset * 2, maxHeight);
        height = height / (elementHeight + itemOffset) *  (elementHeight + itemOffset) + itemOffset;
        int maxElement = height / (elementHeight + itemOffset);
        // if (src.TileEditor.program != null && src.TileEditor.program.tick % 30 == 0) System.out.println((items.length) / Math.max(1, maxElement));
        rectangle = new Rectangle(x, y, (elementWidth + itemOffset) * ((items.size() - 1) / Math.max(1, maxElement) + 1) + itemOffset, height);
        if (expandLeft) {
            rectangle.x -= rectangle.width;
        }
        rectangle.y -= rectangle.height;
    }

    public int getRowCount() {
        int maxElement = (int) rectangle.height / (elementHeight + itemOffset);
        return (items.size() - 1) / Math.max(1, maxElement) + 1;
    }

    public void setElementDimensions(int width, int height, int offset) {
        elementWidth = width;
        elementHeight = height;
        itemOffset = offset;
    }

    public Rectangle[] getItemButtons() {
        Rectangle[] buttons = new Rectangle[items.size()];
        for (int i = 0; i < items.size(); i++) {
            int maxY = Math.max(1, (int) rectangle.height / (elementHeight + itemOffset));
            int relY = i;
            int x = relY / maxY;
            int y = relY % maxY;
            y = maxY - y - 1;

            buttons[i] = new Rectangle(rectangle.x + x * (elementWidth + itemOffset) + itemOffset, rectangle.y + y * (elementHeight + itemOffset) + itemOffset, elementWidth, elementHeight);
        }
        return buttons;
    }

    public int getOverlappedElement(Vector2 point) {
        Rectangle[] buttons = getItemButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].contains(point)) {
                return i;
            }
        }
        return -1;
    }

    public void forEach(Consumer<E> action) {
        for (E e : items) {
            action.accept(e);
        }
    }

    public Rectangle getMenuRectangle() {
        return rectangle;
    }

    public boolean hasSubMenu() {
        return subMenu != null;
    }

    public E getItem(int index) {
        return items.get(index);
    }

    public boolean addItem(E item) {
        return items.add(item);
    }

    public void renderMenuRectangle(Graphics g, Patch patch) {
        g.drawPatch(patch, rectangle, 3);
    }

    public void update() {
    }

    public int getSize() {
        return items.size();
    }
}