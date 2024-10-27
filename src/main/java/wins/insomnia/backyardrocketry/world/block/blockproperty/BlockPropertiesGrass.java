package wins.insomnia.backyardrocketry.world.block.blockproperty;

import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Block;

public class BlockPropertiesGrass extends BlockProperties {

	private static boolean blockIsTransparent(int x, int y, int z) {
		return Block.isBlockTransparent(BitHelper.getBlockIdFromBlockState(World.get().getBlockState(x, y, z)));
	}

	@Override
	public String getBlockModelName(int blockState) {

		if (BitHelper.getBit(blockState, 0) == 1) return "deep";

		return "default";
	}


	@Override
	public int onPlace(int blockState, Chunk chunk, int x, int y, int z) {
		int localX = chunk.toLocalX(x);
		int localY = chunk.toLocalY(y);
		int localZ = chunk.toLocalZ(z);

		int randomNum = BlockModelData.getRandomBlockNumberBasedOnBlockPosition(localX, localY, localZ);
		boolean deep = randomNum % 2 == 0;

		return getBlockState(blockState, deep);
	}

	/*
	@Override
	public void onRandomTick(int blockState, Chunk chunk, int x, int y, int z) {

		if (y < Chunk.SIZE_Y - 1) {
			if (!Block.isBlockTransparent(chunk.getBlock(x, y + 1, z))) {
				chunk.setBlock(x, y, z, Block.DIRT);
			}
		} else if (!blockIsTransparent(chunk.toGlobalX(x), chunk.toGlobalY(y + 1), chunk.toGlobalZ(z))) {
			chunk.setBlock(x, y, z, Block.DIRT);
		}

	}

	@Override
	public int getBlockState(int currentBlockState, Object... properties) {
		if ((Boolean)properties[0]) {
			currentBlockState = BitHelper.setBitToOne(currentBlockState, 0);
		} else {
			currentBlockState = BitHelper.setBitToZero(currentBlockState, 0);
		}

		return currentBlockState;
	}

	 */
}
