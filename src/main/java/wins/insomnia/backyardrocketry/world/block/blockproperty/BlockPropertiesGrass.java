package wins.insomnia.backyardrocketry.world.block.blockproperty;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.Chunk;

public class BlockPropertiesGrass extends BlockProperties {
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

	@Override
	public int onTick(int blockState, Chunk chunk, int x, int y, int z) {
		return blockState;
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
}
