package com.sab.littleh.mainmenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.campaign.visual_novel.menu.VnTitleMenu;
import com.sab.littleh.controls.ControlInputs;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class TitleMenu extends MainMenu {
    public static final TitleMenu titleScreen = new TitleMenu();
    protected Menu<MenuButton> buttonMenu;
    protected List<MenuButton> buttons;

    public TitleMenu() {
        Rectangle buttonRect = new Rectangle(0, 0, 640, 80);;
        buttons = new ArrayList<>();
        buttons.add(new MenuButton("button", "Play Campaign", buttonRect, () -> {
            program.switchMenu(new VnTitleMenu());
        }));
//        buttons.get(0).setDisabled(true);
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
        buttonMenu = new Menu<>(buttons.toArray(new MenuButton[0]), 640, 80, 32);
        buttonMenu.setMenuRectangle(-320 - 32, (int) buttonMenu.getMenuRectangle().height / 2, (80 + 32) * buttons.size(), false);
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        buttonMenu.setMenuRectangle(-320 - 32, (int) buttonMenu.getMenuRectangle().height / 2, (80 + 32) * buttons.size(), false);
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

        buttonMenu.forEach(button -> button.render(g));
    }
}
