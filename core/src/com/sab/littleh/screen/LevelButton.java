package com.sab.littleh.screen;

import com.badlogic.gdx.math.Rectangle;
import com.sab.littleh.LittleH;
import com.sab.littleh.util.sab_format.SabData;

import java.io.File;

public class LevelButton extends ScreenButton {
    private File associatedFile;
    public LevelButton(String patchString, final File file, final SabData mapData, float x, float y, float width, float height) {
        super(patchString, "", x, y, width, height);
        if (mapData.getValue("name") == null) text = "ERROR";
        else text = mapData.getValue("name").getRawValue().trim();
        boolean appendEllipses = text.length() > 8;
        text = text.substring(0, Math.min(text.length(), 8));
        if (appendEllipses) text += "...";
        this.associatedFile = file;
        onPress = () -> {
            LittleH.program.switchScreen(new LevelOptionsScreen(file, mapData));
        };
    }

    public LevelButton(String patchString, File file, SabData mapData, Rectangle rectangle) {
        this(patchString, file, mapData, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }
}
