package com.sab.littleh.util.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.SoundEngine;
import com.sun.tools.javac.Main;

import java.awt.Font;

public class Dialogue {
   private String[] text;
   private int atPosition;
   private Font font;
   private String[] characterNames;
   private String[] fileNames;
   private String lastBlock;
   private int atBlock;
   private int waitFor;
   private int blipTimer;
   private boolean finished;
   private float blockSpeed;
   private float characterFillup;

   public Dialogue(String[] text, String[] characterNames, String[] fileNames) {
      this.text = text;
      atPosition = 0;
      atBlock = 0;
      this.characterNames = characterNames;
      this.fileNames = fileNames;
      waitFor = 0;
      finished = false;
      lastBlock = "";
      blockSpeed = 1f;
      blipTimer = 0;
   }
   
   public String getPortrait() {
      return fileNames[Integer.parseInt(text[atBlock].substring(0, 1)) - 1];
   }
   
   public void toEnd() {
      waitFor = 0;
      while (!finishedBlock()) {
         next(false);
      }
   }
   
   public String getName() {
      return characterNames[Integer.parseInt(text[atBlock].substring(0, 1)) - 1];
   }
   
   public void nextBlock() {
      if (atBlock + 1 >= text.length) {
         finished = true;
         return;
      }
      lastBlock = "";
      blockSpeed = 1;
      atBlock++;
      atPosition = 0;
      blipTimer = 0;
   }
   
   public boolean finishedBlock() {
      return atPosition >= text[atBlock].length() - 1;
   }
   
   public boolean finished() {
      return atBlock >= text.length - 1 && finishedBlock();
   }
   
   public String next() {
      return next(true);
   }

   public String next(boolean playBlip) {
      characterFillup += blockSpeed;
      while (characterFillup >= 1f) {
         characterFillup--;
         if (waitFor > 0 || finishedBlock()) {
            waitFor -= blockSpeed;
            return lastBlock;
         }

         atPosition++;

         String next = text[atBlock].substring(Math.max(atPosition, 1), atPosition + 1);

         if (playBlip && !next.equals("\\") && !next.isBlank() && Character.isAlphabetic(next.charAt(0))) {
            if (blipTimer % 4 == 0)
               SoundEngine.playSound("blip.ogg");
            blipTimer++;
         }

         // Dialogue commands
         if (next.equals("\\")) {
            String command = getNext() + getNext();
            int parens = 0;
            if (getNext().equals("(")) parens++;
            else malformedCommand(command, "No parentheses");
            String parameter = "";
            while (parens > 0) {
               String shortNext = null;
               try {
                  shortNext = getNext();
               } catch (Exception e) {
                  malformedCommand(command, "Parentheses never closed");
               }
               if (shortNext.equals("(")) parens++;
               else if (shortNext.equals(")")) parens--;
               else parameter += shortNext;
            }
            runCommand(command, parameter);
         } else {
            lastBlock += next;
         }
      }
      return lastBlock;
   }

   public String getNext() {
      atPosition++;
      String next = text[atBlock].substring(Math.max(atPosition, 1), atPosition + 1);
      return next;
   }

   private void runCommand(String command, String parameter) {
      switch (command) {
         // playSound (path)
         case "pS" -> {
            SoundEngine.playSound(parameter);
         }
         // playMusic (path)
         case "pM" -> {
            SoundEngine.playMusic(parameter);
         }
         // waitFor (ticks)
         case "wF" -> {
            waitFor = Integer.parseInt(parameter);
         }
         // endBlock (silent)
         case "eB" -> {
            if (!Boolean.parseBoolean(parameter))
               SoundEngine.playSound("blip.ogg");
            toEnd();
            nextBlock();
         }
         // goFast (letters/tick)
         case "gF" -> {
            blockSpeed = Float.parseFloat(parameter);
         }
         // characterName (id)
         case "cN" -> {
            lastBlock += characterNames[Integer.parseInt(parameter)] + ": ";
         }
         // killPlayer (end dialogue)
         case "kP" -> {
            if (Boolean.parseBoolean(parameter)) {
               while (!finished()) {
                  toEnd();
                  nextBlock();
               }
            }
            if (Level.currentLevel != null && Level.currentLevel.inGame()) {
               Level.currentLevel.player.kill();
            }
         }
         default -> {
            malformedCommand(command, "Command does not exist");
         }
      }
   }

   public void render(Graphics g) {
      g.drawPatch(Patch.get("menu"), new Rectangle(MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 192), 8);

      if (getPortrait().endsWith("player")) {
         g.setColor(Images.getHColor());
         g.draw(Images.getImage(getPortrait() + "_color.png"), MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 192);
         g.resetColor();
         g.draw(Images.getImage(getPortrait() + ".png"), MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 192);
      } else {
         g.draw(Images.getImage(getPortrait()), MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 192);
      }
      g.setColor(new Color(0, 0, 0, 0.5f));
      g.draw(Images.getImage("pixel.png"), MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 48);
      g.resetColor();

      g.drawPatch(Patch.get("menu_hollow"), new Rectangle(MainMenu.relZeroX(), MainMenu.relZeroY(), 192, 192), 8);

      g.drawString(getName(), LittleH.font, MainMenu.relZeroX() + 192 / 2, MainMenu.relZeroY() + 32, LittleH.defaultFontScale * 0.9f, 0);

      Rectangle textArea = new Rectangle(MainMenu.relZeroX() + 192, MainMenu.relZeroY(), LittleH.program.getWidth() - 192, 192);
      g.drawPatch(Patch.get("menu_globbed"), textArea, 8);
      textArea.y -= 32;
      textArea.x += 16;
      textArea.width -= 32;
      if (lastBlock != null)
         g.drawString(lastBlock, LittleH.font, textArea, 8, LittleH.defaultFontScale * 0.95f, -1, 1);
   }

   public static void malformedCommand(String command, String reason) {
      throw new RuntimeException("Malformed command: " + command + ". " + reason + ".");
   }
}