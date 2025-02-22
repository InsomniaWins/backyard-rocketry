package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;

public class BlockStone extends Block {
	public BlockStone() {
		super("Stone", "stone", null, false, true, 120, BlockAudio.GENERIC_STONE);
	}
}
