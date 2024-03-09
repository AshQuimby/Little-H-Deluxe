package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class ImageButton extends ScreenButton {
    protected float imageOffsetX, imageOffsetY, imageWidth, imageHeight;
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
    public void render(Graphics g, int patchScale, float fontScale) {
        if (patchString != null)
            g.drawPatch(getPatch(), this, patchScale);
        float xOff = 0;
        float yOff = 0;
        if (imageWidth < 0) {
            xOff = -imageWidth;
        }
        if (imageHeight < 0) {
            yOff = -imageHeight;
        }
        g.draw(Images.getImage(text), x + imageOffsetX + xOff, y + getTextOffsetY(patchScale) + yOff, imageWidth, imageHeight);
    }
}
