package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;

public class BlockAir extends Block {
	public BlockAir() {
		super("Air", null, null, true, false, -1);
	}

	@Override
	public BoundingBox getBlockCollision() {
		return null;
	}
}
