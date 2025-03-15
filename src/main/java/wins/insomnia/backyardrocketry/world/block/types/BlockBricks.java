package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;

public class BlockBricks extends Block {
	public BlockBricks() {
		super("Bricks", "bricks", null, false, true, 120, BlockAudio.GENERIC_STONE);
	}


}
