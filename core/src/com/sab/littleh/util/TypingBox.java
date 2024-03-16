package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;

public class TypingBox extends TypingQuery {
    protected boolean focused;

    public TypingBox(String description, String startingValue, Rectangle box) {
        super(description, startingValue, box);
    }

    public TypingBox(String startingValue, Rectangle box) {
        super("", startingValue, box);
    }

    @Override
    public void mouseClicked() {
        focused = contains(MouseUtil.getMousePosition());
    }

    @Override
    public void updateQueryKey(int keycode, int max, boolean enterIsNewline) {
        if (focused)
            super.updateQueryKey(keycode, max, enterIsNewline);
    }

    @Override
    public void updateQueryChar(char character, int max) {
        if (focused)
            super.updateQueryChar(character, max);
    }

    public boolean isFocused() {
        return focused;
    }

    @Override
    public void render(Graphics g, int patchScale, float fontSize) {
        g.drawPatch(Patch.get(focused ? "menu_flat" : "menu_flat_dark"), this, patchScale);
        Rectangle textRect = new Rectangle(this);
        textRect.x += 16;
        textRect.y += 16;
        textRect.width -= 32;
        textRect.height -= 32;
        String display = focused ? getDisplayQuery() : getQuery();
        while (Fonts.getStringWidth(display, LittleH.font, LittleH.defaultFontScale * 0.775f) > textRect.width) {
            if (focused) {
                display = display.substring(1);
            } else {
                display = display.substring(0, display.length() - 1);
            }
        }
        g.drawString(display, LittleH.font, textRect, 8, LittleH.defaultFontScale * 0.775f * fontSize, -1, 0);
        textRect.x -= textRect.width + patchScale * 6;
        g.drawString(getPrompt(), LittleH.font, textRect, 8, LittleH.defaultFontScale * 0.775f * fontSize, 1, 0);
    }
}
