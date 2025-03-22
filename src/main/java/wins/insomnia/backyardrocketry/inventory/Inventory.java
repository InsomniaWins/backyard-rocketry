package wins.insomnia.backyardrocketry.inventory;


import org.checkerframework.checker.units.qual.A;
import org.joml.Math;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.audio.AudioPlayer;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
	private final int MAX_STACK_AMOUNT;
	private final ArrayList<ItemStack> ITEMS;

	public Inventory(int maxStackAmount) {
		MAX_STACK_AMOUNT = maxStackAmount;
		ITEMS = new ArrayList<>();
	}


	public ItemStack[] getItems() {
		return ITEMS.toArray(new ItemStack[0]);
	}

	public int getMaxStackAmount() {
		return MAX_STACK_AMOUNT;
	}

	public int getStackAmount() {
		return ITEMS.size();
	}


	public void addItemStack(ItemStack itemStack) {

		AudioPlayer audioPlayer = AudioManager.get().playAudio(
				World.RANDOM.nextInt(2) == 0 ? AudioManager.get().getAudioBuffer("inventory_pickup_0") : AudioManager.get().getAudioBuffer("inventory_pickup_1"),
				false, true, true);
		audioPlayer.setGain(0.2f);

		for (ItemStack stack : ITEMS) {

			stack.combine(itemStack);

			if (itemStack.getAmount() <= 0) return;

		}

		// still some items remain in itemStack
		if (ITEMS.size() >= MAX_STACK_AMOUNT) return;

		ITEMS.add(new ItemStack(itemStack.getItem(), itemStack.getAmount()));
		itemStack.setAmount(0);
	}

	public ItemStack getItemStack(Item item) {

		for (ItemStack itemStack : ITEMS) {
			if (itemStack.getItem() == item) {
				return itemStack;
			}
		}

		return null;
	}

}
