package com.sab.littleh.campaign.visual_novel.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.visual_novel.dialogue.VnDialogue;
import com.sab.littleh.controls.Control;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.util.*;
import com.sab.littleh.util.dialogue.Dialogues;

import java.util.ArrayList;
import java.util.List;

public class VnOverworldMenu extends MainMenu {
    private static final Animation idleAnimation = new Animation(20, 0, 1);
    private static final Animation walkAnimation = new Animation(6, 2, 3, 4, 5);
    private static final Animation runAnimation = new Animation(4, 2, 3, 4, 5);
    static {
        walkAnimation.setOnFrameChange(frame -> {
            if (frame % 2 == 1) {
                SoundEngine.playSound("step.ogg");
            }
        });
        runAnimation.setOnFrameChange(frame -> {
            if (frame % 2 == 1) {
                SoundEngine.playSound("step.ogg");
            }
        });
    }
    private List<List<Tile>> terrainMap;
    private List<List<Tile>> terrainMapBackground;
    private Rectangle player;
    private int playerVerticalDirection;
    private int playerHorizontalDirection;
    private float playerRotation;
    private float playerSpeed;
    private Animation playerAnimation;

    public VnOverworldMenu() {
        player = new Rectangle(0, 0, 48, 48);
        terrainMap = new ArrayList<>();
        terrainMapBackground = new ArrayList<>();
        LevelLoader.loadTerrainMap("/scripts/visual_novel/overworld/terrain.map", terrainMap, terrainMapBackground);
    }

    @Override
    public void start() {
        LittleH.program.dynamicCamera.reset();
    }

    @Override
    public void update() {
        boolean moving = false;

        boolean movingVert = ControlInputs.isPressed("up") ^ ControlInputs.isPressed("down");
        boolean movingHori = ControlInputs.isPressed("left") ^ ControlInputs.isPressed("right");
        float speedDenom = 1f;
        if (movingVert && movingHori)
            speedDenom = 1.414f;

        if (movingVert) {
            if (ControlInputs.isPressed("up")) {
                player.y += playerSpeed / speedDenom * 0.67f;
                playerVerticalDirection = 1;
            }
            if (ControlInputs.isPressed("down")) {
                player.y -= playerSpeed / speedDenom * 0.67f;
                playerVerticalDirection = -1;
            }
            moving = true;
        }
        if (movingHori) {
            if (ControlInputs.isPressed("left")) {
                player.x -= playerSpeed / speedDenom;
                playerHorizontalDirection = -1;
            }
            if (ControlInputs.isPressed("right")) {
                player.x += playerSpeed / speedDenom;
                playerHorizontalDirection = 1;
            }
            moving = true;
        }

        playerSpeed *= 0.5f;
        if (moving) {
            float speedToAdd = 0;
            if (ControlInputs.isPressed("sprint")) {
                speedToAdd = 4f;
                if (playerAnimation == walkAnimation)
                    runAnimation.frame = walkAnimation.frame;
                playerAnimation = runAnimation;
                playerRotation -= 4f * playerHorizontalDirection;
            } else {
                speedToAdd = 2f;
                if (playerAnimation == runAnimation)
                    walkAnimation.frame = runAnimation.frame;
                playerAnimation = walkAnimation;
                playerRotation -= 0.25f * playerHorizontalDirection;
            }
            playerSpeed += speedToAdd;
            idleAnimation.reset();
        } else {
            runAnimation.reset();
            walkAnimation.reset();
            playerAnimation = idleAnimation;
        }
        playerRotation *= 0.75f;

        playerAnimation.stepLooping();
        LittleH.program.dynamicCamera.targetPosition = player.getPosition(new Vector2());
    }

    @Override
    public void close() {
        LittleH.program.dynamicCamera.reset();
    }

    @Override
    public void keyDown(int keycode) {
    }

    @Override
    public void mouseUp(int button) {
    }

    private String vnTileFromTileset(String identifier, boolean background) {
        String tileName = identifier.substring(identifier.lastIndexOf("/") + 1);
        if (tileName.equals("grass")) {
            return "campaign/visual_novel/tiles/grass_temp.png";
        }
        if (tileName.equals("sandstone")) {
            return "campaign/visual_novel/tiles/path.png";
        }
        if (tileName.equals("stone")) {
            return "campaign/visual_novel/tiles/stone_foreground.png";
        }
        return "missing.png";
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        Vector2 renderAround = g.getCameraPosition();
        int centerX = (int) (renderAround.x / 64);
        int centerY = (int) (renderAround.y / 32);
        int screenTileWidth = (int) Math.ceil(LittleH.program.getWidth() / 64f * LittleH.program.dynamicCamera.zoom + 4);
        int screenTileHeight = (int) Math.ceil(LittleH.program.getHeight() / 32f * LittleH.program.dynamicCamera.zoom + 4);
        int startX = centerX - screenTileWidth / 2;
        int startY = centerY - screenTileHeight / 2;
        int endX = startX + screenTileWidth;
        int endY = startY + screenTileHeight;

        for (int i = startX; i < endX; i++) {
            if (i < 0 || i >= terrainMapBackground.size()) continue;
            // Optimization :)
            for (int j = endY; j >= startY; j--) {
                if (j < 0 || j >= terrainMapBackground.get(i).size()) continue;
                Tile tile = terrainMapBackground.get(i).get(j);
                if (tile != null)
                    g.draw(Images.getImage(vnTileFromTileset(tile.image, true)), tile.x * 64, tile.y * 32 - 32, 64, 64);
            }
        }

        for (int i = startX; i < endX; i++) {
            if (i < 0 || i >= terrainMap.size()) continue;
            // Optimization :)
            for (int j = endY; j >= startY; j--) {
                if (j < 0 || j >= terrainMap.get(i).size()) continue;
                Tile tile = terrainMap.get(i).get(j);
                if (tile != null)
                    g.draw(Images.getImage(vnTileFromTileset(tile.image, true)), tile.x * 64, tile.y * 32, 64, 64);
            }
        }

        int frame = playerAnimation.getFrame();
        boolean lookLeft = (playerHorizontalDirection == -1);
        Rectangle drawTo = new Rectangle(player.x - 8 + (lookLeft ? player.width + 8 : 0), player.y, (player.width + 8) * (lookLeft ? -1 : 1), player.height + 8);
        g.setColor(Images.getHColor());
        g.drawImage(Images.getImage("campaign/visual_novel/player/player_color.png"), drawTo.x, drawTo.y, drawTo.width, drawTo.height, new Rectangle(0, frame * 16, 16, 16), playerRotation);
        g.resetColor();
        g.drawImage(Images.getImage("campaign/visual_novel/player/player.png"), drawTo.x, drawTo.y, drawTo.width, drawTo.height, new Rectangle(0, frame * 16, 16, 16), playerRotation);
    }
}
