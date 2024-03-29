package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.enemy.Enemy;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.net.NetPlayer;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

public class Powerup {
    protected Player player;
    protected boolean noGravity;
    protected boolean swappedGravityThisFrame;

    public Powerup(Player player) {
        this.player = player;
        init(player);
    }

    public void init(Player player) {
        player.image = "player/h";
        if (!(player instanceof NetPlayer))
            Cursors.switchCursor("none");
    }

    public void jump(Level game) {
        if (player.touchingWater) {
            if (player.controller.isJustPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP))) {
                player.velocityY = 14 * -player.getGravityMagnitude();
                SoundEngine.playSound("swim.ogg");
            }
            return;
        }
        if (player.crushed) return;
        if (player.leftGroundFor < 6) {
            SoundEngine.playSound("jump.ogg");
            player.velocityY = 8 * -player.getGravityMagnitude();
            player.leftGroundFor = 8;
            player.jumpStrength++;
            game.addParticle(new Particle(player.x - 24, player.getFeetY() - (player.flippedGravity ? 8 : 0), 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
            onJump(false);
        } else if (player.jumpReleased && player.leftWallFor < 6) {
            SoundEngine.playSound("double_jump.ogg");
            player.velocityY = 26 * -player.getGravityMagnitude();
            player.leftWallFor = 6;
            player.x += -2 * player.wallDirection;
            player.velocityX = -16 * player.wallDirection;
            if (18 > player.maxGroundSpeed) {
                player.maxGroundSpeed = 18;
            }
        } else if (player.jumpReleased && player.doubleJump && (player.bonusDoubleJump || game.mapData.getValue("double_jumping").asBool())) {
            game.addParticle(new Particle(player.x, player.getFeetY() - (player.flippedGravity ? 8 : 0), 0f, 0f, 48, 32, 6, 4, 1, 0f, 0f, 0, 2, "particles/double_jump.png", 9));
            SoundEngine.playSound("double_jump.ogg");
            if (player.fallingFasterThan(16)) player.coolRoll = (float) (Math.PI * 3);
            if (player.fallingFasterThan(-25)) player.velocityY = 25 * -player.getGravityMagnitude();
            else player.velocityY += 14f * -player.getGravityMagnitude();
            player.doubleJump = false;
            onJump(true);
        }
    }

    public void onJump(boolean airJump) {

    }

    public void update(Level game) {
        if (!player.jumpReleased && (player.controller.isPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP))) && player.jumpStrength > 0 && player.jumpStrength < 8 || player.jumpStrength > 0 && player.jumpStrength < 5) {
            player.jumpStrength++;
            player.velocityY += 3.5f * -player.getGravityMagnitude();
        } else {
            player.jumpStrength = 0;
        }
        if (player.touchingGround) {
            player.leftGroundFor = 0;
//            if (game.mapSettings[Level.ALLOW_AIR_JUMP]) doubleJump = true;
            player.doubleJump = true;
            if (player.controller.isPressed(Controls.LEFT) ^ player.controller.isPressed(Controls.RIGHT)) {
                player.currentAnimation = player.runAnimation;
            } else {
                player.currentAnimation = player.idleAnimation;
            }
            if (player.crouched) {
                if (Math.abs(player.velocityX) > 2f) {
                    player.currentAnimation = player.slideAnimation;
                } else {
                    player.currentAnimation = player.crouchAnimation;
                }
            }
        } else {
            player.leftGroundFor++;
            if (player.falling()) {
                player.currentAnimation = player.fallAnimation;
            } else {
                player.currentAnimation = player.jumpAnimation;
            }
            if (player.touchingWall) {
                if (player.falling()) {
                    if (!player.slippery) player.velocityY *= 0.85f;
                    player.currentAnimation = player.wallSlideAnimation;
                }
                player.doubleJump = true;
                player.leftWallFor = 0;
                player.wallDirection = player.direction;
            } else {
                player.leftWallFor++;
            }
        }

        player.coolRoll *= 0.85f;
        if (player.coolRoll > 0.025f) player.coolRoll -= 0.025f;
        if (Math.abs(player.velocityX) < 2f) player.coolRoll = 0;

        if (player.controller.isPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP))) {
            player.jump(game);
            player.jumpReleased = false;
        } else {
            player.jumpReleased = true;
        }
        if (!player.crouched) {
            move();
        }
        swappedGravityThisFrame = false;
    }

    public void move() {
        if (player.controller.isPressed(Controls.LEFT)) {
            if (!player.touchingWall) player.direction = -1;
            player.velocityX -= 1.2f * (player.touchingWater ? 0.5f : 1);
        }

        if (player.controller.isPressed(Controls.RIGHT)) {
            if (!player.touchingWall) player.direction = 1;
            player.velocityX += 1.2f * (player.touchingWater ? 0.5f : 1);
        }
    }

    public void onCollision(boolean horizontal, boolean vertical) {
        if (horizontal) {
            player.velocityX = 0;
        }
        if (vertical) {
            if (player.fallingFasterThan(32)) {
                SoundEngine.playSound("hit.ogg");
                player.coolRoll = (float) (Math.PI * 4);
            }
            player.velocityY = 0;
        }
    }

    public void touchingTile(Tile tile) {
        if (!swappedGravityThisFrame && tile.hasTag("gravity_swapper")) {
            boolean flip = true;
            for (Tile other : player.lastTouchedTiles) {
                if (other.tileType == tile.tileType && other.hasTag("gravity_swapper") && other.tileType % 2 == 1 && other.tileType == tile.tileType) {
                    flip = false;
                    break;
                }
            }
            if (flip) {
                if (!player.flippedGravity && tile.tileType / 2 == 0) {
                    swappedGravityThisFrame = true;
                    player.flippedGravity = true;
                    SoundEngine.playSound("gravity_swap.ogg");
                } else if (player.flippedGravity && tile.tileType / 2 == 1) {
                    swappedGravityThisFrame = true;
                    player.flippedGravity = false;
                    SoundEngine.playSound("gravity_swap.ogg");
                } else if (tile.tileType / 2 == 2) {
                    swappedGravityThisFrame = true;
                    player.flippedGravity = !player.flippedGravity;
                    SoundEngine.playSound("gravity_swap.ogg");
                }
            }
        }
    }

    public void updateVelocity() {
        if (player.touchingWater) {
            player.velocityX *= 0.94f;
            player.velocityY *= 0.94f;
            player.velocityY += 0.3f * player.getGravityMagnitude();
            return;
        }
        if (player.leftGroundFor < 4) {
            player.maxGroundSpeed = Math.abs(player.velocityX);
        }
        if (!(player.slippery && player.crouched)) {
            if (player.touchingGround || player.maxGroundSpeed < Math.abs(player.velocityX)) {
                player.velocityX *= 0.92f;
            } else {
                player.velocityX *= 0.95f;
            }
        } else {
            player.velocityX *= 0.98f;
        }
        player.velocityY *= 0.98f;
        if (!noGravity)
            player.velocityY += 1.2f * player.getGravityMagnitude();

        if (player.touchingGround && player.controller.isPressed(Controls.RIGHT) == player.controller.isPressed(Controls.LEFT)) {
            if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
        }
    }

    public void preDrawPlayer(Graphics g, Level game) {
        player.rotation = player.velocityX * (player.touchingGround ? 1f / 96f : 1f / 128f) * (-player.velocityY / 16f + 1f);
        player.rotation -= player.coolRoll * player.direction;
        if (player.crouched || player.win) player.rotation = 0;
    }

    public void drawPlayer(Graphics g, Level game) {
        g.setColor(Images.getHColor());

        g.drawImage(Images.getImage(player.image + "_color.png"), new Rectangle(player.x - 8, player.y - (player.flippedGravity ? player.crouched ? 40 : 16 : 0), 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame + (player.flippedGravity ? 8 : 0), (player.direction == 1 ? 8 : -8), player.flippedGravity ? -8 : 8),
                -MathUtils.radiansToDegrees * player.rotation);
        g.resetColor();

        g.drawImage(Images.getImage(player.image + ".png"), new Rectangle(player.x - 8, player.y - (player.flippedGravity ? player.crouched ? 40 : 16 : 0), 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame + (player.flippedGravity ? 8 : 0), (player.direction == 1 ? 8 : -8), player.flippedGravity ? -8 : 8),
                -MathUtils.radiansToDegrees * player.rotation);
    }

    public void touchingEnemy(Enemy enemy) {
        player.kill();
    }

    public boolean onCollide(Level game, Rectangle entityHitbox, Rectangle tileHitbox, Tile tile, boolean yCollision) {
        if (tile.hasTag("key_box")) {
            if (tile.hasTag("evil")) {
                if (player.evilKeys.size() > 0) {
                    player.evilKeys.remove(0);
                    for (int i = 0; i < 4; i++) {
                        game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -20), (float) (Math.random() * -10), 32, 32, 4, 4, 1, 0.98f, 0f, i, 0, "particles/evil_key_box_rubble.png", 30));
                    }
                    SoundEngine.playSound("hit.ogg");
                    game.inGameRemoveTile(tile);
                    return false;
                }
            } else if (player.keyCount > 0) {
                player.keyCount--;
                for (int i = 0; i < 4; i++) {
                    game.addParticle(new Particle(tileHitbox.x + tileHitbox.width / 2 - 16, tileHitbox.y + tileHitbox.height / 2 - 16, (float) ((Math.random() - 0.5) * -8), (float) (Math.random() * -10), 32, 32, 4, 4, 1, 0.98f, 1.2f, i, 0, "particles/key_box_rubble.png", 30));
                }
                SoundEngine.playSound("hit.ogg");
                game.inGameRemoveTile(tile);
                return false;
            }
        }
        if (tile.hasTag("slippery")) {
            player.slippery = true;
        }
        if (yCollision) {
            if (player.falling()) {
                player.touchingGround = true;
            }
            if (tile.hasTag("slippery")) {
                player.slippery = true;
            }
        } else {
            if (game.mapData.getValue("wall_sliding").asBool() && !tile.hasTag("slick")) {
                player.touchingWall = true;
            }
        }
        return true;
    }
}