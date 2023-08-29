package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
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
      player.image = "player/stone_h";
   }

   @Override
   public void jump(Level game) {
      if (player.crushed) return;
      if (player.leftGroundFor < 6) {
         SoundEngine.playSound("jump.ogg");
         player.velocityY = 8;
         player.leftGroundFor = 8;
         player.jumpStrength++;
         game.addParticle(new Particle(player.x - 24, player.y, 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
      } else if (player.jumpReleased && player.leftWallFor < 8) {
         SoundEngine.playSound("double_jump.ogg");
         player.velocityY = 26;
         player.leftWallFor = 8;
         player.x += -2 * player.wallDirection;
         player.velocityX = -16 * player.wallDirection;
      } else if (player.jumpReleased && player.doubleJump && game.mapData.getValue("double_jumping").asBool()) {
         game.addParticle(new Particle(player.x, player.y, 0f, 0f, 48, 32, 6, 4, 1, 0f, 0f, 0, 2, "particles/double_jump.png", 9));
         SoundEngine.playSound("double_jump.ogg");
         if (player.velocityY < -16) player.coolRoll = (float) (Math.PI * 3);
         if (player.velocityY < 16) player.velocityY = 16;
         else player.velocityY += 7f;
         player.doubleJump = false;
      }
   }

   @Override
   public void move() {
      if (groundSlam == 0 || groundSlam >= GROUND_SLAM_DELAY) {
         if (ControlInputs.isPressed(Control.LEFT)) {
            if (!player.touchingWall) player.direction = -1;
            player.velocityX -= 1.2f * (player.touchingWater ? 0.9f : 1) * (groundSlam >= GROUND_SLAM_DELAY ? 0.5f : 1);
         }

         if (ControlInputs.isPressed(Control.RIGHT)) {
            if (!player.touchingWall) player.direction = 1;
            player.velocityX += 1.2f * (player.touchingWater ? 0.9f : 1) * (groundSlam >= GROUND_SLAM_DELAY ? 0.5f : 1);
         }
      }
   }

   public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
      if (groundSlam >= GROUND_SLAM_DELAY) {
         if (tile.hasTag("crumbling")) {
            player.keyCount--;
            for (int i = 0; i < 4; i++) {
               game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -8), (float) (Math.random() * -10), 64, 64, 8, 8, 1, 0.98f, 1.2f, i, 0, "particles/mud_rubble.png", 30));
            }
            SoundEngine.playSound("hit.ogg");
            game.inGameRemoveTile(tile);
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
         if (ControlInputs.isPressed(Control.JUMP) || ControlInputs.isPressed(Control.UP)) {
            player.velocityY = Math.abs(player.velocityY) * 0.9f;
            player.velocityY += 4f;
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
                  if (!ControlInputs.isPressed(Control.DOWN)) {
                     groundSlam = 0;
                  }
               }
               player.coolRoll = -MathUtils.PI * 4 + MathUtils.PI * 4 / (groundSlam / 2f) - MathUtils.PI / 2;
               player.velocityY = 0;
               player.velocityX *= 0.1f;
            } else {
               if (ControlInputs.isJustPressed(Control.JUMP) || ControlInputs.isJustPressed(Control.UP)) {
                  groundSlam = 0;
               } else {
                  player.velocityY -= 1;
               }
            }
            player.currentAnimation = slamAnimation;
         }
      }

      if (ControlInputs.isJustPressed(Control.DOWN)) {
         if (!player.touchingGround) {
            groundSlam = 1;
         }
      }
   }

   @Override
   public void updateVelocity() {
      if (!(player.slippery && player.crouched)) {
         player.velocityX *= 0.92f;
      } else {
         player.velocityX *= 0.98f;
      }
      player.velocityY *= 0.98f;
      if (player.touchingWater)
         player.velocityY *= 0.99f;
      if (!noGravity)
         player.velocityY -= 1.2f;
   }
}