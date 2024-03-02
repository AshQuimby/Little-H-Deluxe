package com.sab.littleh.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.GameObject;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.entity.player.powerups.GunMode;
import com.sab.littleh.game.level.wiring.Wiring;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.mainmenu.LevelEditorMenu;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.dialogue.Dialogue;
import com.sab.littleh.util.dialogue.Dialogues;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Level {
    public static final String[] backgrounds = {
            "mountains",
            "cold_mountains",
            "desert",
            "cave",
            "tundra",
            "hyperspace"
    };
    public static final Color[] themeTints = {
            new Color(0.7f, 0.4f, 0.0f, 0.05f),
            new Color(0.3f, 0.2f, 0.9f, 0.12f),
            new Color(0.8f, 0.6f, 0.0f, 0.1f),
            new Color(0.1f, 0.1f, 0.2f, 0.24f),
            new Color(0.0f, 0.4f, 0.8f, 0.1f),
            new Color(0.7f, 0.0f, 0.65f, 0.04f),
    };
    public static Level currentLevel;
    public final SabData mapData;
    private Vector2 cameraFocus;
    public HashMap<String, MapLayer> mapLayers;
//    public List<Tile> backgroundTiles;
//    public List<Tile> allTiles;
//    public List<Tile> checkpointSavedTiles;
//    public List<Tile> notifiableTiles;
//    public List<Tile> checkpointState;
//    public List<Tile> updatableTiles;
//    public List<List<Tile>> tileMap;
//    public List<List<Tile>> backgroundMap;
    public List<Tile> notifiableTiles;
    public List<Tile> updatableTiles;

    public Player playerBackup;
    public Player player;
    public int timeLimit;
    public int gameTick;
    private Tile testTile;
    private boolean levelEnded;
    private boolean tilesDesynced;
    private final List<Particle> particles;
    private final List<Enemy> enemies;
    private final List<GameObject> gameObjects;
    private String background;
    private Point startPos;
    private Popup popup;
    private ParallaxBackground parallaxBackground;
    private long currentTime;
    private long checkedTime;
    private Dialogue currentDialogue;
    private boolean ignoreDialogue;

    public Wiring wiring;
    private List<Tile> poweredTiles;
    private List<Tile> oldPoweredTiles;

    public Level(SabData mapData) {
        mapLayers = new HashMap<>();
        particles = new ArrayList<>();
        enemies = new ArrayList<>();
        gameObjects = new ArrayList<>();
        notifiableTiles = new ArrayList<>();
        updatableTiles = new ArrayList<>();
        this.mapData = mapData;
        background = mapData.getRawValue("background");
        timeLimit = mapData.getValue("time_limit").asInt();
        if (mapData.hasValue("movement_options")) {
            String value = mapData.getRawValue("movement_options");
            mapData.remove("movement_options");
            String[] values = value.replace(",", "").replace("[", "").replace("]", "").split(" ");
            String[] options = {
                    "double_jumping",
                    "wall_sliding",
                    "crouching"
            };
            for (int i = 0; i < 3; i++) {
                mapData.insertValue(options[i], new SabValue(values[i]));
            }
        }
        currentLevel = this;
    }

    public void init() {
        parallaxBackground = new ParallaxBackground(background, mapData.getValue("bonus_background"));
    }

    public void startGame(Point startPos) {
        GunMode.bullets.clear();
        enemies.clear();
        levelEnded = true;
        Dialogues.resetDialogues();
        player = new Player(new Point(startPos.x, startPos.y));
        playerBackup = player;
        LittleH.program.dynamicCamera.setPosition(player.getCenter());
        desyncTiles();
        SoundEngine.playMusic("areas/" + mapData.getRawValue("background") + "_song.ogg");
        gameTick = 0;
        timeLimit = mapData.getValue("time_limit").asInt();
        this.startPos = startPos;
        currentTime = 0;
        checkedTime = currentTime;
        Cursors.switchCursor("none");
        notify("game_start", startPos.x, startPos.y);

        // This cannot be moved. It must happen here
        for (Tile tile : getBaseLayer().allTiles) {
            // For wiring
            if (tile.hasTag("power_source")) {
                Tile wiringTile = getTileAt("wiring", tile.x, tile.y);
                if (wiringTile != null && wiringTile.hasTag("wire")) {
                    wiringTile.tags.addTag("receiver");
                }
            }
        }
        for (Tile tile : getWiringLayer().allTiles) {
            // For wiring
            if (tile.hasTag("repeater")) {
                int dx = Wiring.dx(tile.tileType);
                int dy = Wiring.dy(tile.tileType);
                Tile facing = getTileAt("wiring", tile.x + dx, tile.y + dy);
                if (facing != null && facing.hasTag("wire")) {
                    facing.tags.addTag("receiver");
                }
            }
            if (tile.hasTag("and_gate")) {
                byte left = (byte) ((tile.tileType - 1) % 4);
                byte right = (byte) ((tile.tileType + 1) % 4);

                Tile other = getTileAt("wiring", tile.x + Wiring.dx(left), tile.y + Wiring.dy(left));
                if (other != null && other.hasTag("wire")) {
                    other.tags.addTag("receiver");
                }
                other = getTileAt("wiring", tile.x + Wiring.dx(right), tile.y + Wiring.dy(right));
                if (other != null && other.hasTag("wire")) {
                    other.tags.addTag("receiver");
                }

                Tile out = getTileAt("wiring", tile.x + Wiring.dx(tile.tileType), tile.y + Wiring.dy(tile.tileType));
                if (out != null && out.hasTag("wire")) {
                    out.tags.addTag("receiver");
                }
            }
        }

        wiring = new Wiring(this);
        poweredTiles = new ArrayList<>();
        saveCheckpointState();
    }

    public void removeTile(String layer, int x, int y) {
        if (inGame())
            throw new IllegalStateException("Do NOT call removeTile() in-game, it will PERMANENTLY delete a tile from the level while in the level editor and will NOT be reset to checkpoint states.");
        Tile toRemove = getTileAt(layer, x, y);
        if (toRemove != null) {
            while (mapLayers.get(layer).allTiles.contains(toRemove))
                mapLayers.get(layer).allTiles.remove(toRemove);
        }
    }

    public void changeBackground(String background) {
        mapData.insertValue("background", background);
        this.background = background;
        parallaxBackground = new ParallaxBackground(background);
    }

    public void startPopup(String text, int length) {
        popup = new Popup(text, length);
    }

    public void addTile(String layer, Tile tile) {
        mapLayers.get(layer).allTiles.add(tile);
    }

    public void addTileToMap(String layer, Tile tile) {
        mapLayers.get(layer).tileMap.get(tile.x).set(tile.y, tile);
    }

    public void addTiles(List<Tile> tiles, int levelWidth, int levelHeight) {
        MapLayer newLayer = new MapLayer();
        mapLayers.put("normal", newLayer);
        newLayer.tileMap = new ArrayList<>(levelWidth);
        for (int i = 0; i < levelWidth; i++) {
            newLayer.tileMap.add(i, new ArrayList<>(levelHeight));
            for (int j = 0; j < levelHeight; j++) {
                newLayer.tileMap.get(i).add(null);
            }
        }
        newLayer.allTiles.addAll(tiles);
        for (Tile tile : newLayer.allTiles) {
            newLayer.tileMap.get(tile.x).set(tile.y, tile);
        }
    }

    public void addBackground(List<Tile> tiles) {
        MapLayer newLayer = new MapLayer();
        mapLayers.put("background", newLayer);
        newLayer.tileMap = new ArrayList<>(getWidth());
        for (int i = 0; i < getWidth(); i++) {
            newLayer.tileMap.add(i, new ArrayList<>(getHeight()));
            for (int j = 0; j < getHeight(); j++) {
                newLayer.tileMap.get(i).add(null);
            }
        }
        newLayer.allTiles.addAll(tiles);
        for (Tile tile : newLayer.allTiles) {
            newLayer.tileMap.get(tile.x).set(tile.y, tile);
        }
    }

    public void addWiring(List<Tile> tiles) {
        MapLayer newLayer = new MapLayer();
        mapLayers.put("wiring", newLayer);
        newLayer.tileMap = new ArrayList<>(getWidth());
        for (int i = 0; i < getWidth(); i++) {
            newLayer.tileMap.add(i, new ArrayList<>(getHeight()));
            for (int j = 0; j < getHeight(); j++) {
                newLayer.tileMap.get(i).add(null);
            }
        }
        newLayer.allTiles.addAll(tiles);
        for (Tile tile : newLayer.allTiles) {
            newLayer.tileMap.get(tile.x).set(tile.y, tile);
        }
    }

    public void addWiringComponents(List<Tile> tiles) {
        MapLayer newLayer = new MapLayer();
        mapLayers.put("wiring_components", newLayer);
        newLayer.tileMap = new ArrayList<>(getWidth());
        for (int i = 0; i < getWidth(); i++) {
            newLayer.tileMap.add(i, new ArrayList<>(getHeight()));
            for (int j = 0; j < getHeight(); j++) {
                newLayer.tileMap.get(i).add(null);
            }
        }
        newLayer.allTiles.addAll(tiles);
        for (Tile tile : newLayer.allTiles) {
            newLayer.tileMap.get(tile.x).set(tile.y, tile);
        }
    }

    public void powerTile(Tile tile) {
        if (!poweredTiles.contains(tile)) {
            poweredTiles.add(tile);
        }
    }

    public boolean wasPowered(Tile tile) {
        return oldPoweredTiles.contains(tile);
    }

    public void mouseUp() {
        if (currentDialogue != null) {
            currentDialogue.mouseUp();
        }
    }

    public void enterPressed() {
        if (currentDialogue != null) {
            if (currentDialogue.finished()) {
                currentDialogue = null;
            } else if (currentDialogue.finishedBlock()) {
                currentDialogue.nextBlock();
            } else {
                currentDialogue.toEnd();
            }
        }
    }

    public boolean escapePressed() {
        if (currentDialogue != null) {
            currentDialogue = null;
            return false;
        }
        return true;
    }

    public void update() {
        if (inGame()) {
            gameTick++;

            oldPoweredTiles = poweredTiles;
            poweredTiles = new ArrayList<>();
            for (Tile powered : oldPoweredTiles) {
                powered.signalReceived(this);
            }

            if (currentDialogue != null) {
                currentDialogue.update();
                if (!currentDialogue.finishedBlock()) {
                    currentDialogue.next();
                }
                ControlInput.localControls.releaseControl(Controls.UP);
                ControlInput.localControls.releaseControl(Controls.DOWN);
                ControlInput.localControls.releaseControl(Controls.LEFT);
                ControlInput.localControls.releaseControl(Controls.RIGHT);
                ControlInput.localControls.releaseControl(Controls.JUMP);
                player.update(this);
                particles.forEach(Particle::update);
                particles.removeIf(particle -> !particle.alive);
                return;
            }

            if (player.startSpeedrunTimer() && !player.win) {
                if (player.ticksAlive % 60 == 0) {
                    timeLimit--;
                    if (timeLimit == 0) {
                        player.trueKill();
                        timeLimit = mapData.getValue("time_limit").asInt();
                    }
                }

                currentTime += 16 + (gameTick % 3 == 0 ? 0 : 1);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && mapData.getValue("look_around").asBool()) {
                if (Cursors.cursorIs("none")) {
                    Gdx.input.setCursorPosition(-MainMenu.relZeroX(), -MainMenu.relZeroY());
                    Cursors.switchCursor("magnifier");
                }
                LittleH.program.dynamicCamera.setPosition(player.getCenter());
                LittleH.program.dynamicCamera.update();
                LittleH.program.dynamicCamera.setPosition(MouseUtil.getDynamicMousePosition());
            } else {
                if (Cursors.cursorIs("magnifier")) {
                    Gdx.input.setCursorPosition(-MainMenu.relZeroX(), -MainMenu.relZeroY());
                    Cursors.switchCursor("none");
                }
            }

            cameraFocus = player.getCenter();
            player.update(this);

            for (Tile tile : updatableTiles) {
                tile.update(this);
            }

            float cameraScalar = Math.min(2400f / LittleH.program.getWidth(), 1350f / LittleH.program.getHeight());
            LittleH.program.dynamicCamera.targetZoom = cameraScalar / Settings.localSettings.zoomScalar.asFloat();
            if (player != null && !player.warpingOut())
                LittleH.program.dynamicCamera.targetPosition = cameraFocus;

        }

        for (GameObject gameObject : gameObjects) {
            gameObject.update(this);
        }

        for (Particle particle : particles) {
            particle.update();
        }

        for (Enemy enemy : enemies) {
            enemy.update(this);
        }

        enemies.removeIf(enemy -> {
            if (enemy.remove) {
                enemy.getParent().removeTag("enemy");
            }
            return enemy.remove || enemy.despawn;
        });
        gameObjects.removeIf(gameObject -> gameObject.remove);
        particles.removeIf(particle -> !particle.alive);
    }

    public MapLayer getBaseLayer() {
        return mapLayers.get("normal");
    }

    public MapLayer getBackgroundLayer() {
        return mapLayers.get("background");
    }

    public MapLayer getWiringLayer() {
        return mapLayers.get("wiring");
    }

    public void inGameSetTile(String layer, int x, int y, Tile tile) {
        inGameSetTile(mapLayers.get(layer), x, y, tile);
    }
    public void inGameSetTile(MapLayer mapLayer, int x, int y, Tile tile) {
        if (tilesDesynced) {
            if (tile == null) {
                // Delete a tile in game
                Tile tileAt = mapLayer.tileMap.get(x).get(y);
                if (tileAt == null) return;

                if (tileAt.hasTag("checkpoint_saved")) {
                    mapLayer.saveImportantTiles.remove(tileAt);
                }
                if (tileAt.hasTag("notifiable") || tileAt.hasTag("notify")) {
                    notifiableTiles.remove(tileAt);
                }
                if (tileAt.hasTag("update")) {
                    updatableTiles.remove(tileAt);
                }
                mapLayer.tileMap.get(x).set(y, null);
            } else {
                // Add a tile in game
                if (tile.hasTag("checkpoint_saved") && !mapLayer.saveImportantTiles.contains(tile)) {
                    mapLayer.saveImportantTiles.add(tile);
                }
                if ((tile.hasTag("notifiable") || tile.hasTag("notify")) && !notifiableTiles.contains(tile)) {
                    notifiableTiles.add(tile);
                }
                if (tile.hasTag("update") && !updatableTiles.contains(tile)) {
                    updatableTiles.add(tile);
                }
                mapLayer.tileMap.get(x).set(y, tile);
            }
        } else {
            throw new IllegalStateException("The Level must be desynced before calling this to avoid the permanent deletion of tiles");
        }
    }

    public void inGameRemoveTile(Tile tile) {
        if (tilesDesynced) {
            inGameSetTile("normal", tile.x, tile.y, null);
        } else {
            throw new IllegalStateException("The Level must be desynced before calling this to avoid the permanent deletion of tiles");
        }
    }

    public void inGameAddTile(String layer, Tile tile) {
        if (tilesDesynced) {
            if (tile.hasTag("checkpoint_saved")) {
                mapLayers.get(layer).saveImportantTiles.add(tile);
            }
            if (tile.hasTag("notifiable") || tile.hasTag("notify")) {
                notifiableTiles.add(tile);
            }
            if (tile.hasTag("update")) {
                updatableTiles.add(tile);
            }
            mapLayers.get(layer).tileMap.get(tile.x).set(tile.y, tile);
        } else {
            throw new IllegalStateException("The Level must be desynced before calling this to avoid the permanent alteration of tiles");
        }
    }

    public void inGameAddTile(Tile tile) {
        inGameAddTile("normal", tile);
    }

    public boolean inGame() {
        return player != null;
    }

    public int getCheckpointSavedTileCount(String tag, int tileType) {
        int count = 0;
        for (Tile tile : getBaseLayer().saveImportantTiles) {
            if (tile.hasTag(tag) && tile.tileType == tileType) {
                count++;
            }
        }
        return count;
    }

    public int getWidth() {
        return getBaseLayer().tileMap.size();
    }

    public void suicide() {
        if (player != null)
            player.kill();
    }

    public int getHeight() {
        return getBaseLayer().tileMap.get(0).size();
    }

    public Tile getTileAt(String layer, int x, int y) {
        if (x >= 0 && y >= 0 && x < getWidth() && y < getHeight()) return mapLayers.get(layer).tileMap.get(x).get(y);
        return null;
    }

    public void endGame() {
        GunMode.bullets.clear();
        currentDialogue = null;
        player = null;
        levelEnded = true;
        enemies.clear();
        particles.clear();
        gameObjects.clear();
        syncTiles();
        if (LittleH.program.getMenu() instanceof LevelEditorMenu) {
            ((LevelEditorMenu) LittleH.program.getMenu()).resetToolCursor();
            SoundEngine.playMusic(Settings.localSettings.buildingSong.value);
        }
    }

    public void resetToCheckpointState() {
        GunMode.bullets.clear();
        enemies.clear();
        notifiableTiles.removeIf(tile -> tile.hasTag("checkpoint_saved"));
        updatableTiles.removeIf(tile -> tile.hasTag("checkpoint_saved"));
        for (MapLayer mapLayer : mapLayers.values()) {
            mapLayer.saveImportantTiles.clear();
            for (Tile tile : mapLayer.checkpointState) {
                Tile copy = tile.copy();
                inGameSetTile(mapLayer, copy.x, copy.y, copy);
            }
        }
    }

    public void saveCheckpointState() {
        for (MapLayer mapLayer : mapLayers.values()) {
            mapLayer.checkpointState.clear();
            for (Tile tile : mapLayer.saveImportantTiles) {
                mapLayer.checkpointState.add(tile.copy());
            }
        }
    }

    public void syncTiles() {
        enemies.clear();
        notifiableTiles.clear();
        updatableTiles.clear();

        for (MapLayer mapLayer : mapLayers.values()) {
            mapLayer.saveImportantTiles.clear();
            mapLayer.clearTileMap();
            for (Tile tile : mapLayer.allTiles) {
                tile.setTags();
                mapLayer.tileMap.get(tile.x).set(tile.y, tile);
            }
        }
        tilesDesynced = false;
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void addMiscGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public void desyncTiles() {
        notifiableTiles.clear();
        updatableTiles.clear();
        for (MapLayer mapLayer : mapLayers.values()) {
            mapLayer.saveImportantTiles.clear();
            for (Tile tile : mapLayer.allTiles) {
                Tile copy = tile.copy();
                mapLayer.tileMap.get(copy.x).set(copy.y, copy);
                if (tile.hasTag("checkpoint_saved")) {
                    mapLayer.saveImportantTiles.add(copy);
                }
                if (tile.hasTag("notifiable") || tile.hasTag("notify")) {
                    notifiableTiles.add(copy);
                }
                if (tile.hasTag("update")) {
                    updatableTiles.add(copy);
                }
            }
        }
        tilesDesynced = true;
    }

    public void renderBackground(Graphics g) {
        parallaxBackground.render(g);
    }

    public void render(Graphics g) {
        render(g, false, false, false);
    }

    public void render(Graphics g, boolean backgroundPriority, boolean wiringMode, boolean wiringComponentSelected) {
        List<Point> toDrawGrid = new ArrayList<>();
        if (wiringMode && !inGame()) {
            toDrawGrid = renderWiring(g, backgroundPriority, false);
        } else {
            if (wiringComponentSelected) {
                g.setTint(new Color(1, 1, 1, 0.5f));
                renderWiring(g, false, true);
                g.resetTint();
            }
            Vector2 renderAround = g.getCameraPosition();
            int centerX = (int) (renderAround.x / 64);
            int centerY = (int) (renderAround.y / 64);
            int screenTileWidth = (int) Math.ceil(LittleH.program.getWidth() / 64f * LittleH.program.dynamicCamera.zoom + 4);
            int screenTileHeight = (int) Math.ceil(LittleH.program.getHeight() / 64f * LittleH.program.dynamicCamera.zoom + 4);
            int startX = centerX - screenTileWidth / 2;
            int startY = centerY - screenTileHeight / 2;
            int endX = startX + screenTileWidth;
            int endY = startY + screenTileHeight;

            List<Tile> backgroundPostRenders = new ArrayList<>();

            g.resetTint();
            g.resetColor();

            if (!inGame()) {
                if (backgroundPriority) {
                    g.setTint(new Color(0.5f, 0.5f, 0.5f, 0.5f));
                } else {
                    g.setTint(new Color(0.5f, 0.5f, 0.5f, 0.5f));
                }
            } else {
                g.setTint(new Color(0.45f, 0.45f, 0.45f, 1f));
            }

            // Draw in the back
            for (int i = startX; i < endX; i++) {
                if (i < 0 || i >= getWidth()) continue;
                for (int j = startY; j < endY; j++) {
                    if (j < 0 || j >= getHeight()) continue;
                    if (!inGame()) {
                        Tile tile;
                        if (backgroundPriority) {
                            // Draw foreground behind
                            tile = getTileAt("normal", i, j);
                        } else {
                            // Draw background
                            tile = getTileAt("background", i, j);
                        }
                        if (tile != null) {
                            tile.render(inGame(), g);
                            if (tile.hasTag("post_render"))
                                backgroundPostRenders.add(tile);
                        }
                    } else {
                        // Draw background
                        // Tint multiplies with graphics color to make a darker background
                        Tile tile = getTileAt("background", i, j);
                        if (tile != null) {
                            tile.render(inGame(), g);
                            if (tile.hasTag("post_render"))
                                backgroundPostRenders.add(tile);
                            else if (tile.hasTag("special_render") && player != null)
                                backgroundPostRenders.add(tile);
                        }
                    }
                }
            }

            g.resetTint();
            g.resetColor();

            List<Tile> postRenders = new ArrayList<>();
            List<Tile> visibleTiles = new ArrayList<>();

            backgroundPostRenders = (drawPostRenders(g, backgroundPostRenders, true, true, false));

            // Draw in the front but behind players
            for (int i = startX; i < endX; i++) {
                if (i < 0 || i >= getWidth()) continue;
                for (int j = startY; j < endY; j++) {
                    if (j < 0 || j >= getHeight()) continue;
                    if (!inGame()) {
                        // Draw background in front
                        if (backgroundPriority) {
                            Tile tile = getTileAt("background", i, j);
                            if (tile != null) {
                                if (!(tile.isSolid() && tile.hasTag("tileset"))) {
                                    tile.render(inGame(), g);
                                } else visibleTiles.add(tile);
                                if (tile.hasTag("post_render"))
                                    postRenders.add(tile);
                            }
                            toDrawGrid.add(new Point(i * 64, j * 64));
                            continue;
                        }
                        // Draw foreground
                        Tile tile = getTileAt("normal", i, j);
                        if (tile != null) {
                            if (!(tile.isSolid() && tile.hasTag("tileset"))) {
                                tile.render(inGame(), g);
                            } else visibleTiles.add(tile);
                            if (tile.hasTag("post_render"))
                                postRenders.add(tile);
                        }
                        toDrawGrid.add(new Point(i * 64, j * 64));
                    } else {
                        // Draw foreground
                        Tile tile = getTileAt("normal", i, j);
                        if (tile != null) {
                            if (!(tile.isSolid() && !tile.ignoreTiling)) tile.render(inGame(), g);
                            else visibleTiles.add(tile);
                            if (tile.hasTag("post_render"))
                                postRenders.add(tile);
                            else if (tile.hasTag("special_render") && player != null)
                                postRenders.add(tile);
                        }
                    }
                }
            }

            postRenders = drawPostRenders(g, postRenders, false, true, false);

            enemies.forEach(enemy -> enemy.render(g, this));
            gameObjects.forEach(gameObject -> gameObject.render(g, this));
            if (inGame())
                player.render(g, this);
            particles.forEach(particle -> particle.render(g));

            if (!inGame()) {
                if (backgroundPriority) {
                    g.setTint(new Color(0.5f, 0.5f, 0.5f, 0.5f));
                } else {
                    g.setTint(new Color(0.5f, 0.5f, 0.5f, 0.5f));
                }
            } else {
                g.setTint(new Color(0.45f, 0.45f, 0.45f, 1f));
            }

            if (!inGame()) {
                if (backgroundPriority) {
                    g.setTint(new Color(1f, 1f, 1f, 0.65f));
                } else {
                    g.resetTint();
                }
            } else {
                g.resetTint();
            }

            List<Tile> textRenders = drawPostRenders(g, postRenders, false, false, false);
            textRenders.addAll(drawPostRenders(g, backgroundPostRenders, true, false, false));

            // Draw in the front
            for (Tile tile : visibleTiles) {
                if (!inGame()) {
                    // Draw background in front
                    if (backgroundPriority) {
                        if (tile != null) {
                            tile.render(inGame(), g);
                            if (tile.hasTag("post_render"))
                                postRenders.add(0, tile);
                        }
                        continue;
                    }
                    // Draw foreground
                    if (tile != null) {
                        tile.render(inGame(), g);
                        if (tile.hasTag("post_render"))
                            postRenders.add(0, tile);
                    }
                } else {
                    // Draw foreground
                    if (tile != null) {
                        tile.render(inGame(), g);
                        if (tile.hasTag("post_render"))
                            postRenders.add(0, tile);
                        else if (tile.hasTag("special_render") && player != null)
                            postRenders.add(0, tile);
                    }
                }
            }

            drawPostRenders(g, textRenders, false, false, true);
        }

        g.resetTint();

        if (Settings.localSettings.grid.value) {

            float zoom = LittleH.program.dynamicCamera.zoom;
            Color color = new Color(0.875f, 0.875f, 0.875f, Math.max(0, 1 - zoom / 6));

            if (color.a > 0.01f) {

                g.startShapeRenderer(color);

                for (Point point : toDrawGrid) {
                    g.drawRect(point.x, point.y, 64, 64);
                }

                g.endShapeRenderer();

            }
        }

        LittleH.program.useStaticCamera();

        if (wiringMode) {
            g.drawString("WIRING MODE", LittleH.borderedFont, -MainMenu.relZeroX() - 96, -MainMenu.relZeroY() - 96, LittleH.defaultFontScale * 0.75f, 1);
        } else if (backgroundPriority) {
            g.drawString("EDITING BACKGROUND", LittleH.borderedFont, -MainMenu.relZeroX() - 96, -MainMenu.relZeroY() - 96, LittleH.defaultFontScale * 0.75f, 1);
        }

        if (inGame())
            player.renderHUD(g, this);

        if (inGame()) {
            if (gameTick < 240 || Cursors.cursorIs("magnifier")) {
                String[] levelOptions = {
                        "double_jumping",
                        "wall_sliding",
                        "crouching",
                        "look_around"
                };
                String[] levelOptionImages = {
                        "ui/buttons/icons/double_jump.png",
                        "ui/buttons/icons/wall_jump.png",
                        "ui/buttons/icons/slide.png",
                        "ui/buttons/icons/magnifying_glass.png"
                };

                g.setColor(new Color(1, 1, 1, Math.max(1, Math.min(240 - gameTick, 120)) / 120f));
                if (Cursors.cursorIs("magnifier")) {
                    g.resetColor();
                }

                for (int i = 0; i < levelOptions.length; i++) {
                    g.draw(Images.getImage(levelOptionImages[i]),
                            -MainMenu.relZeroX() - levelOptions.length * 80 + i * 80 + 8, MainMenu.relZeroY() + 8, 64, 64);
                    g.draw(Images.getImage(mapData.getValue(levelOptions[i]).asBool() ? "ui/level_setting_on.png" : "ui/level_setting_off.png"),
                            -MainMenu.relZeroX() - levelOptions.length * 80 + i * 80, MainMenu.relZeroY(), 80, 80);
                }

                g.resetColor();
            }
        }

        if (popup != null) {
            popup.render(g);
            if (popup.timeLeft < 0) {
                popup = null;
            }
        }

        if (currentDialogue != null) {
            currentDialogue.render(g);
        }
    }

    // Returns tile positioned run through for drawing the grid
    private List<Point> renderWiring(Graphics g, boolean backgroundPriority, boolean justWiring) {
        Vector2 renderAround = g.getCameraPosition();
        int centerX = (int) (renderAround.x / 64);
        int centerY = (int) (renderAround.y / 64);
        int screenTileWidth = (int) Math.ceil(LittleH.program.getWidth() / 64f * LittleH.program.dynamicCamera.zoom + 4);
        int screenTileHeight = (int) Math.ceil(LittleH.program.getHeight() / 64f * LittleH.program.dynamicCamera.zoom + 4);
        int startX = centerX - screenTileWidth / 2;
        int startY = centerY - screenTileHeight / 2;
        int endX = startX + screenTileWidth;
        int endY = startY + screenTileHeight;
        List<Point> tilesRendered = new ArrayList<>();
        MapLayer layerToRender;
        if (!justWiring) {
            layerToRender = mapLayers.get(backgroundPriority ? "background" : "normal");
            g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.4f));
            for (int i = startX; i < endX; i++) {
                if (i < 0 || i >= getWidth()) continue;
                for (int j = startY; j < endY; j++) {
                    if (j < 0 || j >= getHeight()) continue;
                    Tile tile = layerToRender.tileMap.get(i).get(j);
                    if (tile != null) tile.render(inGame(), g);
                    tilesRendered.add(new Point(i, j));
                }
            }
            g.resetColor();
        }
        layerToRender = mapLayers.get("wiring");
        for (int i = startX; i < endX; i++) {
            if (i < 0 || i >= getWidth()) continue;
            for (int j = startY; j < endY; j++) {
                if (j < 0 || j >= getHeight()) continue;
                Tile tile = layerToRender.tileMap.get(i).get(j);
                if (tile != null) tile.render(inGame(), g);
            }
        }
        return tilesRendered;
    }

    public Tile[][] getNeighbors(int tileX, int tileY, boolean background) {
        // Magic
        Tile[][] neighbors = new Tile[3][3];
        for (int i = tileX - 1; i < tileX + 2; i++) {
            for (int j = tileY - 1; j < tileY + 2; j++) {
                Tile tile = background ? getTileAt("background", i, j) : getTileAt("normal", i, j);
                if (!(i < 0 || j < 0 || i >= getWidth() || j >= getHeight()) && tile != null) {
                    neighbors[tileX - i + 1][tileY - j + 1] = tile;
                } else {
                    neighbors[tileX - i + 1][tileY - j + 1] = null;
                }
            }
        }
        return neighbors;
    }

    public List<Tile> drawPostRenders(Graphics g, List<Tile> postRenders, boolean background, boolean dontDrawForcedFront, boolean drawText) {
        List<Tile> leftovers = new ArrayList<>();
        Shaders.waterShader.bind();
        Shaders.waterShader.setUniformf("u_time", gameTick / 60f);
        Shaders.windyShader.bind();
        Shaders.windyShader.setUniformf("u_time", gameTick / 120f);
        Shaders.vineShader.bind();
        Shaders.vineShader.setUniformf("u_time", gameTick / 120f);
        g.resetShader();

        for (Tile tile : postRenders) {
            if (tile.hasTag("render_normal")) {
                if (tile.hasTag("water")) {
                    if (dontDrawForcedFront) {
                        leftovers.add(tile);
                    } else {
                        Tile[][] neighbors = getNeighbors(tile.x, tile.y, background);
                        Shaders.waterShader.bind();
                        Shaders.waterShader.setUniformMatrix("u_neighbors", new Matrix3(new float[]{
                                neighbors[0][0] != null && neighbors[0][0].isSolid() ? 1 : 0,
                                neighbors[1][0] != null && neighbors[1][0].isSolid() ? 1 : 0,
                                neighbors[2][0] != null && neighbors[2][0].isSolid() ? 1 : 0,
                                neighbors[0][1] != null && neighbors[0][1].isSolid() ? 1 : 0,
                                neighbors[1][1] != null && neighbors[1][1].isSolid() ? 1 : 0,
                                neighbors[2][1] != null && neighbors[2][1].isSolid() ? 1 : 0,
                                neighbors[0][2] != null && neighbors[0][2].isSolid() ? 1 : 0,
                                neighbors[1][2] != null && neighbors[1][2].isSolid() ? 1 : 0,
                                neighbors[2][2] != null && neighbors[2][2].isSolid() ? 1 : 0,
                        }));
                        Shaders.waterShader.setUniformf("u_tilePosition", new Vector2(tile.x, tile.y));
                        g.resetShader();
                        g.drawImageWithShader(Shaders.waterShader, tile.getImage(), tile.x * 64, tile.y * 64, 64, 64, tile.getDrawSection());
                    }
                } else if (tile.hasTag("windy")) {
                    Tile[][] neighbors = getNeighbors(tile.x, tile.y, background);
                    Shaders.windyShader.bind();
                    Shaders.windyShader.setUniformf("u_attached", neighbors[1][2] != null && neighbors[1][2].isSolid() ? 1 : 0);
                    Shaders.windyShader.setUniformf("u_tilePosition", new Vector2(tile.x, tile.y));
                    g.resetShader();
                    g.drawImageWithShader(Shaders.windyShader, tile.getImage(), tile.x * 64, tile.y * 64, 64, 64, tile.getDrawSection());
                } else if (tile.hasTag("vines")) {
                    Shaders.vineShader.bind();
                    float length = -1;
                    float vinePosition = -1;
                    if (getTileAt("normal", tile.x, tile.y + 1) != null) {
                        if (getTileAt("normal", tile.x, tile.y + 1).isSolid()) {
                            vinePosition = 0;
                        } else {
                            int i = 1;
                            while (getTileAt("normal", tile.x, tile.y + i) != null && getTileAt("normal", tile.x, tile.y + i).hasTag("vines")) {
                                i++;
                            }
                            vinePosition = i;
                        }
                    }
                    if (getTileAt("normal", tile.x, tile.y - 1) != null) {
                        int i = 1;
                        while (getTileAt("normal", tile.x, tile.y - i) != null && getTileAt("normal", tile.x, tile.y - i).hasTag("vines")) {
                            i++;
                        }
                        length = i + vinePosition - 1;
                    } else {
                        length = vinePosition;
                    }

                    length++;
                    vinePosition++;

                    Shaders.vineShader.setUniformf("u_attached", vinePosition);
                    Shaders.vineShader.setUniformf("u_vineLength", length);
                    Shaders.vineShader.setUniformf("u_tilePosition", new Vector2(tile.x, tile.y));
                    g.resetShader();
                    g.drawImageWithShader(Shaders.vineShader, tile.getImage(), tile.x * 64, tile.y * 64, 64, 64, tile.getDrawSection());
                } else {
                    tile.render(false, g);
                }
            }
            if (tile.hasTag("text")) {
                if (inGame()) {
                    if (!drawText) {
                        leftovers.add(tile);
                    } else {
                        if (tile.extra == null) tile.extra = "";
                        float size = 0.8f;
                        if (tile.tileType == 0) size = 1f;
                        else if (tile.tileType == 2) size = 0.7f;

                        LittleH.borderedFont.setColor(new Color(1, 1, 1, 1 - Math.min(1, player.getCenter().dst2(tile.x * 64 + 32, tile.y * 64 + 32) / (1280f * 1280f))));

                        g.drawString(tile.extra, LittleH.borderedFont, tile.x * 64 + 32, tile.y * 64 + 32, size * LittleH.defaultFontScale, 0);

                        LittleH.borderedFont.setColor(Color.WHITE);
                    }
                }
            }
            if (tile.hasTag("levitating")) {
                g.drawImage(tile.getImage(), tile.x * 64, tile.y * 64 + MathUtils.sinDeg(LittleH.getTick() / 2f + (tile.x + tile.y) * 15) * 8, 64, 64, tile.getDrawSection());
            }
            g.resetColor();
        }

        return leftovers;
    }

    public void renderHUD(Graphics g) {
        g.resetColor();
        g.resetTint();
        g.resetShader();
        if (timeLimit > -1) {
            g.draw(Images.getImage("ui/buttons/icons/clock.png"), -MainMenu.relZeroX() - 72, -MainMenu.relZeroY() - 72, 64, 64);
            g.drawString("" + timeLimit, LittleH.borderedFont, -MainMenu.relZeroX() - 80, -MainMenu.relZeroY() - 28 - 14, LittleH.defaultFontScale, 1);
        }
        g.drawString(getTime(), LittleH.borderedFont, MainMenu.relZeroX() + 16, -MainMenu.relZeroY() - 28, LittleH.defaultFontScale, -1);
    }

    public void reset() {
        resetToCheckpointState();
        GunMode.bullets.clear();
        player = null;
        ignoreDialogue = true;
        startGame(startPos);
    }

    public Point getStartPos() {
        for (Tile tile : getBaseLayer().allTiles) {
            if (tile.hasTag("start")) {
                return new Point(tile.x, tile.y);
            }
        }
        return null;
    }

    public void notify(String notification, int... data) {
        for (Tile tile : notifiableTiles) {
            if (tile.hasTag(notification)) {
                tile.notify(this, notification, data);
            }
            if (tile.hasTag("notify")) {
                for (String string : tile.tags.getTagParameters("notify")) {
                    if (string.equals(notification))
                        tile.notify(this, notification, data);
                }
            }
        }
    }

    public void showTimer() {
        startPopup(getSplit(), 120);
    }

    public String getSplit() {
        long time = currentTime - checkedTime;
        checkedTime = currentTime;
        return LittleH.formatTime(time);
    }

    public String getTime() {
        long time = currentTime;
        return LittleH.formatTime(time);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void setDialogue(Dialogue dialogue) {
        if (!ignoreDialogue)
            this.currentDialogue = dialogue;
    }

    public boolean hasDialogue() {
        return currentDialogue != null;
    }

    public void ignoreDialogue() {
        ignoreDialogue = true;
    }

    public void resumeDialogue() {
        ignoreDialogue = false;
    }

    public long getTimeMillis() {
        return currentTime;
    }

    public void setCameraFocus(Vector2 position) {
        cameraFocus = position;
    }

    private class Popup {
        private int timeLeft;
        private String text;

        private Popup(String text, int length) {
            timeLeft = length;
            this.text = text;
        }

        private void render(Graphics g) {
            LittleH.borderedFont.setColor(new Color(1, 1, 1, Math.min(1, timeLeft / 60f)));
            g.drawString(text, LittleH.borderedFont, 0, 64, LittleH.defaultFontScale, 0);
            timeLeft--;
            LittleH.borderedFont.setColor(Color.WHITE);
        }
    }
}
