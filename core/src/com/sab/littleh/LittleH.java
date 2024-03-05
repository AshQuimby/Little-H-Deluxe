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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.campaign.SaveFile;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.level.editor.LevelEditor;
import com.sab.littleh.game.level.editor.LevelEditorScreen;
import com.sab.littleh.screen.Screen;
import com.sab.littleh.screen.*;
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
    public static Screen pendingScreen;
    private static List<Controller> controllers = new ArrayList<>();
    private int tick;
    public static BitmapFont font;
    public static BitmapFont borderedFont;
    public static float defaultFontScale = 0.2f;
    public static final int resolutionX = 1024;
    public static final int resolutionY = 576;
    public static LittleH program;
    private Graphics g;
    private NestedFrameBuffer buffer;
    private NestedFrameBuffer[] tempBuffers;
    private int tempBufferLayer = -1;
    private NestedFrameBuffer bufferHoldover;
    public OrthographicCamera staticCamera;
    public DynamicCamera dynamicCamera;
    private Screen screen;
    private String hoverInfo;
    private Stack<ShaderProgram> shaderStack = new Stack<>();
    private boolean dontRender, hardPause;
    private boolean controllersNotSupported;
    public static File mapsFolder;
    private String savedMapsPath;
    private String savedDataPath;
    @Override
    public void create() {
        program = this;

        if (System.getProperty("os.name").contains("Win")) {
            savedDataPath = String.format("%s\\TheLittleH", System.getenv("AppData"));
        } else if (System.getProperty("os.name").contains("Mac")) {
            savedDataPath = String.format("%s/Library/Application Support/TheLittleH", System.getProperty("user.home"));
        }

        File file = new File(savedDataPath);
        if (!file.exists()) {
            boolean success = file.mkdirs();
            if (!success) {
                throw new RuntimeException("Program failed to create necessary files");
            }
        }
        savedMapsPath = String.format("%s/maps", savedDataPath);
        mapsFolder = new File(savedMapsPath);
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
        Shaders.load();
        Settings.loadSettings();
        Controls.load();
        SaveFile.load();

        g = new Graphics();
        staticCamera = new OrthographicCamera(resolutionX, resolutionY);
        dynamicCamera = new DynamicCamera(resolutionX, resolutionY);
        SoundEngine.load();
        Cursors.loadCursors();
        try {
            Controllers.addListener(program);
        } catch (Exception e) {
            controllersNotSupported = true;
        }
        Screen.program = this;
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
        Fonts.loadFont("minecraft.ttf", 128);
        Fonts.loadFont("minecraft.ttf", 128, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("shitfont23.ttf", 240);
        Fonts.loadFont("shitfont23.ttf", 240, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("arial.ttf", 100);
        Fonts.loadFont("arial.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        Fonts.loadFont("comic_snas.ttf", 100);
        Fonts.loadFont("comic_snas.ttf", 100, new Color(0.5f, 0.5f, 0.5f, 1f), 10);
        updateFont();
        switchScreen(new TitleScreen());
        Gdx.input.setInputProcessor(this);
        Cursors.switchCursor("cursor");
        dontRender = false;
        SoundEngine.playMusic("menu/menu_theme.ogg");
        SoundEngine.update();
        Images.cacheHColor();
//        VnDialogue.load();
        buffer = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        bufferHoldover = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        tempBuffers = new NestedFrameBuffer[4];
        tempBuffers[0] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        tempBuffers[1] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        tempBuffers[2] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        tempBuffers[3] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, (int) staticCamera.viewportWidth, (int) staticCamera.viewportHeight, false);
        recheckShaderStack();
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

    public static List<Controller> getControllers() {
        return controllers;
    }

    public static File getFileResource(String resource) {
        return new File(String.format("%s/resources/%s", program.savedDataPath, resource));
    }

    public static String formatTime(long time) {
        return String.format("%s:%s:%s", getTimeWithZeros(time / 60000, 2),
                getTimeWithZeros(time / 1000 % 60, 2),
                getTimeWithZeros(time % 1000, 3));
    }

    public static String getTimeWithZeros(long time, int minChars) {
        String str = String.valueOf(time);

        while (str.length() < minChars) {
            str = 0 + str;
        }

        return str;
    }

    public void recheckShaderStack() {
        shaderStack.clear();
        if (Settings.localSettings.useShaders.value) {
            if (Settings.localSettings.retroMode.value) {
                shaderStack.add(Shaders.crushShader);
                shaderStack.add(Shaders.paletteShader);
            }
        }
    }

    public void update() {
        SoundEngine.update();
        Images.cacheHColor();
        if (tick == 0) {
            resetWindow();
        }
        screen.update();

        staticCamera.viewportWidth = getWidth();
        staticCamera.viewportHeight = getHeight();
        staticCamera.update();
        dynamicCamera.viewportWidth = getWidth();
        dynamicCamera.viewportHeight = getHeight();
        dynamicCamera.updateCamera();
        if (pendingScreen != null) {
            switchScreen(pendingScreen);
            pendingScreen = null;
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

    public static InputStream getScript(String path) {
        return LittleH.class.getResourceAsStream("/scripts/" + path);
    }

    public void switchScreen(Screen newScreen) {
        if (newScreen == null) return;
        if (screen != null) screen.close();
        screen = newScreen;
        screen.start();
        dontRender = true;
    }

    public static int getTick() {
        return program.tick;
    }

    @Override
    public void dispose() {
        g.dispose();
        buffer.dispose();
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
            String imagePath = (Images.inArchive ? "menushots/" : "../screenshots/") + Calendar.getInstance().getTime().toString().replace(":", "-").replace(" ", "-") + ".png";
            PixmapIO.writePNG(Gdx.files.local(imagePath), pixmap, Deflater.DEFAULT_COMPRESSION, true);
            pixmap.dispose();
        }
        ControlInput.localControls.press(keycode);
        screen.keyDown(keycode);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        ControlInput.localControls.release(keycode);
        screen.keyUp(keycode);
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        screen.keyTyped(character);
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screen.mouseDown(button);
        MouseUtil.leftMouseDown();
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screen.mouseUp(button);
        return true;
    }

    @Override
    public void resize(int width, int height) {
        try {
            buffer = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            bufferHoldover = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            tempBuffers[0] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            tempBuffers[1] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            tempBuffers[2] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
            tempBuffers[3] = new NestedFrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        screen.mouseScrolled(amountY);
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
        controllers.add(controller);
    }

    @Override
    public void disconnected(Controller controller) {
        controllers.remove(controller);
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (!controllers.contains(controller))
            connected(controller);
        if (buttonCode < 2) {
            ControlInput.localControls.pressControl(Controls.JUMP);
            ControlInput.localControls.pressControl(Controls.get("select"));
        } else if (buttonCode < 4) {
            ControlInput.localControls.pressControl(Controls.DOWN);
        } else if (buttonCode == 11) {
            ControlInput.localControls.pressControl(Controls.UP);
            ControlInput.localControls.pressControl(Controls.JUMP);
        } else if (buttonCode == 12) {
            ControlInput.localControls.pressControl(Controls.DOWN);
        } else if (buttonCode == 13) {
            ControlInput.localControls.pressControl(Controls.LEFT);
        } else if (buttonCode == 14) {
            ControlInput.localControls.pressControl(Controls.RIGHT);
        } else if (buttonCode == 6) {
            ControlInput.localControls.pressControl(Controls.get("return"));
        } else if (buttonCode == 4) {
            ControlInput.localControls.pressControl(Controls.get("select"));
        }
        keyDown(-1);
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (!controllers.contains(controller))
            connected(controller);
        if (buttonCode < 2) {
            ControlInput.localControls.releaseControl(Controls.JUMP);
            ControlInput.localControls.releaseControl(Controls.get("select"));
        } else if (buttonCode < 4) {
            ControlInput.localControls.releaseControl(Controls.DOWN);
        } else if (buttonCode == 11) {
            ControlInput.localControls.releaseControl(Controls.UP);
            ControlInput.localControls.releaseControl(Controls.JUMP);
        } else if (buttonCode == 12) {
            ControlInput.localControls.releaseControl(Controls.DOWN);
        } else if (buttonCode == 13) {
            ControlInput.localControls.releaseControl(Controls.LEFT);
        } else if (buttonCode == 14) {
            ControlInput.localControls.releaseControl(Controls.RIGHT);
        } else if (buttonCode == 6) {
            ControlInput.localControls.releaseControl(Controls.get("return"));
        } else if (buttonCode == 4) {
            ControlInput.localControls.releaseControl(Controls.get("select"));
        }
        keyUp(-1);
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (!controllers.contains(controller))
            connected(controller);
        if (axisCode == 0) {
            if (value > 0.5f) {
                ControlInput.localControls.pressControl(Controls.RIGHT);
            } else if (value < -0.5f) {
                ControlInput.localControls.pressControl(Controls.LEFT);
            } else {
                ControlInput.localControls.releaseControl(Controls.RIGHT);
                ControlInput.localControls.releaseControl(Controls.LEFT);
            }
        } else if (axisCode == 1) {
            if (value < -0.5f) {
                ControlInput.localControls.pressControl(Controls.UP);
            } else if (value > 0.5f) {
                ControlInput.localControls.pressControl(Controls.DOWN);
            } else {
                ControlInput.localControls.releaseControl(Controls.UP);
                ControlInput.localControls.releaseControl(Controls.DOWN);
            }
        } else if (axisCode == 3) {
            if (value < -0.5f) {
                ControlInput.localControls.pressControl(Controls.JUMP);
            } else if (value > 0.5f) {
                ControlInput.localControls.pressControl(Controls.DOWN);
            } else {
                ControlInput.localControls.releaseControl(Controls.JUMP);
                ControlInput.localControls.releaseControl(Controls.DOWN);
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

    public Screen getScreen() {
        return screen;
    }

    public boolean attemptClose() {
        System.out.println("hello");
        if (screen instanceof LevelEditorScreen) {
            System.out.println("im here");
            LevelEditorScreen screen = (LevelEditorScreen) this.screen;
            if (!LevelEditor.saved) {
                System.out.println("im living in the wall");
                screen.confirmProgramExit();
                return false;
            }
        }
        return true;
    }

    @Override
    public void render() {
        if (hardPause)
            return;
        update();

        if (dontRender) {
            dontRender = false;
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1);

        g.begin();
        buffer.begin();
        g.setBlendFunction(-1, -1);
        Gdx.gl20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        useStaticCamera();

        screen.render(g);

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

        g.flush();
        buffer.end();
        g.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Run through the shader stack w/o blending for post-processing

//        Gdx.gl.glDisable(GL20.GL_BLEND);

        shaderStack.forEach(shaderProgram -> {
            TextureRegion tex = new TextureRegion(buffer.getColorBufferTexture());
            NestedFrameBuffer temp = buffer;
            buffer = bufferHoldover;
            bufferHoldover = temp;
            buffer.begin();
            shaderProgram.bind();
            g.setShader(shaderProgram);
            g.draw(tex, Screen.relZeroX(), -Screen.relZeroY(), staticCamera.viewportWidth, -staticCamera.viewportHeight);
            g.flush();
            buffer.end();
        });

        g.resetShader();

        g.flush();
        g.draw(buffer.getColorBufferTexture(), Screen.relZeroX(), -Screen.relZeroY(), staticCamera.viewportWidth, -staticCamera.viewportHeight);

        g.end();

//        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        hoverInfo = null;
        MouseUtil.update();
        ControlInput.localControls.update();
        tick++;
    }

    public void beginTempBuffer() {
        g.flush();
        tempBufferLayer++;
        tempBuffers[tempBufferLayer].begin();
        ScreenUtils.clear(1, 1, 1, 0);
    }

    public void endTempBuffer() {
        g.flush();
        tempBuffers[tempBufferLayer].end();
        tempBufferLayer--;
    }

    public void drawTempBuffer() {
        g.draw(tempBuffers[tempBufferLayer + 1].getColorBufferTexture(), Screen.relZeroX(), -Screen.relZeroY(), staticCamera.viewportWidth, -staticCamera.viewportHeight);
    }

    public NestedFrameBuffer getFrameBuffer() {
        return buffer;
    }
}
