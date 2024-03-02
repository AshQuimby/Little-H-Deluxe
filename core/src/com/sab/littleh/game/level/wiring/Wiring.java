package com.sab.littleh.game.level.wiring;

import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.MapLayer;
import com.sab.littleh.game.tile.Tile;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Wiring {
    private static final boolean logWiringInfo = true;

    private final Map<Tile, List<Tile>> connections;

    public List<Tile> getPoweredTiles(Tile source) {
        return connections.get(source);
    }

    public Wiring(Level level) {
        connections = new HashMap<>();
        MapLayer wiring = level.getWiringLayer();

        List<Tile> tiles = new ArrayList<>(wiring.allTiles);

        List<List<Tile>> systems = new ArrayList<>();
        while (tiles.size() > 0) {
            Tile tile = tiles.remove(tiles.size() - 1);
            List<Tile> connectedWiring = getConnectedWiring(level, tile);
            systems.add(connectedWiring);
            tiles.removeAll(connectedWiring);
        }

        for (List<Tile> system : systems) {
            for (Tile i : system) {
                if (i.hasTag("receiver") || i.hasTag("power_source")) {
                    if (!connections.containsKey(i)) {
                        connections.put(i, new ArrayList<>());
                    }
                    for (Tile j: system) {
                        if (j != i && j.hasTag("powered")) {
                            connections.get(i).add(j);
                        }
                    }
                }
            }
        }

        if (logWiringInfo) {
            System.out.println("\nWiring Systems:");
            for (List<Tile> system : systems) {
                System.out.print("[ ");
                for (Tile tile : system) {
                    String[] imagePath = tile.image.split("/");
                    System.out.printf("(%s, %s, %s), ", imagePath[imagePath.length - 1], tile.x, tile.y);
                }
                System.out.println("]");
            }

            System.out.println("\nWiring Connections:");
            for (Map.Entry<Tile, List<Tile>> entry : connections.entrySet()) {
                String[] imagePath = entry.getKey().image.split("/");
                System.out.printf("%s -> [ ", imagePath[imagePath.length - 1]);
                for (Tile connected : entry.getValue()) {
                    imagePath = connected.image.split("/");
                    System.out.printf("(%s, %s, %s), ", imagePath[imagePath.length - 1], connected.x, connected.y);
                }
                System.out.println("]");
            }
            System.out.println();
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
            // Wires of the same color can connect
            if (b.hasTag("wire") && a.tags.getTag("wire").equals(b.tags.getTag("wire"))) {
                return true;
            }

            // Wires can connect to actuators that share a color
            String wireType = a.tags.getTag("wire");
            byte wireId = 0;
            if (wireType.equals("red")) wireId = 0;
            if (wireType.equals("yellow")) wireId = 1;
            if (wireType.equals("green")) wireId = 2;
            if (wireType.equals("blue")) wireId = 3;

            if (b.hasTag("inputs")) {
                byte[] rotations = parseDirections(b.tags.getTagParameters("inputs"));
                for (byte r : rotations) {
                    int dx = dx(rotated(b.tileType, r));
                    int dy = dy(rotated(b.tileType, r));

                    if (a.x == b.x + dx && a.y == b.y + dy) {
                        return true;
                    }
                }
            }

            if (b.hasTag("outputs")) {
                byte[] rotations = parseDirections(b.tags.getTagParameters("outputs"));
                for (byte r : rotations) {
                    int dx = dx(rotated(b.tileType, r));
                    int dy = dy(rotated(b.tileType, r));

                    if (a.x == b.x + dx && a.y == b.y + dy) {
                        return true;
                    }
                }
            }

            if (b.hasTag("powered")) {
                if (b.hasTag("wire_color_component")) {
                    return b.tileType == 4 || b.tileType == wireId;
                }
                return true;
            }

            // Wires can connect to junctions and power sources
            if (b.hasTag("junction") || b.hasTag("power_source")) {
                return true;
            }
        }

        // Actuators can connect to other actuators that share a color
        if (a.hasTag("wire_color_component") && b.hasTag("wire_color_component")) {
            return b.tileType == 4 || a.tileType == b.tileType;
        }

        return false;
    }

    public static int dx(byte direction) {
        return direction == 1 ? 1 : direction == 3 ? -1 : 0;
    }

    public static int dy(byte direction) {
        return direction == 0 ? 1 : direction == 2 ? -1 : 0;
    }

    private static byte[] parseDirections(String[] directions) {
        byte[] rotations = new byte[directions.length];
        for (int i = 0; i < rotations.length; i++) {
            if (directions[i].equals("front")) rotations[i] = 0;
            if (directions[i].equals("right")) rotations[i] = 1;
            if (directions[i].equals("back")) rotations[i] = 2;
            if (directions[i].equals("left")) rotations[i] = 3;
        }

        return rotations;
    }

    public static byte rotated(byte r, byte dr) {
        return (byte) ((r + dr) % 4);
    }
}
