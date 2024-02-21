package com.sab.littleh.campaign.overworld;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.SaveFile;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.mainmenu.InternalLevelMenu;
import com.sab.littleh.mainmenu.LoadingMenu;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.sab_format.SabValue;

import java.awt.*;

public class OverworldMenu extends MainMenu {
    private static final DynamicCamera camera = LittleH.program.dynamicCamera;
    private WorldMap worldMap;
    private LevelIndicator selectedLevel;
    private Level playedLevel;
    private MenuButton playButton;
    private boolean selectClick;
    public OverworldMenu() {
        worldMap = new WorldMap();
        camera.reset();
        camera.setPosition(worldMap.getFirstLevel().getWorldPosition());
        camera.setZoom(0.5f);
    }

    @Override
    public void start() {
        if (worldMap == null) {
            if (playedLevel.playerBackup.win) {
                long time = playedLevel.getTimeMillis();
                if (SaveFile.saveData.get("clear_times").getValue(selectedLevel.getId()) == null ||
                        time < SaveFile.saveData.get("clear_times").getValue(selectedLevel.getId()).asLong()) {
                    SaveFile.saveData.get("clear_times").insertValue(selectedLevel.getId(), String.valueOf(time));
                }
                SabValue unlocks = selectedLevel.getData().getValue("unlocks");
                if (unlocks != null) {
                    for (String string : unlocks.asStringArray()) {
                        SaveFile.saveData.get("unlocked_levels").insertValue(string, "true");
                    }
                }
                SaveFile.saveGame();
            }

            camera.reset();
            camera.setPosition(selectedLevel.getWorldPosition());
            camera.setZoom(0.5f);

            selectedLevel = null;
            worldMap = new WorldMap();
        }
    }

    @Override
    public void update() {
        if (ControlInput.localControls.isJustPressed("pause")) {
            LittleH.program.switchMenu(new OverworldPauseMenu(this));
            return;
        }
        worldMap.update();
        if (MouseUtil.isLeftMouseDown()) {
            if (MouseUtil.getMouseDelta().len2() > 0) {
                selectClick = false;
                camera.targetPosition.sub(MouseUtil.getMouseDelta().scl(camera.zoom));
            }
        }
        if (camera.targetPosition.x < -24) camera.targetPosition.x = -24;
        if (camera.targetPosition.y < -24) camera.targetPosition.y = -24;
        if (camera.targetPosition.x > 1048) camera.targetPosition.x = 1048;
        if (camera.targetPosition.y > 784) camera.targetPosition.y = 784;
        if (playButton != null)
            playButton.update();
        camera.updateCamera(4);
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();
        worldMap.render(g);
        if (selectedLevel != null) {
            Vector2 position = selectedLevel.getWorldPosition();
            g.drawPatch(Patch.get("menu"), position.x - 128, position.y + 32, 256, 128, 2);
            g.drawString(selectedLevel.getLevelData().getRawValue("name"), LittleH.font, position.x, position.y + 146, LittleH.defaultFontScale * 0.5f, 0);
            g.drawString("By: " + selectedLevel.getLevelData().getRawValue("author"), LittleH.font, position.x, position.y + 128, LittleH.defaultFontScale * 0.25f, 0);
            g.drawString("Best time: " + (selectedLevel.getClearTime() == null ? "Not Cleared!" : LittleH.formatTime(selectedLevel.getClearTime().asLong())), LittleH.font, position.x, position.y + 110, LittleH.defaultFontScale * 0.35f, 0);
            g.drawString("Big H time: " + LittleH.formatTime(selectedLevel.getPTime().asLong()), LittleH.font, position.x, position.y + 96, LittleH.defaultFontScale * 0.35f, 0);
            g.drawString("(Hold shift and click play to remove dialogue)", LittleH.font, position.x, position.y + 70, LittleH.defaultFontScale * 0.25f, 0);
            if (selectedLevel.isPRank())
                g.draw(Images.getImage("campaign/overworld/p_rank.png"), position.x - 120, position.y + 40, 16, 16);
            playButton.render(g, 2, 0.4f);
        }
        LittleH.program.useStaticCamera();
    }

    @Override
    public void mouseDown(int button) {
        if (playButton != null)
            playButton.mouseDown();
        selectClick = true;
    }

    @Override
    public void mouseUp(int button) {
        if (selectClick) {
            if (playButton != null)
                playButton.mouseClicked();
            if (selectedLevel == null || selectedLevel != null && !new Rectangle(selectedLevel.getWorldPosition().x - 128, selectedLevel.getWorldPosition().y + 32,
                    256, 128).contains(MouseUtil.getDynamicMousePosition())) {
                selectedLevel = worldMap.getOverlappedLevel(MouseUtil.getDynamicMousePosition());
                if (selectedLevel != null && !selectedLevel.isUnlocked()) {
                    selectedLevel = null;
                }

                if (selectedLevel != null) {
                    playButton = new MenuButton("button", "Play",
                            new Rectangle(selectedLevel.getWorldPosition().x - 32, selectedLevel.getWorldPosition().y + 38, 64, 24), () -> {
                        LoadingUtil.startLoading(() -> {
                            worldMap = null;
                            InternalLevelMenu levelMenu = new InternalLevelMenu(this, selectedLevel.getLevel(), ControlInput.localControls.isPressed("shift"));
                            playedLevel = levelMenu.level;
                            LittleH.pendingMenu = levelMenu;
                        });
                        LittleH.program.switchMenu(new LoadingMenu());
                    });
                    playButton.dynamic = true;
                }
            }
        }
    }

    @Override
    public void mouseScrolled(float amountY) {
        camera.targetPosition.sub(MouseUtil.getMousePosition().cpy().scl(amountY).scl(addZoom(amountY / 4f)));
    }

    // Returns by how much zoom changed
    public float addZoom(float zoom) {
        float zoomBefore = camera.targetZoom;
        camera.targetZoom += zoom;
        camera.targetZoom = Math.max(Math.min(camera.targetZoom, 1f), 0.25f);
        return Math.abs(zoomBefore - camera.targetZoom);
    }
}
