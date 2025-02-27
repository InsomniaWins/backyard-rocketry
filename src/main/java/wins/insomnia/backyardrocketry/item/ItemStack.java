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

	public double getKilograms() {
		return getItem().getKilogramsPerLiter() * getVolume();
	}

	public float getStackDisplayAmount() {
		return getVolume() / (float) getItem().getVolumePerItem();
	}

	public double getItemAmount() {
		return getVolume() / (double) item.getVolumePerItem();
	}

	@Override
	public String toString() {
		return "ItemStack{" + item.getName() + " x " + getItemAmount() + " m^3 (" + volume + " liters)}";
	}

}
