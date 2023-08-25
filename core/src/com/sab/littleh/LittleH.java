package com.sab.littleh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.LevelErrorMenu;
import com.sab.littleh.mainmenu.LevelSelectMenu;
import com.sab.littleh.mainmenu.LoadingMenu;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sab.littleh.util.Graphics;
import com.sun.tools.javac.Main;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.zip.Deflater;

public class LittleH extends ApplicationAdapter implements InputProcessor, ControllerListener {
    public static final String TITLE = "The Little H Deluxe";
    public static MainMenu pendingMenu;
    private int tick;
    public static final File mapsFolder = new File("../maps/");
    public static BitmapFont font;
    public static BitmapFont borderedFont;
    public static float defaultFontScale = 0.2f;
    public static final int resolutionX = 1024;
    public static final int resolutionY = 576;
    public static LittleH program;
    private ShapeRenderer shapeRenderer;
    private Graphics g;
    public OrthographicCamera staticCamera;
    public DynamicCamera dynamicCamera;
    private MainMenu mainMenu;
    private String hoverInfo;
    private boolean dontRender;

    static {
        Settings.localSettings.load();
    }

    public static void updateFont() {
        font = Fonts.getFont(Settings.localSettings.font.asRawValue());
        font.setColor(Color.WHITE);
        borderedFont = Fonts.getFont(Settings.localSettings.font.asRawValue() + "_bordered");
        borderedFont.setColor(Color.WHITE);
        defaultFontScale = Settings.localSettings.font.getFontSize();
    }

    public static void setTitle(String title) {
        if (!System.getProperty("os.name").startsWith("Mac"))
            Gdx.graphics.setTitle(TITLE + title);
    }

    @Override
    public void create() {
        Level.waterShader = new ShaderProgram(Gdx.files.internal("shaders/water.vsh"), Gdx.files.internal("shaders/water.fsh"));
        if (!Level.waterShader.isCompiled()) {
            System.out.println(Level.waterShader.getLog());
            System.exit(1);
        }

        g = new Graphics();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        staticCamera = new OrthographicCamera(resolutionX, resolutionY);
        dynamicCamera = new DynamicCamera(resolutionX, resolutionY);
        Images.load();
        SoundEngine.load();
        Cursors.loadCursors();
        program = this;
        MainMenu.program = this;
        Patch.cacheButtonPatch("button", "ui/buttons/button");
        Patch.cacheButtonPatch("square_button", "ui/buttons/square_button");
        Patch.cacheButtonPatch("tile_button", "ui/buttons/tile_button");
        Patch.cachePatch("menu", new Patch("ui/menu/menu.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_globbed", new Patch("ui/menu/menu_globbed.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_light_globbed", new Patch("ui/menu/menu_light_globbed.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_flat", new Patch("ui/menu/menu_flat.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_flat_dark", new Patch("ui/menu/menu_flat_dark.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_rounded", new Patch("ui/menu/menu_rounded.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_indented", new Patch("ui/menu/menu_indented.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_light", new Patch("ui/buttons/square_button_hovered.png", 7, 7, 3, 3));
        Patch.cachePatch("menu_hollow", new Patch("ui/menu/menu_hollow.png", 7, 7, 3, 3));
        Fonts.loadFont("sab_font.ttf", 100);
        Fonts.loadFont("sab_font.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("minecraft.ttf", 100);
        Fonts.loadFont("minecraft.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("shitfont23.ttf", 240);
        Fonts.loadFont("shitfont23.ttf", 240, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("arial.ttf", 100);
        Fonts.loadFont("arial.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("comic_snas.ttf", 100);
        Fonts.loadFont("comic_snas.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        updateFont();
        switchMenu(new LevelSelectMenu());
        Gdx.input.setInputProcessor(this);
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        Cursors.switchCursor("cursor");
        dontRender = false;
        SoundEngine.playMusic("menu/menu_theme.ogg");
        SoundEngine.update();
        Controllers.addListener(program);
        Images.cacheHColor();
    }

    public void update() {
        SoundEngine.update();
        Images.cacheHColor();
        if (tick == 0) {
            resetWindow();
        }
        mainMenu.update();

        staticCamera.viewportWidth = getWidth();
        staticCamera.viewportHeight = getHeight();
        staticCamera.update();
        dynamicCamera.viewportWidth = getWidth();
        dynamicCamera.viewportHeight = getHeight();
        dynamicCamera.updateCamera();
        if (pendingMenu != null) {
            switchMenu(pendingMenu);
            pendingMenu = null;
        }
        MouseUtil.updateJustPressed();
    }

    public void setHoverInfo(String info) {
        hoverInfo = info;
    }

    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    public void useStaticCamera() {
        g.setProjectionMatrix(staticCamera.combined);
        shapeRenderer.setProjectionMatrix(staticCamera.combined);
    }

    public void useDynamicCamera() {
        g.setProjectionMatrix(dynamicCamera.combined);
        shapeRenderer.setProjectionMatrix(dynamicCamera.combined);
    }

    public static File[] findMaps() {
        List<File> files = new ArrayList<>();
        try {
            Files.walk(mapsFolder.toPath())
                    .sorted((path1, path2) -> -path1.compareTo(path2))
                    .forEach(path -> {
                        if (path.toString().endsWith(".map")) files.add(path.toFile());
                    });
        } catch (IOException e) {
        }
        return files.toArray(new File[files.size()]);
    }

    public static InputStream getInternalLevel(String path) {
        return LittleH.class.getResourceAsStream("/maps/" + path);
    }

    public void switchMenu(MainMenu newMenu) {
        if (newMenu == null) return;
        if (mainMenu != null) mainMenu.close();
        mainMenu = newMenu;
        mainMenu.start();
        dontRender = true;
    }

    @Override
    public void render() {
        update();

        if (dontRender) {
            dontRender = false;
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        g.begin();

        useStaticCamera();

        mainMenu.render(g);

        if (hoverInfo != null) {
            Rectangle hoverInfoRect = Fonts.getStringBounds(hoverInfo, font, MouseUtil.getMouseX() + 32, MouseUtil.getMouseY(), defaultFontScale * 0.75f, -1);
            if (hoverInfoRect.x + hoverInfoRect.width > getWidth() / 2) hoverInfoRect.x -= hoverInfoRect.width + 32;
            if (hoverInfoRect.y - hoverInfoRect.height * 3 < -getHeight() / 2)
                hoverInfoRect.y += hoverInfoRect.height * 5;
            hoverInfoRect.y -= 16 + 48;
            hoverInfoRect.width += 16;
            hoverInfoRect.height += 24;
            g.drawPatch(Patch.get("menu_globbed"), hoverInfoRect, 8);
            g.drawString(hoverInfo, font, hoverInfoRect.x + 8, hoverInfoRect.y + hoverInfoRect.height / 2 + 4, defaultFontScale * 0.75f, -1);
        }

        g.end();
        hoverInfo = null;
        shapeRenderer.begin();
        shapeRenderer.end();
        MouseUtil.update();
        ControlInputs.update();
        tick++;
    }

    public static int getTick() {
        return program.tick;
    }

    @Override
    public void dispose() {
        g.dispose();
        Level.waterShader.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F11) {
            if (Gdx.graphics.isFullscreen())
                Gdx.graphics.setWindowedMode(1600, 900);
            else
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else if (keycode == Input.Keys.F3) {
            Images.clearCache();
        } else if (keycode == Input.Keys.F12) {
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            ByteBuffer pixels = pixmap.getPixels();

            int size = Gdx.graphics.getBackBufferWidth() * Gdx.graphics.getBackBufferHeight() * 4;
            for (int i = 3; i < size; i += 4) {
                pixels.put(i, (byte) 255);
            }
            String imagePath = "../screenshots/" + Calendar.getInstance().getTime().toString().replace(":", "-").replace(" ", "-") + ".png";
            PixmapIO.writePNG(Gdx.files.local(imagePath), pixmap, Deflater.DEFAULT_COMPRESSION, true);
            pixmap.dispose();
        }
        ControlInputs.press(keycode);
        mainMenu.keyDown(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        ControlInputs.release(keycode);
        mainMenu.keyUp(keycode);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        mainMenu.keyTyped(character);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        mainMenu.mouseDown(button);
        MouseUtil.leftMouseDown();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        mainMenu.mouseUp(button);
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        mainMenu.mouseScrolled(amountY);
        return false;
    }

    // Not used
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (buttonCode < 2) {
            ControlInputs.pressControl(Control.JUMP);
        } else if (buttonCode < 4) {
            ControlInputs.pressControl(Control.DOWN);
        } else if (buttonCode == 11) {
            ControlInputs.pressControl(Control.UP);
            ControlInputs.pressControl(Control.JUMP);
        } else if (buttonCode == 12) {
            ControlInputs.pressControl(Control.DOWN);
        } else if (buttonCode == 13) {
            ControlInputs.pressControl(Control.LEFT);
        } else if (buttonCode == 14) {
            ControlInputs.pressControl(Control.RIGHT);
        }
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (buttonCode < 2) {
            ControlInputs.releaseControl(Control.JUMP);
        } else if (buttonCode < 4) {
            ControlInputs.releaseControl(Control.DOWN);
        } else if (buttonCode == 11) {
            ControlInputs.releaseControl(Control.UP);
            ControlInputs.releaseControl(Control.JUMP);
        } else if (buttonCode == 12) {
            ControlInputs.releaseControl(Control.DOWN);
        } else if (buttonCode == 13) {
            ControlInputs.releaseControl(Control.LEFT);
        } else if (buttonCode == 14) {
            ControlInputs.releaseControl(Control.RIGHT);
        }
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (axisCode == 0) {
            if (value > 0.5f) {
                ControlInputs.pressControl(Control.RIGHT);
            } else if (value < -0.5f) {
                ControlInputs.pressControl(Control.LEFT);
            } else {
                ControlInputs.releaseControl(Control.RIGHT);
                ControlInputs.releaseControl(Control.LEFT);
            }
        } else if (axisCode == 1) {
            if (value < -0.5f) {
                ControlInputs.pressControl(Control.UP);
            } else if (value > 0.5f) {
                ControlInputs.pressControl(Control.DOWN);
            } else {
                ControlInputs.releaseControl(Control.UP);
                ControlInputs.releaseControl(Control.DOWN);
            }
        } else if (axisCode == 3) {
            if (value < -0.5f) {
                ControlInputs.pressControl(Control.JUMP);
            } else if (value > 0.5f) {
                ControlInputs.pressControl(Control.DOWN);
            } else {
                ControlInputs.releaseControl(Control.JUMP);
                ControlInputs.releaseControl(Control.DOWN);
            }
        }
        return true;
    }

    public void resetWindow() {
        switch (Settings.localSettings.fullscreen.value) {
            case 0 -> {
                Gdx.graphics.setWindowedMode(1600, 900);
            }
            case 1 -> {
            }
            case 2 -> {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
    }

    public MainMenu getMenu() {
        return mainMenu;
    }
}
