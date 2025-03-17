package wins.insomnia.backyardrocketry.entity.player;

import wins.insomnia.backyardrocketry.gui.elements.GuiElement;
import wins.insomnia.backyardrocketry.gui.elements.GuiElementPlayerInventory;

public class PlayerInventoryManager {

	private final IPlayer PLAYER;
	private boolean open = false;
	private GuiElementPlayerInventory guiElement;


	public PlayerInventoryManager(IPlayer player) {

		this.PLAYER = player;

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
