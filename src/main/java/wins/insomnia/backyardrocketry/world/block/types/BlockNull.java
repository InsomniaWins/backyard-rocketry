package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.physics.BoundingBox;

public class BlockNull extends BlockWorldBorder {
	@Override
	public BoundingBox getBlockCollision() {
		return null;
	}
}
