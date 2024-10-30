package wins.insomnia.backyardrocketry.item;

public class Item {

	private final byte ID;
	private final String NAME;
	private final String ID_SYNONYM;
	private final float WEIGHT;
	private static byte nextAvailableItemIdForRegistration = Byte.MIN_VALUE;




	public static final Item COBBLESTONE = registerItem(
			"Cobblestone",
			"cobblestone",
			1f
	);




	public Item(byte itemId, String itemName, String itemSynonym, float itemWeight) {
		this.ID = itemId;
		this.NAME = itemName;
		this.ID_SYNONYM = itemSynonym;
		this.WEIGHT = itemWeight;
	}

	public float getWeight() {
		return WEIGHT;
	}

	public String getIdSynonym() {
		return ID_SYNONYM;
	}

	public String getName() {
		return NAME;
	}

	public int getId() {
		return ID;
	}

	public static Item registerItem(String itemName, String itemSynonym, float itemWeight) {
		return new Item(
				nextAvailableItemIdForRegistration++,
				itemName,
				itemSynonym,
				itemWeight
		);
	}
}
