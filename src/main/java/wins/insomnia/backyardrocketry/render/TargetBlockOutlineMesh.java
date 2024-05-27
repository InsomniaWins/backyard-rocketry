package wins.insomnia.backyardrocketry.render;

import wins.insomnia.backyardrocketry.world.block.Block;

public class TargetBlockOutlineMesh {


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

	public static Mesh get(Block.Face face) {

		switch (face) {
			case NEG_Y -> {
				return NEG_Y_MESH;
			}
			case POS_X -> {
				return POS_X_MESH;
			}
			case NEG_X -> {
				return NEG_X_MESH;
			}
			case POS_Z -> {
				return POS_Z_MESH;
			}
			case NEG_Z -> {
				return NEG_Z_MESH;
			}
			default -> {
				return POS_Y_MESH;
			}
		}

	}

}
