package com.sab.littleh.mainmenu;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class ImageButton extends MenuButton {
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
    public void render(Graphics g) {
        if (patchString != null)
            g.drawPatch(getPatch(), this, 8);
        g.draw(Images.getImage(text), x + imageOffsetX, y + imageOffsetY - (pressed ? 8 : 0), imageWidth, imageHeight);
    }
}
