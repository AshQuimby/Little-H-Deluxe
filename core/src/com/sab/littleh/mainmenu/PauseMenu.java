package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PauseMenu extends MainMenu {
    public static final Rectangle buttonRect = new Rectangle(0, 0, 512, 64);
    protected MainMenu menuBehind;
    protected Menu<MenuButton> buttonMenu;
    protected List<MenuButton> buttons;

    public PauseMenu(MainMenu menuBehind) {
        this.menuBehind = menuBehind;
        buttons = new ArrayList<>();
        buttons.add(new MenuButton("button", "Return", buttonRect, () -> {
            program.switchMenu(menuBehind);
        }));
        buttons.add(new MenuButton("button", "Settings", buttonRect, () -> {
            program.switchMenu(new SettingsMenu(this));
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
        if (keycode == Input.Keys.ESCAPE) {
            program.switchMenu(menuBehind);
        }
    }

    @Override
    public void mouseUp(int button) {
        buttonMenu.forEach(MenuButton::mouseClicked);
    }

    @Override
    public void render(Graphics g) {
        menuBehind.render(g);
        g.setColor(new Color(0, 0, 0, 0.5f));
        g.draw(Images.getImage("pixel.png"), relZeroX(), relZeroY(), program.getWidth(), program.getHeight());
        g.resetColor();

        buttonMenu.forEach(button -> button.render(g));
    }
}
