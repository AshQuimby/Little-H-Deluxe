package com.sab.littleh.campaign.overworld;

import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.SaveFile;
import com.sab.littleh.mainmenu.*;

public class OverworldPauseMenu extends PauseMenu {

    public OverworldPauseMenu(OverworldMenu overworldMenu) {
        super(overworldMenu);
        buttons.add(new MenuButton("button", "To Main Menu", buttonRect, () -> {
            SaveFile.saveGame();
            LittleH.program.switchMenu(TitleMenu.titleScreen);
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
