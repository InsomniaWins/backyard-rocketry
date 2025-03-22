package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.Math;
import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.entity.player.EntityPlayer;
import wins.insomnia.backyardrocketry.entity.player.PlayerInventoryManager;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.util.io.device.MouseInput;
import wins.insomnia.backyardrocketry.util.update.Updater;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;

public class GuiElementPlayerInventory extends GuiElement {


	private final EntityClientPlayer PLAYER;
	private final Texture INVENTORY_TEXTURE = TextureManager.getTexture("inventory");
	private final Texture SLOT_TEXTURE = TextureManager.getTexture("inventory_slot");
	private final Texture SLOT_HIGHLIGHT_TEXTURE = TextureManager.getTexture("inventory_slot_highlight");
	private final Texture ITEM_INFO_BACKGROUND_TEXTURE = TextureManager.getTexture("item_info_background");
	private final Texture SCROLL_BAR_TEXTURE = TextureManager.getTexture("scroll_bar");
	private static final int ITEMS_PER_ROW = 5;
	private static final int ROWS_ON_SCREEN = 4;
	private int currentRowIndex = 0; // current vertical-scroll value

	public GuiElementPlayerInventory(EntityClientPlayer player) {

		PLAYER = player;


	}


	public void register() {

		Updater.get().registerUpdateListener(this);
		Updater.get().registerFixedUpdateListener(this);
		Renderer.get().addRenderable(this);


	}

	public void unregister() {

		Updater.get().unregisterUpdateListener(this);
		Updater.get().unregisterFixedUpdateListener(this);
		Renderer.get().removeRenderable(this);


	}


	@Override
	public void fixedUpdate() {
		super.fixedUpdate();

		MouseInput mouseInput = MouseInput.get();
		currentRowIndex -= (int) mouseInput.getMouseScrollY();

		PlayerInventoryManager inventoryManager = getPlayer().getInventoryManager();
		int inventoryStackAmount = inventoryManager.getInventory().getStackAmount();
		int rowAmount = (inventoryStackAmount - 1) / ITEMS_PER_ROW;

		currentRowIndex = Math.clamp(0, rowAmount - (ROWS_ON_SCREEN - 1), currentRowIndex);

	}


	@Override
	public void render() {

		Renderer renderer = Renderer.get();

		int backgroundX = renderer.getCenterAnchorX() - INVENTORY_TEXTURE.getWidth() / 2;
		int backgroundY = renderer.getCenterAnchorY() - INVENTORY_TEXTURE.getHeight() / 2;
		TextureRenderer.drawGuiTexture(INVENTORY_TEXTURE, backgroundX, backgroundY);

		PlayerInventoryManager inventoryManager = getPlayer().getInventoryManager();



		// render items
		ItemStack[] inventoryItems = inventoryManager.getInventory().getItems();

		ItemStack hoveredItemStack = null;
		int itemInfoX = 0;
		int itemInfoY = 0;

		Vector2i mousePosition = Window.get().getViewportMouse();

		int maxRowIndex = ROWS_ON_SCREEN - 1; // only 4 rows on screen at a time
		int firstItemIndex = currentRowIndex * ITEMS_PER_ROW;

		for (int i = 0; i < inventoryItems.length; i++) {

			int itemIndex = firstItemIndex + i;

			if (itemIndex >= inventoryItems.length) break;

			ItemStack itemStack = inventoryItems[itemIndex];
			if (itemStack == null) continue;

			int columnIndex = i % ITEMS_PER_ROW;
			int rowIndex = i / ITEMS_PER_ROW;

			int x = backgroundX + 3 + columnIndex * 21;
			int y = backgroundY + 3 + rowIndex * 21;

			if (rowIndex > maxRowIndex) break;

			if (new Rectanglei(x - 1, y - 1, x + 21, y + 21).containsPoint(mousePosition)) {
				hoveredItemStack = itemStack;
				itemInfoX = x + 20;//mousePosition.x;
				itemInfoY = y - 1;//mousePosition.y;
				TextureRenderer.drawGuiTexture(SLOT_HIGHLIGHT_TEXTURE, x - 1, y - 1);
			} else {
				TextureRenderer.drawGuiTexture(SLOT_TEXTURE, x, y);
			}


			PlayerGui.renderItemIcon(itemStack.getItem(), x + 10, y + 10);

		}


		int scrollBarWidth = 4;
		int inventoryStackAmount = inventoryManager.getInventory().getStackAmount();
		int rowAmount = (inventoryStackAmount - 1) / ITEMS_PER_ROW;
		float scrollBarHeight = 83 * Math.min((ROWS_ON_SCREEN / (float) (rowAmount + 1)), 1);

		int scrollBarX = backgroundX + 112;
		int scrollBarY = backgroundY + 3 + Math.round(currentRowIndex * (scrollBarHeight / (float) ROWS_ON_SCREEN));


		TextureRenderer.drawGuiTextureFit(SCROLL_BAR_TEXTURE, scrollBarX, scrollBarY, scrollBarWidth, (int) scrollBarHeight);



		if (hoveredItemStack != null) {

			StringBuilder itemInfoString = new StringBuilder(hoveredItemStack.getItem().getName())
					.append("\nAmount: ").append(hoveredItemStack.getAmount());


			TextureRenderer.drawGuiTextureNineSlice(
					ITEM_INFO_BACKGROUND_TEXTURE,
					itemInfoX, itemInfoY,
					TextRenderer.getTextPixelWidth(itemInfoString.toString()) + 8, TextRenderer.getTextPixelHeight(itemInfoString.toString()) + 8,
					3, true
			);

			TextRenderer.drawText(itemInfoString.toString(), itemInfoX + 4, itemInfoY + 4);

		}

	}

	public EntityClientPlayer getPlayer() {
		return PLAYER;
	}

	@Override
	public int getRenderPriority() {
		return 2;
	}


}
