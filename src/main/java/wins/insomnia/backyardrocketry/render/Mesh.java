package wins.insomnia.backyardrocketry.render;


import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.util.OpenGLWrapper;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.glCopyBufferSubData;

public class Mesh implements IRenderable, IMesh {

    protected AtomicBoolean isClean;
    protected int vao;
    protected int vbo;
    protected int ebo;
    protected int indexCount;
    protected int vertexCount;
    protected float[] vertexArray;
    protected int[] indexArray;

    public Mesh(Mesh mesh) {

        float[] vertexArray = mesh.vertexArray;
        int[] indexArray = mesh.indexArray;

        indexCount = indexArray.length;
        vertexCount = vertexArray.length;

        vao = OpenGLWrapper.glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        isClean = new AtomicBoolean(false);
    }

    public Mesh() {
        vao = -1;
        vbo = -1;
        ebo = -1;
        indexCount = 0;
        vertexCount = 0;
        isClean = new AtomicBoolean(true);
    }

    public Mesh(float[] vertexArray, int[] indexArray) {

        this.indexArray = indexArray;
        this.vertexArray = vertexArray;

        indexCount = indexArray.length;
        vertexCount = vertexArray.length;

        vao = OpenGLWrapper.glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        isClean = new AtomicBoolean(false);
    }

    public void clean() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            throw new RuntimeException("Tried cleaning openGL data on another thread!");
        }

        isClean.set(true);

        OpenGLWrapper.glDeleteVertexArrays(vao);
        vao = -1;
        glDeleteBuffers(ebo);
        ebo = -1;
        glDeleteBuffers(vbo);
        vbo = -1;

        indexCount = 0;
        vertexCount = 0;
    }

    @Override
    public int getRenderPriority() {
        return 0;
    }

    public boolean isClean() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            throw new RuntimeException("Tried checking if openGL data is clean on another thread!");
        }

        return isClean.get();
    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVao() {
        return vao;
    }

    @Override
    public boolean shouldRender() {
        return !isClean();
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
