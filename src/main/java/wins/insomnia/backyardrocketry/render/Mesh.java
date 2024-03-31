package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh implements IRenderable {

    private int vao;
    private int indexCount;
    private Matrix4f modelMatrix;

    public Mesh(float[] vertexArray, int[] indexArray) {

        indexCount = indexArray.length;

        vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        modelMatrix = new Matrix4f().identity();
    }

    public void clean() {



    }

    public int getIndexCount() {
        return indexCount;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public int getVao() {
        return vao;
    }

    @Override
    public void render() {

    }
}
