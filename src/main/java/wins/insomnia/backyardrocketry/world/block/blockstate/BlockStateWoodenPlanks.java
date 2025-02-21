package wins.insomnia.backyardrocketry.world.block.blockstate;

import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyInteger;

public class BlockStateWoodenPlanks extends BlockState {

	public static final int PROPERTY_ALT_TEXTURE_INDEX = 0;

	private final BlockStateProperty<?>[] PROPERTIES = new BlockStateProperty[] {
			new PropertyInteger("planks_style", 0, 1),
	};


	public void setAltTextureIndex(int index) {
		((PropertyInteger) PROPERTIES[PROPERTY_ALT_TEXTURE_INDEX]).setValue(index);
	}

	public int getAltTextureIndex() {
		return ((PropertyInteger) PROPERTIES[PROPERTY_ALT_TEXTURE_INDEX]).getValue();
	}


	@Override
	public BlockStateProperty<?>[] getProperties() {
		return PROPERTIES;
	}


}
