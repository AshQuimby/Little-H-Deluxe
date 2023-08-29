package com.sab.littleh.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sab.littleh.LittleH;

import java.util.ArrayList;
import java.util.List;

public class Graphics extends SpriteBatch {
    private final PolygonSpriteBatch polyBatch = new PolygonSpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Color tint = Color.WHITE;
    private Color trueColor = Color.WHITE;

    public Graphics() {
        super();
        shapeRenderer.setAutoShapeType(true);
    }

    public void drawPatch(Patch patch, float x, float y, float width, float height, int patchScale) {
        patch.render(this, patchScale, x, y, width, height);
    }
    public void drawPatch(Patch patch, Rectangle rect, int patchScale) {
        patch.render(this, patchScale, rect);
    }

    public void drawImage(Texture image, float x, float y, float width, float height, Rectangle drawFrom) {
        draw(image, x, y, width, height, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
    }

    public void drawImageWithShader(ShaderProgram shader, Texture image, float x, float y, float width, float height, Rectangle drawFrom) {
        setShader(shader);
        draw(image, x, y, width, height, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
        setShader(null);
    }

    public void drawImage(Texture image, float x, float y, float width, float height, Rectangle drawFrom, float rotation) {
        Vector2 origin = new Vector2(width / 2, height / 2);
        draw(image, x, y, origin.x, origin.y, width, height, 1, 1, rotation, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
    }

    public void drawImage(Texture image, Rectangle drawTo, Rectangle drawFrom) {
        draw(image, drawTo.x, drawTo.y, drawTo.width, drawTo.height, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
    }

    public void drawImage(Texture image, Rectangle drawTo, Rectangle drawFrom, float rotation) {
        Vector2 origin = new Vector2(drawTo.width / 2, drawTo.height / 2);
        draw(image, drawTo.x, drawTo.y, origin.x, origin.y, drawTo.width, drawTo.height, 1, 1, rotation, (int) drawFrom.x, (int) drawFrom.y, (int) drawFrom.width, (int) drawFrom.height, false, false);
    }

    public Rectangle drawString(String text, BitmapFont font, float x, float y, float size, int anchor) {
        if (text.contains("\n")) {
            String[] splitString = text.split("\n");
            Rectangle bounds = Fonts.getMultiStringBounds(splitString, font, x, y, size, anchor);
            return drawString(text, font, bounds, 4, size, anchor, 0);
        } else {
            Rectangle bounds = Fonts.getStringBounds(text, font, x, y, size, anchor);
            font.draw(this, text, bounds.x, bounds.y + bounds.height);
            return bounds;
        }
    }

    public Rectangle drawString(String text, BitmapFont font, final Rectangle fitTo, float lineSpacing, float size, int horizontalAnchor, int verticalAnchor) {
        if (text.contains("\n")) {
            String[] splitString = text.split("\n");
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 0; i < splitString.length; i++) {
                textBuilder.append(splitString[i]);
                if (i < splitString.length - 1) textBuilder.append(" \n ");
            }
            text = textBuilder.toString();
        }
        Rectangle bounds = Fonts.getStringBounds(text, font, fitTo.x, fitTo.y, size, horizontalAnchor);
        bounds.height = Fonts.getStringBounds("|", font, 0, 0, size, 0).height;
        float lineHeight = bounds.height + lineSpacing;
        float height = 0 + bounds.height;
        float width = fitTo.width;
        bounds = new Rectangle(fitTo);
        List<String> lines = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            String buffer = lineBuilder + word;
            float lineWidth = Fonts.getStringWidth(buffer, font, size);
            if (lineWidth > fitTo.width || word.equals("\n")) {
                String line = lineBuilder.toString();
                lines.add(line.stripTrailing());
                height += lineHeight;
                lineBuilder = new StringBuilder();
                if (lineWidth > width)
                    width = lineWidth;
            }
            if (!word.equals("\n"))
                lineBuilder.append(word).append(" ");
        }
        String line = lineBuilder.toString();
        line = line.stripTrailing();
        if (!line.isBlank()) {
            lines.add(line);
        }

        for (int i = 0; i < lines.size(); i++) {
            String string = lines.get(i);
            if (string.isBlank()) {
                lines.remove(i);
                i--;
                height -= lineHeight;
            } else {
                break;
            }
        }

        bounds.y -= height;
        float oldCenterX = bounds.x + bounds.width / 2;
        bounds.width = width;
        bounds.x = oldCenterX - bounds.width / 2;
        bounds.height = Math.max(height, fitTo.height);

        float horizontalAnchorOffset = horizontalAnchor == 0 ? fitTo.width / 2 : horizontalAnchor == 1 ? fitTo.width : 0;
        float verticalAnchorOffset = 0;

        float softHeight = lineHeight * lines.size() - lineSpacing;

        if (verticalAnchor == 1) verticalAnchorOffset = fitTo.height;
        else if (verticalAnchor == 0) verticalAnchorOffset = softHeight + softHeight / 2 + bounds.height / 2 - fitTo.y + bounds.y - lineHeight / 2 + lineSpacing / 2;
        else if (verticalAnchor == -1) verticalAnchorOffset = height;

        for (int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            drawString(line, font, fitTo.x + horizontalAnchorOffset, fitTo.y - lineHeight * i + verticalAnchorOffset, size, horizontalAnchor);
        }

        return fitTo;
    }

    public void drawMesh(float[] vertices) {
        end();
        PolygonSprite poly;
        polyBatch.begin();
        polyBatch.setProjectionMatrix(getProjectionMatrix());
        Texture textureSolid;

        // Creating the color filling (but textures would work the same way)
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(getColor());
        pix.fill();
        textureSolid = new Texture(pix);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid),
                vertices,
                new short[] {
                3, 4, 5,
                0, 1, 8,
                1, 2, 7,
                2, 3, 6,
                7, 8, 1,
                6, 7, 2,
                5, 6, 3
        });
        poly = new PolygonSprite(polyReg);
        poly.setOrigin(0, 0);
        poly.draw(polyBatch);
        polyBatch.end();
        pix.dispose();
        begin();
    }

    @Override
    public void setColor(Color color) {
        color = color.cpy();
        trueColor = color.cpy();
        color.mul(tint);
        super.setColor(color);
    }

    public void resetColor() {
        trueColor = Color.WHITE;
        setColor(trueColor);
    }

    public void resetTint() {
        tint = Color.WHITE;
        setColor(trueColor);
    }

    public Vector2 getCameraPosition() {
        return LittleH.program.dynamicCamera.getPosition();
    }

    public void setTint(Color tint) {
        this.tint = tint;
        setColor(trueColor);
    }

    public Color getTint() {
        return tint;
    }

    public void startShapeRenderer(Color color) {
        end();
        shapeRenderer.begin();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setColor(color);
        shapeRenderer.setProjectionMatrix(getProjectionMatrix());
    }

    public void endShapeRenderer() {
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        begin();
    }

    public void drawRect(float x, float y, float width, float height) {
        shapeRenderer.rect(x, y, width, height);
    }

    public void resetShader() {
        setShader(null);
        getShader().bind();
    }
}
