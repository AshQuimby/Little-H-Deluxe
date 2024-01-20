package com.sab.littleh.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.entity.player.powerups.GunMode;
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
    public HashMap<String, MapLayer> mapLayers;
//    public List<Tile> backgroundTiles;
//    public List<Tile> allTiles;
//    public List<Tile> volatileTiles;
//    public List<Tile> notifiableTiles;
//    public List<Tile> checkpointState;
//    public List<Tile> updatableTiles;
//    public List<List<Tile>> tileMap;
//    public List<List<Tile>> backgroundMap;

    public Player player;
    public int timeLimit;
    public int gameTick;
    private Tile testTile;
    private boolean levelEnded;
    private boolean tilesDesynced;
    private List<Particle> particles;
    private List<Enemy> enemies;
    private String background;
    private Point startPos;
    private Popup popup;
    private ParallaxBackground parallaxBackground;
    private long currentTime;
    private long checkedTime;
    private Dialogue currentDialogue;

    public Level(SabData mapData) {
        mapLayers = new HashMap<>();
        particles = new ArrayList<>();
        enemies = new ArrayList<>();
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
        parallaxBackground = new ParallaxBackground(background);
    }

    public void startGame(Point startPos) {
        GunMode.bullets.clear();
        enemies.clear();
        levelEnded = true;
        Dialogues.resetDialogues();
        player = new Player(new Point(startPos.x, startPos.y));
        LittleH.program.dynamicCamera.setPosition(player.getCenter());
        desyncTiles();
        SoundEngine.playMusic("areas/" + mapData.getRawValue("background") + "_song.ogg");
        gameTick = 0;
        timeLimit = mapData.getValue("time_limit").asInt();
        this.startPos = startPos;
        currentTime = 0;
        checkedTime = currentTime;
        Cursors.switchCursor("none");
        notify("notify_game_start", startPos.x, startPos.y);
    }

    public void removeTile(String layer, int x, int y) {
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

            if (currentDialogue != null) {
                currentDialogue.update();
                if (!currentDialogue.finishedBlock()) {
                    currentDialogue.next();
                }
                ControlInputs.releaseControl(Controls.UP);
                ControlInputs.releaseControl(Controls.DOWN);
                ControlInputs.releaseControl(Controls.LEFT);
                ControlInputs.releaseControl(Controls.RIGHT);
                ControlInputs.releaseControl(Controls.JUMP);
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
        }

        if (player != null) {
            if (!player.warpingOut())
                LittleH.program.dynamicCamera.targetPosition = new Vector2(player.getCenterX(), player.y + player.height / 2);
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
            float cameraScalar = Math.min(2400f / LittleH.program.getWidth(), 1350f / LittleH.program.getHeight());
            LittleH.program.dynamicCamera.targetZoom = cameraScalar / Settings.localSettings.zoomScalar.asFloat();
            player.update(this);
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

        for (Tile tile : getBaseLayer().updatableTiles) {
            Vector2 tileCenter = new Vector2(tile.x * 64 + 32, tile.y * 64 + 32);
            if (player.getCenter().dst2(tileCenter) < 1420 * 1420) {
                tile.update(this);
            }
        }
        particles.removeIf(particle -> !particle.alive);
    }

    public MapLayer getBaseLayer() {
        return mapLayers.get("normal");
    }

    public MapLayer getBackgroundLayer() {
        return mapLayers.get("background");
    }

    public int getVolatileTileCount() {
        return getBaseLayer().volatileTiles.size();
    }

    public void inGameRemoveTile(Tile tile) {
        if (tilesDesynced) {
            if (tile.hasTag("volatile")) {
                getBaseLayer().volatileTiles.remove(tile);
            }
            if (tile.hasTag("notifiable")) {
                getBaseLayer().notifiableTiles.remove(tile);
            }
            if (tile.hasTag("updatable")) {
                getBaseLayer().updatableTiles.remove(tile);
            }
            getBaseLayer().tileMap.get(tile.x).set(tile.y, null);
        } else {
            throw new IllegalStateException("The Level must be desynced before calling this to avoid the permanent deletion of tiles");
        }
    }

    public boolean inGame() {
        return player != null;
    }

    public int getVolatileTileCount(String tag, int tileType) {
        int count = 0;
        for (Tile tile : getBaseLayer().volatileTiles) {
            if (tile.hasTag(tag) && tile.tileType == tileType)
                count++;
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
        syncTiles();
        if (LittleH.program.getMenu() instanceof LevelEditorMenu) {
            ((LevelEditorMenu) LittleH.program.getMenu()).resetToolCursor();
            SoundEngine.playMusic(Settings.localSettings.buildingSong.value);
        }
    }

    public void resetToCheckpointState() {
        GunMode.bullets.clear();
        enemies.clear();
        getBaseLayer().volatileTiles.clear();
        getBaseLayer().notifiableTiles.removeIf(tile -> tile.hasTag("volatile"));
        getBaseLayer().updatableTiles.removeIf(tile -> tile.hasTag("volatile"));
        for (Tile tile : getBaseLayer().checkpointState) {
            Tile copy = tile.copy();
            if (tile.hasTag("notifiable"))
                getBaseLayer().notifiableTiles.add(copy);
            if (tile.hasTag("updatable")) {
                getBaseLayer().updatableTiles.add(copy);
            }
            getBaseLayer().volatileTiles.add(copy);
            addTileToMap("normal", copy);
        }
    }

    public void saveCheckpointState() {
        getBaseLayer().checkpointState.clear();
        for (Tile tile : getBaseLayer().volatileTiles) {
            getBaseLayer().checkpointState.add(tile.copy());
        }
    }

    public void syncTiles() {
        enemies.clear();
        getBaseLayer().volatileTiles.clear();
        getBaseLayer().notifiableTiles.clear();
        getBaseLayer().updatableTiles.clear();
        for (Tile tile : getBaseLayer().allTiles) {
            tile.setTags();
            getBaseLayer().tileMap.get(tile.x).set(tile.y, tile);
        }
        tilesDesynced = false;
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void desyncTiles() {
        getBaseLayer().volatileTiles.clear();
        getBaseLayer().notifiableTiles.clear();
        getBaseLayer().updatableTiles.clear();
        for (Tile tile : getBaseLayer().allTiles) {
            Tile copy = tile.copy();
            getBaseLayer().tileMap.get(copy.x).set(copy.y, copy);
            if (tile.hasTag("volatile")) {
                getBaseLayer().volatileTiles.add(copy);
            }
            if (tile.hasTag("notifiable")) {
                getBaseLayer().notifiableTiles.add(copy);
            }
            if (tile.hasTag("updatable")) {
                getBaseLayer().updatableTiles.add(copy);
            }
        }
        for (Tile tile : getBackgroundLayer().allTiles) {
            if (tile.hasTag("ignore_background")) {
                Tile copy = tile.copy();
                if (tile.hasTag("updatable")) {
                    getBaseLayer().updatableTiles.add(copy);
                }
                if (tile.hasTag("notifiable")) {
                    getBaseLayer().notifiableTiles.add(copy);
                }
            }
        }
        saveCheckpointState();
        tilesDesynced = true;
    }

    public void renderBackground(Graphics g) {
        parallaxBackground.render(g);
    }

    public void render(Graphics g) {
        render(g, false);
    }

    public void render(Graphics g, boolean backgroundPriority) {
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
                        else if (tile.hasTag("post_render_in_game") && player != null)
                            backgroundPostRenders.add(tile);
                    }
                }
            }
        }

        g.resetTint();
        g.resetColor();

        List<Tile> postRenders = new ArrayList<>();
        List<Tile> visibleTiles = new ArrayList<>();
        List<Point> toDrawGrid = new ArrayList<>();

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
                            }
                            else visibleTiles.add(tile);
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
                        }
                        else visibleTiles.add(tile);
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
                        else if (tile.hasTag("post_render_in_game") && player != null)
                            postRenders.add(tile);
                    }
                }
            }
        }

        postRenders = drawPostRenders(g, postRenders, false, true, false);

        enemies.forEach(enemy -> enemy.render(g, this));
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
                    else if (tile.hasTag("post_render_in_game") && player != null)
                        postRenders.add(0, tile);
                }
            }
        }

        drawPostRenders(g, textRenders, false, false, true);

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

        if (backgroundPriority) {
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
            if (tile.hasTag("sinusoid")) {
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
        for (Tile tile : getBaseLayer().notifiableTiles) {
            if (tile.hasTag(notification)) {
                tile.notify(this, notification, data);
            }
        }
    }

    public void showTimer() {
        startPopup(getSplit(), 120);
    }

    public String getSplit() {
        long time = currentTime - checkedTime;
        checkedTime = currentTime;
        return String.format("%s:%s:%s", getTimeWithZeros(time / 60000, 2),
                getTimeWithZeros(time / 1000 % 60, 2),
                getTimeWithZeros(time % 1000, 3));
    }

    public String getTimeWithZeros(long time, int minChars) {
        String str = String.valueOf(time);

        while (str.length() < minChars) {
            str = 0 + str;
        }

        return str;
    }

    public String getTime() {
        long time = currentTime;
        return String.format("%s:%s:%s", getTimeWithZeros(time / 60000, 2),
                getTimeWithZeros(time / 1000 % 60, 2),
                getTimeWithZeros(time % 1000, 3));
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setDialogue(Dialogue dialogue) {
        this.currentDialogue = dialogue;
    }

    public boolean hasDialogue() {
        return currentDialogue != null;
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
