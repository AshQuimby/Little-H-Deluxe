package com.sab.littleh.campaign.overworld;

import com.sab.littleh.LittleH;
import com.sab.littleh.campaign.SaveFile;
import com.sab.littleh.screen.*;

public class OverworldPauseScreen extends PauseScreen {

    public OverworldPauseScreen(OverworldScreen overworldScreen) {
        super(overworldScreen);
        buttons.add(new ScreenButton("button", "To Main Menu", buttonRect, () -> {
            SaveFile.saveGame();
            LittleH.program.switchScreen(TitleScreen.titleScreen);
        }));
    }

    @Override
    public void start() {
        super.start();
    }
}
