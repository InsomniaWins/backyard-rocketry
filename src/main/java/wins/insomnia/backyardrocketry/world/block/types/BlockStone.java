package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateStone;

public class BlockStone extends Block {

	private static final BlockStateStone BS = new BlockStateStone();

	public BlockStone() {
		super("Stone", "stone", BlockStateStone.class, false, true, 120, BlockAudio.GENERIC_STONE);
	}


	@Override
	public byte onPlace(int localBlockX, int localBlockY, int localBlockZ, Blocks.Face face) {

		BS.setVariation(BlockModelData.getRandomBlockNumberBasedOnBlockPosition(localBlockX, localBlockY, localBlockZ) % 3);

		return BlockStateManager.getBlockStateIndex(Blocks.STONE, BS);
	}

}
