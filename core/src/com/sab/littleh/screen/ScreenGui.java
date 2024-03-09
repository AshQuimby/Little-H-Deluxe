package com.sab.littleh.screen;

import com.sab.littleh.util.Graphics;

import java.util.ArrayList;
import java.util.List;

public class ScreenGui<E extends ScreenElement> extends ArrayList<E> {
    private final List<E> toAdd = new ArrayList<>();
    private final List<E> toRemove = new ArrayList<>();
    public void queueAdd(E e) {
        toAdd.add(e);
    }
    public void queueRemove(E e) {
        toRemove.add(e);
    }
    public void update() {
        forEach(E::update);
        if (!toAdd.isEmpty()) {
            addAll(toAdd);
            toAdd.clear();
        }
        if (!toRemove.isEmpty()) {
            removeAll(toRemove);
            toRemove.clear();
        }
    }
    public void mouseClicked() {
        forEach(E::mouseClicked);
    }
    public void render(Graphics g, int patchScale, float fontScale) {
        forEach(e -> e.render(g, patchScale, fontScale));
    }
}
