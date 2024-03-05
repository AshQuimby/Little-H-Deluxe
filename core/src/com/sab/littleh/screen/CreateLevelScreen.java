package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.settings.ListSetting;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class CreateLevelScreen extends Screen {
    List<TypingBox> inputFields;
    List<ScreenButton> buttons;
    public CreateLevelScreen(String levelName) {
        LittleH.setTitle(" | Creating a New Level");
        inputFields = new ArrayList<>();
        buttons = new ArrayList<>();

        inputFields.add(new TypingBox("Level Name ", levelName, new Rectangle(-1024 / 2 + 228, 208, 256, 64)));
        inputFields.add(new TypingBox("Author Name ", Settings.localSettings.authorName.value, new Rectangle(-1024 / 2 + 228, 92, 256, 64)));

        buttons.add(new ScreenButton("button", "Create", new Rectangle(-176 - 64, -256, 176, 72), () -> {
            LoadingUtil.startLoading(() -> LevelLoader.createLevel(getName(), getFileName(), getAuthorName(), ((SettingButton) buttons.get(2)).setting.asRawValue()));
        }));

        buttons.add(new ScreenButton("button", "Back", new Rectangle(64, -256, 176, 72), () -> {
            program.switchScreen(new LevelSelectScreen());
        }));

        buttons.add(new SettingButton(new ListSetting("background", "Background", 0, Level.backgrounds, new String[] {
                "Woodlands",
                "Summit",
                "Desert",
                "Cave",
                "Tundra",
                "Hyperspace"
        }), 128, 48));
    }

    public String getName() {
        return inputFields.get(0).getQuery();
    }

    public String getAuthorName() {
        return inputFields.get(1).getQuery();
    }

    public String getFileName() {
        String tryName = inputFields.get(0).getQuery().toLowerCase().replace(" ", "_");
        return tryName;
    }

    @Override
    public void update() {
        buttons.forEach(ScreenButton::update);
    }

    @Override
    public void keyDown(int keycode) {
        inputFields.forEach(field -> field.updateQueryKey(keycode, 20, false));
    }

    @Override
    public void keyTyped(char character) {
        inputFields.forEach(field -> field.updateQueryChar(character, 20));
    }

    @Override
    public void mouseUp(int button) {
        inputFields.forEach(TypingBox::mouseClicked);
        buttons.forEach(ScreenButton::mouseClicked);
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle screenPanel = new Rectangle(-1024 / 2, -576 / 2, 1024, 576);
        g.drawPatch(Patch.get("menu"), screenPanel, 8);

        inputFields.forEach(field -> field.render(g));
        buttons.forEach(button -> button.render(g));

        SettingButton backgroundButton = (SettingButton) buttons.get(2);

        Vector2 backgroundPosition = new Vector2(backgroundButton.getCenterX() - 256 / 2, backgroundButton.y + 64);

        g.drawPatch(Patch.get("menu"), backgroundPosition.x - 8, backgroundPosition.y - 16, 256 + 16, 144 + 24, 8);
        g.draw(Images.getImage("backgrounds/" + backgroundButton.setting.asRawValue() + "/whole.png"), backgroundPosition.x, backgroundPosition.y, 256, 144);

        g.drawString("File name: \n " + getFileName(), LittleH.font, -1024 / 2 + 228, 200, LittleH.defaultFontScale * 0.7f, 0);
    }
}
