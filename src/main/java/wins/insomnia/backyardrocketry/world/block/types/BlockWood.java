package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;

public class BlockWood extends Block {
	public BlockWood() {
		super("Wood", "wood", null, false, true, 90, BlockAudio.GENERIC_WOOD);
	}
}
