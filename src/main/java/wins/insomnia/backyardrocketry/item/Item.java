package wins.insomnia.backyardrocketry.item;

public class Item {
	private final String NAME;
	private final String ID_SYNONYM;
	private final int MAX_STACK_AMOUNT;

	public Item(String itemName, String itemSynonym) {
		this(itemName, itemSynonym, 64);
	}

	public Item(String itemName, String itemSynonym, int maxStackAmount) {
		this.NAME = itemName;
		this.ID_SYNONYM = itemSynonym;
		this.MAX_STACK_AMOUNT = maxStackAmount;
	}

	public int getMaxStackAmount() {
		return MAX_STACK_AMOUNT;
	}

	public String getIdSynonym() {
		return ID_SYNONYM;
	}

	public String getName() {
		return NAME;
	}

	public int getId() {
		return Items.getItemIdFromSynonym(getIdSynonym());
	}


}
