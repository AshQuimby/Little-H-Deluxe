package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;

import java.awt.*;

public class CelesteMode extends Powerup {
    private static Animation climbAnimation = new Animation(12, 8, 27);
    private static Animation clingAnimation = new Animation(1, 8);
    private static Animation dashAnimation = new Animation(1, 28);
    private int stamina;
    private int dashTime;
    private Vector2 dash;

    public CelesteMode(Player player) {
        super(player);
    }

    @Override
    public void init(Player player) {
        player.image = "player/celeste_h";
        dashTime = 0;
    }

    @Override
    public void jump(Level game) {
        if (player.crushed) return;
        if (player.touchingGround && dashTime > 0) {
            dashTime = 0;
            player.maxGroundSpeed = 32;
            player.velocityX = 32 * player.direction;
        }
        if (player.leftGroundFor < 6) {
            SoundEngine.playSound("jump.ogg");
            player.velocityY = 8;
            player.leftGroundFor = 8;
            player.jumpStrength++;
            game.addParticle(new Particle(player.x - 24, player.y, 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
        } else if (player.leftWallFor < 8 && ControlInputs.isJustPressed(Control.JUMP)) {
            SoundEngine.playSound("double_jump.ogg");
            player.velocityY = 13;
            player.leftWallFor = 8;
            player.x += -2 * player.wallDirection;
            player.velocityX = -24 * player.wallDirection;
        } else if (player.doubleJump && game.mapData.getValue("double_jumping").asBool() && ControlInputs.isJustPressed(Control.JUMP)) {
            SoundEngine.playSound("dash.ogg");
            player.doubleJump = false;
            dash = new Vector2(24, 0);
            float rotation = getDashRotation();
            dash.rotateDeg(rotation);
            dashTime = 15;
        }
    }

    public float getDashRotation() {
        float rotation = 90f;
        if (ControlInputs.isPressed(Control.UP)) {
            rotation = 90f;
        } else if (ControlInputs.isPressed(Control.DOWN)) {
            rotation = 270f;
        }

        if (ControlInputs.isPressed(Control.RIGHT) ^ ControlInputs.isPressed(Control.LEFT)) {
            float x = 90f;
            if (ControlInputs.isPressed(Control.DOWN) ^ ControlInputs.isPressed(Control.UP)) {
                x *= 0.5f;
                if (ControlInputs.isPressed(Control.DOWN)) {
                    x *= -1f;
                }
            }
            if (ControlInputs.isPressed(Control.RIGHT)) {
                rotation -= x;
            }
            if (ControlInputs.isPressed(Control.LEFT)) {
                rotation += x;
            }
        }

        return rotation;
    }

    @Override
    public void update(Level game) {
        if (!player.jumpReleased && (ControlInputs.isPressed(Control.JUMP) || ControlInputs.isPressed(Control.UP)) && player.jumpStrength > 0 && player.jumpStrength < 8 || player.jumpStrength > 0 && player.jumpStrength < 5) {
            player.jumpStrength++;
            player.velocityY += 3.5f;
        } else {
            player.jumpStrength = 0;
        }

        if (player.touchingGround) {
            player.leftGroundFor = 0;
            if (dashTime > 0) {
                if (ControlInputs.isPressed(Control.JUMP))
                    player.jump(game);
                dashTime = 0;
            }
            stamina = 240;
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
            if (player.touchingWall && !ControlInputs.isPressed(Control.JUMP)) {
                player.currentAnimation = clingAnimation;
                dashTime = 0;
                if (stamina > 0) {
                    if (ControlInputs.isPressed(Control.LEFT) ^ ControlInputs.isPressed(Control.RIGHT)) {
                        stamina--;
                        if (player.wallDirection == player.direction) {
                            if (ControlInputs.isPressed(Control.DOWN)) {
                                player.currentAnimation = climbAnimation;
                                player.velocityY = 0;
                            } else if (ControlInputs.isPressed(Control.UP)) {
                                player.currentAnimation = climbAnimation;
                                player.velocityY = 2.8f;
                            } else {
                                player.velocityY = 1.2f;
                            }
                        } else {
                            player.velocityY = 0;
                        }
                    }
                } else {
                    player.velocityY *= 0.8f;
                }
                player.leftWallFor = 0;
                player.wallDirection = player.direction;
            } else {
                player.leftWallFor++;
            }
        }

        player.coolRoll *= 0.85f;
        if (player.coolRoll > 0.025f) player.coolRoll -= 0.025f;
        if (Math.abs(player.velocityX) < 2f) player.coolRoll = 0;

        if ((ControlInputs.isPressed(Control.JUMP) || ControlInputs.isPressed(Control.UP))) {
            if (player.jumpReleased && !(player.touchingWall && !player.touchingGround) || ControlInputs.isJustPressed(Control.JUMP))
                player.jump(game);
            player.jumpReleased = false;
        } else {
            player.jumpReleased = true;
        }

        if (dashTime > 0) {
            noGravity = true;
            player.velocityX = dash.x;
            player.velocityY = dash.y;

            Vector2 mathDash = new Vector2(20, 0);
            float rotation = getDashRotation();
            mathDash.rotateDeg(rotation);
            dash = mathDash.add(dash.scl(19)).scl(0.05f);

            dashTime--;

            player.currentAnimation = dashAnimation;
        } else {
            noGravity = false;
        }

        if (!player.crouched) {
            if (ControlInputs.isPressed(Control.LEFT)) {
                if (!player.touchingWall) player.direction = -1;
                player.velocityX -= 1.2f;
            }

            if (ControlInputs.isPressed(Control.RIGHT)) {
                if (!player.touchingWall) player.direction = 1;
                player.velocityX += 1.2f;
            }
        }
    }

    @Override
    public void preDrawPlayer(Graphics g, Level game) {
        if (dashTime > 0) {
            if (player.previousPositions.size() >= 10) {
                for (int i = 0; i < 5; i++) {
                    Point pos = player.previousPositions.get(i * 2);
                    g.setColor(new Color(1, 1, 1, Math.min(1f / i, 1)));
                    if (player.direction == 1)
                        g.draw(Images.getImage("player/h_trail.png"), pos.x - 8, pos.y, 64, 64);
                    else
                        g.draw(Images.getImage("player/h_trail.png"), pos.x - 8 + 64, pos.y, -64, 64);
                }
                g.resetColor();
            }
        }
        super.preDrawPlayer(g, game);
    }

    // TODO: Make celeste h draw hair
}
