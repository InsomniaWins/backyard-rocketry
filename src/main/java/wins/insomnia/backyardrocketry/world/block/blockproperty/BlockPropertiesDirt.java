package wins.insomnia.backyardrocketry.world.block.blockproperty;

import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Block;

public class BlockPropertiesDirt extends BlockProperties {

	private static boolean blockIsGrass(int x, int y, int z) {
		return Block.GRASS == BitHelper.getBlockIdFromBlockState(World.getServerWorld().getBlockState(x, y, z));
	}

	private static boolean blockIsTransparent(int x, int y, int z) {
		return Block.isBlockTransparent(World.getServerWorld().getBlock(x, y, z));
	}

	/*
	public void onRandomTick(int blockState, Chunk chunk, int x, int y, int z) {

		int globalX = chunk.toGlobalX(x);
		int globalY = chunk.toGlobalY(y);
		int globalZ = chunk.toGlobalZ(z);

		if (y < Chunk.SIZE_Y - 1) {
			if (!Block.isBlockTransparent(chunk.getBlock(x, y + 1, z))) {
				return;
			}
		} else {
			if (!blockIsTransparent(globalX, globalY + 1, globalZ)) {
				return;
			}
		}

		if (blockIsGrass(globalX + 1, globalY + 1, globalZ) ||
				blockIsGrass(globalX - 1, globalY + 1, globalZ) ||
				blockIsGrass(globalX, globalY + 1, globalZ + 1) ||
				blockIsGrass(globalX, globalY + 1, globalZ - 1) ||

				// the bottom y-layer check should check if neighbor has block above it
				// if it does, then it shouldn't spread
				(blockIsGrass(globalX - 1, globalY - 1, globalZ) &&
						blockIsTransparent(globalX - 1, globalY, globalZ)) ||
				(blockIsGrass(globalX + 1, globalY - 1, globalZ) &&
						blockIsTransparent(globalX + 1, globalY, globalZ)) ||
				(blockIsGrass(globalX, globalY - 1, globalZ - 1) &&
						blockIsTransparent(globalX, globalY, globalZ - 1)) ||
				(blockIsGrass(globalX, globalY - 1, globalZ + 1) &&
						blockIsTransparent(globalX, globalY, globalZ + 1)) ||

				blockIsGrass(globalX - 1, globalY, globalZ - 1) ||
				blockIsGrass(globalX - 1, globalY, globalZ) ||
				blockIsGrass(globalX - 1, globalY, globalZ + 1) ||
				blockIsGrass(globalX, globalY, globalZ - 1) ||
				blockIsGrass(globalX, globalY, globalZ + 1) ||
				blockIsGrass(globalX + 1, globalY, globalZ - 1) ||
				blockIsGrass(globalX + 1, globalY, globalZ) ||
				blockIsGrass(globalX + 1, globalY, globalZ + 1)
		) {
			chunk.setBlock(x,y,z, Block.GRASS);
		}
	}
*/
}
