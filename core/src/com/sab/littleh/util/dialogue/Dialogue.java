package com.sab.littleh.util.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;
import com.sun.tools.javac.Main;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Dialogue {
   protected Menu<MenuButton> dialogueOptions;
   protected boolean started;
   private Map<String, Integer> breakPoints;
   private String[] text;
   private int atPosition;
   private Font font;
   private String[] characterNames;
   private String[] fileNames;
   private String lastBlock;
   private int atBlock;
   private int waitFor;
   private int blipTimer;
   private boolean changedBlock;
   private boolean stop;
   private float blockSpeed;
   private float characterFillup;
   protected int autoTime;

   public Dialogue(String[] text, String[] characterNames, String[] fileNames, Map<String, Integer> breakPoints) {
      this.breakPoints = breakPoints;
      this.text = text;
      atPosition = 0;
      atBlock = 0;
      this.characterNames = characterNames;
      this.fileNames = fileNames;
      waitFor = 0;
      lastBlock = "";
      blockSpeed = 1f;
      blipTimer = 0;
      autoTime = 0;
      started = true;
   }
   
   public String getPortrait() {
      return fileNames[Integer.parseInt(text[atBlock].substring(0, 1)) - 1];
   }

   public void setDialogueOptions(String[] options, String[] breakPoints) {
      MenuButton[] buttons = new MenuButton[options.length];
      for (int i = 0; i < options.length; i++) {
         final int finalI = i;
         buttons[i] = new MenuButton("button", options[i], 0, 0, 512, 64, () -> {
            runCommand("gB", breakPoints[finalI]);
            dialogueOptions = null;
         });
      }
      dialogueOptions = new Menu<MenuButton>(buttons, 512, 64, 8);
      update();
   }

   public void update() {
      if (dialogueOptions != null) {
         dialogueOptions.setMenuRectangle(0, 0, 73 * 2, false);
         dialogueOptions.setCenterX(0);
         dialogueOptions.setCenterY(MainMenu.relZeroY() + 196 + 48 + dialogueOptions.items.length / 2 * 32);
         dialogueOptions.forEach(MenuButton::update);
      }
   }

   public boolean mouseUp() {
      if (dialogueOptions != null)
         dialogueOptions.forEach(MenuButton::mouseClicked);
      else
         return true;
      return false;
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
         return;
      }
      reset();
   }

   public void reset() {
      changedBlock = true;
      lastBlock = "";
      blockSpeed = 1;
      characterFillup = 0;
      atBlock++;
      atPosition = 0;
      blipTimer = 0;
      autoTime = 0;
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

   protected String getLastBlock() {
      return lastBlock;
   }

   public String next(boolean playBlip) {
      if (!started) {
         autoTime++;
         if (Settings.localSettings.autoDialogue.value) {
            if (autoTime > 60) {
               started = true;
               autoTime = 0;
            }
         }
         return lastBlock;
      }
      if (finishedBlock()) {
         autoTime++;
         if (Settings.localSettings.autoDialogue.value && dialogueOptions == null) {
            if (!(atBlock + 1 >= text.length || text[atBlock + 1].trim().equals("1\\eD(true)"))) {
               if (autoTime > 120) {
                  nextBlock();
               }
            }
         }
         return lastBlock;
      }
      changedBlock = false;
      characterFillup += blockSpeed * Settings.localSettings.dialogueSpeed.asFloat();
      while (characterFillup >= 1f) {
         characterFillup--;
         if (waitFor > 0 || finishedBlock()) {
            waitFor -= blockSpeed;
            return lastBlock;
         }

         atPosition++;

         String next = text[atBlock].substring(Math.max(atPosition, 1), atPosition + 1);

         if (playBlip && !next.equals("\\") && !next.equals(" ")) {
            if ((blipTimer % (int) (2 + 2 * blockSpeed)) == 0 || next.equals("."))
               SoundEngine.playSound("blip.ogg");
            blipTimer++;
         }

         // VnDialogue commands
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

   protected void runCommand(String command, String parameter) {
      switch (command) {
         // playSound (path)
         case "pS" :
            SoundEngine.playSound(parameter);
            break;
         // playMusic (path)
         case "pM" :
            SoundEngine.playMusic(parameter);
            break;
         // waitFor (ticks)
         case "wF" :
            waitFor = Integer.parseInt(parameter);
            break;
         // endBlock (silent)
         case "eB" :
            if (!Boolean.parseBoolean(parameter))
               SoundEngine.playSound("blip.ogg");
            toEnd();
            nextBlock();
            break;
         // endDialogue (silent)
         case "eD" :
            stop = true;
            break;
         // goFast (letters/tick)
         case "gF" :
            blockSpeed = Float.parseFloat(parameter);
                 break;
         // characterName (id)
         case "cN" :
            lastBlock += characterNames[Integer.parseInt(parameter)] + ": ";
                 break;
         // killPlayer (end dialogue)
         case "kP" :
            if (Boolean.parseBoolean(parameter)) {
               while (!finished()) {
                  toEnd();
                  nextBlock();
               }
            }
            if (Level.currentLevel != null && Level.currentLevel.inGame()) {
               Level.currentLevel.player.kill();
            }
                 break;
         // crashGame (crash message)
         case "cG" :
            throw new RuntimeException(parameter);

         // dialogueTree (option1, breakPoint1, option2, breakPoint2...)
         case "dT" :
            String[] parameters = parameter.split("\\|");
            for (int i = 0; i < parameters.length; i++) {
               parameters[i] = parameters[i].trim();
            }
            if (parameters.length % 2 != 0)
               malformedCommand(command, "dialogueTree command must have an even number of parameters");
            int count = parameters.length / 2;
            String[] options = new String[count];
            String[] breakPointArray = new String[count];
            for (int i = 0; i < count; i++) {
               String text = parameters[i * 2];
               options[i] = text;
               String breakPoint = parameters[i * 2 + 1];
               breakPointArray[i] = breakPoint;
            }
            setDialogueOptions(options, breakPointArray);
            break;
         // go(to)Breakpoint (breakPoint)
         case "gB" :
            atBlock = breakPoints.get(parameter);
            reset();
            break;
         default :
            malformedCommand(command, "Command does not exist");
            break;
      }
   }

   public void malformedCommand(String command, String reason) {
      throw new RuntimeException("Malformed command at: " + lastBlock + "<. Command: " + command + ". " + reason + ".");
   }

   public boolean changedBlock() {
      return changedBlock;
   }

   public boolean shouldEnd() {
      return stop;
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

      if (dialogueOptions != null) {
         Rectangle[] buttons = dialogueOptions.getItemButtons();

         for (int i = 0; i < dialogueOptions.items.length; i++) {
            dialogueOptions.getItem(i).set(buttons[i]);
         }
         dialogueOptions.forEach(menuButton -> menuButton.render(g));
      }

      if (finishedBlock() && autoTime > 120) {
         LittleH.borderedFont.setColor(new Color(1, 1, 1, MathUtils.sinDeg(autoTime * 2) / 2f + 0.5f));
         if (finished()) {
            g.drawString("Press 'select' (enter) to end dialogue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
         } else {
            g.drawString("Press 'select' (enter) to continue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
         }
      } else if (!started) {
         LittleH.borderedFont.setColor(new Color(1, 1, 1, MathUtils.sinDeg(LittleH.getTick() * 2) / 2f + 0.5f));
         g.drawString("Press 'select' (enter) to start dialogue", LittleH.borderedFont, -MainMenu.relZeroX() - 4, MainMenu.relZeroY() + 16, LittleH.defaultFontScale * 0.75f, 1);
      }
      LittleH.borderedFont.setColor(Color.WHITE);
   }

   public boolean started() {
      return started;
   }

   public void start() {
      started = true;
   }

   public void stop() {
      started = false;
   }
}