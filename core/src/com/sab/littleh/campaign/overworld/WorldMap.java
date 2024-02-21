package com.sab.littleh.campaign.overworld;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.campaign.SaveFile;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.Patch;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabValue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorldMap {
    private final List<LevelIndicator> levelIndicators;

    public WorldMap() {
        levelIndicators = new ArrayList<>();
        SabData levelData = SabReader.read(WorldMap.class.getResourceAsStream("/scripts/campaign/overworld/levels/manifest.sab"));
        Collection<SabValue> levels = levelData.getValues().values();
        SabData unlockedLevels = SaveFile.saveData.get("unlocked_levels");
        for (SabValue value : levels) {
            InputStream stream = WorldMap.class.getResourceAsStream(String.format("/scripts/campaign/overworld/levels/%s.sab", value.getRawValue()));
            if (stream == null) continue;
            SabData data = SabReader.read(stream);
            if (unlockedLevels.hasValue(value.getRawValue()))
                levelIndicators.add(new LevelIndicator(data, SaveFile.saveData));
            if (data.hasValue("unlocks") && SaveFile.saveData.get("clear_times").hasValue(value.getRawValue())) {
                for (String level : data.getValue("unlocks").asStringArray()) {
                    if (!containsLevel(level)) {
                        levelIndicators.add(new LevelIndicator(SabReader.read(WorldMap.class.getResourceAsStream(
                                String.format("/scripts/campaign/overworld/levels/%s.sab", level))), SaveFile.saveData));
                        SaveFile.saveData.get("unlocked_levels").insertValue(level, "true");
                    }
                }
            }
        }
    }

    public void update() {
        for (LevelIndicator levelIndicator : levelIndicators) {
            levelIndicator.update();
        }
    }

    public LevelIndicator getFirstLevel() {
        return levelIndicators.get(0);
    }

    public boolean containsLevel(String level) {
        for (LevelIndicator levelIndicator : levelIndicators) {
            if (levelIndicator.getId().equals(level))
                return true;
        }
        return false;
    }

    public LevelIndicator getOverlappedLevel(Vector2 point) {
        for (LevelIndicator levelIndicator : levelIndicators) {
            if (levelIndicator.isHovered())
                return levelIndicator;
        }
        return null;
    }

    public void render(Graphics g) {
        g.setColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        g.draw(Images.getImage("campaign/overworld/overworld_map.png"), -1024, -760, 3072, 2280);
        g.resetColor();
        g.drawPatch(Patch.get("menu"), -6, -6, 1036, 772, 2);
        g.drawPatch(Patch.get("menu_light"), -4, -4, 1032, 768, 2);
        g.draw(Images.getImage("campaign/overworld/overworld_map.png"), 0, 0, 1024, 760);
        for (LevelIndicator levelIndicator : levelIndicators) {
            levelIndicator.render(g);
        }
    }
}
