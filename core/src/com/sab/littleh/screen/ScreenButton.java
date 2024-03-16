package com.sab.littleh.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.MouseUtil;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.SoundEngine;

public class ScreenButton extends ScreenElement {
    public boolean dynamic;
    public String text;
    protected String patchString;
    protected boolean hovered;
    protected boolean pressed;
    protected boolean disabled;
    protected Runnable onPress;
    private String hoverText;
    private Vector2 screenPosition;

    public ScreenButton(String patchString, String text, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.patchString = patchString;
        this.text = text;
    }

    public ScreenButton(String patchString, String text, Rectangle rectangle) {
        super(rectangle);
        this.patchString = patchString;
        this.text = text;
    }

    public ScreenButton(String patchString, String text, float x, float y, float width, float height, Runnable onPress) {
        super(x, y, width, height);
        this.patchString = patchString;
        this.text = text;
        this.onPress = onPress;
    }

    public ScreenButton(String patchString, String text, Rectangle rectangle, Runnable onPress) {
        super(rectangle);
        this.patchString = patchString;
        this.text = text;
        this.onPress = onPress;
    }

    public void setDisabled(boolean value) {
        this.disabled = value;
    }

    public Patch getPatch() {
        return Patch.getButton(patchString, hovered, pressed, disabled);
    }
    public final boolean hasDepth() {
        if (patchString == null) return false;
        return Patch.buttonHasDepth(patchString);
    }
    public void onHover() {

    }
    public void onDown() {

    }

    public float getTextOffsetY(int patchScale) {
        return -(pressed && hovered && hasDepth() ? patchScale : 0);
    }

    @Override
    public void update() {
        if (!disabled && contains(dynamic ? MouseUtil.getDynamicMousePosition() : MouseUtil.getMousePosition())) {
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
            pressed = false;
        }
    }

    public void mouseDown() {
    }

    @Override
    public void mouseClicked() {
        if (!disabled && hovered && onPress != null) {
            onPress.run();
            SoundEngine.playSound("blip.ogg");
        }
    }

    public final ScreenButton setHoverText(String text) {
        this.hoverText = text;
        return this;
    }

    @Override
    public void render(Graphics g, int patchScale, float fontScale) {
        g.drawPatch(getPatch(), this, patchScale);
        if (text != null && !text.isBlank()) {
            LittleH.font.setColor(disabled ? Color.GRAY : Color.WHITE);
            g.drawString(text, LittleH.font, getCenterX(), getCenterY() + 4 - (pressed && hovered ? patchScale : 0) - 1 / fontScale, LittleH.defaultFontScale * fontScale, 0);
        }
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void run() {
        if (onPress != null)
            onPress.run();
    }
}
