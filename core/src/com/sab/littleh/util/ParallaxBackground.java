package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabParsingException;
import com.sab.littleh.util.sab_format.SabReader;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ParallaxBackground {
    public static final String[] recognizedLayers = new String[] {
            "full",
            "back",
            "outmost",
            "far",
            "middle",
            "near",
            "front"
    };

    public static final String[] drawOrder = new String[] {
            "full",
            "back",
            "outmost",
            "far",
            "middle",
            "near",
            "front",
    };

    public static final float[] parallaxScalar = new float[] {
            0f,
            0f,
            32f,
            24f,
            16f,
            12f,
            8f
    };
    public float parallaxMultiplier = 1f;
    public float ambientSpeedMultiplier = 1f;
    protected int width;
    protected int height;
    private Map<String, Texture> layers;

    public ParallaxBackground(String background, InputStream metaFile) {
        SabData data;
        try {
            data = SabReader.read(metaFile);
        } catch (SabParsingException e) {
            throw new RuntimeException(e);
        }
        boolean firstLoaded = false;
        layers = new HashMap<>();
        for (String layer : data.getValues().keySet()) {
            Texture texture = Images.getImage("backgrounds/" + background + "/" + layer + ".png");
            if (layer.equals("back") || firstLoaded) {
                width = texture.getWidth();
                height = texture.getHeight();
            }
            layers.put(layer, texture);
        }
    }

    public ParallaxBackground(String backgroundName) {
        this(backgroundName, ParallaxBackground.class.getResourceAsStream("/images/backgrounds/" + backgroundName + "/meta.sab"));
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimension getDimensions() {
        return new Dimension(width, height);
    }

    public void render(Graphics g) {
        LittleH.program.useStaticCamera();
        DynamicCamera camera = LittleH.program.dynamicCamera;
        int screenWidth = LittleH.program.getWidth();
        int screenHeight = LittleH.program.getHeight();
        float backgroundScalar = Math.max((float) screenWidth / width, (float) screenHeight / height);
        float cameraX = camera.getPosition().x;
        float cameraY = camera.getPosition().y;
        Rectangle drawTo;
        for (int i = 0; i < drawOrder.length; i++) {
            String layer = drawOrder[i];
            for (String backgroundLayer : layers.keySet()) {
                int patchWidth = (int) Math.ceil(width * backgroundScalar);
                int patchHeight = (int) Math.ceil(height * backgroundScalar);
                Texture texture = layers.get(backgroundLayer);
                patchWidth *= (float) texture.getWidth() / width;
                patchHeight *= (float) texture.getHeight() / height;
                if (backgroundLayer.contains(layer)) {
                    boolean isStatic = backgroundLayer.contains("static") || backgroundLayer.equals("back") || backgroundLayer.equals("full");
                    if (isStatic) {
                        g.draw(texture, -screenWidth / 2, -screenHeight / 2, screenWidth, screenHeight);
                    } else {
                        for (int j = -1; j <= 1; j++) {
                            float drawX = -cameraX;

                            if (backgroundLayer.contains("ambient")) {
                                if (backgroundLayer.contains("reverse")) {
                                    drawX -= LittleH.program.getTick() * 2 * ambientSpeedMultiplier;
                                } else {
                                    drawX += LittleH.program.getTick() * 2 * ambientSpeedMultiplier;
                                }
                            }

                            drawTo = new Rectangle(drawX, cameraY, patchWidth, patchHeight);
                            drawTo.x /= parallaxScalar[i] * parallaxMultiplier;
                            drawTo.y -= camera.viewportHeight / 2;
                            drawTo.y /= parallaxScalar[i] * parallaxMultiplier;
                            drawTo.y = Math.min(-screenHeight / 2, -screenHeight / 2 - drawTo.y);
                            drawTo.x = (drawTo.x - patchWidth) % patchWidth;
                            drawTo.x -= patchWidth / 2;
                            drawTo.x += patchWidth * j;

                            if (backgroundLayer.contains("super")) {
                                drawTo.y += patchHeight;
                                drawTo.y = Math.max(drawTo.y, -patchHeight / 2);
                            }

                            g.draw(texture, drawTo.x, drawTo.y, (int) drawTo.width, (int) drawTo.height);
                        }
                    }
                }
            }
        }
    }

    private static boolean isLayerRecognized(String layer) {
        boolean legal = false;
        for (String string : recognizedLayers) {
            if (layer.contains(string)) legal = true;
        }
        return legal;
    }
}
