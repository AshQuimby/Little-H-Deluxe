package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders {
    public static ShaderProgram waterShader;
    public static ShaderProgram windyShader;
    public static ShaderProgram vineShader;

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
    }

    public static void dispose() {
        waterShader.dispose();
        windyShader.dispose();
        vineShader.dispose();
    }
}
