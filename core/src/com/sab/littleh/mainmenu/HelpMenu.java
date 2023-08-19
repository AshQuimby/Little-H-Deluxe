package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Menu;
import com.sun.tools.javac.Main;

public class HelpMenu extends MainMenu {
    private LocalText[] helpPanels;
    private Menu<String> buttonMenu;
    // Keep this constant throughout
    private static int index;
    public HelpMenu() {
        helpPanels = new LocalText[13];
        for (int i = 0; i < helpPanels.length; i++) {
            helpPanels[i] = Localization.getLocalizedTextFile("help/" + i + ".txt");
        }
        buttonMenu = new Menu<>(Localization.getTexts(0, helpPanels), 128, 128);
        index = 0;
    }

    @Override
    public void start() {
        Gdx.graphics.setTitle(LittleH.TITLE + " | Viewing Help Menu");
    }

    @Override
    public void update() {
        buttonMenu.setElementDimensions((int) ((program.getWidth() - 64) / (float) buttonMenu.items.length), 128, 4);
        buttonMenu.setMenuRectangle(MainMenu.relZeroX(), MainMenu.relZeroY() + 128, 0, false);
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.ENTER) {
            goBack();
        }
    }

    @Override
    public void mouseUp(int button) {
        int newIndex = buttonMenu.getOverlappedElement(MouseUtil.getMousePosition());

        if (newIndex == buttonMenu.items.length - 1) {
            goBack();
            // Don't update the index;
            return;
        } else if (newIndex == buttonMenu.items.length - 2) {
            LoadingUtil.startLoading(() -> {
                InternalLevelMenu tutorial = new InternalLevelMenu(this, LevelLoader.readInternalLevel("tutorial.map"));
                LittleH.pendingMenu = tutorial;
            });
            // Don't update the index;
            return;
        }

        index = newIndex != -1 ? newIndex : index;
    }

    public void goBack() {
        program.switchMenu(new LevelSelectMenu());
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle[] buttons = buttonMenu.getItemButtons();

        for (int i = 0; i < buttons.length; i++) {
            Rectangle button = buttons[i];
            boolean hoveredButton = button.contains(MouseUtil.getMousePosition());
            boolean selectedButton = i == index;
            int yOffset = 64;

            Rectangle rect = new Rectangle(button.x - 6, -6 - yOffset + button.y + (selectedButton ? 64 : 0) - (hoveredButton ? 32 : 48), button.width + 12, button.height + 96 + 12);
            Patch.get("menu_flat").render(g, 8, rect);

            String buttonName = buttonMenu.items[i];
            int limitLength = program.getWidth() / 230;
            if (buttonName.length() > limitLength) buttonName = buttonName.substring(0, limitLength).trim() + "...";

            Vector2 center = button.getCenter(new Vector2());
            rect.y += 80;
            g.drawString(buttonName, LittleH.font, rect, 8, LittleH.defaultFontScale * 0.67f, 0, 0);
        }

        String text = helpPanels[index].getText()[1];
        Rectangle textRect = new Rectangle(MainMenu.relZeroX() + 32, MainMenu.relZeroY() + 128 + 64, program.getWidth() - 64, program.getHeight() - 128 - 128);

        g.drawPatch(Patch.get("menu_globbed"), textRect, 8);

        textRect.x += 16;
        textRect.y += 16 - 32;
        textRect.width -= 32;
        textRect.height -= 32;
        g.drawString(helpPanels[index].getText()[0], LittleH.font, 0, -MainMenu.relZeroY() - 40, LittleH.defaultFontScale * 1.2f, 0);
        g.drawString(text, LittleH.font, textRect, 8, LittleH.defaultFontScale * 0.75f, -1, 1);
    }
}
