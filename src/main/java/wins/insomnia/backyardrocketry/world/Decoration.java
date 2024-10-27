package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.physics.BoundingBox;

public class Decoration {

	private final BoundingBox BOUNDING_BOX;
	public final int[][] BLOCK_DATA;

	public Decoration(BoundingBox boundingBox, int[][] blockData) {

		this.BOUNDING_BOX = boundingBox;
		this.BLOCK_DATA = blockData;

	}

	public BoundingBox getBoundingBox() {
		return new BoundingBox(BOUNDING_BOX);
	}

}
