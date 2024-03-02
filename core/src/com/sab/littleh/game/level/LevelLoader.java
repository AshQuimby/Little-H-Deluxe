package com.sab.littleh.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.game.tile.TileTags;
import com.sab.littleh.mainmenu.InternalLevelMenu;
import com.sab.littleh.mainmenu.LevelEditorMenu;
import com.sab.littleh.mainmenu.LevelErrorMenu;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.LoadingUtil;
import com.sab.littleh.util.sab_format.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class LevelLoader {
    public static final Map<String, TileTags> tileTagCache = new HashMap<>();
    public static final SabData tagsByTile;
    private static boolean invertY;
    private static boolean updateTilesets;

    private static final String[] expectedProperties = new String[] {
            "crouching",
            "double_jumping",
            "wall_sliding",
            "look_around",
            "author",
            "name",
            "background",
            "time_limit",
            "version",
            "is_deluxe"
    };

    private static String[] defaultValues = getDefaultValues();

    private static String[] getDefaultValues() {
        return new String[] {
                "true",
                "true",
                "true",
                "true",
                Settings.localSettings.authorName.value,
                "My Level",
                "mountains",
                "-1",
                LittleH.VERSION,
                "true"
        };
    }

    static {
        try {
            tagsByTile = SabReader.read(LevelLoader.class.getResourceAsStream("/scripts/base_tags.sab"));
        } catch (SabParsingException e) {
            System.out.println("AshQuimby forgot to put the file there :P");
            throw new RuntimeException(e);
        }
    }

    public static void refreshDefaults() {
        defaultValues = getDefaultValues();
    }

    public static Level readInternalLevel(String streamSource) {
        return readLevel(LittleH.getInternalLevel(streamSource), LittleH.getInternalLevel(streamSource));
    }

    public static Level readLevel(InputStream dataStream, InputStream inputStream) {
        // Updates for if the level was originally made in the Little H as opposed to the Little H Deluxe
        // Basically just flips the y coordinates of every tile
        invertY = false;
        updateTilesets = false;

        SabData mapData = SabReader.read(dataStream);

        if (mapData.getValue("is_deluxe") == null || !mapData.getValue("is_deluxe").asBool()) {
            invertY = true;
        }

        if (!mapData.getValue("version").toString().trim().equals(LittleH.VERSION)) {
            updateTilesets = true;
        }

        for (int i = 0; i < expectedProperties.length; i++) {
            String string = expectedProperties[i];
            if (!mapData.getValues().containsKey(string)) {
                mapData.insertValue(string, defaultValues[i]);
            }
        }

        Level level = new Level(mapData);
        Scanner scanner = SabReader.skipSabPreface(new Scanner(inputStream));

        List<Tile> tiles = new ArrayList<>();
        List<Tile> background = new ArrayList<>();
        List<Tile> wiring = new ArrayList<>();
        List<Tile> wiring_components = new ArrayList<>();

        int levelWidth = 31;
        int levelHeight = 31;

        Set<Point> usedPositions = new HashSet<>();

        while (scanner.hasNext()) {
            String nextLine = scanner.nextLine();

            // Load background
            if (nextLine.startsWith("@background_tiles")) {
                while (scanner.hasNext()) {
                    if (scanner.hasNext("@wiring_tiles"))
                        break;
                    nextLine = scanner.nextLine();
                    if (nextLine.isBlank())
                        break;
                    Tile tile = getTile(nextLine);
                    if (tile != null && !tile.image.equals("delete")) {
                        Point tilePosition = new Point(tile.x, tile.y);
                        levelWidth = Math.max(levelWidth, tile.x);
                        levelHeight = Math.max(levelHeight, tile.y);
                        if (tile.hasTag("start"))
                            LittleH.program.dynamicCamera.setPosition(new Vector2(tile.x * 64 + 32, tile.y * 64 + 32));
                        background.add(tile);
                        usedPositions.add(tilePosition);
                    }
                }
            }

            // Load wiring
            if (nextLine.startsWith("@wiring_tiles")) {
                while (scanner.hasNext()) {
                    if (scanner.hasNext("@wiring_component_tiles"))
                        break;
                    nextLine = scanner.nextLine();
                    if (nextLine.isBlank())
                        break;
                    Tile tile = getTile(nextLine);
                    if (tile != null && !tile.image.equals("delete")) {
                        Point tilePosition = new Point(tile.x, tile.y);
                        levelWidth = Math.max(levelWidth, tile.x);
                        levelHeight = Math.max(levelHeight, tile.y);
                        if (tile.hasTag("start"))
                            LittleH.program.dynamicCamera.setPosition(new Vector2(tile.x * 64 + 32, tile.y * 64 + 32));
                        wiring.add(tile);
                        usedPositions.add(tilePosition);
                    }
                }
                break;
            }

            // Load wiring
            if (nextLine.startsWith("@wiring_component_tiles")) {
                while (scanner.hasNext()) {
                    nextLine = scanner.nextLine();
                    if (nextLine.isBlank())
                        break;
                    Tile tile = getTile(nextLine);
                    if (tile != null && !tile.image.equals("delete")) {
                        Point tilePosition = new Point(tile.x, tile.y);
                        levelWidth = Math.max(levelWidth, tile.x);
                        levelHeight = Math.max(levelHeight, tile.y);
                        if (tile.hasTag("start"))
                            LittleH.program.dynamicCamera.setPosition(new Vector2(tile.x * 64 + 32, tile.y * 64 + 32));
                        wiring_components.add(tile);
                        usedPositions.add(tilePosition);
                    }
                }
                break;
            }

            Tile tile = getTile(nextLine);
            if (tile != null && !tile.image.equals("delete")) {
                Point tilePosition = new Point(tile.x, tile.y);
                if (usedPositions.contains(tilePosition)) continue;
                levelWidth = Math.max(levelWidth, tile.x);
                levelHeight = Math.max(levelHeight, tile.y);
                if (tile.hasTag("start"))
                    LittleH.program.dynamicCamera.setPosition(new Vector2(tile.x * 64 + 32, tile.y * 64 + 32));
                tiles.add(tile);
                usedPositions.add(tilePosition);
            }
        }

        levelWidth += 1;
        levelHeight += 1;

        if (invertY) {
            for (Tile tile : tiles) {
                tile.y = levelHeight - tile.y - 1;
                if (tile.hasTag("start"))
                    LittleH.program.dynamicCamera.targetPosition = new Vector2(tile.x * 64 + 32, tile.y * 64 + 32);
            }
        }

        scanner.close();

        int minY;
        int minX;

        if (tiles.size() > 0) {
            minX = tiles.get(0).x;
            minY = tiles.get(0).y;
        } else {
            minX = 0;
            minY = 0;
        }

        if (minY != 0 && minX != 0) {
            for (Tile tile : tiles) {
                minX = Math.min(tile.x, minX);
                minY = Math.min(tile.y, minY);
                if (minY == 0 && minX == 0) break;
            }

            for (Tile tile : background) {
                minX = Math.min(tile.x, minX);
                minY = Math.min(tile.y, minY);
                if (minY == 0 && minX == 0) break;
            }

            for (Tile tile : wiring) {
                minX = Math.min(tile.x, minX);
                minY = Math.min(tile.y, minY);
                if (minY == 0 && minX == 0) break;
            }

            if (minY != 0 || minX != 0) {
                for (Tile tile : tiles) {
                    tile.x -= minX;
                    tile.y -= minY;
                    if (tile.hasTag("start"))
                        LittleH.program.dynamicCamera.setPosition(new Vector2(tile.x * 64 + 32, tile.y * 64 + 32));
                }

                for (Tile tile : background) {
                    tile.x -= minX;
                    tile.y -= minY;
                }

                for (Tile tile : wiring) {
                    tile.x -= minX;
                    tile.y -= minY;
                }
            }
        }

        level.addTiles(tiles, levelWidth, levelHeight);
        level.addBackground(background);
        level.addWiring(wiring);
        level.addWiringComponents(wiring_components);

        if (updateTilesets) {
            LevelEditor levelEditor = new LevelEditor(level, "normal");
            LevelEditor backgroundEditor = new LevelEditor(level, "background");
            LevelEditor wiringEditor = new LevelEditor(level, "wiring");
            for (Tile tile : tiles) {
                levelEditor.checkTiling(levelEditor.getNeighbors(tile, tile.x, tile.y), tile);
            }
            for (Tile tile : background) {
                backgroundEditor.checkTiling(backgroundEditor.getNeighbors(tile, tile.x, tile.y), tile);
            }
            for (Tile tile : wiring) {
                wiringEditor.checkTiling(wiringEditor.getNeighbors(tile, tile.x, tile.y), tile);
            }
        }

        mapData.insertValue("version", LittleH.VERSION);

        return level;
    }

    public static Level readLevel(SabData mapData, File file) throws IOException {
        try {
            Level level = readLevel(new FileInputStream(file), new FileInputStream(file));
            if (invertY) {
                saveLevel(file, level);
            }

            if (updateTilesets) {
                saveLevel(file, level);
            }

            return level;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void trim(List<List<Tile>> tilemap) {

    }

    public static Tile getTile(String string) {
        Scanner scanner = new Scanner(string + " ");
        String image = "";
        try {
            // Position & image
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            image = scanner.next();

            // Set up identifier for tags
            String identifier = image;
            if (!image.startsWith("."))
                identifier = image.substring(image.lastIndexOf("/") + 1);

            if (identifier.equals("delete"))
                return null;

            // Tile type (which part of the spritesheet)
            int tileType = scanner.nextInt();

            String extra = scanner.nextLine().stripTrailing();
            if (extra.isBlank())
                // Save RAM by not storing a reference to a blank string
                extra = null;

            // Generate the tile
            scanner.close();
            TileTags tileTags = getTileTags(image);
            return new Tile(x, y, image, tileType, new TileTags(tileTags), extra);
        } catch (Exception e) {
            System.out.println(image);
            e.printStackTrace();
            if (scanner.hasNext()) {
                System.out.println(scanner.next());
            }
            scanner.close();
            return null;
        }
    }

    public static TileTags getTileTags(String image) {
        if (!tileTagCache.containsKey(image)) {
            String[] tags = tagsByTile.getValue(image.substring(image.lastIndexOf("/") + 1)).asStringArray();
            TileTags tileTags = new TileTags();
            for (String tag : tags) {
                int index = tag.indexOf("(");
                if (index != -1) {
                    tileTags.addTag(tag.substring(0, index), tag.substring(index + 1, tag.lastIndexOf(")")));
                } else {
                    tileTags.addTag(tag);
                }
            }
            tileTagCache.put(image, tileTags);
        }
        return tileTagCache.get(image);
    }

    public static void saveLevel(File file, Level level) {
        try {
            file.delete();
            file.createNewFile();
            SabWriter.write(file, level.mapData);
            FileWriter writer = new FileWriter(file, true);
            for (Tile tile : level.getBaseLayer().allTiles) {
                writer.write(tile.x + " " + tile.y + " " + tile.image + " " + tile.tileType + " ");
                if (tile.extra != null)
                    writer.write(tile.extra + " ");
                writer.write("\n");
            }
            if (level.getBackgroundLayer().allTiles.size() > 0) {
                writer.write("@background_tiles\n");
                for (Tile tile : level.getBackgroundLayer().allTiles) {
                    writer.write(tile.x + " " + tile.y + " " + tile.image + " " + tile.tileType + " ");
                    if (tile.extra != null)
                        writer.write(tile.extra + " ");
                    writer.write("\n");
                }
            }
            if (level.getWiringLayer().allTiles.size() > 0) {
                writer.write("@wiring_tiles\n");
                for (Tile tile : level.getWiringLayer().allTiles) {
                    writer.write(tile.x + " " + tile.y + " " + tile.image + " " + tile.tileType + " ");
                    if (tile.extra != null)
                        writer.write(tile.extra + " ");
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createLevel(String name, String fileName, String authorName, String background) {
        File[] files = LittleH.findMaps();
        Set<String> levelNames = new HashSet<>();
        for (File file : files) {
            levelNames.add(file.getName());
        }
        String fileNameTemp = fileName + ".map";
        int i = 0;
        while (levelNames.contains(fileNameTemp)) {
            i++;
            fileNameTemp = fileName + " (" + i + ").map";
        }
        fileName = fileNameTemp;

        File levelFile = new File(LittleH.mapsFolder.getPath() + "/" + fileName);
        try {
            levelFile.createNewFile();
        } catch (IOException e) {
            LittleH.pendingMenu = new LevelErrorMenu("Program does not have the permission to create files or one with the same file name already exists");
        }
        SabData mapData = new SabData();
        for (i = 0; i < defaultValues.length; i++) {
            mapData.insertValue(expectedProperties[i], defaultValues[i]);
        }
        mapData.insertValue("name", name);
        mapData.insertValue("author", authorName);
        mapData.insertValue("background", background);

        Level level = new Level(mapData);

        level.addTiles(new ArrayList<>(), 16, 16);
        level.addBackground(new ArrayList<>());
        level.addWiring(new ArrayList<>());

        LevelLoader.saveLevel(levelFile, level);

        LittleH.pendingMenu = new LevelEditorMenu(levelFile, level);
    }
}
