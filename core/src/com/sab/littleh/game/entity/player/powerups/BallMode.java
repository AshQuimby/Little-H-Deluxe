package com.sab.littleh.game.entity.player.powerups;

import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Animation;
import com.sab.littleh.util.Control;
import com.sab.littleh.util.ControlInputs;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.SoundEngine;

import java.awt.*;
import java.awt.geom.AffineTransform;

// BALLMODE
// (BALLS!!!!!)

public class BallMode extends Powerup {

   private static Animation rollAnimation = new Animation(1, 0);
   private float rotationSpeed;
   private float ballRotation;
   private boolean stopBounce;
   private boolean superSlam;

   public BallMode(Player player) {
      super(player);
   }

   public void init(Player player) {
      superSlam = false;
      player.image = "player/ball_h";
   }

   @Override
   public void updateVelocity() {
      if (!(player.slippery && player.crouched)) player.velocityX *= 0.975f;
      else player.velocityX *= 0.985f;
      player.velocityY *= 0.985f;
      player.velocityY -= 1f;
   }
   
   @Override
   public void update(Level game) {
      if (player.win) {
         if (player.currentAnimation.getFrame() > 19) {
            ballRotation *= 0.5f;
         } else {
            ballRotation += rotationSpeed;
            rotationSpeed = player.velocityX / 32f;
         }
      }
      if ((ControlInputs.isPressed(Control.RIGHT) ^ ControlInputs.isPressed(Control.LEFT)) || !ControlInputs.isPressed(Control.DOWN)) {
         stopBounce = false;
      }
      if (player.touchingGround) superSlam = false;
      if (ControlInputs.isJustPressed(Control.DOWN) && game.mapData.getValue("crouching").asBool()) {
         if (!player.touchingGround && !superSlam) {
            superSlam = true;
         } else {
            stopBounce = true;
         }
      }
      // Makes the little H SLAM down with BIG BALL ENERGY
      if (superSlam) {
         player.velocityY -= 2;
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
   }
   
   // Bounce like BALL in BALLMODE
   @Override
   public void onCollision(boolean horizontal, boolean vertical) {
      if (horizontal) {
         player.velocityX *= -1f;
         if (Math.abs(player.velocityX) > 8f) SoundEngine.playSound("ball_bounce.ogg");
         // Minimum BOUNCING BALL SPEED
         player.velocityX = Math.max(15, Math.abs(player.velocityX)) * Math.signum(player.velocityX);
      }
      if (vertical) {
         if (stopBounce) {
            player.velocityY = 0;
         } else {
            player.velocityY *= -1f;
            // Minimum BOUNCING BALL SPEED
            player.velocityY = Math.max(15, Math.abs(player.velocityY)) * Math.signum(player.velocityY);
            if (Math.abs(player.velocityY) > 8f) SoundEngine.playSound("ball_bounce.ogg");
         }
      }
   }
   
   @Override
   public void touchingTile(Tile tile) {
      // Makes bouncy tiles LAUNCH the little H
      if (tile.hasTag("bounce")) {
         player.velocityY -= 0.5f;
         player.velocityY = Math.min(96f, player.velocityY);
      }
   }

   @Override
   public void preDrawPlayer(Graphics g, Level game) {
      player.rotation = ballRotation;
   }
}