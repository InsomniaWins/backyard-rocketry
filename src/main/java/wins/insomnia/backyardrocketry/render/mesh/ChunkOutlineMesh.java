package wins.insomnia.backyardrocketry.render.mesh;

import org.lwjgl.opengl.GL30;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class ChunkOutlineMesh {

	private static final float MIN = 0.25f;
	private static final float MAX = Chunk.SIZE_X - 0.25f;

	private static final float[] OUTLINE_VERTS = new float[] {
			MIN, MIN, MIN,
			MIN, MIN, MAX,
			MIN, MAX, MAX,
			MIN, MAX, MIN,

			MAX, MIN, MIN,
			MAX, MIN, MAX,
			MAX, MAX, MAX,
			MAX, MAX, MIN
	};
	private static final int[] OUTLINE_INDICES = new int[] {
			0, 1,
			1, 2,
			2, 3,
			3, 0,

			4, 5,
			5, 6,
			6, 7,
			7, 4,

			0, 4,
			1, 5,
			2, 6,
			3, 7
	};




	private static final int OUTLINE_VAO;
	private static final int OUTLINE_EBO;
	private static final int OUTLINE_VBO;
	static {

		OUTLINE_VAO = GL30.glGenVertexArrays();
		OUTLINE_VBO = glGenBuffers();
		OUTLINE_EBO = glGenBuffers();

		glBindVertexArray(OUTLINE_VAO);

		glBindBuffer(GL_ARRAY_BUFFER, OUTLINE_VBO);
		glBufferData(GL_ARRAY_BUFFER, OUTLINE_VERTS, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, OUTLINE_EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, OUTLINE_INDICES, GL_STATIC_DRAW);

		int totalDataSize = 3 * Float.BYTES;
		glVertexAttribPointer(0, 3, GL_FLOAT, false, totalDataSize, 0);
	}

	public static int getVertexCount() {
		return OUTLINE_VERTS.length;
	}
	public static int getIndexCount() {
		return OUTLINE_INDICES.length;
	}
	public static int getVao() {
		return OUTLINE_VAO;
	}

	public static int getVbo() {
		return OUTLINE_VBO;
	}
	public static void clean() {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			throw new RuntimeException("Tried cleaning openGL data on another thread!");
		}

		GL30.glDeleteVertexArrays(OUTLINE_VAO);
		glDeleteBuffers(OUTLINE_EBO);
		glDeleteBuffers(OUTLINE_VBO);

	}


}
