package wins.insomnia.backyardrocketry.inventory;





public class Inventory {

	private final int MAXIMUM_WEIGHT;

	public Inventory(int maximumWeight) {
		MAXIMUM_WEIGHT = maximumWeight;
	}

	public int getCurrentWeight() {
		return 0;
	}

	public int getMaximumWeight() {
		return MAXIMUM_WEIGHT;
	}

}
