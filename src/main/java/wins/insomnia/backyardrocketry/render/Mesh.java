package wins.insomnia.backyardrocketry.render;


import wins.insomnia.backyardrocketry.Main;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh implements IRenderable, IMesh {

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

    public void clean() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            throw new RuntimeException("Tried cleaning openGL data on another thread!");
        }

        isClean = true;

        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);

    }

    public boolean isClean() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            synchronized (this) {
                return isClean;
            }
        }

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
        render(GL_TRIANGLES);
    }

    public void render(int primitveRenderType) {

        if (vao < 0) {
            return;
        }

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(primitveRenderType, getIndexCount(), GL_UNSIGNED_INT, 0);

    }

    @Override
    public boolean hasTransparency() {
        return false;
    }
}
