package wins.insomnia.backyardrocketry.world.block.blockproperty;

import wins.insomnia.backyardrocketry.world.Chunk;

public class BlockProperties {

	public String getBlockModelName(int blockState) {
		return "default";
	}

	public int onPlace(int blockState, Chunk chunk, int x, int y, int z) {
		return blockState;
	}

	public int onTick(int blockState, Chunk chunk, int x, int y, int z) {
		return blockState;
	}

	public int getBlockState(int currentBlockState, Object... properties) {
		return currentBlockState;
	}
}
