package com.sab.littleh.util;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.*;
import com.sab.littleh.LittleH;
import com.sab.littleh.settings.Settings;

import java.util.List;

public class DynamicCamera extends OrthographicCamera {
    public Vector2 targetPosition;
    public float targetZoom;
    public float targetRotation;
    private float internalZoom;
    private float rotation;
    private int screenShake;

    public DynamicCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        targetPosition = new Vector2(0, 0);
        targetZoom = 1;
        internalZoom = zoom;
        targetRotation = 0;
    }

    public void moveToTarget(float easing) {
        position.x += (targetPosition.x - position.x) / easing;
        position.y += (targetPosition.y - position.y) / easing;
    }

    public void addScreenShake(int intensity) {
        if (Settings.localSettings.screenShake.value) {
            screenShake += intensity;
            screenShake = Math.min(screenShake, 32);
            if (Settings.localSettings.controllerVibration.value)
                LittleH.getControllers().forEach(controller -> controller.startVibration(screenShake * 20, Math.min(screenShake / 20f, 1)));
        }
    }

    public void stopScreenShake() {
        screenShake = 0;
    }

    public void updateZoom(float easing) {
        internalZoom += (targetZoom - internalZoom) / easing;
    }
    public void updateRotation(float easing) {
        rotation = getCameraRotation();
        rotate((targetRotation - rotation) / easing * 2);
    }

    public float getCameraRotation() {
        return (float) Math.atan2(up.x, up.y) * MathUtils.radiansToDegrees;
    }

    public void updateCamera() {
        updateCamera(12);
    }

    public void updateCamera(float easing) {
        super.update();
        moveToTarget(easing);
        updateZoom(easing);
        updateRotation(easing);
        if (Float.isNaN(internalZoom)) {
            internalZoom = 1;
        }
        zoom = internalZoom;
        if (screenShake > 0) {
            float mag = MathUtils.random(-screenShake * screenShake, screenShake * screenShake) / 10f;
            position.x += mag;
            mag = MathUtils.random(-screenShake * screenShake, screenShake * screenShake) / 10f;
            position.y += mag;
            mag = MathUtils.random(-screenShake, screenShake) / 10f;
            rotation += mag;
            rotate(mag);
        }
        if (screenShake > 0)
            screenShake--;
    }

    public void reset() {
        position.scl(0);
        targetPosition = new Vector2();
        internalZoom = 1;
        targetZoom = 1;
        rotation = 0;
        targetRotation = 0;
        rotate(0);
    }

    public void setPosition(Vector2 vector2) {
        position.x = vector2.x;
        position.y = vector2.y;
        targetPosition.x = vector2.x;
        targetPosition.y = vector2.y;
    }

    public void setZoom(float zoom) {
        this.internalZoom = zoom;
        this.targetZoom = zoom;
    }

    public Vector2 getPosition() {
        return new Vector2(position.x, position.y);
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public Rectangle getScaledViewport() {
        float width = viewportWidth * zoom;
        float height = viewportHeight * zoom;
        return new Rectangle(position.x - width / 2, position.y - height / 2, width, height);
    }

    public Rectangle getViewport() {
        return new Rectangle(position.x - viewportWidth / 2, position.y - viewportHeight / 2, viewportWidth, viewportHeight);
    }
}
