package wins.insomnia.backyardrocketry.world.block.blockstate.property;

public class PropertyBoolean extends BlockStateProperty<Boolean> {

	private boolean value = false;

	public PropertyBoolean(String propertyName) {
		super(propertyName);
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}

	@Override
	public int getPossibleCombinationsAmount() {
		return 2;
	}

	@Override
	public Boolean[] getPossibleCombinations() {
		return new Boolean[] {false, true};
	}


	@Override
	public int getBitsForProperty() {
		return 1;
	}
}
