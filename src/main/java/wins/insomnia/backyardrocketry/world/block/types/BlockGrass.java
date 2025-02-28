package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateGrass;

public class BlockGrass extends Block {

	private static final BlockStateGrass BS = new BlockStateGrass();

	public BlockGrass() {
		super("Grass", "grass", BlockStateGrass.class, false, true, 40, BlockAudio.GENERIC_DIRT);
	}

	@Override
	public byte onPlace(int localBlockX, int localBlockY, int localBlockZ, Blocks.Face face) {

		float f = BlockModelData.getRandomFloatBasedOnBlockPosition(localBlockX, localBlockY, localBlockZ);

		BS.setFlowers(f < 0.2f);

		return BlockStateManager.getBlockStateIndex(Blocks.GRASS, BS);
	}
}
