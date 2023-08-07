package com.sab.littleh.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sab.littleh.LittleH;

public class MouseUtil {
    private static Vector2 previousMousePosition;
    private static boolean leftMouseDown;
    private static boolean rightMouseDown;
    public static Vector2 getDynamicMousePosition() {
        Vector3 mousePos = LittleH.program.dynamicCamera.unproject(new Vector3(getRawX(), getRawY(), 0));
        return new Vector2(mousePos.x, mousePos.y);
    }
    public static void update() {
        previousMousePosition = getMousePosition();
    }
    public static Vector2 getMouseDelta() {
        return getMousePosition().sub(previousMousePosition);
    }
    public static Vector2 getMousePosition() {
        Vector3 mousePos = LittleH.program.staticCamera.unproject(new Vector3(getRawX(), getRawY(), 0));
        return new Vector2(mousePos.x, mousePos.y);
    }

    public static float getMouseX() {
        return getMousePosition().x;
    }

    public static float getMouseY() {
        return getMousePosition().y;
    }

    public static int getRawX() {
        return Gdx.input.getX();
    }

    public static int getRawY() {
        return Gdx.input.getY();
    }

    public static boolean isLeftMouseDown() {
        return Gdx.input.isButtonPressed(0);
    }

    public static boolean isRightMouseDown() {
        return Gdx.input.isButtonPressed(1);
    }
}
