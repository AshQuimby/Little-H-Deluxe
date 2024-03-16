package com.sab.littleh.game.level;

import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class MapLayer {
    public List<Tile> allTiles;
    public List<Tile> saveImportantTiles;
    public List<Tile> checkpointState;
    public List<List<Tile>> tileMap;
    public LevelEditor editor;

    public MapLayer(Level level, String layer) {
        allTiles = new ArrayList<>();
        saveImportantTiles = new ArrayList<>();
        checkpointState = new ArrayList<>();
        tileMap = new ArrayList<>();
        editor = new LevelEditor(level, layer);
    }

    public void clearTileMap() {
        for (List<Tile> tiles : tileMap) {
            tiles.replaceAll(ignored -> null);
        }
    }
}
