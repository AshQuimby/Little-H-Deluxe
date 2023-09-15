package com.sab.littleh.campaign.visual_novel.particle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Particle;

public class HeartParticle extends Particle {
    public HeartParticle(float x, float y) {
        super(x, y, MathUtils.random(-24, 24), MathUtils.random(2, 20), 0, 40,
                11, 10, 1, 0, 0, 0, 0, "campaign/visual_novel/particle/heart.png", 2000);
    }

    @Override
    public void update() {
        Vector2 center = new Vector2(x + width / 2f, y + height / 2f);
        if (width < 44) {
            width += 4;
        } else {
            width = 44 + (int) Math.abs(MathUtils.sin(life / 16f) * 16);
        }
        x = center.x - width / 2f;
        y = center.y - height / 2f;
        if (velocityY > 16) {
            velocityY *= 0.96f;
        } else {
            velocityY += 0.5f;
        }
        velocityX *= 0.9f;
        rotation = MathUtils.sin(life / 16f) * 30;
        super.update();
    }
}
