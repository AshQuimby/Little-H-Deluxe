package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.Animation;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.SoundEngine;

public class WingedMode extends Powerup {

   private Animation flapAnimation = new Animation(14, 6, 7);
   private Animation glideAnimation = new Animation(1, 7);
   private float flyingRotation;
   private boolean gliding;

   public WingedMode(Player player) {
      super(player);
   }

   public void init(Player player) {
      super.init(player);
      gliding = false;
      player.controller.releaseControl(Controls.JUMP);
      player.image = "player/wing_h";
   }

   @Override
   public void updateVelocity() {
      if (player.touchingWater) {
         gliding = false;
         player.velocityX *= 0.92f;
         player.velocityY *= 0.92f;
         player.velocityY -= 0.4f;
         return;
      }
      if (!gliding) {
         if (!(player.slippery && player.crouched)) player.velocityX *= 0.9f;
         else player.velocityX *= 0.95f;
         player.velocityY *= 0.99f;
         player.velocityY -= 0.9f;
      } else {
         player.velocityX *= 0.9999f;
         player.velocityY *= 0.9999f;
         player.velocityY -= 1.5f;
         float magnitude = player.velocityMagnitude();
         player.velocityX = (player.velocityX * 2 + magnitude * (float) Math.cos(flyingRotation)) / 3;
         player.velocityY = (player.velocityY * 2 + magnitude * (float) Math.sin(flyingRotation)) / 3;
      }

      if (player.touchingGround && player.controller.isPressed(Controls.RIGHT) == player.controller.isPressed(Controls.LEFT)) {
         if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
      }
   }

   @Override
   public void jump(Level game) {
      // Winged H can't swim
      if (player.touchingWater) {
         if (player.controller.isJustPressed(Controls.JUMP) || player.controller.isJustPressed(Controls.UP)) {
            player.velocityY = 1;
            SoundEngine.playSound("swim.ogg");
         }
         return;
      }
      if (player.crushed) return;
      if (player.leftGroundFor < 8) {
         SoundEngine.playSound("wing_jump.ogg");
         player.velocityY = 12;
         player.leftGroundFor = 8;
         player.jumpStrength++;
         game.addParticle(new Particle(player.x - 24, player.y + 32, 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
         onJump(false);
      } else if (player.jumpReleased && player.leftWallFor < 8) {
         SoundEngine.playSound("double_jump.ogg");
         player.velocityY = 16;
         player.leftWallFor = 8;
         player.x += -2 * player.wallDirection;
         player.velocityX = -24 * player.wallDirection;
      } else if (player.jumpReleased && player.doubleJump && (player.bonusDoubleJump || game.mapData.getValue("double_jumping").asBool()) && player.velocityY < 18) {
         SoundEngine.playSound("double_jump.ogg");
         game.addParticle(new Particle(player.x, player.y + 16, 0f, 0f, 48, 32, 6, 4, 1, 0f, 0f, 0, 2, "particles/double_jump.png", 9));
         player.velocityY = 18 - Math.max(player.velocityY, 0);
         player.doubleJump = false;
         onJump(true);
      }
   }

   @Override
   public void update(Level game) {
      if (game.mapData.getValue("double_jumping").asBool()) player.doubleJump = true;
      if (!player.dead && !player.win) {
         gliding = player.controller.getPressedFor(Controls.JUMP) > 30;
         if (gliding) {
            if (player.controller.isPressed(Controls.RIGHT)) flyingRotation -= Math.PI / 24;
            if (player.controller.isPressed(Controls.LEFT)) flyingRotation += Math.PI / 24;
            if (player.touchingGround) {
               gliding = false;
               player.controller.releaseControl(Controls.JUMP);
            }
            if (player.velocityMagnitude() < 20 && player.velocityY < 8) {
               player.frame = flapAnimation.stepLooping();
            } else {
               player.frame = glideAnimation.stepLooping();
            }
         } else {
            flyingRotation = MathUtils.PI / 2;
         }
      } else {
         flyingRotation = MathUtils.PI;
      }
      super.update(game);
   }

   @Override
   public void preDrawPlayer(Graphics g, Level game) {
      if (gliding)
         player.rotation = -flyingRotation + MathUtils.PI / 2;
      else
         super.preDrawPlayer(g, game);
   }
}