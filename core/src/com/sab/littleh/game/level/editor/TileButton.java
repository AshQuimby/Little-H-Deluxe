package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.screen.ScreenButton;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;

public class TileButton extends ScreenButton {
    private final Tile tile;
    private boolean drawGear;

    public TileButton(Tile tile, float x, float y) {
        super(null, "", x, y, 60, 60, null);
        this.tile = tile;
        this.drawGear = tile.hasTag("states");
        this.text = tile.image;
    }

    public TileButton(Tile tile, Rectangle rect) {
        super(null, "", rect.x, rect.y, rect.width, rect.height, null);
        this.tile = tile;
        this.drawGear = tile.hasTag("states");
        this.text = tile.image;
    }

    public TileButton quickCreate(Tile tile, Runnable onPress) {
        TileButton button = new TileButton(tile.copy(), x, y);
        button.drawGear = drawGear;
        return button;
    }

    public TileButton hideGear() {
        drawGear = false;
        return this;
    }

    @Override
    public void render(Graphics g, int patchScale, float fontScale) {
        if (tile.hasTag("render_color") && tile.extra != null && !tile.extra.isBlank()) {
            g.setColor(Color.valueOf("#" + tile.extra.toUpperCase().trim()));
        }
        g.drawImage(tile.getImage(), x + 6, y + 6, 48, 48, tile.getDrawSection());
        g.resetColor();
        if (drawGear && tile.hasTag("states")) {
            g.draw(Images.getImage("ui/properties_gear.png"), x + 6, y + 6, 48, 48);
        }
        if (tile.extra != null && !tile.extra.isBlank()) {
            g.draw(Images.getImage("ui/copy_paper.png"), x + 6, y + 6, 48, 48);
        }
    }

    public Tile getTile() {
        return tile;
    }
}
