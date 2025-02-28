package wins.insomnia.backyardrocketry.world.block.blockstate.types;

import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyBoolean;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyInteger;

public class BlockStateGrass extends BlockState {

	private final BlockStateProperty<?>[] PROPERTIES = new BlockStateProperty[] {
			new PropertyBoolean("flowers"),
	};

	public void setFlowers(boolean flowers) {
		((PropertyBoolean) PROPERTIES[0]).setValue(flowers);
	}

	@Override
	public BlockStateProperty<?>[] getProperties() {
		return PROPERTIES;
	}


}
