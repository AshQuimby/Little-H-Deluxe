package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.util.Graphics;

public class ScreenElement extends Rectangle {
    public ScreenElement(Rectangle rectangle) {
        super(rectangle);
    }
    public ScreenElement(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    public ScreenElement() {
        super();
    }

    public void update() {
    }

    public void mouseClicked() {
    }

    public float getCenterX() {
        return getCenter().x;
    }
    public float getCenterY() {
        return getCenter().y;
    }
    public Vector2 getCenter() {
        return getCenter(new Vector2());
    }
    public Vector2 getPosition() {
        return getPosition(new Vector2());
    }

    public final void render(Graphics g) {
        render(g, 8);
    }
    public final void render(Graphics g, int patchScale) {
        render(g, patchScale, 1);
    }
    public void render(Graphics g, int patchScale, float fontScale) {
    }
}
