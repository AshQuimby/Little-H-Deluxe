package com.sab.littleh.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.screen.ScreenButton;
import com.sab.littleh.util.*;
import com.sab.littleh.util.Graphics;

public class SettingButton extends ScreenButton {
    private static final int BOOL = 0;
    private static final int LIST = 1;
    private static final int PERCENT = 2;
    private static final int STRING = 3;
    public Setting setting;
    private String bonusText;
    private int type;
    private boolean held;
    private boolean drawText;

    public SettingButton(Setting setting, int x, int y, int width, boolean drawText) {
        this(setting, x, y, width);
        this.drawText = drawText;
    }

    public SettingButton(Setting setting, int x, int y, int width) {
        this(setting, x, y);
        this.width = width;
    }

    public SettingButton(Setting setting, int x, int y, String bonusText) {
        super(null, setting.name, x, y, 0, 0);
        drawText = true;
        this.setting = setting;
        this.bonusText = bonusText;
        type = getSettingType();
        switch (type) {
            case BOOL :
                width = 72;
                height = 36;
                break;
            case LIST :
                width = 280;
                height = 8;
                break;
            case PERCENT :
                width = 256;
                height = 32;
                break;
            case STRING :
                width = 128;
                height = 64;
                break;
        }
    }

    public SettingButton(Setting setting, int x, int y) {
        this(setting, x, y, null);
    }

    public int getSettingType() {
        if (setting instanceof BoolSetting)
            return 0;
        else if (setting instanceof ListSetting)
            return 1;
        else if (setting instanceof PercentageSetting)
            return 2;
        else if (setting instanceof StringSetting)
            return 3;
        return -1;
    }

    @Override
    public void update() {
        super.update();
        switch (type) {
            case PERCENT :
                if (held) {
                    PercentageSetting percentageSetting = (PercentageSetting) setting;
                    float original = percentageSetting.value;
                    float percent = (MouseUtil.getMouseX() - x - 12) / (width - 24);
                    percent = Math.min(1f, Math.max(0f, percent));
                    percentageSetting.value = Math.round(percentageSetting.getMinValue() + percent * (percentageSetting.getMaxValue() - percentageSetting.getMinValue()));
                    float dV = original - percentageSetting.value;
                    if (dV != 0f)
                        SoundEngine.resetCurrentMusicVolume();
                }
                break;
        }
        if (!pressed)
            held = false;
    }

    @Override
    public void mouseDown() {
        if (hovered)
            held = true;
    }

    @Override
    public void mouseClicked() {
        if (contains(MouseUtil.getMousePosition())) {
            if (type == BOOL) {
                ((BoolSetting) setting).next();
                SoundEngine.playSound("blip.ogg");
            }
        }
        if (type == LIST) {
            ListSetting listSetting = (ListSetting) setting;
            if (new Rectangle(x - 64, y - 12, 36, 36).contains(MouseUtil.getMousePosition())) {
                SoundEngine.playSound("blip.ogg");
                listSetting.previous();
            }
            if (new Rectangle(x + 64 - 36 + width, y - 12, 36, 36).contains(MouseUtil.getMousePosition())) {
                SoundEngine.playSound("blip.ogg");
                listSetting.next();
            }
        }
    }

    public void render(Graphics g, int patchScale, float fontScale) {
        LittleH.font.setColor(disabled ? Color.GRAY : Color.WHITE);
        switch (type) {
            case BOOL :
                boolean value = ((BoolSetting) setting).value;
                Rectangle bounds = new Rectangle(x - 6, y - 6, width + 12, height + 12);
                g.drawPatch(Patch.get("menu_flat"), bounds, 3);
                if (pressed && hovered) {
                    g.draw(Images.getImage("ui/buttons/slider_pressed.png"), x, y, width, height);
                } else if (value) {
                    g.draw(Images.getImage("ui/buttons/slider_on.png"), x, y, width, height);
                } else {
                    g.draw(Images.getImage("ui/buttons/slider_off.png"), x, y, width, height);
                }
                break;
            case PERCENT :
                PercentageSetting percentageSetting = (PercentageSetting) setting;
                bounds = new Rectangle(x - 3, y, width + 6, height + 3);
                g.drawPatch(Patch.get("menu_flat"), bounds, 3);
                g.draw(Images.getImage("ui/buttons/slider_bar.png"), x + 12, y + 12, (width - 24), 12);
                g.draw(Images.getImage("ui/buttons/slider_notch.png"), x + percentageSetting.asRelativeFloat() * (width - 24) - 18 + 12, y, 36, 36);
                if (drawText)
                    g.drawString(percentageSetting.display(), LittleH.font, getCenterX(), getCenterY() - 32, LittleH.defaultFontScale * 0.67f, 0);
                break;
            case LIST :
                ListSetting listSetting = (ListSetting) setting;
                bounds = new Rectangle(x - 24, y - 12, width + 48, height + 24);
                g.drawPatch(Patch.get("menu_flat"), bounds, 3);
                if (drawText)
                    g.drawString(listSetting.display(), LittleH.font, getCenterX(), getCenterY(), LittleH.defaultFontScale * 0.67f, 0);
                g.drawImage(Images.getImage("ui/buttons/arrow.png"), x - 64, y - 14, 36, 36, new Rectangle(0, 0, 12, 12));
                g.draw(Images.getImage("ui/buttons/arrow.png"), x + 64 - 36 + width, y - 14, 36, 36, 0 , 1, 0, true, false);
                break;
        }
        if (drawText)
            g.drawString(text, LittleH.font, getCenterX(), getCenterY() + 32, LittleH.defaultFontScale * 0.7f, 0);
        if (bonusText != null)
            if (drawText)
                g.drawString(getBonusText(), LittleH.font, getCenterX(), y - 48, LittleH.defaultFontScale * 0.6f, 0);
    }

    public String getBonusText() {
        return bonusText;
    }
}
