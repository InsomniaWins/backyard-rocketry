package wins.insomnia.backyardrocketry.render;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class FontMesh {

    private final int[] CHARACTER_SIZE = new int[]{7, 12};
    private final float CHARACTER_UV_WIDTH = (float) CHARACTER_SIZE[0] / 128f;
    private final float CHARACTER_UV_HEIGHT = (float) CHARACTER_SIZE[1] / 128f;
    private final int VAO;
    private int vbo;
    private int ebo;
    private String text;
    private int indexCount;

    private final HashMap<Character, float[]> CHARACTER_LOCATIONS;

    public FontMesh() {

        CHARACTER_LOCATIONS = new HashMap<>();
        createCharacterLocations("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"\\()|}{;<>-+%?,./!:$_=&~*#][`@^ ");

        text = "";
        VAO = glGenVertexArrays();

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

    private void createCharacterLocations(String characters) {

        int columns = 128 / CHARACTER_SIZE[0];
        int rows = 128 / CHARACTER_SIZE[1];

        int characterIndex = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {

                if (characterIndex > characters.length() - 1) break;

                char character = characters.charAt(characterIndex);

                CHARACTER_LOCATIONS.put(character, new float[] {
                        CHARACTER_UV_WIDTH * x,
                        CHARACTER_UV_HEIGHT * y
                });

                characterIndex++;
            }
        }

    }

    public void setText(String text) {
        this.text = text;
        updateMesh();
    }

    private void updateMesh() {

        float pixelAspect = 0.015f;

        int textLength = text.length();

        float[] vertexArray = new float[textLength * 20];
        int[] indexArray = new int[textLength * 6];

        for (int i = 0; i < textLength; i++) {

            char character = text.charAt(i);

            int vertexIndex = i * 20;
            int indexIndex = i * 6;


            float[] characterOffsetAmounts = {
                    i * pixelAspect * CHARACTER_SIZE[0] * 2,
                    0f,
                    0f
            };

            float[] characterUvs = CHARACTER_LOCATIONS.get(character);
            if (characterUvs == null) {
                characterUvs = new float[] {0f, 0f};
            }


            float rightU = characterUvs[0] + CHARACTER_UV_WIDTH;
            float leftU = characterUvs[0];
            float topV = -characterUvs[1];
            float bottomV = topV - CHARACTER_UV_HEIGHT;

            // top right vertex
            vertexArray[vertexIndex] = pixelAspect * 7 + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = pixelAspect * 12 + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = characterOffsetAmounts[2]; // z
            vertexArray[vertexIndex + 3] = rightU; // u
            vertexArray[vertexIndex + 4] = topV; // v

            vertexIndex += 5;

            // bottom right vertex
            vertexArray[vertexIndex] = pixelAspect * 7 + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = -pixelAspect * 12 + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = characterOffsetAmounts[2]; // z
            vertexArray[vertexIndex + 3] = rightU; // u
            vertexArray[vertexIndex + 4] = bottomV; // v

            vertexIndex += 5;

            // bottom left vertex
            vertexArray[vertexIndex] = -pixelAspect * 7 + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = -pixelAspect * 12 + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = characterOffsetAmounts[2]; // z
            vertexArray[vertexIndex + 3] = leftU; // u
            vertexArray[vertexIndex + 4] = bottomV; // v

            vertexIndex += 5;

            // top left vertex
            vertexArray[vertexIndex] = -pixelAspect * 7 + characterOffsetAmounts[0]; // x
            vertexArray[vertexIndex + 1] = pixelAspect * 12 + characterOffsetAmounts[1]; // y
            vertexArray[vertexIndex + 2] = characterOffsetAmounts[2]; // z
            vertexArray[vertexIndex + 3] = leftU; // u
            vertexArray[vertexIndex + 4] = topV; // v



            // indices
            indexArray[indexIndex] = 4 * i; // top right
            indexArray[indexIndex + 1] = 1 + 4 * i; // bottom right
            indexArray[indexIndex + 2] = 3 + 4 * i; // top left

            indexArray[indexIndex + 3] = 1 + 4 * i; // bottom right
            indexArray[indexIndex + 4] = 2 + 4 * i; // bottom left
            indexArray[indexIndex + 5] = 3 + 4 * i; // top left
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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        indexCount = indexArray.length;

    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVao() {
        return VAO;
    }

    public void clean() {

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(VAO);

    }

}
