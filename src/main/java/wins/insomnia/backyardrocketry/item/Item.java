package wins.insomnia.backyardrocketry.item;

public class Item {

	private final byte ID;
	private final String NAME;
	private final String ID_SYNONYM;
	private final int MAX_VOLUME;
	private final int VOLUME_PER_ITEM;
	private static byte nextAvailableItemIdForRegistration = Byte.MIN_VALUE;




	public static final Item COBBLESTONE = registerItem(
			"Cobblestone",
			"cobblestone",
			100,
			9900
	);




	public Item(byte itemId, String itemName, String itemSynonym, int volumePerItem, int maxItemVolume) {
		this.ID = itemId;
		this.NAME = itemName;
		this.ID_SYNONYM = itemSynonym;
		this.MAX_VOLUME = maxItemVolume;
		this.VOLUME_PER_ITEM = volumePerItem;
	}

	public int getMaxVolume() {
		return MAX_VOLUME;
	}

	public int getVolumePerItem() {
		return VOLUME_PER_ITEM;
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

	public static Item registerItem(String itemName, String itemSynonym, int volumePerItem, int maxVolume) {
		return new Item(
				nextAvailableItemIdForRegistration++,
				itemName,
				itemSynonym,
				volumePerItem,
				maxVolume
		);
	}
}
