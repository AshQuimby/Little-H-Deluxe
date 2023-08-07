package com.sab.littleh.settings;

public class StringSetting extends Setting<String> {
    public StringSetting(String id, String name, String defaultValue) {
        super(id, name, defaultValue);
    }

    @Override
    public boolean isValid(String rawValue) {
        return true;
    }

    @Override
    public String asRawValue() {
        return value;
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }

    @Override
    public void set(String rawValue) {
        value = rawValue;
    }

    @Override
    public void next() {
        throw new UnsupportedOperationException("StringSetting does not allow the use of next()");
    }

    @Override
    public void previous() {
        throw new UnsupportedOperationException("StringSetting does not allow the use of previous()");
    }

    @Override
    public String display() {
        return value;
    }
}
