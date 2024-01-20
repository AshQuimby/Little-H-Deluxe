package com.sab.littleh.game.entity.player.powerups;

import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Animation;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.SoundEngine;

// BALLMODE
// (BALLS!!!!!)

public class BallMode extends Powerup {

   private static Animation rollAnimation = new Animation(1, 0);
   private float rotationSpeed;
   private float ballRotation;
   private boolean superSlam;
   private int bounceSoundCool;

   public BallMode(Player player) {
      super(player);
   }

   @Override
   public void init(Player player) {
      super.init(player);
      superSlam = false;
      player.image = "player/ball_h";
      bounceSoundCool = 0;
   }

   @Override
   public void jump(Level game) {
      if (player.touchingWater) {
         if (ControlInputs.isJustPressed(Controls.JUMP) || ControlInputs.isJustPressed(Controls.UP)) {
            player.velocityY = -12;
            SoundEngine.playSound("swim.ogg");
         }
         return;
      }
      super.jump(game);
   }

   @Override
   public void updateVelocity() {
      // Ball floats
      if (player.touchingWater) {
         player.velocityX *= 0.98f;
         player.velocityY *= 0.98f;
         player.velocityY += 0.85f;
         return;
      }
      if (!(player.slippery && player.crouched)) player.velocityX *= 0.975f;
      else player.velocityX *= 0.985f;
      player.velocityY *= 0.985f;
      player.velocityY += 1f * player.getGravityMagnitude();
   }
   
   @Override
   public void update(Level game) {
      if (bounceSoundCool > 0)
         bounceSoundCool--;
      if (player.win) {
         if (player.currentAnimation.getFrame() > 19) {
            ballRotation *= 0.5f;
         } else {
            ballRotation += rotationSpeed;
            rotationSpeed = player.velocityX / 32f;
         }
      }
      if (player.touchingGround) superSlam = false;
      if (ControlInputs.isJustPressed(Controls.DOWN) && game.mapData.getValue("crouching").asBool()) {
         if (!player.touchingGround && !superSlam) {
            superSlam = true;
         }
      }
      // Makes the little H SLAM down with BIG BALL ENERGY
      if (superSlam) {
         player.velocityY += 2 * player.getGravityMagnitude();
      }

      super.update(game);

      if (!player.dead && !player.win) player.currentAnimation = rollAnimation;
      if (player.win || player.dead) return;
      if (player.crouched) {
         ballRotation = 0;
         rotationSpeed = 0;
      } else {
         ballRotation += rotationSpeed;
         rotationSpeed = player.velocityX / 32f;
      }

      if (player.fallingFasterThan(196f)) {
         player.velocityY = 196f * player.getGravityMagnitude();
      } else if (player.risingFasterThan(196f)) {
         player.velocityY = 196f * -player.getGravityMagnitude();
      }
   }
   
   // Bounce like BALL in BALLMODE
   @Override
   public void onCollision(boolean horizontal, boolean vertical) {
      if (horizontal) {
         player.velocityX *= -1f;
         if (bounceSoundCool <= 0) {
            SoundEngine.playSound("ball_bounce.ogg");
            bounceSoundCool = 5;
         }
         // Minimum BOUNCING BALL SPEED
         player.velocityX = Math.max(15, Math.abs(player.velocityX)) * Math.signum(player.velocityX);
      }
      if (vertical) {
         player.velocityY *= -1f;
         // Minimum BOUNCING BALL SPEED
         player.velocityY = Math.max(15, Math.abs(player.velocityY)) * Math.signum(player.velocityY);
         if (bounceSoundCool <= 0) {
            SoundEngine.playSound("ball_bounce.ogg");
            bounceSoundCool = 5;
         }
      }
   }
   
   @Override
   public void touchingTile(Tile tile) {
      super.touchingTile(tile);
      // Makes bouncy tiles LAUNCH the BALL
      if (tile.hasTag("bounce")) {
         player.velocityY += 2.5f * -player.getGravityMagnitude();
      }
   }

   @Override
   public void preDrawPlayer(Graphics g, Level game) {
      player.rotation = ballRotation;
   }
}