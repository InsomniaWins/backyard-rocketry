package wins.insomnia.backyardrocketry.inventory;


import org.checkerframework.checker.units.qual.A;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;

import java.util.ArrayList;

public class Inventory {

	private final double MAXIMUM_WEIGHT;
	private final ArrayList<ItemStack> ITEMS;

	public Inventory(double maximumWeight) {
		MAXIMUM_WEIGHT = maximumWeight;
		ITEMS = new ArrayList<>();
	}

	public double getWeight() {

		double weight = 0.0;

		for (ItemStack itemStack : ITEMS) {
			weight += itemStack.getKilograms();
		}

		return weight;
	}


	// if not all of itemStack can be added, the volume will be the amount not added
	public void addItemStack(ItemStack itemStack) {

		int volumeToAdd = (int) ((getMaximumWeight() - getWeight()) / itemStack.getItem().getKilogramsPerLiter());
		itemStack.removeVolume(volumeToAdd);



		ItemStack existingStack = getItemStack(itemStack.getItem());

		if (existingStack == null || existingStack.getVolume() == 0) {
			ITEMS.add(new ItemStack(itemStack.getItem(), volumeToAdd));
			return;
		}


		existingStack.addVolume(volumeToAdd);

	}

	public ItemStack getItemStack(Item item) {

		for (ItemStack itemStack : ITEMS) {
			if (itemStack.getItem() == item) {
				return itemStack;
			}
		}

		return null;
	}


	public int getCurrentWeight() {
		return 0;
	}

	public double getMaximumWeight() {
		return MAXIMUM_WEIGHT;
	}

}
