package com.sab.littleh.game.entity.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.entity.Entity;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.Animation;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.SoundEngine;

import java.util.HashSet;

public class Enemy extends Entity {
   protected Animation runAnimation;
   protected Animation deathAnimation;
   protected Tile parent;
   protected boolean deathWarn;
   protected boolean dead;
   public boolean despawn;
   public boolean remove;

   public Enemy(int x, int y, Player player, Tile parent) {
      this.x = x * 64 + 8;
      this.y = y * 64 + 8;
      this.direction = (int) Math.signum(player.x - this.x);
      if (direction == 0) direction = -1;
      this.width = 48;
      this.height = 48;
      image = "enemies/e.png";
      frame = 0;
      lastTouchedTiles = new HashSet<>();
      this.parent = parent;
      runAnimation = new Animation(8, 1, 2, 3, 4);
      deathAnimation = new Animation(4, 7, 8, 9, 10, 11, 12);
      despawn = false;
      dead = false;
      remove = false;
      deathWarn = false;
   }

   public static Enemy createEnemy(int x, int y, Player player, Tile parent, byte tileType) {
      switch (tileType) {
         case 0 :
            return new EnemyE(x, y, player, parent);
         case 1 :
            return new EnemyA(x, y, player, parent);
         case 2 :
            return new EnemyF(x, y, player, parent);
         case 3 :
            return new EnemyC(x, y, player, parent);
      }
      return null;
   }

   public final Tile getParent() {
      return parent;
   }
   
   @Override
   public void kill() {
      if (!dead) {
         dead = true;
         SoundEngine.playSound("hit.ogg");
      }
   }
   
   @Override
   public void touchingTile(Tile tile) {
      if (tile.hasTag("death")) kill();
      if (tile.hasTag("bounce")) {
         if (velocityY < 16)
            SoundEngine.playSound("bounce.ogg");
         velocityY = 32;
      }
      if (tile.hasTag("enemy_box")) {
         if (tile.tileType / 2 == 0 || tile.tileType / 2 == getEnemyType()) {
            tile.notify("notify_enemy_touched", new int[0]);
            kill();
         }
      }
   }

   public int getEnemyType() {
      return 0;
   }

   @Override
   public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
      if (tile.hasTag("enemy_box")) {
         if (tile.tileType / 2 == 0 || tile.tileType / 2 == getEnemyType()) {
            tile.notify("notify_enemy_touched", new int[0]);
            kill();
         }
      }
      return super.onCollide(game, entityHitbox, tileHitbox, tile, yCollision);
   }

   @Override
   public void onCollision(boolean horizontal, boolean vertical) {
      if (horizontal) {
         velocityX = 0;
      }
      if (vertical) {
         velocityY = 0;
      }
   }
   
   @Override
   public void render(Graphics g, Level game) {
      rotation = velocityX * (touchingGround ? 1f / 96f : 1f / 128f) * (-velocityY / 16f + 1f);
      super.render(g, game);
   }
   
   @Override
   public boolean equals(Object o) {
      return o != null && ((Enemy) o).parent == parent;
   }
}