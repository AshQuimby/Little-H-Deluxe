package com.sab.littleh.game.level;

import com.sab.littleh.game.tile.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapLayer {
    public List<Tile> allTiles;
    public List<Tile> checkpointSavedTiles;
    public List<Tile> checkpointState;
    public List<List<Tile>> tileMap;

    public MapLayer() {
        allTiles = new ArrayList<>();
        checkpointSavedTiles = new ArrayList<>();
        checkpointState = new ArrayList<>();
        tileMap = new ArrayList<>();
    }
}
