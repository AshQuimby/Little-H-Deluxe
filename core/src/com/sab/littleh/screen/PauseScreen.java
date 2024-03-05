package com.sab.littleh.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.settings.SettingsScreen;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public abstract class PauseScreen extends Screen {
    public static final Rectangle buttonRect = new Rectangle(0, 0, 512, 64);
    protected Screen screenBehind;
    protected Menu<ScreenButton> buttonMenu;
    protected List<ScreenButton> buttons;

    public PauseScreen(Screen screenBehind) {
        this.screenBehind = screenBehind;
        buttons = new ArrayList<>();
        buttons.add(new ScreenButton("button", "Return", buttonRect, () -> {
            program.switchScreen(screenBehind);
        }));
        buttons.add(new ScreenButton("button", "Settings", buttonRect, () -> {
            program.switchScreen(new SettingsScreen(this));
        }));
    }

    @Override
    public void start() {
        buttonMenu = new Menu<>(buttons.toArray(new ScreenButton[0]), 512, 64, 32);
        buttonMenu.setScreenRectangle(-256 - 32, 0, 96 * buttons.size(), false);
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        buttonMenu.setScreenRectangle(-256 - 32, (int) buttonMenu.getScreenRectangle().height / 2, 96 * buttons.size(), false);
        Rectangle[] itemButtons = buttonMenu.getItemButtons();
        for (int i = 0; i < buttonMenu.items.length; i++) {
            buttonMenu.items[i].setPosition(itemButtons[i].x, itemButtons[i].y);
        }
        buttonMenu.forEach(ScreenButton::update);
    }

    @Override
    public void keyDown(int keycode) {
        if (ControlInput.localControls.isJustPressed("return")) {
            program.switchScreen(screenBehind);
        }
    }

    @Override
    public void mouseUp(int button) {
        buttonMenu.forEach(ScreenButton::mouseClicked);
    }

    @Override
    public void render(Graphics g) {
        screenBehind.render(g);
        g.setColor(new Color(0, 0, 0, 0.5f));
        g.draw(Images.getImage("pixel.png"), relZeroX(), relZeroY(), program.getWidth(), program.getHeight());
        g.resetColor();

        buttonMenu.forEach(button -> button.render(g));
    }
}
