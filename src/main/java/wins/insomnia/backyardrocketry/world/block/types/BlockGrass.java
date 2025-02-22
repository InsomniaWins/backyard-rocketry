package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;

public class BlockGrass extends Block {
	public BlockGrass() {
		super("Grass", "grass", null, false, true, 40, BlockAudio.GENERIC_DIRT);
	}
}
