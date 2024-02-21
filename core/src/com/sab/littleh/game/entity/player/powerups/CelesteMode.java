package com.sab.littleh.game.entity.player.powerups;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

public class CelesteMode extends Powerup {
    private Animation climbAnimation = new Animation(12, 8, 27);
    private Animation clingAnimation = new Animation(1, 8);
    private Animation dashAnimation = new Animation(1, 28);
    private int stamina;
    private int dashTime;
    private Vector2 dash;

    public CelesteMode(Player player) {
        super(player);
    }

    @Override
    public void init(Player player) {
        super.init(player);
        player.image = "player/celeste_h";
        dashTime = 0;
        stamina = 240;
    }

    @Override
    public void jump(Level game) {
        if (player.touchingWater) {
            if (player.controller.isJustPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP))) {
                player.velocityY = 14 * -player.getGravityMagnitude();
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
            player.velocityY = 8 * -player.getGravityMagnitude();
            player.leftGroundFor = 8;
            player.jumpStrength++;
            game.addParticle(new Particle(player.x - 24, player.getFeetY() - (player.flippedGravity ? 8 : 0), 0f, 0f, 96, 16, 12, 2, 1, 0f, 0f, 0, 2, "particles/jump.png", 9));
        } else if (player.leftWallFor < 6 && player.controller.isJustPressed(Controls.JUMP)) {
            SoundEngine.playSound("double_jump.ogg");
            player.velocityY = 13 * -player.getGravityMagnitude();
            player.leftWallFor = 6;
            player.x += -2 * player.wallDirection;
            player.velocityX = -24 * player.wallDirection;
        } else if (player.doubleJump && (player.bonusDoubleJump || game.mapData.getValue("double_jumping").asBool()) && player.controller.isJustPressed(Controls.JUMP)) {
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
        if (player.flippedGravity && !(player.controller.isPressed(Controls.RIGHT) || player.controller.isPressed(Controls.LEFT)))
            rotation = 270f;
        if (player.controller.isPressed(Controls.UP)) {
            rotation = 90f;
        } else if (player.controller.isPressed(Controls.DOWN)) {
            rotation = 270f;
        }

        if (player.controller.isPressed(Controls.RIGHT) ^ player.controller.isPressed(Controls.LEFT)) {
            float x = 90f;
            if (player.controller.isPressed(Controls.DOWN) ^ player.controller.isPressed(Controls.UP)) {
                x *= 0.5f;
                if (player.controller.isPressed(Controls.DOWN)) {
                    x *= -1f;
                }
            }
            if (player.controller.isPressed(Controls.RIGHT)) {
                rotation -= x;
            }
            if (player.controller.isPressed(Controls.LEFT)) {
                rotation += x;
            }
        }

        return rotation;
    }

    @Override
    public void update(Level game) {
        if (!player.jumpReleased && (player.controller.isPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP))) && player.jumpStrength > 0 && player.jumpStrength < 8 || player.jumpStrength > 0 && player.jumpStrength < 5) {
            player.jumpStrength++;
            player.velocityY += 3.5f * -player.getGravityMagnitude();
        } else {
            player.jumpStrength = 0;
        }

        if (player.touchingGround) {
            player.leftGroundFor = 0;
            if (dashTime > 0) {
                if (player.controller.isPressed(Controls.JUMP))
                    player.jump(game);
                dashTime = 0;
            }
            stamina = 240;
//            if (game.mapSettings[Level.ALLOW_AIR_JUMP]) doubleJump = true;
            if ((player.bonusDoubleJump || game.mapData.getValue("double_jumping").asBool()))
                player.doubleJump = true;
            if (player.controller.isPressed(Controls.LEFT) ^ player.controller.isPressed(Controls.RIGHT)) {
                player.currentAnimation = player.runAnimation;
            } else {
                if (!player.slippery && !player.crouched) player.velocityX *= 0.5f;
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
            if (player.velocityY < 0) {
                player.currentAnimation = player.fallAnimation;
            } else {
                player.currentAnimation = player.jumpAnimation;
            }
            if (player.touchingWall && !(player.controller.isPressed(Controls.JUMP) && player.velocityY > 0 && (player.currentAnimation != climbAnimation || player.currentAnimation != clingAnimation))) {
                player.currentAnimation = clingAnimation;
                dashTime = 0;
                if (stamina > 0) {
                    if (player.controller.isPressed(Controls.LEFT) ^ player.controller.isPressed(Controls.RIGHT)) {
                        stamina--;
                        if (player.flippedGravity) {
                            if (player.wallDirection == player.direction) {
                                if (player.controller.isPressed(Controls.UP)) {
                                    player.currentAnimation = climbAnimation;
                                    player.velocityY = 0;
                                } else if (player.controller.isPressed(Controls.DOWN)) {
                                    player.currentAnimation = climbAnimation;
                                    player.velocityY = 2.8f;
                                } else {
                                    player.velocityY = 1.2f;
                                }
                            } else {
                                player.velocityY = 0;
                            }

                        } else {
                            if (player.wallDirection == player.direction) {
                                if (player.controller.isPressed(Controls.DOWN)) {
                                    player.currentAnimation = climbAnimation;
                                    player.velocityY = 0;
                                } else if (player.controller.isPressed(Controls.UP)) {
                                    player.currentAnimation = climbAnimation;
                                    player.velocityY = 2.8f;
                                } else {
                                    player.velocityY = 1.2f;
                                }
                            } else {
                                player.velocityY = 0;
                            }
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

        if ((player.controller.isPressed(Controls.JUMP) || (Settings.localSettings.upEqualsJump.value && player.controller.isPressed(Controls.UP)))) {
            if (player.jumpReleased && !(player.touchingWall && !player.touchingGround) || player.controller.isJustPressed(Controls.JUMP))
                player.jump(game);
            player.jumpReleased = false;
        } else {
            player.jumpReleased = true;
        }

        if (dashTime > 0) {
            player.trailSpeed = 64;
            if (dashTime % 3 == 0) {
                game.addParticle(new Particle(player.x, player.y, 0, 0, 64, 64, 8, 8,
                        player.direction, 0, 0, 0, 0, "player/h_trail.png", 20, 0.05f));
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
        swappedGravityThisFrame = false;
    }

    @Override
    public void preDrawPlayer(Graphics g, Level game) {
        if (!player.warpingOut())
            drawHair(g);
        super.preDrawPlayer(g, game);
    }

    // TODO: Make celeste h draw hair pixelified

    public void drawHair(Graphics g) {
        if (player.doubleJump) {
            g.setColor(new Color(0.9f, 0.4f, 0.2f, 1f));
        } else {
            g.setColor(new Color(0.4f, 0.9f, 1f, 1f));
        }

        int[][] trail = new int[2][9];
        trail[0][0] = (int) player.x + player.width / 2;
        trail[1][0] = (int) player.y + player.height / 2 - 12 * player.getGravityDirection();
        for (int i = 1; i < 5; i++) {
            trail[0][i] = player.getPreviousCenter(i * 2 - (i == 4 ? 1 : 0)).x;
            trail[1][i] = player.getPreviousCenter(i * 2 - (i == 4 ? 1 : 0)).y - 12 * i;
        }
        double[] angles = new double[4];
        for (int i = 0; i < 4; i++) {
            angles[i] = Math.atan2(trail[1][i + 1] - trail[1][i], trail[0][i + 1] - trail[0][i]);
        }
        for (int i = 0; i < 4; i++) {
            trail[0][8 - i] = (int) (trail[0][i] + (4f * (5 - i) + 7f) * Math.cos(angles[i] + Math.PI / 2));
            trail[1][8 - i] = (int) (trail[1][i] + (4f * (5 - i) + 7f) * Math.sin(angles[i] + Math.PI / 2));
        }
        for (int i = 0; i < 4; i++) {
            trail[0][i] = (int) (trail[0][i] + (4f * (5 - i) + 7f) * Math.cos(angles[i] + Math.PI / 2 * 3));
            trail[1][i] = (int) (trail[1][i] + (4f * (5 - i) + 7f) * Math.sin(angles[i] + Math.PI / 2 * 3));
        }

        float[] vertices = new float[18];

        for (int i = 0; i < 9; i++) {
            vertices[i * 2] = trail[0][i];
            vertices[i * 2 + 1] = trail[1][i] + 8;
        }

        LittleH.program.beginTempBuffer();
        g.drawMesh(null, vertices);
//        g.drawMesh(null, vertices);
        g.resetColor();
        LittleH.program.endTempBuffer();
        g.setShader(Shaders.crushShader);
        DynamicCamera camera = LittleH.program.dynamicCamera;
        Shaders.crushShader.setUniformf("u_amount", 8f / camera.zoom);
        Shaders.crushShader.setUniform2fv("u_offset", new float[] { camera.getPosition().x - camera.targetPosition.x, camera.getPosition().y + camera.targetPosition.y }, 0, 2);
        LittleH.program.useStaticCamera();
        LittleH.program.drawTempBuffer();
        LittleH.program.useDynamicCamera();
        g.resetShader();
        
        if (player.doubleJump) {
            g.setColor(new Color(0.9f, 0.4f, 0.2f, 1f));
        } else {
            g.setColor(new Color(0.4f, 0.9f, 1f, 1f));
        }
        g.draw(Images.getImage("player/celeste_h_hair_ball.png"), player.x - 8, player.y, 64, 64);
    }
}
