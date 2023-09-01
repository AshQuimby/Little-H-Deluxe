package com.sab.littleh.campaign.visual_novel.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.SoundEngine;
import com.sab.littleh.util.dialogue.Dialogue;

import java.awt.*;

public class VnDialogue extends Dialogue {
   public VnDialogue(String[] text, String[] characterNames, String[] fileNames) {
      super(text, characterNames, fileNames);
   }

   @Override
   protected void runCommand(String command, String parameter) {
      if (command.equals("dT")) {

      } else {
         super.runCommand(command, parameter);
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
      if (getLastBlock() != null)
         g.drawString(getLastBlock(), LittleH.font, textArea, 8, LittleH.defaultFontScale * 0.95f, -1, 1);
   }
}