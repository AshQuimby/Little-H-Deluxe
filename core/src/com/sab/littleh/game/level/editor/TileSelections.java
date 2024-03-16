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
    public static final List<Tile> wiringTiles = new ArrayList<>();
    static {
        addTiles(groundTiles, SabReader.read(LittleH.getScript("level_editor/ground.sab")), true);
        addTiles(levelTiles, SabReader.read(LittleH.getScript("level_editor/level.sab")), true);
        addTiles(dangerTiles, SabReader.read(LittleH.getScript("level_editor/danger.sab")), true);
        addTiles(pickupTiles, SabReader.read(LittleH.getScript("level_editor/pickups.sab")), true);
        addTiles(specialTiles, SabReader.read(LittleH.getScript("level_editor/special.sab")), true);
        addTiles(decorationTiles, SabReader.read(LittleH.getScript("level_editor/decoration.sab")), true);
        addTiles(developerTiles, SabReader.read(LittleH.getScript("level_editor/developer.sab")), true);

        for (List<Tile> selection : selections)
            allTiles.addAll(selection);

        addTiles(wiringTiles, SabReader.read(LittleH.getScript("level_editor/wiring.sab")), false);
    }

    private static void addTiles(List<Tile> selection, SabData data, boolean addToSelections) {
        for (String string : data.getValues().keySet()) {
            selection.add(new Tile(data.getRawValue(string)));
        }
        if (addToSelections)
            selections.add(selection);
    }
}
