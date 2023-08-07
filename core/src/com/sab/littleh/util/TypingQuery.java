package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.mainmenu.MenuButton;

import java.util.regex.Pattern;

public class TypingQuery {
    public final Rectangle rectangle;
    private String prompt;
    private StringBuilder query;
    private int headerPosition;
    private MenuButton acceptButton;
    private MenuButton rejectButton;
    public boolean complete;
    public boolean accepted;
    public TypingQuery(String prompt, String startingString, Rectangle rectangle, boolean hasConfirmButtons) {
        this.rectangle = rectangle;
        this.prompt = prompt;
        query = new StringBuilder(startingString);
        headerPosition = query.length();
        if (hasConfirmButtons) {
            acceptButton = new MenuButton("square_button", "Okay", rectangle.x + rectangle.width / 2 - 128 - 16, rectangle.y - 80, 128, 64,
                    () -> complete(true));
            rejectButton = new MenuButton("square_button", "Nope", rectangle.x + rectangle.width / 2 + 16, rectangle.y - 80, 128, 64,
                    () -> complete(false));
        }
    }
    public TypingQuery(String prompt, String startingString, Rectangle rectangle) {
        this(prompt, startingString, rectangle, false);
    }
    public void complete(boolean accepted) {
        this.accepted = accepted;
        complete = true;
    }
    public void updateQueryKey(int keycode, int max, boolean enterIsNewline) {
        // Move the header
        if (keycode == Input.Keys.LEFT) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                headerPosition = 0;
            else
                headerPosition = Math.max(0, headerPosition - 1);
        } else if (keycode == Input.Keys.RIGHT) {
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
                headerPosition = length();
            else
                headerPosition = Math.min(length(), headerPosition + 1);
        } else if (keycode == Input.Keys.ENTER) {
            if (query.length() < max && enterIsNewline) {
                query.insert(headerPosition, '\n');
                headerPosition++;
            }
        }
    }
    public boolean isValid(char c) {
        return c > 31 && c < 127;
    }
    public void update() {
        if (acceptButton != null) {
            acceptButton.update();
            rejectButton.update();
        }
    }
    public void updateQueryChar(char character, int max, String validRegex) {
        if (Pattern.matches(validRegex, String.valueOf(character)) || character == 0x08 || character == 0x7F) {
            updateQueryChar(character, max);
        }
    }
    public void updateQueryChar(char character, int max) {
        // Backspace
        if (character == 0x08) {
            if (!query.isEmpty()) {
                query = query.deleteCharAt(headerPosition - 1);
                headerPosition--;
            }
        // Delete
        } else if (character == 0x7F) {
            if (!query.isEmpty() && headerPosition < length()) {
                headerPosition++;
                query = query.deleteCharAt(headerPosition - 1);
                headerPosition--;
            }
        // ASCII alphanumerics and symbols
        } else if (query.length() < max && isValid(character)) {
            query.insert(headerPosition, character);
            headerPosition++;
        }
    }
    public String getQuery() {
        return query.toString();
    }
    public String getDisplayQuery() {
        if (LittleH.getTick() / 30 % 2 == 0) {
            return new StringBuilder(query).insert(headerPosition, "<").toString();
        }
        return new StringBuilder(query).insert(headerPosition, "_").toString();
    }
    public int length() {
        return query.length();
    }
    public void mouseClicked() {
        if (acceptButton != null) {
            acceptButton.mouseClicked();
            rejectButton.mouseClicked();
        }
    }
    public void render(Graphics g) {
        g.drawPatch(Patch.get("menu"), rectangle, 8);
        Rectangle textRect = new Rectangle(rectangle);
        textRect.x += 16;
        textRect.y += 16;
        textRect.width -= 32;
        textRect.height -= 32;
        g.drawString(prompt + getDisplayQuery(), LittleH.font, rectangle, 8, LittleH.defaultFontScale * 0.825f, 0, 0);
        if (acceptButton != null) {
            acceptButton.render(g);
            rejectButton.render(g);
        }
    }
}
