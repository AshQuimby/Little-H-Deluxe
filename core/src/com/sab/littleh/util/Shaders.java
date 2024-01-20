package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Shaders {
    public static ShaderProgram waterShader;
    public static ShaderProgram windyShader;
    public static ShaderProgram vineShader;
    public static ShaderProgram crtShader;
    public static ShaderProgram sepiaShader;
    public static ShaderProgram paletteShader;
    public static ShaderProgram crushShader;
    public static ShaderProgram tintShader;
    public static float[][] palette;

    public static void load() {
        waterShader = new ShaderProgram(Gdx.files.internal("shaders/water.vsh"), Gdx.files.internal("shaders/water.fsh"));
        if (!waterShader.isCompiled()) {
            System.out.println(waterShader.getLog());
            System.exit(1);
        }
        windyShader = new ShaderProgram(Gdx.files.internal("shaders/windy.vsh"), Gdx.files.internal("shaders/windy.fsh"));
        if (!windyShader.isCompiled()) {
            System.out.println(windyShader.getLog());
            System.exit(1);
        }
        vineShader = new ShaderProgram(Gdx.files.internal("shaders/vines.vsh"), Gdx.files.internal("shaders/vines.fsh"));
        if (!vineShader.isCompiled()) {
            System.out.println(vineShader.getLog());
            System.exit(1);
        }
        crtShader = new ShaderProgram(Gdx.files.internal("shaders/default.vsh"), Gdx.files.internal("shaders/crt.fsh"));
        if (!crtShader.isCompiled()) {
            System.out.println(crtShader.getLog());
            System.exit(1);
        }
        sepiaShader = new ShaderProgram(Gdx.files.internal("shaders/default.vsh"), Gdx.files.internal("shaders/sepia.fsh"));
        if (!sepiaShader.isCompiled()) {
            System.out.println(sepiaShader.getLog());
            System.exit(1);
        }
        paletteShader = new ShaderProgram(Gdx.files.internal("shaders/default.vsh"), Gdx.files.internal("shaders/palette.fsh"));
        if (!paletteShader.isCompiled()) {
            System.out.println(paletteShader.getLog());
            System.exit(1);
        }
        crushShader = new ShaderProgram(Gdx.files.internal("shaders/default.vsh"), Gdx.files.internal("shaders/crush.fsh"));
        if (!crushShader.isCompiled()) {
            System.out.println(crushShader.getLog());
            System.exit(1);
        }
        tintShader = new ShaderProgram(Gdx.files.internal("shaders/default.vsh"), Gdx.files.internal("shaders/tint.fsh"));
        if (!tintShader.isCompiled()) {
            System.out.println(tintShader.getLog());
            System.exit(1);
        }

        boolean useImage = false;

        String file = "";

        if (!useImage) {
            file = new Scanner(Objects.requireNonNull(Shaders.class.getResourceAsStream("/shaders/loss_palette.txt")))
                    .useDelimiter("\\A").next();
        } else {
            // Slow as hell but doesn't matter
            Texture tex = new Texture("shaders/palette_ref.png");
            tex.getTextureData().prepare();
            Pixmap pixmap = tex.getTextureData().consumePixmap();
            for (int i = 0; i < pixmap.getWidth(); i++) {
                for (int j = 0; j < pixmap.getHeight(); j++) {
                    String hex = Integer.toHexString(pixmap.getPixel(i, j));
                    if (hex.length() == 8 && !file.contains(hex)) {
                        file += "#" + hex + "\n";
                    }
                }
            }
            try {
                FileWriter writer = new FileWriter("palette.txt");
                writer.write(file);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String[] colors = file.split("\n");
        palette = new float[colors.length][3];
        for (int i = 0; i < palette.length; i++) {
            if (colors[i].isEmpty())
                break;
            float[] hsv = Color.valueOf(colors[i]).toHsv(new float[3]);
            hsv[0] = hsv[0] / 360f;
            palette[i] = hsv;
        }
    }

    public static void setScreenTint(Color color) {
        tintShader.bind();
        tintShader.setUniform3fv("u_tint", new float[] { color.r, color.g, color.b }, 0, 3);
        tintShader.setUniformf("u_strength", color.a);
    }

    public static void setupPaletteShader() {
        paletteShader.bind();
        int i = 0;
        for (float[] f : palette) {
            paletteShader.setUniform3fv("u_allowedPalette[" + i + "]", f, 0, 3);
            i++;
        }
        paletteShader.setUniformi("u_paletteSize", palette.length);
    }

    public static void dispose() {
        waterShader.dispose();
        windyShader.dispose();
        vineShader.dispose();
        crtShader.dispose();
        sepiaShader.dispose();
        paletteShader.dispose();
        crushShader.dispose();
        tintShader.dispose();
    }
}
