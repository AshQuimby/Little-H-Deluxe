package com.sab.littleh.settings;

import com.sab.littleh.LittleH;

import java.util.Objects;

public class FontSetting extends ListSetting {
    private final float[] defaultSizes;

    public FontSetting(String id, String name, int defaultValue, String[] options, String[] displayOptions, float[] defaultSizes) {
        super(id, name, defaultValue, options, displayOptions);
        this.defaultSizes = defaultSizes;
    }

    public float getFontSize() {
        return defaultSizes[value];
    }

    @Override
    public void next() {
        super.next();
        LittleH.updateFont();
    }

    @Override
    public void previous() {
        super.previous();
        LittleH.updateFont();
    }
}
