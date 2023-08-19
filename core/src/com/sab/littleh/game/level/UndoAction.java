package com.sab.littleh.game.level;

import java.awt.*;

public abstract class UndoAction {
    public abstract void undo(LevelEditor editor);
    public abstract void redo(LevelEditor editor);
    public abstract void resize(Point size);
}