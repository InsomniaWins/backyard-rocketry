package wins.insomnia.backyardrocketry.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class FontQuad {

    private final float CHARACTER_UV_WIDTH = 7f / 128f;
    private final float CHARACTER_UV_HEIGHT = 12f / 128f;
    private final int VAO;
    private final int VBO;
    private final int EBO;

    private int indexCount;

    public FontQuad() {

        float[] vertexArray = new float[] {
                 0.5f,  0.5f,  -0.5f,  CHARACTER_UV_WIDTH,  CHARACTER_UV_HEIGHT, // top right
                 0.5f, -0.5f,  -0.5f,  CHARACTER_UV_WIDTH,  0.0f, // bottom right
                -0.5f, -0.5f,  -0.5f,  0.0f,  0.0f, // bottom left
                -0.5f,  0.5f,  -0.5f,  0.0f,  CHARACTER_UV_HEIGHT, // top left
        };

        int[] indexArray = new int[] {
                0, 1, 3,
                1, 2, 3
        };

        indexCount = indexArray.length;

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        glBindVertexArray(VAO);

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVao() {
        return VAO;
    }

    public void clean() {

        glDeleteBuffers(VBO);
        glDeleteBuffers(EBO);
        glDeleteVertexArrays(VAO);

    }

}
