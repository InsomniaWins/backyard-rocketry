package wins.insomnia.backyardrocketry.render.gui;

import wins.insomnia.backyardrocketry.render.IMesh;
import wins.insomnia.backyardrocketry.render.IRenderable;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class GuiMesh implements IRenderable, IMesh {

	protected boolean isClean;
	protected int vao;
	protected int vbo;
	protected int ebo;
	protected int indexCount;

	public GuiMesh() {
		vao = -1;
		vbo = -1;
		ebo = -1;
		indexCount = 0;
		isClean = true;
	}

	public GuiMesh(float[] vertexArray, int[] indexArray) {

		indexCount = indexArray.length;

		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		ebo = glGenBuffers();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

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

	public int getVbo() {
		return vbo;
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
		return true;
	}
}
