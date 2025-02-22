package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.block.Block;

public class BlockWater extends Block {
	public BlockWater() {
		super("Water", "water", null, true, true, -1);
	}

	@Override
	public BoundingBox getBlockCollision() {
		return null;
	}
}
