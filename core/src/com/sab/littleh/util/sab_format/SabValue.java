package com.sab.littleh.util.sab_format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Keeps track of the data parsed by the com.sab_format.SabReader. Stored raw as strings, can be parsed to primitive types
 */
public class SabValue {
    private final String rawValue;

    public static SabValue valueOf(String string) {
        return new SabValue(string);
    }

    /**
     * Creates a com.sab_format.SabValue from a boolean
     * @param b
     * The value to be parsed into a com.sab_format.SabValue
     */
    public static SabValue fromBool(boolean b) {
        return new SabValue(Boolean.toString(b));
    }

    /**
     * Creates a com.sab_format.SabValue from an integer
     * @param i
     * The value to be parsed into a com.sab_format.SabValue
     */
    public static SabValue fromInt(int i) {
        return new SabValue(Integer.toString(i));
    }

    /**
     * Creates a com.sab_format.SabValue from a float
     * @param f
     * The value to be parsed into a com.sab_format.SabValue
     */
    public static SabValue fromFloat(float f) {
        return new SabValue(Float.toString(f));
    }

    /**
     * Creates a com.sab_format.SabValue from a string array
     * @param a
     * The array to be parsed into a com.sab_format.SabValue
     */
    public static SabValue fromArray(String[] a) {
        return new SabValue(Arrays.toString(a));
    }

    /**
     * Creates a com.sab_format.SabValue from a string
     * @param value
     * The value to be parsed into a com.sab_format.SabValue
     */
    public SabValue(String value) {
        rawValue = value;
    }

    /**
     * Gets the raw string value of this com.sab_format.SabValue
     * @return
     * The raw string value of this com.sab_format.SabValue
     */
    public String getRawValue() {
        return rawValue;
    }

    /**
     * Gets the parsed boolean value of this com.sab_format.SabValue
     * @return
     * The parsed value of this com.sab_format.SabValue
     */
    public boolean asBool() {
        return Boolean.parseBoolean(rawValue);
    }

    /**
     * Gets the parsed integer value of this com.sab_format.SabValue
     * @return
     * The parsed value of this com.sab_format.SabValue
     */
    // Can we not have exceptions thrown by methods, it's annoying.
    public int asInt() throws NumberFormatException {
        return Integer.parseInt(rawValue);
    }

    /**
     * Gets the parsed float value of this com.sab_format.SabValue
     * @return
     * The parsed value of this com.sab_format.SabValue
     */
    public float asFloat() throws NumberFormatException {
        return Float.parseFloat(rawValue);
    }

    /**
     * Gets the parsed com.sab_format.SabValue array value of this com.sab_format.SabValue
     * @return
     * The parsed value array of this com.sab_format.SabValue
     */
    public SabValue[] asArray() {
        char[] chars = rawValue.toCharArray();

        StringBuilder buffer = new StringBuilder();
        List<String> elements = new ArrayList<>();
        int parens = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '[') parens++;
            else if (c == ']') parens--;
            else if (c != ',') {
                buffer.append(c);
            }
            if (c == ',' || parens == 0) {
                elements.add(buffer.toString().trim());
                buffer = new StringBuilder();
            }
        }

        elements.removeIf((String e) -> e.isBlank() || e.equals(","));
        SabValue[] items = new SabValue[elements.size()];
        for (int i = 0; i < items.length; i++) items[i] = new SabValue(elements.get(i));

        return items;
    }

    public String[] asStringArray() {
        char[] chars = rawValue.toCharArray();

        StringBuilder buffer = new StringBuilder();
        List<String> elements = new ArrayList<>();
        int parens = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '[') parens++;
            else if (c == ']') parens--;
            else if (c != ',') {
                buffer.append(c);
            }
            if (c == ',' || parens == 0) {
                elements.add(buffer.toString().trim());
                buffer = new StringBuilder();
            }
        }

        elements.removeIf((String e) -> e.isBlank() || e.equals(","));
        String[] items = new String[elements.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = elements.get(i);
        }

        return items;
    }

    @Override
    public String toString() {
        return rawValue;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SabValue && ((SabValue) o).getRawValue().equals(getRawValue());
    }

    @Override
    public int hashCode() {
        return rawValue.hashCode();
    }
}
