package com.sab.littleh.settings;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String id, String name, Boolean defaultValue) {
        super(id, name, defaultValue);
    }

    @Override
    public boolean isValid(String rawValue) {
        return rawValue.equals("true") || rawValue.equals("false");
    }

    @Override
    public String asRawValue() {
        return Boolean.toString(value);
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }

    @Override
    public void set(String rawValue) {
        value = Boolean.parseBoolean(rawValue);
    }

    @Override
    public void next() {
        value = !value;
    }

    @Override
    public void previous() {
        value = !value;
    }

    @Override
    public String display() {
        return value ? "On" : "Off";
    }
}
