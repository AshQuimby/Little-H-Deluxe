package com.sab.littleh.game.entity.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Entity;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.powerups.BallMode;
import com.sab.littleh.game.entity.player.powerups.Powerup;
import com.sab.littleh.game.entity.player.powerups.WingedMode;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.dialogue.Dialogues;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Entity {
    public static Animation idleAnimation = new Animation(12, 0, 1);
    public static Animation runAnimation = new Animation(4, 2, 3, 4, 5);
    public static Animation deathAnimation = new Animation(4, 9, 10, 11, 12, 13, 14);
    public static Animation wallSlideAnimation = new Animation(1, 8);
    public static Animation fallAnimation = new Animation(1, 6);
    public static Animation jumpAnimation = new Animation(1, 7);
    public static Animation crouchAnimation = new Animation(1, 24);
    public static Animation slideAnimation = new Animation(4, 25, 26);
    public static Animation winAnimation = new Animation(8, 15, 16, 15, 16, 15, 16, 15, 16, 15, 16, 15, 16, 17, 18, 19, 20, 21, 22, 23);

    public boolean win;
    public boolean end;
    public boolean startTick;
    public Point startPos;

    public boolean jumpReleased;
    public int leftGroundFor;
    public int leftWallFor;
    public int jumpStrength;
    public boolean touchingWall, doubleJump, dead, trueKill;
    // Style points
    public float coolRoll;
    public float maxGroundSpeed;
    public Animation currentAnimation;

    public boolean crouched;
    public boolean crushed;
    public int wallDirection;
    public int keyCount;
    public int ticksAlive;
    public boolean hasEvilKey;
    public boolean savedEvilKey;
    public EvilKey evilKey;
    public int savedKeyCount;
    public int[] totalCoinCounts;
    public int[] coinCounts;
    public Powerup savedPowerup;
    public java.util.List<Point> previousPositions;
    public java.util.List<Float> previousSpeeds;
    private Powerup powerup;

    public Player(Point startPos) {
        ticksAlive = 0;
        dead = false;
        currentAnimation = idleAnimation;
        x = startPos.x * 64 + 8;
        y = startPos.y * 64 + 8;
        this.startPos = startPos;
        direction = 1;
        velocityX = 0;
        velocityY = 0;
        width = 48;
        height = 48;
        slippery = false;
        rotation = 0;
        win = false;
        trueKill = false;
        previousPositions = new ArrayList<>();
        previousSpeeds = new ArrayList<>();
        totalCoinCounts = new int[4];
        coinCounts = new int[4];
        startTick = true;
        lastTouchedTiles = new HashSet<>();
        crushed = false;
        powerup = new Powerup(this);
        savedPowerup = powerup;
    }

    public Player(Point startPos, Level game) {
        this(startPos);
        init(game);
    }

    public void setStartPos(Point startPos) {
        this.startPos.x = startPos.x;
        this.startPos.y = startPos.y;
    }

    public void init(Level game) {
        ticksAlive = 0;
        leftGroundFor = 0;
        leftWallFor = 0;
        touchingGround = false;
        win = false;
        x = startPos.x * 64 + 8;
        y = startPos.y * 64 + 8;
        velocityX = 0;
        velocityY = 0;
        jumpReleased = false;
        doubleJump = false;
        dead = false;
        currentAnimation = idleAnimation;
        deathAnimation.reset();
        winAnimation.reset();
        winAnimation.setAnimationSpeed(8);
        slippery = false;
        keyCount = savedKeyCount;
        hasEvilKey = savedEvilKey;
        if (hasEvilKey) {
            evilKey = new EvilKey((int) x / 64, (int) y / 64);
        }
        previousPositions.clear();
    }

    public void setCoinCounts(Level game) {
        for (int i = 0; i < totalCoinCounts.length; i++) {
            totalCoinCounts[i] = game.getVolatileTileCount("coin", i);
        }
    }

    public void updateCoinCounts(Level game) {
        for (int i = 0; i < coinCounts.length; i++) {
            if (totalCoinCounts[i] > 0) coinCounts[i] = totalCoinCounts[i] - game.getVolatileTileCount("coin", i);
        }
    }

    public void jump(Level game) {
        powerup.jump(game);
    }

    public void updateVelocity() {
        powerup.updateVelocity();
    }

//    public void touchingEnemy(Enemy enemy) {
//        kill();
//    }

    @Override
    public void update(Level game) {
        if (startTick) {
            setCoinCounts(game);
            startTick = false;
        }

        ticksAlive++;

        if (hasEvilKey) {
            evilKey.update(this, game);
        } else {
            evilKey = null;
        }

        previousPositions.add(0, new Point((int) x, (int) y));
        if (previousPositions.size() > 512) {
            previousPositions.remove(previousPositions.size() - 1);
        }
        previousSpeeds.add(0, velocityMagnitude());
        if (previousSpeeds.size() > 8) {
            previousSpeeds.remove(previousSpeeds.size() - 1);
        }

        frame = currentAnimation.stepLooping();

        if (currentAnimation == runAnimation) {
            if (runAnimation.tick == 0 && runAnimation.frame % 2 == 0) SoundEngine.playSound("step.mp3");
        }

        if (end) {
            game.endGame();
            return;
        }
        if (win) {
            dead = false;
            currentAnimation = winAnimation;
            if (currentAnimation.getFrame() >= 21) {
                currentAnimation.setAnimationSpeed(4);
                velocityX = 0;
                velocityY += 3f;
                y += velocityY;
            } else if (currentAnimation.getFrame() >= 17) {
                currentAnimation.setAnimationSpeed(2);
                velocityX *= 0;
                velocityY *= 0;
            } else {
                updateVelocity();
                collide(game);
            }
            if (currentAnimation.getFinished()) {
                end = true;
            }
            return;
        }
        if (dead) {
            if (currentAnimation.getFinished()) {
                if (trueKill) {
                    game.reset();
                    return;
                }
                init(game);
                powerup = savedPowerup;
                powerup.init(this);
                game.resetToCheckpointState();
                updateCoinCounts(game);
            }
            return;
        }

        powerup.update(game);

        if (y < -64)
            kill();

        // Crouching
//        if (game.mapSettings[Level.ALLOW_CROUCH]) {
        if (true) {
            if (ControlInputs.isJustPressed(Control.LEFT) ^ ControlInputs.isJustPressed(Control.RIGHT) || !ControlInputs.isPressed(Control.DOWN) || !touchingGround) {
                height = 48;
                crouched = false;
            }
            if (ControlInputs.isJustPressed(Control.DOWN) && touchingGround && game.mapData.getValue("crouching").asBool()) {
                touchingWall = false;
                velocityX *= 1.5f;
                height = 24;
                crouched = true;
            }
        }

        crushed = false;
        updateVelocity();
        touchingGround = false;
        touchingWall = false;
        slippery = false;
        collide(game);
    }

    public Vector2 getCenter() {
        return new Vector2(x + 24, y + 24);
    }

    public int getCenterX() {
        return (int) (x + width / 2);
    }

    public int getCenterY() {
        return (int) (y + height / 2);
    }

    // Return false to prevent the player from having their velocity set to 0
    @Override
    public boolean onCollide(Level game, com.badlogic.gdx.math.Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
        if (tile.hasTag("key_box")) {
            if (tile.hasTag("evil")) {
                if (hasEvilKey) {
                    hasEvilKey = false;
                    for (int i = 0; i < 4; i++) {
                        game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -20), (float) (Math.random() * -10), 32, 32, 4, 4, 1, 0.98f, 0f, i, 0, "particles/evil_key_box_rubble.png", 30));
                    }
                    SoundEngine.playSound("hit.mp3");
                    game.inGameRemoveTile(tile);
                    return false;
                }
            } else if (keyCount > 0) {
                keyCount--;
                for (int i = 0; i < 4; i++) {
                    game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -8), (float) (Math.random() * -10), 32, 32, 4, 4, 1, 0.98f, 1.2f, i, 0, "particles/key_box_rubble.png", 30));
                }
                SoundEngine.playSound("hit.mp3");
                game.inGameRemoveTile(tile);
                return false;
            }
        }
        if (tile.hasTag("slippery")) {
            slippery = true;
        }
        if (yCollision) {
            if (velocityY < 0) {
                touchingGround = true;
            }
            if (tile.hasTag("slippery")) {
                slippery = true;
            }
        } else {
            if (game.mapData.getValue("wall_sliding").asBool() && !tile.hasTag("slick")) {
                touchingWall = true;
            }
        }
        return super.onCollide(game, entityHitbox, tileHitbox, tile, yCollision);
    }

    public boolean isFresh() {
        return ticksAlive <= 1;
    }

    public void collide(Level game) {
        java.util.List<Tile> collisions = new ArrayList<Tile>();
        Rectangle playerHitbox = new Rectangle(x, y, width, height);

        solidInteractions(playerHitbox, collisions, game);
        collisions = getNearbyTiles(game.tileMap);
        tileInteractions(playerHitbox, collisions, game);

        set(playerHitbox);
    }

    public void onCollision(boolean horizontal, boolean vertical) {
        powerup.onCollision(horizontal, vertical);
    }

    public void tileInteractions(Rectangle playerHitbox, List<Tile> collisions, Level game) {
        Set<Tile> newLastTouchedTiles = new HashSet<>();
        for (Tile tile : collisions) {
            if (crouched && tile.isSolid() && tile.hasTag("half") && (tile.tileType == 0 || tile.tileType == 2)) {
                ControlInputs.pressControl(Control.DOWN);
                crushed = true;
            }
            Rectangle tileHitbox = tile.toRectangle();
            if (playerHitbox.overlaps(tileHitbox)) {
                touchingTile(tile);
                if (tile.hasTag("death")) {
                    if (playerHitbox.overlaps(tileHitbox)) kill();
                } else if (tile.hasTag("bounce")) {
                    if (velocityY < 30 && !lastTouchedTiles.contains(tile)) SoundEngine.playSound("bounce.mp3");
                    if (velocityY < -36) velocityY *= -1.5f;
                    else if (velocityY < 36) velocityY = 36;
                }
                if (tile.hasTag("checkpoint") && tile.tileType % 2 == 0) {
                    SoundEngine.playSound("checkpoint.mp3");
                    startPos.x = tile.x;
                    startPos.y = tile.y;
                    game.notify("notify_checkpoint", null);
                    tile.setTileType(1);
                    game.saveCheckpointState();
                    savedKeyCount = keyCount;
                    savedEvilKey = hasEvilKey;
                    savedPowerup = powerup;
                    game.showTimer();
                } else if (!win && tile.hasTag("end")) {
                    game.showTimer();
                    for (int i = 0; i < 16; i++) {
                        game.addParticle(new Particle(x + width / 4, y + height / 4, (float) ((Math.random() - 0.5) * -16), (float) ((Math.random() - 0.5) * -16), 24, 24, 3, 3, 1, 0.96f, 0f, (int) (Math.random() * 2), 0, "particles/twinkle.png", 120));
                    }
                    win();
                }
                if (tile.hasTag("pickup")) {
                    if (playerHitbox.overlaps(tileHitbox)) {
                        game.inGameRemoveTile(tile);
                        if (tile.hasTag("dialogue")) {
                            String key = tile.extra.trim();
                            game.setDialogue(Dialogues.getDialogue(key));
                            continue;
                        }
                        if (tile.hasTag("coin")) {
                            SoundEngine.playSound("coin.mp3");
                            coinCounts[tile.tileType]++;
                            if (game.getVolatileTileCount("coin", tile.tileType) == 0) {
                                SoundEngine.playSound("all_coins_collected.mp3");
                                game.notify("notify_all_coins", new int[]{ tile.tileType });
                            }
                        }
                        if (tile.hasTag("powerup")) {
                            SoundEngine.playSound("powerup_get.mp3");
                            if (tile.tileType == 0) powerup = new Powerup(this);
                            else if (tile.tileType == 1) powerup = new BallMode(this);
                            else if (tile.tileType == 2) powerup = new WingedMode(this);
                        }
                        if (tile.hasTag("key")) {
                            SoundEngine.playSound("coin.mp3");
                            if (tile.hasTag("evil")) {
                                evilKey = new EvilKey(tile.x, tile.y);
                                hasEvilKey = true;
                                continue;
                            }
                            keyCount++;
                        }
                        if (tile.hasTag("timer")) {
                            SoundEngine.playSound("powerup_get.mp3");
                            if (game.timeLimit > -1) {
                                switch (tile.getPropertyIndex()) {
                                    case 0 :
                                        game.timeLimit += 10;
                                        game.startPopup("+10 seconds", 180);
                                        break;
                                    case 1 :
                                        game.timeLimit += 30;
                                        game.startPopup("+30 seconds", 180);
                                        break;
                                    case 2 :
                                        game.timeLimit += 60;
                                        game.startPopup("+60 seconds", 180);
                                        break;
                                    case 3 :
                                        game.timeLimit += 100;
                                        game.startPopup("+100 seconds", 180);
                                        break;
                                }
                            }
                            game.timeLimit = Math.min(9999, game.timeLimit);
                        }
                    }
                }
                newLastTouchedTiles.add(tile);
            }
        }
        lastTouchedTiles = newLastTouchedTiles;
    }

    public void kill() {
        if (!win && !dead) {
            SoundEngine.playSound("death.mp3");
            dead = true;
            currentAnimation = deathAnimation;
        }
    }

    public void trueKill() {
        kill();
        trueKill = true;
    }

    public void win() {
        win = true;
        SoundEngine.playSound("win_level.mp3");
        velocityX *= 0.8f;
        velocityY *= 0.8f;
        currentAnimation = winAnimation;
    }

    public void touchingTile(Tile tile) {
        powerup.touchingTile(tile);
    }

    public Point getPreviousCenter(int ticksBehind) {
        if (ticksBehind < previousPositions.size()) return new Point(previousPositions.get(ticksBehind).x + width / 2, previousPositions.get(ticksBehind).y + height / 2);
        return new Point (0, 0);
    }


// TODO: Put the trail back
  /*
    public void renderTrail(Graphics g, Level game) {
        boolean speedy = false;
        float speed = 0;
        for (float f : previousSpeeds) {
            if (f > 24) {
                if (f > speed) speed = f;
                speedy = true;
            }
        }
        if (speedy) {
            int[][] trail = new int[2][9];
            trail[0][0] = (int) x + width / 2;
            trail[1][0] = (int) y + height / 2;
            for (int i = 1; i < 5; i++) {
                trail[0][i] = getPreviousCenter(i * 2).x;
                trail[1][i] = getPreviousCenter(i * 2).y;
            }
            double[] angles = new double[4];
            for (int i = 0; i < 4; i++) {
                angles[i] = Math.atan2(trail[1][i + 1] - trail[1][i], trail[0][i + 1] - trail[0][i]);
            }
            for (int i = 0; i < 4; i++) {
                trail[0][8 - i] = (int) (trail[0][i] + 6 * (5 - i) * Math.cos(angles[i] + Math.PI / 2));
                trail[1][8 - i] = (int) (trail[1][i] + 6 * (5 - i) * Math.sin(angles[i] + Math.PI / 2));
            }
            for (int i = 0; i < 4; i++) {
                trail[0][i] = (int) (trail[0][i] + 6 * (5 - i) * Math.cos(angles[i] + Math.PI / 2 * 3));
                trail[1][i] = (int) (trail[1][i] + 6 * (5 - i) * Math.sin(angles[i] + Math.PI / 2 * 3));
            }
        }
    }
*/

    public void render(Graphics g, Level game) {
        for (int i = keyCount; i > 0; i--) {
            if (previousPositions.size() > 0) {
                Point position = previousPositions.get(Math.max(0, Math.min(previousPositions.size() - 1, 5 * (2 + i))));
                g.drawImage(Images.getImage("tiles/key.png"), new Rectangle(position.x - 8, position.y, 64, 64), new Rectangle(0, 0, 8, 8));
            }
        }
        if (evilKey != null) {
            evilKey.render(g, this, game);
        }
//        renderTrail(g, game);
        powerup.preDrawPlayer(game);
        drawPlayer(g, game);
//        for (int i = 0; i < totalCoinCounts.length; i++) {
//            int total = totalCoinCounts[i];
//            if (total > 0) {
//                TextUtils.drawText(g, TileEditor.program.getWidth() - 48, 48 + 48 * i + 12, 24, coinCounts[i] + "/" + totalCoinCounts[i], TileEditor.getSecondaryColor(), 1);
//                Images.drawImage(g, Images.getImage("ui/coins.png"), new Rectangle(TileEditor.program.getWidth() - 40, 48 + 48 * i, 32, 40), new Rectangle(0, 5 * i, 4, 5));
//            }
//        }
    }

    public void drawPlayer(Graphics g, Level game) {
        g.drawImage(Images.getImage(image + ".png"), new Rectangle(x - 8, y, 64, 64), new Rectangle((direction == 1 ? 0 : 8), 8 * frame, (direction == 1 ? 8 : -8), 8), -MathUtils.radiansToDegrees * rotation);
        g.drawImage(Images.getImage(image + "_color.png"), new Rectangle(x - 8, y, 64, 64), new Rectangle((direction == 1 ? 0 : 8), 8 * frame, (direction == 1 ? 8 : -8), 8), -MathUtils.radiansToDegrees * rotation);
    }

    public void touchingEnemy(Enemy enemy) {
        kill();
    }

    public class EvilKey {
        public Rectangle hitbox;
        public float keyVelX, keyVelY;
        public int startUp;

        public EvilKey(int tileX, int tileY) {
            hitbox = new Rectangle(tileX * 64 + 16, tileY * 64 + 16, 48, 48);
            startUp = 120;
        }

        public void update(Player player, Level game) {
            if (startUp > 0) {
                if (startUp % 4 == 0) {
                    game.addParticle(new Particle(hitbox.x + hitbox.width / 2 - 24, hitbox.y + hitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -6), (float) ((Math.random() - 0.5) * -6), 32, 32, 4, 4, 1, 0.98f, 0f, (int) (Math.random() * 4), 0, "particles/evil_smoke.png", 60));
                }
                startUp--;
                return;
            }
            if (Math.random() > 0.98) {
                game.addParticle(new Particle(hitbox.x + hitbox.width / 2 - 24, hitbox.y + hitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -6), (float) ((Math.random() - 0.5) * -6), 32, 32, 4, 4, 1, 0.98f, 0f, (int) (Math.random() * 4), 0, "particles/evil_smoke.png", 30));
            }
            Point target = player.previousPositions.get(30);
            keyVelX = (target.x - hitbox.x) / 3;
            keyVelY = (target.y - hitbox.y) / 3;
            hitbox.x += keyVelX;
            hitbox.y += keyVelY;
            if (hitbox.overlaps(new Rectangle(player.x ,player.y, player.width, player.height))) {
                player.kill();
            }
        }

        public void render(Graphics g, Player player, Level game) {
            Point position = new Point((int) hitbox.x - 16, (int) hitbox.y);
            g.drawImage(startUp > 60 ? Images.getImage("tiles/evil_key.png") : Images.getImage("tiles/evil_key_awake.png"), new Rectangle(position.x, position.y, 64, 64), new Rectangle(0, 0, 8, 8));
        }
    }
}