package wins.insomnia.backyardrocketry.world.blockproperty;

import wins.insomnia.backyardrocketry.util.BitHelper;

public class BlockProperties {
	int blockProperties;

	public BlockProperties(int blockProperties) {
		this.blockProperties = blockProperties;
	}

	public static BlockProperties getBlockPropertiesFromBlockState(int blockState) {
		int properties = BitHelper.getPropertiesFromBlockState(blockState);
		return new BlockProperties(properties);
	}

	public int getPropertiesInt() {
		return blockProperties;
	}
}
