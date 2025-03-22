package wins.insomnia.backyardrocketry.world.block.blockstate.types;

import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyBoolean;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyInteger;

public class BlockStateStone extends BlockState {

	private final BlockStateProperty[] PROPERTIES = new BlockStateProperty[] {
			new PropertyInteger("variation", 0, 2),
	};

	public void setVariation(int variation) {
		PROPERTIES[0].setValue(variation);
	}

	@Override
	public BlockStateProperty<Object>[] getProperties() {
		return PROPERTIES;
	}


}
