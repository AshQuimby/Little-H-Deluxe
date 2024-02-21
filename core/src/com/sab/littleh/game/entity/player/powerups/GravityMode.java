package com.sab.littleh.game.entity.player.powerups;

import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.*;

public class GravityMode extends Powerup {
    public GravityMode(Player player) {
        super(player);
    }

    @Override
    public void init(Player player) {
        super.init(player);
        player.image = "player/v_h";
    }

    @Override
    public void update(Level game) {
        player.canCrouch = false;
        super.update(game);
    }

    @Override
    public void jump(Level game) {
        if (player.leftGroundFor < 6)
            if (player.controller.isJustPressed(Controls.JUMP) || player.controller.isJustPressed(Controls.UP))
                flipGravity();
    }

    @Override
    public void onCollision(boolean horizontal, boolean vertical) {
        if (vertical) {
            player.touchingGround = (player.flippedGravity ? 1 : -1) == Math.signum(player.velocityY);
        }
        super.onCollision(horizontal, vertical);
    }

    public void flipGravity() {
        SoundEngine.playSound("gravity_swap.ogg");
        player.flippedGravity = !player.flippedGravity;
    }

    @Override
    public void updateVelocity() {
        if (player.touchingWater) {
            player.velocityX *= 0.94f;
            player.velocityY *= 0.94f;
            player.velocityY += 0.3f * (player.flippedGravity ? 1 : -1);
            return;
        }
        if (player.touchingGround) {
            player.maxGroundSpeed = Math.abs(player.velocityX);
        }

        if (!(player.slippery && player.crouched)) {
            if (player.touchingGround || player.maxGroundSpeed < Math.abs(player.velocityX)) {
                player.velocityX *= 0.92f;
            } else {
                player.velocityX *= 0.95f;
            }
        } else {
            player.velocityX *= 0.98f;
        }
        player.velocityY *= 0.98f;
        if (!noGravity)
            player.velocityY += 1f * (player.flippedGravity ? 1 : -1);

        if (player.touchingGround && player.controller.isPressed(Controls.RIGHT) == player.controller.isPressed(Controls.LEFT)) {
            if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
        }
    }
}
