package wins.insomnia.backyardrocketry.world.block.blockstate.property;

public abstract class BlockStateProperty<T> {

	private String name;

	public BlockStateProperty(String propertyName) {
		this.name = propertyName;
	}


	public String getName() {
		return name;
	}

	public abstract int getPossibleCombinationsAmount();

	public abstract T[] getPossibleCombinations();

	public abstract T getValue();
	public abstract void setValue(T value);
	public int getBitsForProperty() {

		return (int) (Math.log(getPossibleCombinationsAmount()) / Math.log(2));

	}

	@Override
	public String toString() {
		return getName() + " : " + getValue();
	}

}
