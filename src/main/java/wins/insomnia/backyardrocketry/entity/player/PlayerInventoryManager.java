package wins.insomnia.backyardrocketry.entity.player;

import wins.insomnia.backyardrocketry.gui.elements.GuiElement;
import wins.insomnia.backyardrocketry.gui.elements.GuiElementPlayerInventory;
import wins.insomnia.backyardrocketry.inventory.Inventory;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;

public class PlayerInventoryManager {

	private final IPlayer PLAYER;
	private boolean open = false;
	private GuiElementPlayerInventory guiElement;
	private final Inventory INVENTORY;
	private Item[] hotbarItems = new Item[10];
	private int currentHotbarItemIndex = 0;

	public PlayerInventoryManager(IPlayer player) {

		this.PLAYER = player;
		INVENTORY = new Inventory(5000);

	}

	public Item getHotbarItem(int hotbarIndex) {
		return hotbarItems[hotbarIndex];
	}

	public Item getCurrentHotbarItem() {
		return getHotbarItem(currentHotbarItemIndex);
	}


	public ItemStack getItemStack(Item item) {

		return getInventory().getItemStack(item);

	}


	public Inventory getInventory() {
		return INVENTORY;
	}


	public void toggleInventory() {
		if (isClosed()) {
			openInventory();
		} else {
			closeInventory();
		}
	}

	public void openInventory() {
		if (isOpen()) return;

		guiElement = new GuiElementPlayerInventory();
		guiElement.register();

		open = true;
	}

	public void closeInventory() {
		if (isClosed()) return;

		guiElement.unregister();
		guiElement = null;

		open = false;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean isClosed() {
		return !isOpen();
	}

	public IPlayer getPlayer() {
		return PLAYER;
	}
}
