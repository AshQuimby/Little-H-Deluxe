package com.sab.littleh.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class Prop extends GameObject {
    private Rectangle bounds;

    public Prop(String image, float x, float y) {
        this.x = x;
        this.y = y;
        this.image = image;
        Texture tex = Images.getImage(image);
        bounds = new Rectangle(x, y, tex.getWidth() * 8, tex.getHeight() * 8);
    }

    @Override
    public void render(Graphics g, Level game) {
        if (LittleH.program.dynamicCamera.getScaledViewport().overlaps(bounds)) {
            g.draw(Images.getImage(image), bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
