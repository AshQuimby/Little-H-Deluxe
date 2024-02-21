package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;

public class CubeMode extends Powerup {

   public CubeMode(Player player) {
      super(player);
   }

   public void init(Player player) {
      super.init(player);
      player.image = "player/cube_h";
   }

   @Override
   public void update(Level game) {
      super.update(game);
      if (Math.abs(player.velocityX) > 0.1f && player.currentAnimation == player.idleAnimation)
         player.currentAnimation = player.runAnimation;
   }

   @Override
   public void onJump(boolean airJump) {
      if (airJump && !player.fallingFasterThan(25)) {
         player.coolRoll = MathUtils.PI;
      } else if (!airJump) {
         player.coolRoll = MathUtils.PI * 2;
      }
   }

   @Override
   public void move() {
      if (player.controller.isPressed(Controls.LEFT)) {
         if (player.touchingGround && !player.slippery) player.direction = -1;
      }
      if (player.controller.isPressed(Controls.RIGHT)) {
         if (player.touchingGround && !player.slippery) player.direction = 1;
      }

      player.velocityX += 1.2f * (player.touchingWater ? 0.8f : 1) * player.direction;
   }

   @Override
   public void updateVelocity() {
      if (player.touchingWater) {
         player.velocityX *= 0.9f;
         player.velocityY *= 0.94f;
         player.velocityY += 0.3f * player.getGravityMagnitude();
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
         player.velocityY += 1.2f * player.getGravityMagnitude();
   }
}