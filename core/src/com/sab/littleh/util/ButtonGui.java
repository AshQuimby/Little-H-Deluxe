package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.mainmenu.MenuButton;

import java.util.List;

public class ButtonGui<E extends MenuButton> extends Gui<E> {
    public ButtonGui(E[] items, int elementWidth, int elementHeight, int itemOffset) {
        super(items, elementWidth, elementHeight, itemOffset);
    }
    public ButtonGui(List<E> items, int elementWidth, int elementHeight, int itemOffset) {
        super(null, elementWidth, elementHeight, itemOffset);
        items = items;
    }

    @Override
    public void update() {
        Rectangle[] itemButtons = getItemButtons();
        for (int i = 0; i < itemButtons.length; i++) {
            getItem(i).set(itemButtons[i]);
        }
        items.forEach(MenuButton::update);
    }

    @Override
    public void setMenuRectangle(float x, float y, int maxHeight, boolean expandLeft) {
        super.setMenuRectangle(x, y, maxHeight, expandLeft);
        update();
    }

    public void mouseClicked() {
        forEach(MenuButton::mouseClicked);
        int index = getOverlappedElement(MouseUtil.getMousePosition());
        if (index != -1)
            itemIndex = index;
    }

    public void render(Graphics g, Patch menuRectPatch) {
        render(false, g, menuRectPatch);
    }

    public void render(boolean indexed, Graphics g, Patch menuRectPatch) {
        if (menuRectPatch != null) {
            renderMenuRectangle(g, menuRectPatch);
        }

        if (indexed) {
            for (int i = 0; i < items.size(); i++) {
                MenuButton button = items.get(i);
                button.render(i, g);
            }
        } else {
            forEach(menuButton -> menuButton.render(g));
        }
    }
}
