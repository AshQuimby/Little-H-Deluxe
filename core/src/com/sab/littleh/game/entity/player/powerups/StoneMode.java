package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.*;

public class StoneMode extends Powerup {
   private static final int GROUND_SLAM_DELAY = 20;
   private static Animation slamAnimation = new Animation(1, 27);
   public int groundSlam;

   public StoneMode(Player player) {
      super(player);
   }

   public void init(Player player) {
      super.init(player);
      player.image = "player/stone_h";
   }

   @Override
   public void jump(Level game) {
      boolean grah = player.touchingWater;
      player.touchingWater = false;
      super.jump(game);
      player.touchingWater = grah;
   }

   @Override
   public void move() {
      if (groundSlam == 0 || groundSlam >= GROUND_SLAM_DELAY) {
         if (player.controller.isPressed(Controls.LEFT)) {
            if (!player.touchingWall) player.direction = -1;
            player.velocityX -= 1.2f * (player.touchingWater ? 0.9f : 1) * (groundSlam >= GROUND_SLAM_DELAY ? 0.5f : 1);
         }

         if (player.controller.isPressed(Controls.RIGHT)) {
            if (!player.touchingWall) player.direction = 1;
            player.velocityX += 1.2f * (player.touchingWater ? 0.9f : 1) * (groundSlam >= GROUND_SLAM_DELAY ? 0.5f : 1);
         }
      }
   }

   public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
      if (groundSlam >= GROUND_SLAM_DELAY) {
         if (tile.hasTag("crumbling")) {
            for (int i = 0; i < 4; i++) {
               game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -8), (float) (Math.random() * -10), 64, 64, 8, 8, 1, 0.98f, 1.2f, i, 0, "particles/mud_rubble.png", 30));
            }
            SoundEngine.playSound("hit.ogg");
            game.inGameRemoveTile(tile);
            player.doubleJump = true;
            return false;
         }
      }
      return super.onCollide(game, entityHitbox, tileHitbox, tile, yCollision);
   }

   @Override
   public void touchingEnemy(Enemy enemy) {
      if (groundSlam < GROUND_SLAM_DELAY) {
         player.kill();
      } else {
         if (player.controller.isPressed(Controls.JUMP) || player.controller.isPressed(Controls.UP)) {
            player.velocityY = Math.abs(player.velocityY) * 0.9f;
            player.velocityY += 4f * -player.getGravityMagnitude();
            LittleH.program.dynamicCamera.addScreenShake(6);
            SoundEngine.playSound("bounce.ogg");
         }
         enemy.kill();
      }
   }

   @Override
   public void update(Level game) {
      super.update(game);

      if (groundSlam > 0) {
         if (player.touchingGround) {
            groundSlam = 0;
            LittleH.program.dynamicCamera.addScreenShake(12);
            SoundEngine.playSound("death.ogg");
         } else {
            groundSlam++;
            if (groundSlam < GROUND_SLAM_DELAY) {
               if (groundSlam > GROUND_SLAM_DELAY / 2) {
                  if (!player.controller.isPressed(Controls.DOWN)) {
                     groundSlam = 0;
                  }
               }
               player.coolRoll = -MathUtils.PI * 4 + MathUtils.PI * 4 / (groundSlam / 2f) - MathUtils.PI / 2;
               player.velocityY = 0;
               player.velocityX *= 0.1f;
            } else {
               if (player.controller.isJustPressed(Controls.JUMP) || player.controller.isJustPressed(Controls.UP)) {
                  groundSlam = 0;
               } else {
                  player.velocityY += 1 * player.getGravityMagnitude();
               }
            }
            player.currentAnimation = slamAnimation;
         }
      }

      if (player.controller.isJustPressed(Controls.DOWN)) {
         if (!player.touchingGround) {
            SoundEngine.playSound("slam_start.ogg");
            groundSlam = 1;
         }
      }
   }

   @Override
   public void updateVelocity() {
      if (player.touchingGround) {
         player.maxGroundSpeed = Math.abs(player.velocityX);
      }
      if (!(player.slippery && player.crouched)) {
         player.velocityX *= 0.92f;
      } else {
         player.velocityX *= 0.98f;
      }
      player.velocityY *= 0.98f;
      if (player.touchingWater)
         player.velocityY *= 0.99f;
      if (!noGravity)
         player.velocityY -= 1.2f * -player.getGravityMagnitude();

      if (player.touchingGround && player.controller.isPressed(Controls.RIGHT) == player.controller.isPressed(Controls.LEFT)) {
         if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
      }
   }
}