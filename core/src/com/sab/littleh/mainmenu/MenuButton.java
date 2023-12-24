package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.MouseUtil;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.SoundEngine;
import com.sun.tools.javac.Main;

public class MenuButton extends Rectangle {
    protected String patchString;
    protected String text;
    protected boolean hovered;
    protected boolean pressed;
    protected boolean disabled;
    protected Runnable onPress;
    private String hoverText;
    private Vector2 screenPosition;

    public MenuButton(String patchString, String text, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.patchString = patchString;
        this.text = text;
    }

    public MenuButton(String patchString, String text, Rectangle rectangle) {
        super(rectangle);
        this.patchString = patchString;
        this.text = text;
    }

    public MenuButton(String patchString, String text, float x, float y, float width, float height, Runnable onPress) {
        super(x, y, width, height);
        this.patchString = patchString;
        this.text = text;
        this.onPress = onPress;
    }

    public MenuButton(String patchString, String text, Rectangle rectangle, Runnable onPress) {
        super(rectangle);
        this.patchString = patchString;
        this.text = text;
        this.onPress = onPress;
    }

    public void setDisabled(boolean value) {
        this.disabled = value;
    }

    public Patch getPatch() {
        return Patch.get(patchString + (disabled ? "_disabled" : hovered ? pressed ? "_pressed" : "_hovered" : ""));
    }
    public void onHover() {

    }
    public void onDown() {

    }
    public void update() {
        if (!disabled && contains(MouseUtil.getMousePosition())) {
            if (!hovered) {
                hovered = true;
                onHover();
            }
            if (hoverText != null)
                LittleH.program.setHoverInfo(hoverText);
            if (MouseUtil.isLeftMouseDown()) {
                if (!pressed) {
                    pressed = true;
                    onDown();
                }
            } else {
                pressed = false;
            }
        } else {
            hovered = false;
            if (!MouseUtil.isLeftMouseDown())
                pressed = false;
        }
    }

    public void mouseDown() {
    }

    public void mouseClicked() {
        if (!disabled && hovered && onPress != null) {
            onPress.run();
            SoundEngine.playSound("blip.ogg");
        }
    }

    public final MenuButton setHoverText(String text) {
        this.hoverText = text;
        return this;
    }

    public void render(Graphics g, int patchScale, float fontScale) {
        g.drawPatch(getPatch(), this, patchScale);
        if (text != null && !text.isBlank()) {
            LittleH.font.setColor(disabled ? Color.GRAY : Color.WHITE);
            g.drawString(text, LittleH.font, getCenterX(), getCenterY() + 4 - (pressed && hovered ? patchScale : 0), LittleH.defaultFontScale * fontScale, 0);
        }
    }

    public void render(Graphics g, int patchScale) {
        g.drawPatch(getPatch(), this, patchScale);
        if (text != null && !text.isBlank()) {
            LittleH.font.setColor(disabled ? Color.GRAY : Color.WHITE);
            g.drawString(text, LittleH.font, getCenterX(), getCenterY() + 4 - (pressed && hovered ? patchScale : 0), LittleH.defaultFontScale, 0);
        }
    }

    public void render(Graphics g) {
        g.drawPatch(getPatch(), this, 8);
        if (text != null && !text.isBlank()) {
            LittleH.font.setColor(disabled ? Color.GRAY : Color.WHITE);
            g.drawString(text, LittleH.font, getCenterX(), getCenterY() + 4 - (pressed && hovered ? 8 : 0), LittleH.defaultFontScale, 0);
        }
    }

    public void render(int i, Graphics g) {
        render(g);
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
}
