package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;

public class BlockLight extends Block {

	public BlockLight() {
		super("Light", "light", null, true, true, 20, BlockAudio.GENERIC_GLASS);
	}

	@Override
	public short getMinimumLightLevel() {
		return (short) 0b1100_1100_0011_0000;
	}

}
