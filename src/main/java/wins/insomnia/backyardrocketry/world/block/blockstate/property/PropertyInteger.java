package wins.insomnia.backyardrocketry.world.block.blockstate.property;

import java.util.Arrays;

public class PropertyInteger extends BlockStateProperty<Integer> {

	private int minValue;
	private int maxValue;
	private int value;

	public PropertyInteger(String propertyName, int minValue, int maxValue) {
		super(propertyName);

		if (minValue > maxValue) {
			this.minValue = maxValue;
			this.maxValue = minValue;
		} else {
			this.minValue = minValue;
			this.maxValue = maxValue;
		}

	}


	public Integer getValue() {
		return value;
	}

	public void setValue(int value) {

		this.value = Math.min(Math.max(value, minValue), maxValue);

	}


	public int getMinValue() {
		return minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	@Override
	public int getPossibleCombinationsAmount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Integer[] getPossibleCombinations() {
		int range = maxValue - minValue + 1;
		Integer[] returnArray = new Integer[range];

		for (int i = 0; i < range; i++) {
			returnArray[i] = minValue + i;
		}

		return returnArray;
	}

	@Override
	public int getBitsForProperty() {
		return 32;
	}
}
