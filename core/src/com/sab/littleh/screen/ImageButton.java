package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class ImageButton extends ScreenButton {
    private float imageOffsetX, imageOffsetY, imageWidth, imageHeight;
    public ImageButton(String patchString, String image, float x, float y, float width, float height, float imageOffsetX, float imageOffsetY, float imageDrawWidth, float imageDrawHeight, Runnable onPress) {
        super(patchString, image, x, y, width, height, onPress);
        this.imageOffsetX = imageOffsetX;
        this.imageOffsetY = imageOffsetY;
        this.imageWidth = imageDrawWidth;
        this.imageHeight = imageDrawHeight;
    }

    public ImageButton(String patchString, String image, Rectangle rectangle, float imageOffsetX, float imageOffsetY, float imageDrawWidth, float imageDrawHeight, Runnable onPress) {
        super(patchString, image, rectangle, onPress);
        this.imageOffsetX = imageOffsetX;
        this.imageOffsetY = imageOffsetY;
        this.imageWidth = imageDrawWidth;
        this.imageHeight = imageDrawHeight;
    }

    public ImageButton quickCreate(String image, String hoverText, Runnable onPress) {
        ImageButton button = new ImageButton(patchString, image, this, imageOffsetX, imageOffsetY, imageWidth, imageHeight, onPress);
        button.setHoverText(hoverText);
        return button;
    }

    @Override
    public float getTextOffsetY(int patchScale) {
        return imageOffsetY - (pressed && hovered && hasDepth() ? patchScale : 0);
    }

    @Override
    public void render(Graphics g, int patchScale) {
        if (patchString != null)
            g.drawPatch(getPatch(), this, patchScale);
        g.draw(Images.getImage(text), x + imageOffsetX, y + getTextOffsetY(patchScale), imageWidth, imageHeight);
    }
}
