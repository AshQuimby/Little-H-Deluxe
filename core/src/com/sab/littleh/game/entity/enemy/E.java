package com.sab.littleh.game.entity.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Animation;

import java.util.HashSet;

public class E extends Enemy {

   public E(int x, int y, Player player, Tile parent) {
      super(x, y, player, parent);
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
         if (velocityY > 0) {
            frame = 5;
         } else {
            frame = 6;
         }
      }
      float playerDist = game.player.getCenter().dst2(new Vector2(x + 24, y + 24));
      if (playerDist > 1480 * 1480) {
         remove = true;
      }
      if (touchingGround) {
         Tile tileAhead = getTile(direction, 0, game.tileMap);
         if (tileAhead != null && (tileAhead.hasTag("death") || tileAhead.isSolid())) {
            direction *= -1;
         }
         tileAhead = getTile(direction, -1, game.tileMap);
         if (tileAhead == null || !tileAhead.isSolid()) {
            direction *= -1;
         }
      }
      velocityY -= 1f;
      velocityX += 0.7f * direction;
      velocityX *= 0.9f;
      velocityY *= 0.98f;
      if (toRectangle().overlaps(game.player.toRectangle())) game.player.touchingEnemy(this);
      touchingGround = false;
   }
   
   @Override
   public void onCollision(boolean horizontal, boolean vertical) {
      if (horizontal) {
         velocityX *= -1;
         direction *= -1;
      }
      if (vertical) {
         velocityY = 0;
      }
   }
}