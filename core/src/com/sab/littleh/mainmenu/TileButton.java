package com.sab.littleh.mainmenu;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class TileButton extends MenuButton {
    private Tile tile;
    private boolean drawGear;

    public TileButton(Tile tile, float x, float y) {
        this(tile, x, y, true);
    }

    public TileButton(Tile tile, float x, float y, boolean drawGear) {
        super(null, "", x, y, 64, 64, null);
        this.tile = tile;
        this.drawGear = drawGear;
    }


    public TileButton(Tile tile, Rectangle rectangle) {
        this(tile, rectangle.x, rectangle.y);
    }

    public TileButton quickCreate(Tile tile, Runnable onPress) {
        TileButton button = new TileButton(tile.copy(), this);
        button.drawGear = drawGear;
        return button;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(tile.getImage(), x, y, 64, 64, tile.getDrawSection());
        if (drawGear && tile.hasTag("property_set")) {
            g.draw(Images.getImage("ui/properties_gear.png"), x, y, 64, 64);
        }
    }

    public Tile getTile() {
        return tile;
    }
}
