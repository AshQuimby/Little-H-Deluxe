package com.sab.littleh.util.sab_format;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a map of SabValues via their associated property as a String key.
 */
public class SabData {
    private final Map<String, SabValue> values;

    /**
     * Creates a com.sab_format.SabData object for writing to and reading from.
     */
    public SabData() {
        values = new HashMap<>();
    }

    /**
     * Gets a value via its associated key.
     * @param identifier
     * The key to use
     * @return
     * The value associated with the key
     */
    public SabValue getValue(String identifier) {
        return values.get(identifier);
    }

    /**
     * Gets a value from the map via its associated key.
     * @param identifier
     * The key to use
     * @return
     * The value associated with the key
     */
    public boolean hasValue(String identifier) {
        return values.containsKey(identifier);
    }

    /**
     * Puts a value in the map with specified key.
     * @param identifier
     * The key to use
     * @param value
     * The com.sab_format.SabValue to store
     */
    public void insertValue(String identifier, SabValue value) {
        values.put(identifier, value);
    }
    public void insertValue(String identifier, String rawValue) {
        values.put(identifier, new SabValue(rawValue));
    }

    /**
     * Returns the map containing all the values.
     * @return
     * The HashMap object containing the values
     */
    public Map<String, SabValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("com.sab_format.SabData {\n");
        for (String ident : values.keySet()) {
            SabValue value = values.get(ident);
            builder.append(String.format("\t%s: %s\n", ident, value));
        }
        builder.append("}");
        return builder.toString();
    }

    public String getRawValue(String identifier) {
        return getValue(identifier).getRawValue();
    }

    public void remove(String identifier) {
        values.remove(identifier);
    }
}
