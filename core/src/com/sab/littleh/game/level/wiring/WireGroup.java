package com.sab.littleh.game.level.wiring;

import com.sab.littleh.game.tile.Tile;

import java.util.HashSet;
import java.util.Set;

public class WireGroup {
    public final int id;
    public boolean powered;

    // The wires that make up this group
    public Set<Tile> wires;

    // The logic gates that are connected to this group
    public Set<Tile> connectedGates;

    // The wiring components that are connected to this group
    public Set<Tile> connectedComponents;

    public WireGroup(int id) {
        this(id, new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    public WireGroup(int id, Set<Tile> wires, Set<Tile> connectedGates, Set<Tile> connectedComponents) {
        this.id = id;
        this.wires = wires;
        this.connectedGates = connectedGates;
        this.connectedComponents = connectedComponents;
    }
}
