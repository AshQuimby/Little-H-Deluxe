package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.screen.ScreenButton;

import java.util.List;

public class ButtonGui<E extends ScreenButton> extends Gui<E> {
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
        items.forEach(ScreenButton::update);
    }

    @Override
    public void setScreenRectangle(float x, float y, int maxHeight, boolean expandLeft) {
        super.setScreenRectangle(x, y, maxHeight, expandLeft);
        update();
    }

    public void mouseClicked() {
        forEach(ScreenButton::mouseClicked);
        int index = getOverlappedElement(MouseUtil.getMousePosition());
        if (index != -1)
            itemIndex = index;
    }

    public void render(Graphics g, Patch screenRectPatch) {
        render(false, g, screenRectPatch);
    }

    public void render(boolean indexed, Graphics g, Patch screenRectPatch) {
        if (screenRectPatch != null) {
            renderScreenRectangle(g, screenRectPatch);
        }

        if (indexed) {
            for (int i = 0; i < items.size(); i++) {
                ScreenButton button = items.get(i);
                button.render(i, g);
            }
        } else {
            forEach(screenButton -> screenButton.render(g));
        }
    }
}
