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
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.campaign.visual_novel.dialogue.VnDialogue;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.level.LevelEditor;
import com.sab.littleh.mainmenu.*;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sab.littleh.util.Graphics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.zip.Deflater;

public class LittleH extends ApplicationAdapter implements InputProcessor, ControllerListener {
    public static final String TITLE = "The Little H Deluxe";
    public static final String VERSION = "0.1.5";
    public static MainMenu pendingMenu;
    private int tick;
    public static final File mapsFolder = new File(Images.inArchive ? "maps/" : "../maps/");
    public static BitmapFont font;
    public static BitmapFont borderedFont;
    public static float defaultFontScale = 0.2f;
    public static final int resolutionX = 1024;
    public static final int resolutionY = 576;
    public static LittleH program;
    static {
        Settings.localSettings.load();
    }
    private Graphics g;
    private FrameBuffer buffer;
    public OrthographicCamera staticCamera;
    public DynamicCamera dynamicCamera;
    private MainMenu mainMenu;
    private String hoverInfo;
    private boolean dontRender, hardPause;
    private boolean controllersNotSupported;

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
        Shaders.load();
        Controls.load();

        g = new Graphics();
        staticCamera = new OrthographicCamera(resolutionX, resolutionY);
        dynamicCamera = new DynamicCamera(resolutionX, resolutionY);
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
        switchMenu(new TitleMenu());
        Gdx.input.setInputProcessor(this);
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        Cursors.switchCursor("cursor");
        dontRender = false;
        SoundEngine.playMusic("menu/menu_theme.ogg");
        SoundEngine.update();
        try {
            Controllers.addListener(program);
        } catch (Exception e) {
            controllersNotSupported = true;
        }
        Images.cacheHColor();
        VnDialogue.load();
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
    }

    public void useDynamicCamera() {
        g.setProjectionMatrix(dynamicCamera.combined);
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
//        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
//        buffer.begin();
//        buffer.bind();
        if (hardPause)
            return;
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

//        g.end();
//        buffer.end();
//        g.draw(buffer.getColorBufferTexture(), MainMenu.relZeroX(), -MainMenu.relZeroY(), staticCamera.viewportWidth, -staticCamera.viewportHeight);
        g.end();

        hoverInfo = null;
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
        Shaders.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F1) {
            hardPause = !hardPause;
        } else if (keycode == Input.Keys.F11) {
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
            String imagePath = (Images.inArchive ? "screenshots/" : "../screenshots/") + Calendar.getInstance().getTime().toString().replace(":", "-").replace(" ", "-") + ".png";
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
            ControlInputs.pressControl(Controls.JUMP);
        } else if (buttonCode < 4) {
            ControlInputs.pressControl(Controls.DOWN);
        } else if (buttonCode == 11) {
            ControlInputs.pressControl(Controls.UP);
            ControlInputs.pressControl(Controls.JUMP);
        } else if (buttonCode == 12) {
            ControlInputs.pressControl(Controls.DOWN);
        } else if (buttonCode == 13) {
            ControlInputs.pressControl(Controls.LEFT);
        } else if (buttonCode == 14) {
            ControlInputs.pressControl(Controls.RIGHT);
        }
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (buttonCode < 2) {
            ControlInputs.releaseControl(Controls.JUMP);
        } else if (buttonCode < 4) {
            ControlInputs.releaseControl(Controls.DOWN);
        } else if (buttonCode == 11) {
            ControlInputs.releaseControl(Controls.UP);
            ControlInputs.releaseControl(Controls.JUMP);
        } else if (buttonCode == 12) {
            ControlInputs.releaseControl(Controls.DOWN);
        } else if (buttonCode == 13) {
            ControlInputs.releaseControl(Controls.LEFT);
        } else if (buttonCode == 14) {
            ControlInputs.releaseControl(Controls.RIGHT);
        }
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (axisCode == 0) {
            if (value > 0.5f) {
                ControlInputs.pressControl(Controls.RIGHT);
            } else if (value < -0.5f) {
                ControlInputs.pressControl(Controls.LEFT);
            } else {
                ControlInputs.releaseControl(Controls.RIGHT);
                ControlInputs.releaseControl(Controls.LEFT);
            }
        } else if (axisCode == 1) {
            if (value < -0.5f) {
                ControlInputs.pressControl(Controls.UP);
            } else if (value > 0.5f) {
                ControlInputs.pressControl(Controls.DOWN);
            } else {
                ControlInputs.releaseControl(Controls.UP);
                ControlInputs.releaseControl(Controls.DOWN);
            }
        } else if (axisCode == 3) {
            if (value < -0.5f) {
                ControlInputs.pressControl(Controls.JUMP);
            } else if (value > 0.5f) {
                ControlInputs.pressControl(Controls.DOWN);
            } else {
                ControlInputs.releaseControl(Controls.JUMP);
                ControlInputs.releaseControl(Controls.DOWN);
            }
        }
        return true;
    }

    public void resetWindow() {
        switch (Settings.localSettings.fullscreen.value) {
            case 0 :
                Gdx.graphics.setWindowedMode(1600, 900);
                break;
            case 1 :
                break;
            case 2 :
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        }
    }

    public MainMenu getMenu() {
        return mainMenu;
    }

    public boolean attemptClose() {
        System.out.println("hello");
        if (mainMenu instanceof LevelEditorMenu) {
            System.out.println("im here");
            LevelEditorMenu menu = (LevelEditorMenu) mainMenu;
            if (!LevelEditor.saved) {
                System.out.println("im living in the wall");
                menu.confirmProgramExit();
                return false;
            }
        }
        return true;
    }
}
