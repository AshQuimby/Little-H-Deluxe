package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders {
    public static ShaderProgram waterShader;

    public static void load() {
        waterShader = new ShaderProgram(Gdx.files.internal("shaders/water.vsh"), Gdx.files.internal("shaders/water.fsh"));
        if (!waterShader.isCompiled()) {
            System.out.println(waterShader.getLog());
            System.exit(1);
        }
    }

    public static void dispose() {
        waterShader.dispose();
    }
}
