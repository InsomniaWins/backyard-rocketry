package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.IUpdateListener;
import wins.insomnia.backyardrocketry.util.Transform;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh implements IRenderable {

    protected boolean isClean;
    protected int vao;
    protected int vbo;
    protected int ebo;
    protected int indexCount;

    public Mesh() {
        vao = -1;
        vbo = -1;
        ebo = -1;
        indexCount = 0;
        isClean = true;
    }

    public Mesh(float[] vertexArray, int[] indexArray) {

        indexCount = indexArray.length;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        isClean = false;
    }

    public synchronized void clean() {

        isClean = true;

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);

    }

    public synchronized boolean isClean() {
        return isClean;
    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVao() {
        return vao;
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    @Override
    public void render() {
        if (vao < 0) {
            return;
        }

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);
    }
}
