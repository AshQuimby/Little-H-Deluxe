package com.sab.littleh.game.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Animation;

public class EnemyF extends Enemy {
   public EnemyF(int x, int y, Player player, Tile parent) {
      super(x, y, player, parent);
      runAnimation = new Animation(8, 0, 1, 2, 3);
      deathAnimation = new Animation(4, 4, 5, 6, 7, 8, 9);
      image = "enemies/f.png";
   }

   public int getEnemyType() {
      return 3;
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
      float playerDist = game.player.getCenter().dst2(new Vector2(x + 24, y + 24));
      if (playerDist > 1800 * 1800) {
         despawn = true;
      } else {
         Tile tileAhead = getTile(direction, 0, game.tileMap);
         if (tileAhead != null && (tileAhead.hasTag("death") || (!tileAhead.hasTag("enemy_box") && tileAhead.isSolid()))) {
            direction *= -1;
         }
      }
      velocityY = (float) Math.sin(game.gameTick / 8f);
      velocityX += 0.7f * direction;
      velocityX *= 0.9f;
      velocityY *= 0.98f;
      if (toRectangle().overlaps(game.player.toRectangle())) game.player.touchingEnemy(this);
      touchingGround = false;
   }
}