package wins.insomnia.backyardrocketry.item;

public class Item {
	private final String NAME;
	private final String ID_SYNONYM;
	private final int MAX_VOLUME;
	private final int VOLUME_PER_ITEM;


	public Item(String itemName, String itemSynonym, int volumePerItem, int maxItemVolume) {
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
		return Items.getItemIdFromSynonym(getIdSynonym());
	}


}
