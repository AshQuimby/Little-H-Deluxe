package com.sab.littleh.game.level.wiring;

import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.MapLayer;
import com.sab.littleh.game.tile.Tile;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Wiring {
    private static final boolean logWiringInfo = true;

    private final List<WireGroup> groups;
    private final Map<Point, Integer> ids;
    private final Set<Integer> queuedGroups;

    public Wiring(Level level) {
        groups = new ArrayList<>();
        ids = new HashMap<>();
        queuedGroups = new HashSet<>();

        List<Tile> wiring = new ArrayList<>(level.getWiringLayer().allTiles);
        List<Tile> wires = new ArrayList<>();
        List<Tile> gates = new ArrayList<>();

        for (Tile tile : wiring) {
            if (tile.hasTag("wire")) {
                wires.add(tile);
            } else {
                gates.add(tile);
            }
        }

        int nextGroupId = 0;
        while (wires.size() > 0) {
            Tile start = wires.remove(wires.size() - 1);
            List<Tile> connected = getConnectedWiring(level, start);

            WireGroup group = new WireGroup(nextGroupId);
            group.wires.addAll(connected);
            wires.removeAll(connected);

            groups.add(group);
            nextGroupId++;

            if (logWiringInfo) {
                String wireColor = start.tags.getTag("wire");
                System.out.printf("Created wire group %s of color %s\n", group.id, wireColor);
            }
        }

        for (WireGroup group : groups) {
            for (Tile wire : group.wires) {
                ids.put(new Point(wire.x, wire.y), group.id);

                Tile component = level.getTileAt("wiring_components", wire.x, wire.y);

                if (component != null) {
                    group.connectedComponents.add(component);

                    if (logWiringInfo) {
                        System.out.printf("Registered component %s with group %s\n", component.image, group.id);
                    }
                }
            }
        }

        for (Tile gate : gates) {
            if (!gate.hasTag("inputs")) continue;

            byte[] inputDirections = parseDirections(gate.tags.getTagParameters("inputs"));
            for (byte direction : inputDirections) {
                int dx = dx(rotated(gate.tileType, direction));
                int dy = dy(rotated(gate.tileType, direction));
                Point inputLocation = new Point(gate.x + dx, gate.y + dy);

                if (ids.containsKey(inputLocation)) {
                    groups.get(ids.get(inputLocation)).connectedGates.add(gate);

                    if (logWiringInfo) {
                        System.out.printf("Registered gate %s to update when group %s is powered\n", gate.image, ids.get(inputLocation));
                    }
                }
            }
        }
    }

    public boolean isInGroup(int x, int y) {
        return ids.containsKey(new Point(x, y));
    }

    public WireGroup getGroup(int x, int y) {
        return groups.get(ids.get(new Point(x, y)));
    }

    public void power(int groupId) {
        queuedGroups.add(groupId);
    }

    public int poweredInputCount(Tile tile) {
        byte[] inputDirections = parseDirections(tile.tags.getTagParameters("inputs"));

        int powered = 0;
        for (byte direction : inputDirections) {
            int dx = dx(rotated(tile.tileType, direction));
            int dy = dy(rotated(tile.tileType, direction));

            if (isInGroup(tile.x + dx, tile.y + dy) && getGroup(tile.x + dx, tile.y + dy).powered) {
                powered++;
            }
        }

        return powered;
    }

    public void update(Level level) {
        Set<Tile> queuedGates = new HashSet<>();
        Set<Tile> queuedComponents = new HashSet<>();

        for (int id : queuedGroups) {
            WireGroup group = groups.get(id);
            group.powered = true;
            queuedGates.addAll(group.connectedGates);
            queuedComponents.addAll(group.connectedComponents);
        }
        queuedGroups.clear();

        for (Tile gate : queuedGates) {
            gate.wiringUpdate(this);
        }
        for (Tile component: queuedComponents) {
            component.signalReceived(level);
        }

        for (WireGroup group : groups) {
            group.powered = false;
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
        return (a.hasTag("wire") && b.hasTag("wire") && a.tags.getTag("wire").equals(b.tags.getTag("wire")));
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
