package com.sab.littleh.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Collisions;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameObject {
    public float x;
    public float y;
    public String image;
    public Set<Tile> lastTouchedTiles = new HashSet<>();
    public boolean remove;

    public GameObject() {
    }

    public void update(Level game) {
    }

    public void render(Graphics g, Level game) {
        Texture tex = Images.getImage(image);
        g.draw(tex, x, y, tex.getWidth() * 8, tex.getHeight() * 8);
    }
}