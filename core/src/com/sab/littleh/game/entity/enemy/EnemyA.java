package com.sab.littleh.game.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.SoundEngine;

public class EnemyA extends Enemy {
   public EnemyA(int x, int y, Player player, Tile parent) {
      super(x, y, player, parent);
      image = "enemies/a.png";
   }

   public int getEnemyType() {
      return 2;
   }
   
   @Override
   public void update(Level game) {
      if (dead) {
         frame = deathAnimation.step();
         if (deathAnimation.getFinished()) remove = true;
         return;
      }
      frame = runAnimation.stepLooping();
      super.update(game);
      if (!touchingGround) {
         if (velocityY < 0) {
            frame = 5;
         } else {
            frame = 6;
         }
      }
      float playerDist = game.player.getCenter().dst2(new Vector2(x + 24, y + 24));
      if (playerDist > 2000 * 2000) {
         despawn = true;
      }
      Tile tileAhead = getTile(direction, 0, game.getBaseLayer().tileMap);
      boolean jump = touchingGround && tileAhead != null && (tileAhead.isSolid() || tileAhead.hasTag("death"));
      
      float testX = x;
      float testY = y;
      float testVX = velocityX;
      float testVY = velocityY;
      
      if (touchingGround) for (int i = 0; i < 90; i++) {
         testX += testVX;
         testY += testVY;
         testVY -= 1f;
         testVX += 0.6f * direction;
         testVX *= 0.95f;
         testVY *= 0.98f;
         Tile tile = game.getTileAt("normal", (int) (testX / 64), (int) (testY / 64));
         if (tile != null && tile.hasTag("death")) {
            jump = true;
            break;
         }
      }
      if (jump) {
         velocityY = 26;
         SoundEngine.playSound("jump.ogg");
      }
      this.direction = (int) Math.signum(game.player.x - this.x);
      if (direction == 0) direction = -1;
      velocityY -= 1f;
      velocityX += 0.6f * direction;
      velocityX *= 0.95f;
      velocityY *= 0.98f;
      if (toRectangle().overlaps(game.player.toRectangle())) game.player.touchingEnemy(this);
      touchingGround = false;

   }
}