package wins.insomnia.backyardrocketry.world.block.blockstate.property;

public class PropertyBoolean extends BlockStateProperty<Boolean> {

	private boolean value = false;

	public PropertyBoolean(String propertyName) {
		this(propertyName, false);
	}

	public PropertyBoolean(String propertyName, boolean defaultValue) {
		super(propertyName);
		value = defaultValue;
	}

	public Boolean getValue() {
		return value;
	}

	@Override
	public void setValue(Boolean value) {
		this.value = value;

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
