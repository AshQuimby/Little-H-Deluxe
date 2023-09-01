package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.*;

public class GravityMode extends Powerup {
    public GravityMode(Player player) {
        super(player);
    }
    private boolean flippedGravity;

    @Override
    public void init(Player player) {
        super.init(player);
        player.image = "player/v_h";
        flippedGravity = false;
    }

    @Override
    public void update(Level game) {
        player.canCrouch = false;
        super.update(game);
        if (!player.touchingGround) {
            if (player.touchingWall) {
                if (!player.slippery) player.velocityY *= 0.85f;
                player.currentAnimation = Player.wallSlideAnimation;
                player.leftWallFor = 0;
                player.wallDirection = player.direction;
            } else {
                player.leftWallFor++;
            }
        }
        if (player.y > game.getHeight() * 64 + 256)
            player.kill();

        if (player.currentAnimation == Player.jumpAnimation || player.currentAnimation == Player.fallAnimation) {
            if (player.velocityY < 0) {
                player.currentAnimation = flippedGravity ? Player.jumpAnimation : Player.fallAnimation;
            } else {
                player.currentAnimation = flippedGravity ? Player.fallAnimation : Player.jumpAnimation;
            }
        }
    }

    @Override
    public void jump(Level game) {
        if (player.leftGroundFor < 6)
            if (ControlInputs.isJustPressed(Controls.JUMP) || ControlInputs.isJustPressed(Controls.UP))
                flipGravity();
    }

    @Override
    public void onCollision(boolean horizontal, boolean vertical) {
        if (vertical) {
            player.touchingGround = (flippedGravity ? 1 : -1) == Math.signum(player.velocityY);
        }
        super.onCollision(horizontal, vertical);
    }

    public void flipGravity() {
        SoundEngine.playSound("gravity_swap.ogg");
        flippedGravity = !flippedGravity;
    }

    @Override
    public void updateVelocity() {
        if (player.touchingWater) {
            player.velocityX *= 0.94f;
            player.velocityY *= 0.94f;
            player.velocityY += 0.3f * (flippedGravity ? 1 : -1);
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
            player.velocityY += 1f * (flippedGravity ? 1 : -1);
    }

    @Override
    public void drawPlayer(Graphics g, Level game) {
        g.setColor(Images.getHColor());

        g.drawImage(Images.getImage(player.image + "_color.png"), new Rectangle(player.x - 8, player.y - (flippedGravity ? 16 : 0), 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame + (flippedGravity ? 8 : 0), (player.direction == 1 ? 8 : -8), flippedGravity ? -8 : 8),
                -MathUtils.radiansToDegrees * player.rotation);
        g.resetColor();

        g.drawImage(Images.getImage(player.image + ".png"), new Rectangle(player.x - 8, player.y - (flippedGravity ? 16 : 0), 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame + (flippedGravity ? 8 : 0), (player.direction == 1 ? 8 : -8), flippedGravity ? -8 : 8),
                -MathUtils.radiansToDegrees * player.rotation);
    }
}
