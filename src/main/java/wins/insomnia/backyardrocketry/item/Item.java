package wins.insomnia.backyardrocketry.item;

public class Item {
	private final String NAME;
	private final String ID_SYNONYM;
	private final int VOLUME_PER_ITEM;
	private final double KILOGRAMS_PER_LITER;

	public Item(String itemName, String itemSynonym, int volumePerItem, double kilosPerLiter) {
		this.NAME = itemName;
		this.ID_SYNONYM = itemSynonym;
		this.VOLUME_PER_ITEM = volumePerItem;

		this.KILOGRAMS_PER_LITER = kilosPerLiter;
	}

	public double getKilogramsPerLiter() {
		return KILOGRAMS_PER_LITER;
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
