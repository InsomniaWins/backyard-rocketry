package wins.insomnia.backyardrocketry.entity;

import wins.insomnia.backyardrocketry.item.ItemStack;

public class EntityItem extends Entity {

	private ItemStack itemStack;

	public EntityItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	// returns the item-stack previously stored by this item entity
	public ItemStack setItemStack(ItemStack itemStack) {
		ItemStack previousItemStack = this.itemStack;
		this.itemStack = itemStack;
		return previousItemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

}
