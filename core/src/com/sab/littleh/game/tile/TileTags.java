package com.sab.littleh.game.tile;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TileTags {
    private boolean modified;
    private Map<String, String> tags;

    public TileTags() {
        tags = new HashMap<>();
    }

    public TileTags(TileTags tags) {
       this.tags = tags.tags;
    }

    public boolean hasTag(String tag) {
        return tags.containsKey(tag);
    }

    public String getTag(String tag) {
        return tags.get(tag);
    }

    public void addTag(String tag) {
        modified = true;
        tags = new HashMap<>(tags);
        tags.put(tag, null);
    }

    public String[] getTags() {
        return tags.keySet().toArray(new String[0]);
    }

    public String[] getTagParameters(String tag) {
        String[] params = tags.get(tag).split("\\|");
        for (int i = 0; i < params.length; i++)
            params[i] = params[i].trim();
        return params;
    }

    public void addTag(String tag, String value) {
        tags.put(tag, value);
    }

    public void removeTag(String tag) {
        modified = true;
        tags = new HashMap<>(tags);
        tags.remove(tag);
    }

    public boolean isModified() {
        return modified;
    }

    @Override
    public int hashCode() {
        int i = 0;
        for (String key : tags.keySet()) {
            i += key.hashCode();
            i += tags.get(key) == null ? -1 : tags.get(key).hashCode();
        }
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TileTags) {
            TileTags other = (TileTags) o;
            Set<String> keySet = other.tags.keySet();
            if (tags.keySet().equals(keySet)) {
                for (String key : keySet) {
                    if (!tags.get(key).equals(other.tags.get(key)))
                        return false;
                }
                return true;
            }
        }
        return false;
    }
}
