package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.sab.littleh.LittleH;

import java.util.Stack;

public class NestedFrameBuffer extends FrameBuffer {
    private static final Stack<NestedFrameBuffer> bufferStack = new Stack<>();
    public NestedFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth) {
        super(format, width, height, hasDepth, false);
    }

    public static void unbind() {
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, bufferStack.peek().framebufferHandle);
    }

    @Override
    public void begin() {
        super.begin();
        bufferStack.add(this);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void end() {
        this.end(0, 0, LittleH.program.getWidth(), LittleH.program.getHeight());
    }

    @Override
    public void end(int x, int y, int width, int height) {
        if (bufferStack.peek() != this) {
            throw new IllegalStateException("You may only call end() on the deepest NestedFrameBuffer. Make sure your method calls are in the right order.");
        }

        bufferStack.pop();

        if (bufferStack.isEmpty()) {
            super.end(x, y, width, height);
        } else {
            unbind();
            Gdx.gl20.glViewport(x, y, width, height);
        }
    }
}
