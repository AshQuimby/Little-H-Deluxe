package com.sab.littleh.campaign.visual_novel.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.visual_novel.menu.VnDialogueMenu;
import com.sab.littleh.campaign.visual_novel.particle.HeartParticle;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Menu;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.SoundEngine;
import com.sab.littleh.util.dialogue.Dialogue;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class VnDialogue extends Dialogue {
   private static Patch dialogue;
   private static Patch playerDialogue;
   private static Patch playerDialogueColor;
   private static Patch playerDialogueColorAA;
   private String previousPortrait;
   private String previousText;
   public static void load() {
      dialogue = new Patch("campaign/visual_novel/menu/dialogue_box.png", 30, 24, 14, 8);
      playerDialogue = new Patch("campaign/visual_novel/menu/player_dialogue_box.png", 30, 24, 14, 8);
      playerDialogueColor = new Patch("campaign/visual_novel/menu/player_dialogue_box_color.png", 30, 24, 14, 8);
      playerDialogueColorAA = new Patch("campaign/visual_novel/menu/player_dialogue_box_color_aa.png", 30, 24, 14, 8);
   }
   public VnDialogue(String[] text, String[] characterNames, String[] fileNames, Map<String, Integer> breakPoints) {
      super(text, characterNames, fileNames, breakPoints);
      started = false;
   }

   @Override
   public void setDialogueOptions(String[] options, String[] breakPoints) {
      MenuButton[] buttons = new MenuButton[options.length];
      for (int i = 0; i < options.length; i++) {
         final int finalI = i;
         // Doesn't really support more than 4 options
         buttons[i] = new MenuButton("vn_button_full", options[i], 0, 0, (options.length % 2 != 0 && finalI == options.length - 2) ? 608 : 300, 50, () -> {
            String oldPortrait = getPortrait();
            String oldText = getLastBlock();
//            System.out.println(oldPortrait);
//            System.out.println(previousPortrait);
//            System.out.println(oldPortrait.contains("player") == previousPortrait.contains("player"));
            runCommand("gB", breakPoints[finalI]);
            dialogueOptions = null;
            if (previousPortrait == null ||
                    !(oldPortrait.contains("player") == getPortrait().contains("player"))) {
               previousPortrait = oldPortrait;
               previousText = oldText;
            }
         });
      }
      dialogueOptions = new Menu<MenuButton>(buttons, 300, 56, 8);
      update();
   }

   @Override
   public void reset() {
      super.reset();
      // Dating menus have a slower default
      runCommand("gF", "0.75");
   }

   @Override
   public void update() {
      if (dialogueOptions != null) {
         dialogueOptions.setMenuRectangle(MainMenu.relZeroX() + 20, MainMenu.relZeroY() + 32 + 160, 73 * 2, false);
         dialogueOptions.forEach(MenuButton::update);
      }
   }

   @Override
   public void nextBlock() {
      boolean update = finishedBlock();
      String oldPortrait = getPortrait();
      String oldText = getLastBlock();
      super.nextBlock();
      if (update) {
         if (previousPortrait == null ||
                 !(oldPortrait.contains("player") == getPortrait().contains("player"))) {
            previousPortrait = oldPortrait;
            previousText = oldText;
         }
      }
   }

   @Override
   public void runCommand(String command, String parameter) {
      // Spawn particle
      if (command.equals("sP")) {
         if (LittleH.program.getMenu() instanceof VnDialogueMenu) {
            VnDialogueMenu menu = (VnDialogueMenu) LittleH.program.getMenu();
            String[] params = parameter.split("\\|");
            boolean isPlayer = getPortrait().contains("player");
            String type = params[0].trim();
            int count = Integer.parseInt(params[1].trim());
            for (int i = 0; i < count; i++) {
               if (type.equals("heart")) {
                  if (isPlayer) {
                     menu.addParticle(new HeartParticle(MainMenu.relZeroX() + 64 + MathUtils.random(512), MainMenu.relZeroY() + 160 + 32 + MathUtils.random(512)));
                  } else {
                     menu.addParticle(new HeartParticle(-MainMenu.relZeroX() - 512 - 64 + MathUtils.random(512), MainMenu.relZeroY() + 160 + 32 + MathUtils.random(512)));
                  }
               }
            }
         }
      // Add (particle) spawner
      } else if (command.equals("aS")) {
         if (LittleH.program.getMenu() instanceof VnDialogueMenu) {
            VnDialogueMenu menu = (VnDialogueMenu) LittleH.program.getMenu();
            menu.addParticleSpawner(parameter);
         }
         // Clear (particle) spawners
      } else if (command.equals("cS")) {
         if (LittleH.program.getMenu() instanceof VnDialogueMenu) {
            VnDialogueMenu menu = (VnDialogueMenu) LittleH.program.getMenu();
            menu.clearParticleSpawners();
         }
      }  else {
         super.runCommand(command, parameter);
      }
   }

   public void render(Graphics g, boolean background) {
      if (!background) {
         g.resetTint();
         if (previousPortrait != null)
            render(g, true);
      } else {
         g.setTint(new Color(0.5f, 0.5f, 0.5f, 0.5f));
      }
      String portrait = background ? previousPortrait : getPortrait();
      boolean isPlayer = portrait.contains("player");
      if (isPlayer) {
         g.setColor(Images.getHColor());
         g.draw(Images.getImage(portrait + "_color.png"), MainMenu.relZeroX() + 64, MainMenu.relZeroY() + 160, 512, 512);
         g.resetColor();
         g.draw(Images.getImage(portrait + ".png"), MainMenu.relZeroX() + 64, MainMenu.relZeroY() + 160, 512, 512);
      } else {
         g.draw(Images.getImage(portrait), -MainMenu.relZeroX() - 512 - 64 + 512, MainMenu.relZeroY() + 160, -512, 512);
      }

      Rectangle textArea = new Rectangle(MainMenu.relZeroX() + (isPlayer ? 0 : 640), MainMenu.relZeroY() + 32, LittleH.program.getWidth() - 640, 160);
      if (isPlayer) {
         g.drawPatch(playerDialogue, textArea, 4);
         g.setColor(Images.getHColor(0, -0.25f, 0f));
         g.drawPatch(playerDialogueColor, textArea, 4);
         g.setColor(Images.getHColor(0, -0.67f, 0f));
         g.drawPatch(playerDialogueColorAA, textArea, 4);
         g.resetColor();
      } else {
         g.drawPatch(dialogue, textArea, 4);
      }
      textArea.y -= 32;
      textArea.x += isPlayer ? 32 : 12;
      textArea.width -= 32;
      String text = background ? previousText : getLastBlock();
      if (!background) {
         if (text != null)
            g.drawString(text, LittleH.font, textArea, 8, LittleH.defaultFontScale * 0.9f, -1, 1);
         if (isPlayer)
            g.drawString(getName(), LittleH.borderedFont, MainMenu.relZeroX() + 64 + 256, MainMenu.relZeroY() + 160 + 64, LittleH.defaultFontScale * 0.95f, 0);
         else
            g.drawString(getName(), LittleH.borderedFont, -MainMenu.relZeroX() - 512 - 64 + 256, MainMenu.relZeroY() + 160 + 64, LittleH.defaultFontScale * 0.95f, 0);
      }
      g.resetTint();

      if (dialogueOptions != null) {
         Rectangle[] buttons = dialogueOptions.getItemButtons();

         for (int i = 0; i < dialogueOptions.items.length; i++) {
            dialogueOptions.getItem(i).x = buttons[i].x;
            dialogueOptions.getItem(i).y = buttons[i].y;
         }
         dialogueOptions.forEach(menuButton -> menuButton.render(g, 4, 0.8f));
      }

      if (finishedBlock() && autoTime > 120) {
         LittleH.borderedFont.setColor(new Color(1, 1, 1, MathUtils.sinDeg(autoTime * 2) / 2f + 0.5f));
         if (finished()) {
            g.drawString("Press 'select' or click to end dialogue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
         } else {
            g.drawString("Press 'select' or click to continue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
         }
      } else if (!started) {
         LittleH.borderedFont.setColor(new Color(1, 1, 1, MathUtils.sinDeg(LittleH.getTick() * 2) / 2f + 0.5f));
         g.drawString("Press 'select' or click to start dialogue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
      }
      LittleH.borderedFont.setColor(Color.WHITE);
   }
}