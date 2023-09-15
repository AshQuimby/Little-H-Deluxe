package com.sab.littleh.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Control;
import com.sab.littleh.controls.ControlTypingBox;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.settings.SettingButton;
import com.sab.littleh.settings.Settings;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class ControlsMenu extends MainMenu {
    private SettingsMenu settingsMenu;
    private List<ControlTypingBox> inputFields;
    private List<MenuButton> buttons;
    private Vector2 previousMousePos;
    private float scroll;
    private float effectiveScroll;
    public ControlsMenu(SettingsMenu settingsMenu) {
        this.settingsMenu = settingsMenu;
        inputFields = new ArrayList<>();
        buttons = new ArrayList<>();
        int y = 128;
        for (Control control : Controls.getControls()) {
            ControlTypingBox typingBox = new ControlTypingBox(control, new Rectangle(0, y, 256, 64));
            inputFields.add(typingBox);
            y -= 96;
        }
        buttons.add(new MenuButton("square_button", "Save & Return", -420 / 2 - 256, -640 / 2 - 32, 420, 64, () -> {
            LittleH.program.switchMenu(settingsMenu);
            for (ControlTypingBox typingBox : inputFields) {
                typingBox.save();
            }
            Controls.save();
        }));
        buttons.add(new MenuButton("square_button", "Reset to Defaults", -420 / 2 + 256, -640 / 2 - 32, 420, 64, () -> {
            Controls.resetControls();
            Controls.save();
            inputFields.clear();
            int i = 128;
            for (Control control : Controls.getControls()) {
                ControlTypingBox typingBox = new ControlTypingBox(control, new Rectangle(0, i, 256, 64));
                inputFields.add(typingBox);
                i -= 96;
            }
        }));
        previousMousePos = new Vector2();
    }

    @Override
    public void start() {
    }

    @Override
    public void mouseScrolled(float amountY) {
        if (scroll + amountY <= 0)
            amountY = -scroll;

        scroll += amountY * 32;

        if (scroll < 0) {
            amountY = 0;
            scroll = 0;
        }

        int n = 0;
        for (TypingBox typingBox : inputFields) {
            typingBox.rectangle.y = scroll - 96 * n + 96;
            n++;
        }
    }

    @Override
    public void update() {
        if (MouseUtil.isLeftMouseDown()) {
            effectiveScroll += (MouseUtil.getMousePosition().y - previousMousePos.y) / 16;
        }
        mouseScrolled(effectiveScroll / 8f);
        effectiveScroll *= 7/8f;
        inputFields.forEach(typingBox -> typingBox.update());
        buttons.forEach(typingBox -> typingBox.update());
        previousMousePos = MouseUtil.getMousePosition();
    }
    @Override
    public void keyDown(int keycode) {
        inputFields.forEach(typingBox -> typingBox.updateQueryKey(keycode, 6, false));
    }

    @Override
    public void keyTyped(char character) {
    }

    @Override
    public void mouseDown(int button) {
    }

    @Override
    public void mouseUp(int button) {
        inputFields.forEach(typingBox -> typingBox.mouseClicked());
        buttons.forEach(typingBox -> typingBox.mouseClicked());
    }

    @Override
    public void render(Graphics g) {
        super.render(g);

        Rectangle menuPanel = new Rectangle(-1024 / 2, -576 / 2, 1024, 576);
        g.drawPatch(Patch.get("menu"), menuPanel, 8);

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        Rectangle mask = new Rectangle();
        mask.set(menuPanel);
        ScissorStack.calculateScissors(LittleH.program.staticCamera, g.getTransformMatrix(), menuPanel, mask);
        boolean pop = ScissorStack.pushScissors(mask);

        g.drawString("Click on a field to change the control, backspace to remove one\nBackspace can only be set if no other controls are",
                LittleH.font, 0, scroll + 128 + 96 + 24, LittleH.defaultFontScale * 0.8f, 0);

        for (TypingBox typingBox : inputFields) {
            typingBox.render(g);
        }
        g.drawPatch(Patch.get("menu_hollow"), menuPanel, 8);

        if (pop)
            ScissorStack.popScissors();

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        for (MenuButton button : buttons) {
            button.render(g);
        }
    }
}
