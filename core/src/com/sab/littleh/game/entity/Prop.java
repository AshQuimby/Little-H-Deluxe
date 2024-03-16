package com.sab.littleh.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class Prop extends GameObject {

    public Prop(String image, float x, float y) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    @Override
    public void render(Graphics g, Level game) {
        Texture tex = Images.getImage(image);
        Rectangle bounds = new Rectangle(x, y, tex.getWidth() * 8, tex.getHeight() * 8);
        if (LittleH.program.dynamicCamera.getScaledViewport().overlaps(bounds)) {
            g.draw(Images.getImage(image), bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
