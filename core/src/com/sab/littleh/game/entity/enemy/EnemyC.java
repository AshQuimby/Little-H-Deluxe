package com.sab.littleh.game.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.util.SoundEngine;

public class EnemyC extends Enemy {
   public EnemyC(int x, int y, Player player, Tile parent) {
      super(x, y, player, parent);
      image = "enemies/c.png";
   }

   public int getEnemyType() {
      return 4;
   }
   
   @Override
   public void update(Level game) {
      if (dead) {
         frame = deathAnimation.step();
         if (deathAnimation.getFinished()) remove = true;
         return;
      }
      super.update(game);
      direction = (int) Math.signum(velocityX) == 0 ? direction : (int) Math.signum(velocityX);
      if (touchingGround) {
         if (ControlInputs.isJustPressed(Controls.JUMP) || ControlInputs.isJustPressed(Controls.UP)) {
            velocityY = 25;
            SoundEngine.playSound("jump.ogg");
         }
         if (Math.abs(velocityX) > 2) {
            frame = runAnimation.stepLooping();
         } else {
            frame = 0;
         }
      } else {
         if (velocityY < 0) {
            frame = 5;
         } else {
            frame = 6;
         }
      }
      float playerDist = game.player.getCenter().dst2(new Vector2(x + 24, y + 24));
      if (playerDist > 1800 * 1800) {
         despawn = true;
      }
      if (ControlInputs.isPressed(Controls.LEFT) ^ ControlInputs.isPressed(Controls.RIGHT)) {
         if (ControlInputs.isPressed(Controls.LEFT)) {
            velocityX -= 1f;
         }
         if (ControlInputs.isPressed(Controls.RIGHT)) {
            velocityX += 1f;
         }
      } else if (touchingGround) {
         velocityX *= 0.8f;
      }
      velocityX *= 0.94f;
      velocityY *= 0.98f;
      velocityY -= 1f;

      if (toRectangle().overlaps(game.player.toRectangle())) game.player.touchingEnemy(this);
      touchingGround = false;
   }
}