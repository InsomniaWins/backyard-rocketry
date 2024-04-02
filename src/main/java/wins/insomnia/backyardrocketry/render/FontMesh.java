package wins.insomnia.backyardrocketry.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class FontMesh {

    private final float CHARACTER_UV_WIDTH = 7f / 128f;
    private final float CHARACTER_UV_HEIGHT = 12f / 128f;
    private final int VAO;
    private int vbo;
    private int ebo;

    private String text;
    private int indexCount;

    public FontMesh() {

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

        updateMesh();

    }

    public void setText(String text) {
        this.text = text;
        updateMesh();
    }

    private void updateMesh() {

        float[] vertexArray = new float[] {
                0.5f,  0.5f,  -0.5f,  CHARACTER_UV_WIDTH,  1f, // top right
                0.5f, -0.5f,  -0.5f,  CHARACTER_UV_WIDTH,  1f - CHARACTER_UV_HEIGHT, // bottom right
                -0.5f, -0.5f,  -0.5f,  0.0f,  1f - CHARACTER_UV_HEIGHT, // bottom left
                -0.5f,  0.5f,  -0.5f,  0.0f,  1f, // top left
        };

        int[] indexArray = new int[] {
                0, 1, 3,
                1, 2, 3
        };



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
