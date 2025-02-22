package wins.insomnia.backyardrocketry.render;

import org.joml.Math;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Blocks;

public class TargetBlockOutlineMesh {

	public static float getOutlineAlpha() {
		return ((float) (Math.sin(Updater.getCurrentTime() * 5f) + 1) / 2f);
	}

	private static final int[] INDICES = new int[] {
			0, 1,
			0, 2,
			2, 3,
			1, 3,
	};

	private static final Mesh POS_Y_MESH = new Mesh(
			new float[] {
					0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
					0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
			},
			INDICES
	);

	private static final Mesh NEG_Y_MESH = new Mesh(
			new float[] {
					0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
			},
			INDICES
	);

	private static final Mesh POS_X_MESH = new Mesh(
			new float[] {
					1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
			},
			INDICES
	);

	private static final Mesh NEG_X_MESH = new Mesh(
			new float[] {
					0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
					0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
					0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
			},
			INDICES
	);

	private static final Mesh POS_Z_MESH = new Mesh(
			new float[] {
					0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
					0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
			},
			INDICES
	);

	private static final Mesh NEG_Z_MESH = new Mesh(
			new float[] {
					0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
					1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			},
			new int[] {
					0, 1,
					0, 2,
					2, 3,
					1, 3,
			}
	);

	public static Mesh get(Blocks.Face face) {

		if (face == Blocks.Face.NEG_X) {
			return NEG_X_MESH;
		}

		if (face == Blocks.Face.POS_X) {
			return POS_X_MESH;
		}

		if (face == Blocks.Face.NEG_Y) {
			return NEG_Y_MESH;
		}

		if (face == Blocks.Face.POS_Y) {
			return POS_Y_MESH;
		}

		if (face == Blocks.Face.NEG_Z) {
			return NEG_Z_MESH;
		}

		if (face == Blocks.Face.POS_Z) {
			return POS_Z_MESH;
		}

		return null;
	}


	public static void init() {

	}

	public static void clean() {

		POS_X_MESH.clean();
		POS_Y_MESH.clean();
		POS_Z_MESH.clean();
		NEG_X_MESH.clean();
		NEG_Y_MESH.clean();
		NEG_Z_MESH.clean();

	}
}
