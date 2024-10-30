package wins.insomnia.backyardrocketry.entity.player;

public class PlayerInventoryManager {

	private final IPlayer PLAYER;
	private boolean open = false;


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

		open = true;
	}

	public void closeInventory() {
		if (isClosed()) return;
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
