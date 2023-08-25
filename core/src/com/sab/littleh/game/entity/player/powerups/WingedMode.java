package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.sab.littleh.game.entity.Particle;
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

public class WingedMode extends Powerup {

   private static Animation flapAnimation = new Animation(14, 6, 7);
   private static Animation glideAnimation = new Animation(1, 7);
   private float flyingRotation;
   private boolean gliding;

   public WingedMode(Player player) {
      super(player);
   }

   public void init(Player player) {
      gliding = false;
      player.image = "player/wing_h";
   }

   @Override
   public void updateVelocity() {
      if (player.swimming) {
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
   }

   @Override
   public void jump(Level game) {
      // Winged H can't swim
      if (player.swimming) {
         if (ControlInputs.isJustPressed(Control.JUMP) || ControlInputs.isJustPressed(Control.UP)) {
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
      } else if (player.jumpReleased && player.leftWallFor < 8) {
         SoundEngine.playSound("double_jump.ogg");
         player.velocityY = 16;
         player.leftWallFor = 8;
         player.x += -2 * player.wallDirection;
         player.velocityX = -24 * player.wallDirection;
      } else if (player.jumpReleased && player.doubleJump && game.mapData.getValue("double_jumping").asBool() && player.velocityY < -2) {
         SoundEngine.playSound("double_jump.ogg");
         game.addParticle(new Particle(player.x, player.y + 16, 0f, 0f, 48, 32, 6, 4, 1, 0f, 0f, 0, 2, "particles/double_jump.png", 9));
         player.velocityY = 18;
         player.doubleJump = false;
      }
   }

   @Override
   public void update(Level game) {
      if (game.mapData.getValue("double_jumping").asBool()) player.doubleJump = true;
      if (!player.dead && !player.win) {
         gliding = ControlInputs.getPressedFor(Control.JUMP) > 30;
         if (gliding) {
            if (ControlInputs.isPressed(Control.RIGHT)) flyingRotation -= Math.PI / 24;
            if (ControlInputs.isPressed(Control.LEFT)) flyingRotation += Math.PI / 24;
            if (player.touchingGround) {
               gliding = false;
               ControlInputs.releaseControl(Control.JUMP);
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