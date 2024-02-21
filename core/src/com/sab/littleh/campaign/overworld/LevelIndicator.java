package com.sab.littleh.campaign.overworld;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sab.littleh.LittleH;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.util.Graphics;
import com.sab.littleh.util.Images;
import com.sab.littleh.util.MouseUtil;
import com.sab.littleh.util.sab_format.SabData;
import com.sab.littleh.util.sab_format.SabReader;
import com.sab.littleh.util.sab_format.SabValue;

import java.util.HashMap;

public class LevelIndicator {
    private final HashMap<String, SabData> saveData;
    private final SabData levelData;
    private final SabData data;
    private final Rectangle bounds;
    private boolean hovered;

    public LevelIndicator(SabData data, HashMap<String, SabData> saveData) {
        this.data = data;
        this.saveData = saveData;
        bounds = new Rectangle(data.getValue("x").asInt() * 2 - 24, data.getValue("y").asInt() * 2 - 16, 48, 32);
        levelData = SabReader.read(LittleH.getInternalLevel(getLevelPath()));
        hovered = false;
    }

    public SabValue getClearTime() {
        return saveData.get("clear_times").getValue(getId());
    }

    public String getId() {
        return data.getRawValue("id");
    }

    public void update() {
        hovered = bounds.contains(MouseUtil.getDynamicMousePosition());
    }

    public SabData getData() {
        return data;
    }

    public SabData getLevelData() {
        return levelData;
    }

    public Level getLevel() {
        return LevelLoader.readInternalLevel(getLevelPath());
    }

    public String getLevelPath() {
        return String.format("%s/%s.map", data.getRawValue("location"), getId());
    }

    public Vector2 getWorldPosition() {
        return bounds.getCenter(new Vector2());
    }

    public boolean isHovered() {
        return hovered;
    }
    public void render(Graphics g) {
        if (hovered)
            g.draw(Images.getImage("campaign/overworld/level_indicator_hovered.png"), bounds.x, bounds.y, bounds.width, bounds.height);
        else
            g.draw(Images.getImage("campaign/overworld/level_indicator.png"), bounds.x, bounds.y, bounds.width, bounds.height);
        String image = "level_indicator_complete.png";
        if (isUnlocked()) {
            SabValue levelTime = getClearTime();
            if (levelTime == null || levelTime.getRawValue().equals("incomplete")) {
                image = "level_indicator_incomplete.png";
            } else if (isPRank()) {
                image = "level_indicator_p_rank.png";
                g.setColor(Images.getRainbowColor());
            }
        } else {
            image = "level_indicator_not_unlocked.png";
        }

        g.draw(Images.getImage(String.format("campaign/overworld/%s", image)), bounds.x, bounds.y, bounds.width, bounds.height);
        g.resetColor();
    }

    public boolean isUnlocked() {
        return saveData.get("unlocked_levels").hasValue(getId()) && saveData.get("unlocked_levels").getValue(getId()).asBool();
    }

    public SabValue getPTime() {
        return data.getValue("p_time");
    }

    public boolean isPRank() {
        SabValue levelTime = getClearTime();
        if (levelTime == null) return false;
        SabValue pTime = getPTime();
        return levelTime.asFloat() <= pTime.asFloat();
    }
}
