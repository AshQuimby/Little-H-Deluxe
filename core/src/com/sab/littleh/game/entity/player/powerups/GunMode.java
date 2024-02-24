package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.entity.Entity;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.net.NetPlayer;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class GunMode extends Powerup {
    public static List<Bullet> bullets = new ArrayList<>();
    public static Animation runBackwardsAnimation = new Animation(4, 5, 4, 3, 2);
    private float gunY = 0;
    private float gunRotation;
    private int reloadTime;

    public GunMode(Player player) {
        super(player);
    }

    @Override
    public void init(Player player) {
        super.init(player);
        player.image = "player/gun_h";
    }

    @Override
    public void update(Level game) {
        if (!(player.touchingWall))
            player.direction = getAdjustedGunRotation() > 180 ? -1 : 1;
        if (player.currentAnimation == player.runAnimation) {
            if (Math.signum(player.velocityX) != player.direction) {
                player.currentAnimation = runBackwardsAnimation;
            }
        }
        gunY = 0;
        if (player.frame == 1) gunY = 8 * player.getGravityDirection();
        else if (player.frame > 24) gunY = 16 * player.getGravityDirection();
        else if (player.frame == 4 || player.frame == 5 || player.frame == 6) gunY = 8 * -player.getGravityDirection();
        gunY += 8 * -player.getGravityDirection();
        gunRotation = MouseUtil.getDynamicMousePosition().sub(player.getCenter().add(0, gunY)).angleDeg();
        player.coolRoll = 0;
        super.update(game);
        player.coolRoll = 0;
        if (!(player.touchingWall))
            player.direction = getAdjustedGunRotation() > 180 ? -1 : 1;
        if (!(player instanceof NetPlayer))
            if (Cursors.cursorIsNot("reticle"))
                Cursors.switchCursor("reticle");

        bullets.forEach(bullet -> bullet.update(game));
        bullets.removeIf(bullet -> !bullet.alive);

        if (reloadTime > 0) {
            reloadTime--;
        } else {
            if (MouseUtil.leftMouseJustPressed()) {
                SoundEngine.playSound("gunshot.ogg");
                Bullet bullet = new Bullet(player.getCenterX(), player.getCenterY() + 8);
                for (int i = 0; i < 2; i++) bullet.update(game);
                bullets.add(bullet);
                reloadTime = 30;
            }
        }
    }

    @Override
    public void move() {
        if (player.controller.isPressed(Controls.LEFT)) {
            player.direction = -1;
            player.velocityX -= 1.2f * (player.touchingWater ? 0.5f : 1);
        }

        if (player.controller.isPressed(Controls.RIGHT)) {
            player.direction = 1;
            player.velocityX += 1.2f * (player.touchingWater ? 0.5f : 1);
        }
    }

    public float getAdjustedGunRotation() {
        return (gunRotation + 90) % 360;
    }

    @Override
    public void drawPlayer(Graphics g, Level game) {
        bullets.forEach(bullet -> bullet.render(g, game));
        if (!player.dead && !player.win) {
            g.drawImage(Images.getImage("player/gun_hand.png"),
                    player.getCenterX() - 36 + MathUtils.cosDeg(gunRotation) * 44, player.getCenterY() - 12 + MathUtils.sinDeg(gunRotation) * 36 + gunY,
                    72, 24, new Rectangle(getAdjustedGunRotation() > 180 ? 9 : 0, 0, getAdjustedGunRotation() > 180 ? -9 : 9, 3),
                    (getAdjustedGunRotation() % 180 - 90 + player.rotation * MathUtils.radiansToDegrees));
            g.drawImage(Images.getImage("player/revolver.png"),
                    player.getCenterX() - 36 + MathUtils.cosDeg(gunRotation) * 44, player.getCenterY() - 12 + MathUtils.sinDeg(gunRotation) * 36 + gunY,
                    72, 24, new Rectangle(getAdjustedGunRotation() > 180 ? 9 : 0, 0, getAdjustedGunRotation() > 180 ? -9 : 9, 3),
                    (getAdjustedGunRotation() % 180 - 90 + player.rotation * MathUtils.radiansToDegrees) + player.direction * (reloadTime * reloadTime / 8f));
        }
        super.drawPlayer(g, game);
    }

    private static class Bullet extends Entity {
        private boolean alive;
        private int life;
        public Bullet(float x, float y) {
            this.x = x - 8;
            this.y = y - 8;
            width = 16;
            height = 16;
            image = "player/bullet.png";
            alive = true;
            life = 600;
            Vector2 velocity = MouseUtil.getDynamicMousePosition().sub(new Vector2(x, y)).nor().scl(16);
            velocityX = velocity.x;
            velocityY = velocity.y;
        }

        @Override
        public void update(Level game) {
            for (int i = 0; i < 4; i++) {
                if (!alive) break;
                super.update(game);
                game.getEnemies().forEach(enemy -> {
                    if (enemy.toRectangle().overlaps(toRectangle())) {
                        alive = false;
                        enemy.kill();
                    }
                });
                game.addParticle(new Particle(x, y, 0, 0, 16, 16, 8, 8,
                        0, 0, 0, 0, 0, "player/bullet.png", 20, 0.1f));
            }
            frame = 0;
            if (--life <= 0) alive = false;
        }

        @Override
        public void touchingTile(Level game, Tile tile) {
            if (tile.hasTag("coin")) {
                game.inGameRemoveTile(tile);
                SoundEngine.playSound("coin.ogg");
                game.player.coinCounts[tile.tileType]++;
                if (game.player.shouldRenderCoinCounts[tile.tileType] && game.getCheckpointSavedTileCount("coin", tile.tileType) == 0) {
                    SoundEngine.playSound("all_coins_collected.ogg");
                    game.notify("notify_all_coins", tile.tileType);
                }
                Enemy bestTarget = null;
                for (Enemy enemy : game.getEnemies()) {
                    if (bestTarget == null || enemy.toRectangle().getCenter(new Vector2()).dst2(x, y) < bestTarget.toRectangle().getCenter(new Vector2()).dst2(x, y))
                        bestTarget = enemy;
                }
                if (bestTarget != null) {
                    Vector2 velocity = bestTarget.toRectangle().getCenter(new Vector2()).sub(new Vector2(x, y)).nor().scl(16);
                    velocityX = velocity.x;
                    velocityY = velocity.y;
                }
                SoundEngine.playSound("bounce.ogg");
            }
            if (tile.hasTag("death")) {
                alive = false;
            }
        }

        @Override
        public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
            if (tile.hasTag("crumbling")) {
                alive = false;
                for (int i = 0; i < 4; i++) {
                    game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -8), (float) (Math.random() * -10), 64, 64, 8, 8, 1, 0.98f, 1.2f, i, 0, "particles/mud_rubble.png", 30));
                }
                SoundEngine.playSound("hit.ogg");
                game.inGameRemoveTile(tile);
                return false;
            }
            return true;
        }

        @Override
        public void onCollision(boolean horizontal, boolean vertical) {
            if (horizontal)
                velocityX *= -1;
            if (vertical)
                velocityY *= -1;
            SoundEngine.playSound("bounce.ogg");
            life -= 60;
        }

        @Override
        public void render(Graphics g, Level game) {
            g.draw(Images.getImage(image), x, y, width, height);
        }
    }
}