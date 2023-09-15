package com.sab.littleh.campaign.visual_novel.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.visual_novel.dialogue.VnDialogue;
import com.sab.littleh.game.entity.Particle;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.mainmenu.TitleMenu;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.ParallaxBackground;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.dialogue.Dialogue;
import com.sab.littleh.util.dialogue.Dialogues;

import java.util.ArrayList;
import java.util.List;

public class VnDialogueMenu extends MainMenu {
    private VnDialogue dialogue;
    private ParallaxBackground background;
    private MainMenu menuBehind;
    private List<Particle> particles;
    private List<ParticleSpawner> particleSpawners;

    public VnDialogueMenu(String backgroundName, String dialogueName, MainMenu menuBehind) {
        background = new ParallaxBackground("campaign/visual_novel/backgrounds/" + backgroundName, true);
        dialogue = Dialogues.getVnDialogue("vn/" + dialogueName);
        this.menuBehind = menuBehind;
        particles = new ArrayList<>();
        particleSpawners = new ArrayList<>();
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        LittleH.program.dynamicCamera.targetPosition.x += 8;
        if (dialogue.changedBlock()) {
            particleSpawners.clear();
        }
        dialogue.update();
        dialogue.next();
        if (dialogue != null && dialogue.shouldEnd()) {
            dialogue = null;
            particleSpawners.clear();
        }
        particleSpawners.forEach(ParticleSpawner::update);
        particles.forEach(Particle::update);
        particles.removeIf(particle -> !particle.alive);

        if (dialogue == null)
            program.switchMenu(menuBehind);
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {
            doDialogue();
        }
    }

    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    @Override
    public void mouseUp(int button) {
        if (dialogue.mouseUp())
            doDialogue();
    }

    public void doDialogue() {
        if (!dialogue.started()) {
            dialogue.start();
        } else {
            if (dialogue != null) {
                if (dialogue.finished()) {
                    program.switchMenu(menuBehind);
                } else if (dialogue.finishedBlock()) {
                    dialogue.nextBlock();
                } else {
                    dialogue.toEnd();
                }
            }
        }
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        background.render(g);

        LittleH.program.useStaticCamera();

        if (dialogue != null)
            dialogue.render(g, false);
        particles.forEach(particle -> particle.render(g));
    }

    public void addParticleSpawner(String type) {
        particleSpawners.add(new ParticleSpawner(type, 60));
    }

    public void clearParticleSpawners() {
        particleSpawners.clear();
    }

    private class ParticleSpawner {
        String type;
        int delay;
        int timer;
        ParticleSpawner(String type, int delay) {
            this.type = type;
            this.delay = delay;
            timer = delay;
        }

        public void update() {
            if (++timer >= delay) {
                dialogue.runCommand("sP", type + " | 1");
                timer = 0;
            }
        }
    }
}
