package com.sab.littleh.game;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.screen.LevelOptionsScreen;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.*;

import java.awt.*;
import java.io.File;

public class GameScreen extends Screen {
    private static DynamicCamera camera = LittleH.program.dynamicCamera;
    public File file;
    public Level level;
    public boolean failedPlaying = false;

    public GameScreen(File file, Level level, boolean ignoreDialogue) {
        this.level = level;
        if (ignoreDialogue)
            level.ignoreDialogue();
        this.file = file;
        LittleH.setTitle(" | Playing level: " + level.mapData.getValue("name"));
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
        Cursors.switchCursor("none");
    }

    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            if (level.escapePressed())
                program.switchScreen(new GamePauseScreen(this));
        } else if (ControlInput.localControls.isJustPressed("select")) {
            level.enterPressed();
        } else if (ControlInput.localControls.isJustPressed("suicide")) {
            level.suicide();
        } else if (ControlInput.localControls.isJustPressed("quick_restart")) {
            level.player.trueKill();
        }
    }

    public void stop() {
        program.switchScreen(new LevelOptionsScreen(file, level.mapData));
        Cursors.switchCursor("cursor");
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    @Override
    public void update() {
        level.update();
        if (!level.inGame()) {
            stop();
        }
    }

    @Override
    public void mouseUp(int button) {
        if (!level.inGame()) {
            level.mouseUp();
        }
    }

    @Override
    public void close() {
    }

    public int getBackgroundIndex() {
        String background = level.mapData.getRawValue("background");
        for (int i = 0; i < Level.backgrounds.length; i++) {
            if (background.equals(Level.backgrounds[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        if (Settings.localSettings.useShaders.value) {
            LittleH.program.beginTempBuffer();

            level.renderBackground(g);

            LittleH.program.useDynamicCamera();

            level.render(g);

            LittleH.program.endTempBuffer();

            g.setPostTint(Level.themeTints[getBackgroundIndex()]);
            g.setShader(Shaders.tintShader);

            LittleH.program.useStaticCamera();

            LittleH.program.drawTempBuffer();

            g.resetShader();
        } else {
            level.renderBackground(g);

            LittleH.program.useDynamicCamera();

            level.render(g);

            LittleH.program.useStaticCamera();
        }

        if (level.inGame()) {
            level.renderHUD(g);
        }
    }
}
