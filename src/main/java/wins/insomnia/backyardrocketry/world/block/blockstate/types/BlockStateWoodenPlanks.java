package wins.insomnia.backyardrocketry.world.block.blockstate.types;

import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyInteger;

public class BlockStateWoodenPlanks extends BlockState {

	private final BlockStateProperty[] PROPERTIES = new BlockStateProperty[] {
			new PropertyInteger("planks_style", 0, 1),
	};

	@Override
	public BlockStateProperty<Object>[] getProperties() {
		return PROPERTIES;
	}


}
