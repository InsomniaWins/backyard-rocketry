package wins.insomnia.backyardrocketry.item;

import org.joml.Math;

public class ItemStack {
	private Item item;
	private int amount;


	public ItemStack(Item item, int amount) {
		this.item = item;
		setAmount(amount);
	}

	public Item getItem() {
		return item;
	}

	public void setAmount(int amount) {
		this.amount = Math.clamp(0, item.getMaxStackAmount(), amount);
	}

	public void combine(ItemStack stack) {
		if (stack.getItem() != item) return;

		int amountAvailable = item.getMaxStackAmount() - getAmount();
		int amountCombined = Math.min(stack.amount, amountAvailable);

		stack.amount -= amountCombined;
		amount += amountCombined;
	}

	public int getAvailableAmount() {
		return getItem().getMaxStackAmount() - amount;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "ItemStack{" + item.getName() + " x " + getAmount() + "}";
	}

}
