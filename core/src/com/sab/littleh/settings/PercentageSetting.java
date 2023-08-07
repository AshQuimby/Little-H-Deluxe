package com.sab.littleh.settings;

public class PercentageSetting extends Setting<Integer> {
    private int minValue;
    private int maxValue;

    public PercentageSetting(String id, String name, int defaultValue, int minValue, int maxValue) {
        super(id, name, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public PercentageSetting(String id, String name, int defaultValue) {
        this(id, name, defaultValue, 0, 100);
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Minimum value: " + minValue + " cannot be greater than or equal to maximum value: " + maxValue);
        }
    }

    @Override
    public boolean isValid(String rawValue) {
        try {
            int v = Integer.parseInt(rawValue);
            return v >= minValue && v <= maxValue;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String asRawValue() {
        return Integer.toString(value);
    }

    @Override
    public boolean isDiscrete() {
        return false;
    }

    @Override
    public void set(String rawValue) {
        value = Integer.parseInt(rawValue);
    }

    @Override
    public void next() {
        if (value < maxValue) {
            value = Math.min(maxValue, value + 1);
        }
    }

    @Override
    public void previous() {
        if (value > minValue) {
            value = Math.max(minValue, value - 1);
        }
    }

    @Override
    public String display() {
        return value + "%";
    }

    public float asFloat() {
        return value / 100f;
    }

    public float asRelativeFloat() {
        return (value - getMinValue()) / (float) (getMaxValue() - getMinValue());
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }
}