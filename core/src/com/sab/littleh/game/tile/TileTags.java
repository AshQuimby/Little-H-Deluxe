package com.sab.littleh.game.tile;

import java.util.HashMap;
import java.util.Map;

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
}
