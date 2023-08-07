package com.sab.littleh.settings;

import java.util.Objects;

public class ListSetting extends Setting<Integer> {
    private final String[] options;
    private final String[] displayOptions;

    public ListSetting(String id, String name, int defaultValue, String[] options, String[] displayOptions) {
        super(id, name, defaultValue);
        this.options = options;
        this.displayOptions = displayOptions;
    }

    @Override
    public boolean isValid(String value) {
        for (String option : options) {
            if (Objects.equals(option, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String asRawValue() {
        return options[value];
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }

    @Override
    public void set(String rawValue) {
        for (int i = 0; i < options.length; i++) {
            if (Objects.equals(options[i], rawValue)) {
                value = i;
            }
        }
    }

    @Override
    public void next() {
        value++;
        if (value >= options.length) {
            value = 0;
        }
    }

    @Override
    public void previous() {
        value--;
        if (value < 0) {
            value = options.length - 1;
        }
    }

    @Override
    public String display() {
        return displayOptions[value];
    }
}
