package com.sab.littleh.game.level;

import com.sab.littleh.game.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class MapLayer {
    public List<Tile> allTiles;
    public List<Tile> saveImportantTiles;
    public List<Tile> checkpointState;
    public List<List<Tile>> tileMap;

    public MapLayer() {
        allTiles = new ArrayList<>();
        saveImportantTiles = new ArrayList<>();
        checkpointState = new ArrayList<>();
        tileMap = new ArrayList<>();
    }

    public void clearTileMap() {
        for (List<Tile> tiles : tileMap) {
            tiles.replaceAll(ignored -> null);
        }
    }
}
