package wins.insomnia.backyardrocketry.world.block.blockstate;

import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;

import java.util.List;

public abstract class BlockState {


	public abstract BlockStateProperty<Object>[] getProperties();
	public String getStateString() {

		BlockStateProperty<?>[] properties = getProperties();

		StringBuilder stateName = new StringBuilder("{");

		for (int i = 0; i < properties.length; i++) {

			stateName.append(properties[i].getName())
					.append('=')
					.append(properties[i].getValue().toString());

			if (i < properties.length - 1) {
				stateName.append(", ");
			}

		}
		stateName.append('}');

		return stateName.toString();
	}

	@Override
	public String toString() {
		return getStateString();
	}
}
