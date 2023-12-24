package com.sab.littleh.game.level;

import com.sab.littleh.game.tile.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapLayer {
    public List<Tile> allTiles;
    public List<Tile> volatileTiles;
    public List<Tile> notifiableTiles;
    public List<Tile> checkpointState;
    public List<Tile> updatableTiles;
    public List<List<Tile>> tileMap;
    public MapLayer() {
        allTiles = new ArrayList<>();
        volatileTiles = new ArrayList<>();
        notifiableTiles = new ArrayList<>();
        checkpointState = new ArrayList<>();
        updatableTiles = new ArrayList<>();
        tileMap = new ArrayList<>();
    }
}
