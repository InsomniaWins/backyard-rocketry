package wins.insomnia.backyardrocketry.item;

public class ItemStack {
	private Item item;
	private int volume;


	public ItemStack(Item item, int volume) {
		this.item = item;
		this.volume = volume;
	}

	public Item getItem() {
		return item;
	}

	public int getVolume() {
		return volume;
	}

	public int getMaxVolume() {
		return item.getMaxVolume();
	}

	public int getRemainingVolumeForFullStack() {
		return item.getMaxVolume() - volume;
	}

	public float getStackDisplayAmount() {
		return getVolume() / (float) getItem().getVolumePerItem();
	}

}
