package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.lighting.ChunkLighting;

public class BlockLight extends Block {

	public BlockLight() {
		super("Light", "light", null, true, true, 20, BlockAudio.GENERIC_GLASS);
	}

	@Override
	public short getMinimumLightLevel(byte blockState, int localBlockX, int localBlockY, int localBlockZ) {

		int random = BlockModelData.getRandomBlockNumberBasedOnBlockPosition(localBlockX, localBlockY, localBlockZ) % 3;

		if (random == 0) {
			return (short) 0b1111_0000_0000_0000;
		} else if (random == 1) {
			return (short) 0b0000_1111_0000_0000;
		}

		return 0b0000_0000_1111_0000;
	}

}
