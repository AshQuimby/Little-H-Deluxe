package com.sab.littleh.util;

import java.util.function.Consumer;

public class Animation {
   public int[] frames;
   public int frameDelay;
   public int tick;
   public int frame;
   public int frameCount;
   private Consumer<Integer> action;

   public Animation(int frameDelay, int... frames) {
      this.frameDelay = frameDelay;
      this.frames = frames;
      frameCount = frames.length;   
      frame = 0;
   }
   
   public void reset() {
      tick = 0;
      frame = 0;
   }

   public void setOnFrameChange(Consumer<Integer> action) {
      this.action = action;
   }
   
   public boolean getFinished() {
      return frame > frameCount - 1 || frame == frameCount - 1 && tick == frameDelay - 1;
   }
   
   public int getFrame() {
      return frames[frame];
   }
   
   public int step() {
      if (++tick >= frameDelay) {
         frame++;
         if (action != null) action.accept(frame);
         tick = 0;
      }
      return frames[Math.min(frame, frameCount - 1)];
   }
   
   public void setAnimationSpeed(int delay) {
      frameDelay = delay;
   }
   
   public int stepLooping() {
      if (++tick >= frameDelay) {
         if (++frame >= frameCount) {
            frame = 0;
         }
         if (action != null) action.accept(frame);
         tick = 0;
      }
      return frames[frame];
   }
}