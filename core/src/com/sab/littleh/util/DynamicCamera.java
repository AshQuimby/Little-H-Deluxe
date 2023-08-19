package com.sab.littleh.util;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class DynamicCamera extends OrthographicCamera {

    public Vector2 targetPosition;
    public float targetZoom;
    public float internalZoom;

    public DynamicCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        targetPosition = new Vector2(0, 0);
        targetZoom = 1;
        internalZoom = zoom;
    }

    public void moveToTarget(float easing) {
        position.x += (targetPosition.x - position.x) / easing;
        position.y += (targetPosition.y - position.y) / easing;
    }

    public void updateZoom(float easing) {
        internalZoom += (targetZoom - internalZoom) / easing;
    }

    public void updateCamera() {
        updateCamera(12);
    }

    public void updateCamera(float easing) {
        super.update();
        moveToTarget(easing);
        updateZoom(easing);
        zoom = internalZoom;
    }

    public void reset() {
        position.scl(0);
        targetPosition = new Vector2();
        internalZoom = 1;
        targetZoom = 1;
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

    public Rectangle getViewport() {
        return new Rectangle(position.x, position.y, viewportWidth, viewportHeight);
    }
}
