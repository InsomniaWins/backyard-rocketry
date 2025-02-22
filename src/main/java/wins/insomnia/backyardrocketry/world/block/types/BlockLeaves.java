package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;

public class BlockLeaves extends Block {
	public BlockLeaves() {
		super("Leaves", "leaves", null, true, false, 20, BlockAudio.GENERIC_LEAVES);
	}
}
