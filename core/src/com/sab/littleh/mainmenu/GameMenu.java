package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.*;

import java.awt.*;
import java.io.File;

public class GameMenu extends MainMenu {
    private static DynamicCamera camera = LittleH.program.dynamicCamera;
    private File file;
    private Level level;
    public boolean failedPlaying = false;

    public GameMenu(File file, Level level) {
        this.file = file;
        this.level = level;
        Gdx.graphics.setTitle(LittleH.TITLE + " | Playing level: " + level.mapData.getValue("name"));
        Point startPos = level.getStartPos();
        if (startPos == null) {
            failedPlaying = true;
        } else {
            level.startGame(startPos);
        }
    }

    @Override
    public void start() {
        level.init();
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
        } else if (keycode == Input.Keys.ENTER) {
            level.enterPressed();
        }
    }

    @Override
    public void update() {
        level.update();
        if (!level.inGame()) {
            LittleH.program.switchMenu(new LevelOptionsMenu(file, level.mapData));
        }
    }

    @Override
    public void close() {
        SoundEngine.playMusic("menu_song.wav");
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        camera.updateCamera();

        level.renderBackground(g);

        LittleH.program.useDynamicCamera();

        level.render(g);

        LittleH.program.useStaticCamera();

        if (level.inGame()) {
            level.renderHUD(g);
            return;
        }
    }
}
