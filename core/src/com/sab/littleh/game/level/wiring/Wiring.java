package com.sab.littleh.game.level.wiring;

import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.MapLayer;
import com.sab.littleh.game.tile.Tile;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Wiring {
    private final Map<Tile, List<Tile>> connections;

    public List<Tile> getPoweredTiles(Tile source) {
        return connections.get(source);
    }

    public Wiring(Level level) {
        connections = new HashMap<>();
        MapLayer wiring = level.getWiringLayer();

        List<Tile> wires = new ArrayList<>();
        for (Tile tile : wiring.allTiles) {
            if (tile.hasTag("wire")) {
                wires.add(tile);
            }
        }

        List<List<Tile>> systems = new ArrayList<>();
        while (wires.size() > 0) {
            Tile wire = wires.remove(wires.size() - 1);
            List<Tile> connectedWiring = getConnectedWiring(level, wire);
            systems.add(connectedWiring);
            wires.removeAll(connectedWiring);
        }

        for (List<Tile> system : systems) {
            for (Tile i : system) {
                if (i.hasTag("receiver") || i.hasTag("power_source")) {
                    connections.put(i, new ArrayList<>());
                    for (Tile j: system) {
                        if (j != i && j.hasTag("powered")) {
                            connections.get(i).add(j);
                        }
                    }
                }
            }
        }
    }

    private static List<Tile> getConnectedWiring(Level level, Tile start) {
        List<Tile> connected = new ArrayList<>();

        List<Point> open = new ArrayList<>();
        Set<Point> closed = new HashSet<>();

        Point origin = new Point(start.x, start.y);
        open.add(origin);
        closed.add(origin);

        while (open.size() > 0) {
            Point current = open.get(0);
            open.remove(0);

            Tile currentTile = level.getTileAt("wiring", current.x, current.y);
            connected.add(currentTile);

            int x = current.x;
            int y = current.y;

            if (x < level.getWidth() - 1 && !closed.contains(new Point(current.x + 1, current.y))) {
                if (canConnect(currentTile, level.getTileAt("wiring", current.x + 1, current.y))) {
                    open.add(new Point(x + 1, y));
                    closed.add(new Point(x + 1, y));
                }
            }
            if (x > 0 && !closed.contains(new Point(current.x - 1, current.y))) {
                if (canConnect(currentTile, level.getTileAt("wiring", current.x - 1, current.y))) {
                    open.add(new Point(x - 1, y));
                    closed.add(new Point(x - 1, y));
                }
            }
            if (y < level.getHeight() - 1 && !closed.contains(new Point(current.x, current.y + 1))) {
                if (canConnect(currentTile, level.getTileAt("wiring", current.x, current.y + 1))) {
                    open.add(new Point(x, y + 1));
                    closed.add(new Point(x, y + 1));
                }
            }
            if (y > 0 && !closed.contains(new Point(current.x, current.y - 1))) {
                if (canConnect(currentTile, level.getTileAt("wiring", current.x, current.y - 1))) {
                    open.add(new Point(x, y - 1));
                    closed.add(new Point(x, y - 1));
                }
            }
        }

        return connected;
    }

    private static boolean canConnect(Tile a, Tile b) {
        if (b == null) return false;
        if (a.hasTag("wire")) {
            String wireType = a.tags.getTag("wire");
            byte wireId = 0;
            if (wireType.equals("red")) wireId = 0;
            if (wireType.equals("yellow")) wireId = 1;
            if (wireType.equals("green")) wireId = 2;
            if (wireType.equals("blue")) wireId = 3;

            if (b.hasTag("junction") || b.hasTag("power_source")) {
                return true;
            }
            if (b.hasTag("powered")) {
                if (b.hasTag("actuator")) {
                    return b.tileType == 4 || b.tileType == wireId;
                }
                return true;
            }
            return b.hasTag("wire") && a.tags.getTag("wire").equals(b.tags.getTag("wire"));
        }
        if (a.hasTag("actuator") && b.hasTag("actuator")) {
            return b.tileType == 4 || a.tileType == b.tileType;
        }
        return false;
    }
}
