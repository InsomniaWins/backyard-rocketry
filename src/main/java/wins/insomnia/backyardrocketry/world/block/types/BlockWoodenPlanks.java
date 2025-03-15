package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateWoodenPlanks;

public class BlockWoodenPlanks extends Block {
	public BlockWoodenPlanks() {
		super("Wooden Planks", "wooden_planks", BlockStateWoodenPlanks.class, false, true, 90, BlockAudio.GENERIC_WOOD);
	}

}
