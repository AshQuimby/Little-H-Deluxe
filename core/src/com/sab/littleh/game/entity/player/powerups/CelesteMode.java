package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.util.*;

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
        stamina = 240;
    }

    @Override
    public void jump(Level game) {
        if (player.touchingWater) {
            if (ControlInputs.isJustPressed(Control.JUMP) || ControlInputs.isJustPressed(Control.UP)) {
                player.velocityY = 14;
                SoundEngine.playSound("swim.ogg");
            }
            return;
        }
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
            dash = new Vector2(28, 0);
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
            if (game.mapData.getValue("double_jumping").asBool())
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
            if (player.touchingWall && !(ControlInputs.isPressed(Control.JUMP) && player.velocityY > 0 && (player.currentAnimation != climbAnimation || player.currentAnimation != clingAnimation))) {
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
            player.trailSpeed = 64;
            if (dashTime % 3 == 0) {
                game.addParticle(new Particle(player.x, player.y, 0, 0, 64, 64, 8, 8, player.direction, 0, 0, 0, 0, "player/h_trail.png", 20, 0.05f));
            }
            noGravity = true;
            player.velocityX = dash.x;
            player.velocityY = dash.y;

            Vector2 mathDash = new Vector2(22, 0);
            float rotation = getDashRotation();
            mathDash.rotateDeg(rotation);
            dash = mathDash.add(dash.scl(19)).scl(0.05f);

            dashTime--;

            player.currentAnimation = dashAnimation;
        } else {
            noGravity = false;
        }

        if (!player.crouched) {
            move();
        }
    }

    // TODO: Make celeste h draw hair
}
