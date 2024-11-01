package wins.insomnia.backyardrocketry.inventory;

import wins.insomnia.backyardrocketry.item.ItemStack;

public class Inventory {

	private final ItemStack[] ITEMS;

	private final int SIZE;

	public Inventory(int size) {
		SIZE = size;
		ITEMS = new ItemStack[SIZE];
	}

	public int getSize() {
		return SIZE;
	}

	public ItemStack setSlot(int slotIndex, ItemStack itemStack) {

		ItemStack previousStack = getItemStackInSlot(slotIndex);
		ITEMS[slotIndex] = itemStack;
		return previousStack;

	}

	public ItemStack[] getItems() {
		return ITEMS;
	}

	public ItemStack getItemStackInSlot(int slotIndex) {
		return ITEMS[slotIndex];
	}


}
