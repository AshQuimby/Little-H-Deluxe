package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.tile.Tile;
import com.sab.littleh.util.*;

public class Powerup {
    protected Player player;
    protected boolean noGravity;

    public Powerup(Player player) {
        this.player = player;
        init(player);
    }

    public void init(Player player) {
        player.image = "player/h";
    }

    public void jump(Level game) {
        if (player.swimming) {
            if (ControlInputs.isJustPressed(Control.JUMP) || ControlInputs.isJustPressed(Control.UP)) {
                player.velocityY = 14;
                SoundEngine.playSound("swim.ogg");
            }
            return;
        }
        if (player.crushed) return;
        if (player.leftGroundFor < 6) {
            SoundEngine.playSound("jump.ogg");
            player.velocityY = 8;
            player.leftGroundFor = 8;
            player.jumpStrength++;
            game.addParticle(new Particle(player.x - 24, player.y, 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
        } else if (player.jumpReleased && player.leftWallFor < 8) {
            SoundEngine.playSound("double_jump.ogg");
            player.velocityY = 26;
            player.leftWallFor = 8;
            player.x += -2 * player.wallDirection;
            player.velocityX = -16 * player.wallDirection;
        } else if (player.jumpReleased && player.doubleJump && game.mapData.getValue("double_jumping").asBool()) {
            game.addParticle(new Particle(player.x, player.y, 0f, 0f, 48, 32, 6, 4, 1, 0f, 0f, 0, 2, "particles/double_jump.png", 9));
            SoundEngine.playSound("double_jump.ogg");
            if (player.velocityY < -16) player.coolRoll = (float) (Math.PI * 3);
            if (player.velocityY < 25) player.velocityY = 25;
            else player.velocityY += 14f;
            player.doubleJump = false;
        }
    }

    public void update(Level game) {
        if (!player.jumpReleased && (ControlInputs.isPressed(Control.JUMP) || ControlInputs.isPressed(Control.UP)) && player.jumpStrength > 0 && player.jumpStrength < 8 || player.jumpStrength > 0 && player.jumpStrength < 5) {
            player.jumpStrength++;
            player.velocityY += 3.5f;
        } else {
            player.jumpStrength = 0;
        }
        if (player.touchingGround) {
            player.leftGroundFor = 0;
//            if (game.mapSettings[Level.ALLOW_AIR_JUMP]) doubleJump = true;
            player.doubleJump = true;
            if (ControlInputs.isPressed(Control.LEFT) ^ ControlInputs.isPressed(Control.RIGHT)) {
                player.currentAnimation = Player.runAnimation;
            } else {
                if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
                player.currentAnimation = Player.idleAnimation;
            }
            if (player.crouched) {
                if (Math.abs(player.velocityX) > 2f) {
                    player.currentAnimation = Player.slideAnimation;
                } else {
                    player.currentAnimation = Player.crouchAnimation;
                }
            }
        } else {
            player.leftGroundFor++;
            if (player.velocityY < 0) {
                player.currentAnimation = Player.fallAnimation;
            } else {
                player.currentAnimation = Player.jumpAnimation;
            }
            if (player.touchingWall) {
                if (player.velocityY < 0) {
                    if (!player.slippery) player.velocityY *= 0.85f;
                    player.currentAnimation = Player.wallSlideAnimation;
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

        if (ControlInputs.isPressed(Control.JUMP) || ControlInputs.isPressed(Control.UP)) {
            player.jump(game);
            player.jumpReleased = false;
        } else {
            player.jumpReleased = true;
        }
        if (!player.crouched) {
            if (ControlInputs.isPressed(Control.LEFT)) {
                if (!player.touchingWall) player.direction = -1;
                player.velocityX -= 1.2f * (player.swimming ? 0.5f : 1);
            }

            if (ControlInputs.isPressed(Control.RIGHT)) {
                if (!player.touchingWall) player.direction = 1;
                player.velocityX += 1.2f * (player.swimming ? 0.5f : 1);
            }
        }
    }

    public void onCollision(boolean horizontal, boolean vertical) {
        if (horizontal) {
            player.velocityX = 0;
        }
        if (vertical) {
            if (player.velocityY < -32) {
                SoundEngine.playSound("hit.ogg");
                player.coolRoll = (float) (Math.PI * 4);
            }
            player.velocityY = 0;
        }
    }

    public void touchingTile(Tile tile) {

    }

    public void updateVelocity() {
        if (player.swimming) {
            player.velocityX *= 0.94f;
            player.velocityY *= 0.94f;
            player.velocityY -= 0.3f;
            return;
        }
        if (player.touchingGround) {
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
            player.velocityY -= 1.2f;
    }

    public void preDrawPlayer(Graphics g, Level game) {
        player.rotation = player.velocityX * (player.touchingGround ? 1f / 96f : 1f / 128f) * (-player.velocityY / 16f + 1f);
        player.rotation -= player.coolRoll * player.direction;
        if (player.crouched || player.win) player.rotation = 0;
    }

    public void drawPlayer(Graphics g, Level game) {
        g.setColor(Images.getHColor());

        g.drawImage(Images.getImage(player.image + "_color.png"), new Rectangle(player.x - 8, player.y, 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame, (player.direction == 1 ? 8 : -8), 8),
                -MathUtils.radiansToDegrees * player.rotation);
        g.resetColor();

        g.drawImage(Images.getImage(player.image + ".png"), new Rectangle(player.x - 8, player.y, 64, 64),
                new Rectangle((player.direction == 1 ? 0 : 8), 8 * player.frame, (player.direction == 1 ? 8 : -8), 8),
                -MathUtils.radiansToDegrees * player.rotation);
    }
}