package com.sab.littleh.campaign.visual_novel.menu;

import com.badlogic.gdx.Input;
import com.sab.littleh.LittleH;
import com.sab.littleh.mainmenu.MainMenu;
import com.sab.littleh.mainmenu.MenuButton;
import com.sab.littleh.mainmenu.TitleMenu;
import com.sab.littleh.util.*;

import java.util.ArrayList;
import java.util.List;

public class VnTitleMenu extends MainMenu {
    private List<MenuButton> buttons;
    static {
        Patch.cachePatch("vn_menu",
                new Patch("campaign/visual_novel/menu/menu.png", 9, 9, 3, 3));
        Patch.cachePatch("vn_menu_indented",
                new Patch("campaign/visual_novel/menu/menu_indented.png", 9, 9, 3, 3));
        Patch.cachePatch("vn_menu_globbed",
                new Patch("campaign/visual_novel/menu/menu_globbed.png", 9, 9, 3, 3));
        Patch.cachePatch("vn_menu_hollow",
                new Patch("campaign/visual_novel/menu/menu_hollow.png", 9, 9, 3, 3));
        Patch.cacheButtonPatch("vn_button", "campaign/visual_novel/menu/button/button");
        Patch.cacheButtonPatch("vn_button_full", "campaign/visual_novel/menu/button/button_full");
    }
    private ParallaxBackground background;
    public VnTitleMenu() {
        buttons = new ArrayList<>();
        background = new ParallaxBackground("campaign/visual_novel/backgrounds/city", true);
        int i = 1;
        buttons.add(new MenuButton("vn_button", "Start Game", -384 / 2, -32 - 96 * i, 384, 64, () -> {
//            program.switchMenu(new VnDialogueMenu("city", "peepee.dlg", this));
//            program.switchMenu(new VnOverworldMenu());
        }));
        i++;
        buttons.add(new MenuButton("vn_button", "To Main Title Screen", -384 / 2, -32 - 96 * i, 384, 64, () -> {
            program.switchMenu(TitleMenu.titleScreen);
        }));
    }

    @Override
    public void start() {
        SoundEngine.playMusic("themes/dating_theme.ogg");
    }

    @Override
    public void update() {
        LittleH.program.dynamicCamera.targetPosition.x += 16;
        buttons.forEach(button -> button.update());
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ENTER) {

        }
    }

    @Override
    public void mouseUp(int button) {
        buttons.forEach(menuButton -> menuButton.mouseClicked());
    }

    @Override
    public void render(Graphics g) {
        LittleH.program.useDynamicCamera();

        background.render(g);

        LittleH.program.useStaticCamera();

        g.setColor(Images.getHColor(0, -0.75f, 0));
        g.draw(Images.getImage("campaign/visual_novel/title_color.png"), -624 / 2, -relZeroY() - 680 - 16, 624, 680);
        g.setColor(Images.getHColor(0, -0.5f, 0));
        g.draw(Images.getImage("campaign/visual_novel/title_color_aa.png"), -624 / 2, -relZeroY() - 680 - 16, 624, 680);
        g.resetColor();
        g.draw(Images.getImage("campaign/visual_novel/title.png"), -624 / 2, -relZeroY() - 680 - 16, 624, 680);

        buttons.forEach(button -> button.render(g, 4));
    }
}
