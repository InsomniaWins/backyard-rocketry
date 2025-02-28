package wins.insomnia.backyardrocketry.render.text;

import org.lwjgl.opengl.GL30;
import wins.insomnia.backyardrocketry.render.texture.FontTexture;

import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class FontMesh {
    private final int VAO;
    private int vbo;
    private int ebo;
    private String text;
    private FontTexture fontTexture;
    private int indexCount;

    private final HashMap<Character, float[]> CHARACTER_LOCATIONS;

    public FontMesh() {

        CHARACTER_LOCATIONS = new HashMap<>();
        updateCharacterLocations();

        text = "";
        VAO = GL30.glGenVertexArrays();

        glBindVertexArray(VAO);

        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, new float[0], GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, new int[0], GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        indexCount = 0;
    }

    public void setFontTexture(FontTexture fontTexture) {
        this.fontTexture = fontTexture;
        updateCharacterLocations();
    }

    private void updateCharacterLocations() {

        if (fontTexture == null || fontTexture.isClean()) return;

        String characters = fontTexture.getKeyString();

        CHARACTER_LOCATIONS.clear();

        for (int characterIndex = 0; characterIndex < characters.length(); characterIndex++) {

            char character = characters.charAt(characterIndex);
            CHARACTER_LOCATIONS.put(character, new float[] {
                    fontTexture.getCharacterX(characterIndex) / (float) fontTexture.getWidth(),
                    fontTexture.getCharacterY(characterIndex) / (float) fontTexture.getHeight(),
                    fontTexture.getCharacterWidth(characterIndex)
            });

        }

    }

    public void setText(String text, FontTexture fontTexture) {
        this.text = text;

        if (this.fontTexture != fontTexture) {
            setFontTexture(fontTexture);
        }

        updateMesh();
    }

    private int getSpecialCharacterCount(String input) {
        int specialCharacterCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (CHARACTER_LOCATIONS.get(c) == null) {
                specialCharacterCount++;
            }
        }
        return specialCharacterCount;
    }

    private void updateMesh() {

        if (fontTexture == null || fontTexture.isClean()) return;

        float pixelAspect = 1f;
        int currentLine = 0;
        int textLength = text.length();
        int specialCharacterCount = getSpecialCharacterCount(text);

        float[] vertexArray = new float[(textLength - specialCharacterCount) * 16];
        int[] indexArray = new int[(textLength - specialCharacterCount) * 6];

        float[] characterOffsetAmounts = {0f, 0f};

        int vertexIndex = 0;
        int indexIndex = 0;
        int currentVisibleCharacter = 0;

        for (int i = 0; i < textLength; i++) {

            char character = text.charAt(i);

            // how to handle a new line
            if (character == '\n') {
                currentLine++;
                characterOffsetAmounts[1] = currentLine * pixelAspect * fontTexture.getCharacterHeight();
                characterOffsetAmounts[0] = 0f;
                continue;
            }

            // get texture coordinates of current character
            float[] characterUvs = CHARACTER_LOCATIONS.get(character);

            // what to do when character not in font texture occurs
            if (characterUvs == null) {
                characterUvs = CHARACTER_LOCATIONS.get(' ');
            }

            int characterWidth = (int) characterUvs[2];
            int characterHeight = fontTexture.getCharacterHeight();
            float characterUvWidth = characterWidth / (float) fontTexture.getWidth();
            float characterUvHeight = characterHeight / (float) fontTexture.getHeight();

            // calculate texture coordinates for vbo
            float rightU = characterUvs[0] + characterUvWidth;
            float leftU = characterUvs[0];
            float topV = characterUvs[1];
            float bottomV = topV + characterUvHeight;

            // top right vertex
            vertexArray[vertexIndex] = pixelAspect * characterWidth + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = rightU; // u
            vertexArray[vertexIndex + 3] = topV; // v

            vertexIndex += 4;

            // bottom right vertex
            vertexArray[vertexIndex] = pixelAspect * characterWidth + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = pixelAspect * characterHeight + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = rightU; // u
            vertexArray[vertexIndex + 3] = bottomV; // v

            vertexIndex += 4;

            // bottom left vertex
            vertexArray[vertexIndex] = characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = pixelAspect * characterHeight + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = leftU; // u
            vertexArray[vertexIndex + 3] = bottomV; // v

            vertexIndex += 4;

            // top left vertex
            vertexArray[vertexIndex] = characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = leftU; // u
            vertexArray[vertexIndex + 3] = topV; // v

            vertexIndex += 4;

            // indices
            indexArray[indexIndex] = 4 * currentVisibleCharacter; // top right
            indexArray[indexIndex + 1] = 1 + 4 * currentVisibleCharacter; // bottom right
            indexArray[indexIndex + 2] = 3 + 4 * currentVisibleCharacter; // top left
            indexArray[indexIndex + 3] = 1 + 4 * currentVisibleCharacter; // bottom right
            indexArray[indexIndex + 4] = 2 + 4 * currentVisibleCharacter; // bottom left
            indexArray[indexIndex + 5] = 3 + 4 * currentVisibleCharacter; // top left

            indexIndex += 6;
            currentVisibleCharacter++;

            characterOffsetAmounts[0] += pixelAspect * characterWidth;
        }

        glBindVertexArray(VAO);

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);

        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        indexCount = indexArray.length;

    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVao() {
        return VAO;
    }

    public void clean() {

        if (VAO > -1) {
            glDeleteBuffers(vbo);
            glDeleteBuffers(ebo);
            GL30.glDeleteVertexArrays(VAO);
        }

    }

}
