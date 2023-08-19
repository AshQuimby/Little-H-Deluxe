package com.sab.littleh.util;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;

public class TypingBox extends TypingQuery {
    private boolean focused;

    public TypingBox(String description, String startingValue, Rectangle box) {
        super(description, startingValue, box);
    }

    public TypingBox(String startingValue, Rectangle box) {
        super("", startingValue, box);
    }

    @Override
    public void mouseClicked() {
        focused = rectangle.contains(MouseUtil.getMousePosition());
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
    public void render(Graphics g) {
        g.drawPatch(Patch.get(focused ? "menu_flat" : "menu_flat_dark"), rectangle, 8);
        Rectangle textRect = new Rectangle(rectangle);
        textRect.x += 16;
        textRect.y += 16;
        textRect.width -= 32;
        textRect.height -= 32;
        String display = focused ? getDisplayQuery() : getQuery();
        while (Fonts.getStringWidth(display, LittleH.font, LittleH.defaultFontScale * 0.825f) > textRect.width) {
            if (focused) {
                display = display.substring(1);
            } else {
                display = display.substring(0, display.length() - 1);
            }
        }
        g.drawString(display, LittleH.font, textRect, 8, LittleH.defaultFontScale * 0.825f, -1, 0);
        g.drawString(getPrompt(), LittleH.font, textRect.x - 12, textRect.y + 14, LittleH.defaultFontScale * 0.825f, 1);
    }
}
