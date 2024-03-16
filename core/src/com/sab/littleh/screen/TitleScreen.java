package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.overworld.OverworldScreen;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.game.level.editor.NewLevelEditorScreen;
import com.sab.littleh.settings.SettingsScreen;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class TitleScreen extends Screen {
    public static final TitleScreen titleScreen = new TitleScreen();
    protected Menu<ScreenButton> buttonMenu;
    protected List<ScreenButton> buttons;

    public TitleScreen() {
        Rectangle buttonRect = new Rectangle(0, 0, 640, 80);;
        buttons = new ArrayList<>();
        buttons.add(new ScreenButton("button", "Play Campaign", buttonRect, () -> {
//            Client client = new Client();
//            program.switchScreen(client.getScreen());
            program.switchScreen(new OverworldScreen());
        }));
//        buttons.get(0).setDisabled(true);
        buttons.add(new ScreenButton("button", "Level Editor", buttonRect, () -> {
            program.switchScreen(new LevelSelectScreen());
        }));
        buttons.add(new ScreenButton("button", "Settings", buttonRect, () -> {
            program.switchScreen(new SettingsScreen(this));
        }));
        buttons.add(new ScreenButton("button", "Quit", buttonRect, () -> {
            System.exit(0);
        }));
        buttons.add(new ScreenButton("button", "Testing", buttonRect, () -> {
            LittleH.pendingScreen = new NewLevelEditorScreen(null, LevelLoader.readInternalLevel("meadows.map"));
        }));
    }

    @Override
    public void start() {
        buttonMenu = new Menu<>(buttons.toArray(new ScreenButton[0]), 640, 80, 32);
        buttonMenu.setScreenRectangle(-320 - 32, (int) buttonMenu.getScreenRectangle().height / 2, (80 + 32) * buttons.size(), false);
        Cursors.switchCursor("cursor");
    }

    @Override
    public void update() {
        buttonMenu.setScreenRectangle(-320 - 32, (int) buttonMenu.getScreenRectangle().height / 2, (80 + 32) * buttons.size(), false);
        Rectangle[] itemButtons = buttonMenu.getItemButtons();
        for (int i = 0; i < buttonMenu.items.length; i++) {
            buttonMenu.items[i].setPosition(itemButtons[i].x, itemButtons[i].y);
        }
        buttonMenu.forEach(ScreenButton::update);
    }

    @Override
    public void keyDown(int keycode) {
    }

    @Override
    public void mouseUp(int button) {
        buttonMenu.forEach(ScreenButton::mouseClicked);
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        buttonMenu.forEach(button -> button.render(g, 8 ));
    }
}
