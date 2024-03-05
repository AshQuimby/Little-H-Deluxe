package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;

public class NewLevelEditorScreen extends Screen {
    private boolean canPlaceTiles;
    private final TileSelector tileSelector;

    public NewLevelEditorScreen() {
        tileSelector = new TileSelector(0, 0, 256, 256, true);
        tileSelector.resetTo(TileSelections.allTiles);
    }

    @Override
    public void update() {
        // TODO: Make screens detect window size changes to allow for this to only be called when necessary
        tileSelector.updateBounds(new Rectangle(relZeroX(), relZeroY(), program.getWidth(), program.getHeight()));

        tileSelector.update();
    }

    @Override
    public void mouseUp(int button) {
        tileSelector.mouseClicked();
    }

    @Override
    public void render(Graphics g) {
        program.useStaticCamera();
        tileSelector.render(g);
    }
}
