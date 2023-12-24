package com.sab.littleh.game.entity.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.game.entity.Entity;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.powerups.*;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.dialogue.Dialogue;
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
    public boolean ignoreWater;
    public boolean crouched;
    public boolean crushed;
    public int wallDirection;
    public int keyCount;
    public int ticksAlive;
    public float trailSpeed;
    public boolean hasEvilKey;
    public boolean savedEvilKey;
    public EvilKey evilKey;
    public int savedKeyCount;
    public int[] totalCoinCounts;
    public int[] coinCounts;
    public boolean[] shouldRenderCoinCounts;
    public Powerup savedPowerup;
    public java.util.List<Point> previousPositions;
    public java.util.List<Float> previousSpeeds;
    public boolean canCrouch;
    private Powerup powerup;
    private boolean speedrunning;

    public Player(Point startPos) {
        ticksAlive = 0;
        dead = false;
        currentAnimation = idleAnimation;
        x = startPos.x * 64 + 8;
        y = startPos.y * 64;
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
        shouldRenderCoinCounts = new boolean[4];
        startTick = true;
        lastTouchedTiles = new HashSet<>();
        crushed = false;
        powerup = new Powerup(this);
        savedPowerup = powerup;
        previousPositions.clear();
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
        y = startPos.y * 64;
        velocityX = 0;
        velocityY = 0;
        jumpReleased = false;
        doubleJump = false;
        dead = false;
        previousSpeeds.clear();
        currentAnimation = idleAnimation;
        deathAnimation.reset();
        deathAnimation.setAnimationSpeed(4);
        winAnimation.reset();
        winAnimation.setAnimationSpeed(8);
        slippery = false;
        keyCount = savedKeyCount;
        hasEvilKey = savedEvilKey;
        if (hasEvilKey) {
            evilKey = new EvilKey((int) x / 64, (int) y / 64);
        }
    }

    public void setCoinCounts(Level game) {
        for (int i = 0; i < totalCoinCounts.length; i++) {
            totalCoinCounts[i] = game.getVolatileTileCount("coin", i);
            shouldRenderCoinCounts[i] = game.getVolatileTileCount("coin_box", i * 2) > 0 || game.getVolatileTileCount("coin_box", i * 2 + 1) > 0;
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

    public boolean startSpeedrunTimer() {
        return speedrunning;
    }

    @Override
    public void update(Level game) {
        touchingWater = false;
        for (Tile tile : lastTouchedTiles) {
            if (tile.hasTag("water")) {
                touchingWater = true;
                break;
            }
        }

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
        if (previousPositions.size() > 1024) {
            previousPositions.remove(previousPositions.size() - 1);
        }
        previousSpeeds.add(0, velocityMagnitude());
        if (previousSpeeds.size() > 8) {
            previousSpeeds.remove(previousSpeeds.size() - 1);
        }

        frame = currentAnimation.stepLooping();

        if (currentAnimation == runAnimation) {
            if (runAnimation.tick == 0 && runAnimation.frame % 2 == 0) SoundEngine.playSound("step.ogg");
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
            } else if (warpingOut()) {
                currentAnimation.setAnimationSpeed(2);
                velocityX *= 0;
                velocityY *= 0;
            } else {
                deathAnimation.setAnimationSpeed(8);
                updateVelocity();
                collide(game);
            }
            if (currentAnimation.getFinished()) {
                end = true;
            }
            return;
        }
        if (dead) {
            deathAnimation.setAnimationSpeed(4);
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
        if (canCrouch && game.mapData.getValue("crouching").asBool()) {
            if (ControlInputs.isJustPressed(Controls.LEFT) ^ ControlInputs.isJustPressed(Controls.RIGHT) || !ControlInputs.isPressed(Controls.DOWN) || !touchingGround) {
                height = 48;
                crouched = false;
            }
            if (ControlInputs.isJustPressed(Controls.DOWN) && touchingGround) {
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
        canCrouch = true;
        collide(game);

        // Don't start the timer until the player moves (to avoid unfair lag spikes on level load)
        if (!speedrunning) {
            if (previousPositions.size() > 0 && !previousPositions.get(0).equals(new Point((int) x, (int) y))) {
                speedrunning = true;
            }
            if (ticksAlive > 2)
                ticksAlive = 2;
        }
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
    public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
        if (!powerup.onCollide(game, entityHitbox, tileHitbox, tile, yCollision)) {
            return false;
        }
        return super.onCollide(game, entityHitbox, tileHitbox, tile, yCollision);
    }

    public boolean isFresh() {
        return ticksAlive <= 1;
    }

    @Override
    public void onCollision(boolean horizontal, boolean vertical) {
        powerup.onCollision(horizontal, vertical);
    }

    @Override
    public void tileInteractions(Rectangle playerHitbox, List<Tile> collisions, Level game) {
        Set<Tile> newLastTouchedTiles = new HashSet<>();
        for (Tile tile : collisions) {
            if (crouched && tile.isSolid() && tile.hasTag("half") && (tile.tileType == 0 || tile.tileType == 2)) {
                ControlInputs.pressControl(Controls.DOWN);
                crushed = true;
            }
            if (tile.hasTag("multi_hitbox")) {
                List<Rectangle> tileHitboxes = tile.toRectangles();
                for (Rectangle tileHitbox : tileHitboxes) {
                    if (playerHitbox.overlaps(tileHitbox)) {
                        touchingTile(game, playerHitbox, tile, tileHitbox, newLastTouchedTiles);
                        break;
                    }
                }
            } else {
                Rectangle tileHitbox = tile.toRectangle();
                if (playerHitbox.overlaps(tileHitbox)) {
                    touchingTile(game, playerHitbox, tile, tileHitbox, newLastTouchedTiles);
                }
            }
        }
        lastTouchedTiles = newLastTouchedTiles;
    }

    public void kill() {
        if (!win && !dead) {
            LittleH.program.dynamicCamera.addScreenShake(10);
            SoundEngine.playSound("death.ogg");
            dead = true;
            currentAnimation = deathAnimation;
            deathAnimation.setAnimationSpeed(4);
        }
    }

    public void trueKill() {
        kill();
        trueKill = true;
    }

    public void win() {
        win = true;
        SoundEngine.playSound("win_level.ogg");
        velocityX *= 0.8f;
        velocityY *= 0.8f;
        currentAnimation = winAnimation;
        winAnimation.setAnimationSpeed(8);
    }

    public boolean warpingOut() {
        return currentAnimation == winAnimation && currentAnimation.getFrame() >= 17;
    }

    public void touchingTile(Level game, Rectangle playerHitbox, Tile tile, Rectangle tileHitbox, Set<Tile> newLastTouchedTiles) {
        if (tile.hasTag("multi_hitbox")) {
        }
        boolean splash = true;
        touchingTile(game, tile);
        if (tile.hasTag("death")) {
            if (playerHitbox.overlaps(tileHitbox)) kill();
        } else if (tile.hasTag("bounce")) {
            if (velocityY < 30 && !lastTouchedTiles.contains(tile)) SoundEngine.playSound("bounce.ogg");
            if (velocityY < -36) velocityY *= -1.5f;
            else if (velocityY < 36) velocityY = 36;
        }
        if (tile.hasTag("water")) {
            for (Tile otherTile : lastTouchedTiles) {
                if (otherTile.hasTag("water")) splash = false;
            }
            if (splash) {
                if (velocityMagnitude() > 24) {
                    SoundEngine.playSound("splash.ogg");
                } else {
                    SoundEngine.playSound("splish.ogg");
                }
            }
        }
        if (tile.hasTag("checkpoint") && tile.tileType % 2 == 0) {
            SoundEngine.playSound("checkpoint.ogg");
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
                game.addParticle(new Particle(x + width / 4, y + height / 4, (float) ((Math.random() - 0.5) * -16), (float) ((Math.random() - 0.5) * -16),
                        24, 24, 3, 3, 1, 0.96f, 0f, (int) (Math.random() * 2), 0, "particles/twinkle.png", 120));
            }
            win();
        }
        if (tile.hasTag("pickup")) {
            if (playerHitbox.overlaps(tileHitbox)) {
                game.inGameRemoveTile(tile);
                if (tile.hasTag("dialogue")) {
                    String key = tile.extra.trim();
                    Dialogue dialogue = Dialogues.getDialogue(key);
                    if (dialogue != null)
                        game.setDialogue(dialogue);
                    return;
                }
                if (tile.hasTag("coin")) {
                    SoundEngine.playSound("coin.ogg");
                    coinCounts[tile.tileType]++;
                    if (shouldRenderCoinCounts[tile.tileType] && game.getVolatileTileCount("coin", tile.tileType) == 0) {
                        SoundEngine.playSound("all_coins_collected.ogg");
                        game.notify("notify_all_coins", tile.tileType);
                    }
                }
                if (tile.hasTag("powerup")) {
                    SoundEngine.playSound("powerup_get.ogg");
                    if (tile.tileType == 0) powerup = new Powerup(this);
                    else if (tile.tileType == 1) powerup = new BallMode(this);
                    else if (tile.tileType == 2) powerup = new WingedMode(this);
                    else if (tile.tileType == 3) powerup = new CelesteMode(this);
                    else if (tile.tileType == 4) powerup = new GravityMode(this);
                    else if (tile.tileType == 5) powerup = new StoneMode(this);
                    else if (tile.tileType == 6) powerup = new GunMode(this);
                }
                if (tile.hasTag("key")) {
                    SoundEngine.playSound("coin.ogg");
                    if (tile.hasTag("evil")) {
                        evilKey = new EvilKey(tile.x, tile.y);
                        hasEvilKey = true;
                        return;
                    }
                    keyCount++;
                }
                if (tile.hasTag("timer")) {
                    SoundEngine.playSound("powerup_get.ogg");
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
        powerup.touchingTile(tile);
    }

    public Point getPreviousCenter(int ticksBehind) {
        if (ticksBehind < previousPositions.size()) return new Point(previousPositions.get(ticksBehind).x + width / 2, previousPositions.get(ticksBehind).y + height / 2);
        return new Point (0, 0);
    }

    public void drawTrail(Graphics g) {
        boolean speedy = false;
        float speed = 0;
        if (trailSpeed > 0) {
            speed = trailSpeed;
            trailSpeed = 0;
            speedy = true;
        } else {
            for (float f : previousSpeeds) {
                if (f > 24) {
                    if (f > speed) speed = f;
                    speedy = true;
                }
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

            float[] vertices = new float[18];

            for (int i = 0; i < 9; i++) {
                vertices[i * 2] = trail[0][i];
                vertices[i * 2 + 1] = trail[1][i] + 8;
            }

            g.setColor(new Color(1, 1, 1, Math.min(1, Math.max(0, (int) speed - 24) * 2 / 255f)));
            g.drawMesh(vertices);
            g.resetColor();
        }
    }

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
        drawTrail(g);
        powerup.preDrawPlayer(g, game);
        drawPlayer(g, game);
    }

    public void renderHUD(Graphics g, Level game) {
        int drawnCoins = 0;
        for (int i = 0; i < totalCoinCounts.length; i++) {
            int total = totalCoinCounts[i];
            if (shouldRenderCoinCounts[i]) {
                g.drawString(coinCounts[i] + "/" + total, LittleH.borderedFont, -MainMenu.relZeroX() - 48, -MainMenu.relZeroY() - 48 - 48 * drawnCoins + 32 - 70 - 18, LittleH.defaultFontScale, 1);
                g.drawImage(Images.getImage("ui/coins.png"), new Rectangle(-MainMenu.relZeroX() - 40, -MainMenu.relZeroY() - 48 - 48 * drawnCoins - 64 - 12, 32, 40), new Rectangle(0, 5 * i, 4, 5));
                drawnCoins++;
            }
        }
    }

    public void drawPlayer(Graphics g, Level game) {
        powerup.drawPlayer(g, game);
    }

    public void touchingEnemy(Enemy enemy) {
        powerup.touchingEnemy(enemy);
    }

    public class EvilKey {
        public Rectangle hitbox;
        public float keyVelX, keyVelY;
        public int startUp;

        public EvilKey(int tileX, int tileY) {
            hitbox = new Rectangle(tileX * 64 + 16, tileY * 64, 48, 48);
            startUp = 120;
        }

        public void update(Player player, Level game) {
            if (startUp > 0) {
                if (startUp % 4 == 0) {
                    game.addParticle(new Particle(hitbox.x + hitbox.width / 2 - 24, hitbox.y + hitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -6), (float) ((Math.random() - 0.5) * -6), 32, 32, 4, 4, 1, 0.98f, 0f,
                            (int) (Math.random() * 4), 0, "particles/evil_smoke.png", 60, 0.02f));
                }
                startUp--;
                return;
            }
            if (Math.random() > 0.95) {
                game.addParticle(new Particle(hitbox.x + hitbox.width / 2 - 24, hitbox.y + hitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -6), (float) ((Math.random() - 0.5) * -6), 32, 32, 4, 4, 1, 0.98f, 0f,
                        (int) (Math.random() * 4), 0, "particles/evil_smoke.png", 30, 0.02f));
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