package com.sab.littleh.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class TitleMenu extends MainMenu {
    public static final TitleMenu titleScreen = new TitleMenu();
    private Patch menuPatch;
    private Patch menuPatchColor;
    private Patch menuPatchColorAA;
    protected Menu<MenuButton> buttonMenu;
    protected List<MenuButton> buttons;

    public TitleMenu() {
        menuPatch = new Patch("campaign/visual_novel/menu/player_dialogue_box.png", 30, 24, 14, 8);
        menuPatchColor = new Patch("campaign/visual_novel/menu/player_dialogue_box_color.png", 30, 24, 14, 8);
        menuPatchColorAA = new Patch("campaign/visual_novel/menu/player_dialogue_box_color_aa.png", 30, 24, 14, 8);
        Rectangle buttonRect = new Rectangle(0, 0, 512, 80);;
        buttons = new ArrayList<>();
        buttons.add(new MenuButton("button", "Play Campaign", buttonRect, () -> {

        }));
        buttons.get(0).setDisabled(true);
        buttons.add(new MenuButton("button", "Level Editor", buttonRect, () -> {
            program.switchMenu(new LevelSelectMenu());
        }));
        buttons.add(new MenuButton("button", "Settings", buttonRect, () -> {
            program.switchMenu(new SettingsMenu(this));
        }));
        buttons.add(new MenuButton("button", "Quit", buttonRect, () -> {
            System.exit(0);
        }));
    }

    @Override
    public void start() {
        buttonMenu = new Menu<>(buttons.toArray(new MenuButton[0]), 512, 64, 32);
        buttonMenu.setMenuRectangle(-256, 0, 96 * buttons.size(), false);
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        buttonMenu.setMenuRectangle(-256, (int) buttonMenu.getMenuRectangle().height / 2, 96 * buttons.size(), false);
        Rectangle[] itemButtons = buttonMenu.getItemButtons();
        for (int i = 0; i < buttonMenu.items.length; i++) {
            buttonMenu.items[i].setPosition(itemButtons[i].x, itemButtons[i].y);
        }
        buttonMenu.forEach(MenuButton::update);
    }

    @Override
    public void keyDown(int keycode) {
    }

    @Override
    public void mouseUp(int button) {
        buttonMenu.forEach(MenuButton::mouseClicked);
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        g.drawPatch(menuPatch, new Rectangle(relZeroX() + 16, relZeroY() + 16, 640, 128), 4);
        g.setColor(Images.getHColor(0, -0.5f, 0f));
        g.drawPatch(menuPatchColor, new Rectangle(relZeroX() + 16, relZeroY() + 16, 640, 128), 4);
        g.setColor(Images.getHColor(0, -0.75f, 0f));
        g.drawPatch(menuPatchColorAA, new Rectangle(relZeroX() + 16, relZeroY() + 16, 640, 128), 4);
        g.resetColor();

        buttonMenu.forEach(button -> button.render(g));
    }
}
