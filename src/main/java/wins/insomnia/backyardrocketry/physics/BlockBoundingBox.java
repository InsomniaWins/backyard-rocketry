package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.world.old.Chunk;

public class BlockBoundingBox extends BoundingBox {

	public Chunk chunk;
	public Vector3i blockPosition;


	public BlockBoundingBox(BoundingBox boundingBox, Chunk chunk, Vector3i blockPosition) {
		this.getMin().set(boundingBox.getMin());
		this.getMax().set(boundingBox.getMax());
		this.chunk = chunk;
		this.blockPosition = blockPosition;
	}

}
