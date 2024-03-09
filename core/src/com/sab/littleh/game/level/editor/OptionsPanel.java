package com.sab.littleh.game.level.editor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.screen.ScreenElement;
import com.sab.littleh.screen.ScreenGui;
import com.sab.littleh.settings.*;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.TypingBox;
import com.sab.littleh.util.sab_format.SabData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionsPanel extends ScreenElement {
    private final ScreenGui<ScreenElement> buttons;
    private final List<Setting<?>> settings;
    private final SabData mapSettings;
    private final TypingBox timeLimitBox;

    public OptionsPanel(SabData mapSettings) {
        super(-400, -256, 800, 512);
        buttons = new ScreenGui<>();
        this.mapSettings = mapSettings;
        settings = new ArrayList<>();

        BoolSetting airJump = new BoolSetting("double_jumping", "Air Jumping", mapSettings.getValue("double_jumping").asBool());
        settings.add(airJump);
        BoolSetting wallSliding = new BoolSetting("wall_sliding", "Wall Sliding", mapSettings.getValue("wall_sliding").asBool());
        settings.add(wallSliding);
        BoolSetting crouching = new BoolSetting("crouching", "Crouching", mapSettings.getValue("crouching").asBool());
        settings.add(crouching);
        ListSetting background = new ListSetting("background", "Background", Level.indexOfBackground(
                mapSettings.getValue("background").getRawValue()), Level.backgrounds, new String[]{
                "Woodlands",
                "Summit",
                "Desert",
                "Cave",
                "Tundra",
                "Hyperspace"
        });
        settings.add(background);
        StringSetting timeLimit = new StringSetting("time_limit", "Time Limit", mapSettings.getValue("time_limit").getRawValue());
        String string = timeLimit.value;
        if (string.equals("-1")) timeLimit.value = "";
        settings.add(timeLimit);

        buttons.add(new SettingButton(airJump, (int) x + 64, (int) y + 240));
        buttons.add(new SettingButton(wallSliding, (int) x + 64, (int) y + 144));
        buttons.add(new SettingButton(crouching, (int) x + 64, (int) y + 48));
        buttons.add(new SettingButton(background, (int) (x + width) - 360, (int) y + 40));
        timeLimitBox = new TypingBox("Time Limit: ", timeLimit.value, new Rectangle(x + 162, y + height - 68, 256, 52));
        buttons.add(timeLimitBox);
    }

    @Override
    public void update() {
        buttons.update();
    }

    public void keyDown(int keycode) {
        timeLimitBox.updateQueryKey(keycode, 3, false);
    }
    public void keyTyped(char character) {
        timeLimitBox.updateQueryChar(character, 3, "([0-9])");
    }

    public void close(ScreenGui<ScreenElement> container) {
        String string = ((TypingBox) buttons.get(4)).getQuery();
        if (string.isBlank()) string = "-1";
        ((StringSetting) settings.get(4)).value = string;

        for (Setting<?> setting : settings) {
            mapSettings.insertValue(setting.id, setting.asRawValue());
        }
        container.queueRemove(this);
    }

    @Override
    public void mouseClicked() {
        buttons.mouseClicked();
    }
    @Override
    public void render(Graphics g, int patchScale, float fontScale) {
        g.drawPatch(Patch.get("menu_flat"), this, patchScale);
        buttons.render(g, patchScale, fontScale);

        SettingButton backgroundButton = (SettingButton) buttons.get(3);
        Vector2 backgroundPosition = new Vector2(backgroundButton.getCenterX() - 320f / 2f, backgroundButton.y + 64);

        g.drawPatch(Patch.get("menu_flat"), backgroundPosition.x - patchScale, backgroundPosition.y - patchScale,
                320f + patchScale * 2, 180 + patchScale * 2, patchScale);
        g.draw(Images.getImage("backgrounds/" + backgroundButton.setting.asRawValue() + "/whole.png"),
                backgroundPosition.x, backgroundPosition.y, 320f, 180);

    }
}
