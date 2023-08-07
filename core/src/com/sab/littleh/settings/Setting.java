package com.sab.littleh.settings;

public abstract class Setting<T> {
    public final String id;
    public final String name;
    public final T defaultValue;
    public T value;

    public Setting(String id, String name, T defaultValue) {
        this.id = id;
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public abstract boolean isValid(String rawValue);
    public abstract String asRawValue();
    public abstract boolean isDiscrete();
    public abstract void set(String rawValue);
    public abstract void next();
    public abstract void previous();
    public abstract String display();

    public void reset() {
        value = defaultValue;
    }
}
