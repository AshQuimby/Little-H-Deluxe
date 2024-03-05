package com.sab.littleh.game.level.editor;

import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;

import java.util.ArrayList;
import java.util.List;

public class TileSelections {
    public static final List<List<Tile>> selections = new ArrayList<>();
    public static final List<Tile> allTiles = new ArrayList<>();
    public static final List<Tile> groundTiles = new ArrayList<>();
    public static final List<Tile> levelTiles = new ArrayList<>();
    public static final List<Tile> dangerTiles = new ArrayList<>();
    public static final List<Tile> pickupTiles = new ArrayList<>();
    public static final List<Tile> specialTiles = new ArrayList<>();
    public static final List<Tile> decorationTiles = new ArrayList<>();
    public static final List<Tile> developerTiles = new ArrayList<>();
    static {
        addTiles(groundTiles, SabReader.read(LittleH.getScript("level_editor/ground.sab")));
        addTiles(levelTiles, SabReader.read(LittleH.getScript("level_editor/level.sab")));
        addTiles(dangerTiles, SabReader.read(LittleH.getScript("level_editor/danger.sab")));
        addTiles(pickupTiles, SabReader.read(LittleH.getScript("level_editor/pickups.sab")));
        addTiles(specialTiles, SabReader.read(LittleH.getScript("level_editor/special.sab")));
        addTiles(decorationTiles, SabReader.read(LittleH.getScript("level_editor/decoration.sab")));
        addTiles(developerTiles, SabReader.read(LittleH.getScript("level_editor/developer.sab")));

        for (List<Tile> selection : selections)
            allTiles.addAll(selection);
    }

    private static void addTiles(List<Tile> selection, SabData data) {
        for (String string : data.getValues().keySet()) {
            selection.add(new Tile(data.getRawValue(string)));
        }
        selections.add(selection);
    }
}
