package wins.insomnia.backyardrocketry.render.gui;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextureManager;
import wins.insomnia.backyardrocketry.util.TestPlayer;
import wins.insomnia.backyardrocketry.world.block.Block;

public class PlayerGui implements IGuiRenderable {

	private TestPlayer player;

	public PlayerGui(TestPlayer player) {
		this.player = player;
	}

	@Override
	public void render() {

		Renderer renderer = Renderer.get();
		TextureManager textureManager = TextureManager.get();

		int hotbarX = renderer.getCenterAnchorX() - 123;
		int hotbarY = renderer.getBottomAnchor() - 30;
		int selectedHotbarSlotX = hotbarX + player.getCurrentHotbarSlot() * 24;
		renderer.drawGuiTexture(textureManager.HOTBAR_TEXTURE, hotbarX, hotbarY);
		renderer.drawGuiTexture(textureManager.HOTBAR_SLOT_TEXTURE, selectedHotbarSlotX, hotbarY);



		// render hotbar items
		for (int i = 0; i < 10; i++) {

			int currentBlock = player.getHotbarSlotContents(i);

			BlockModelData blockModelData = BlockModelData.getBlockModel(currentBlock, 0, 0, 0);

			if (blockModelData == null) continue;

			String iconName = blockModelData.getTextures().get("icon");

			if (iconName == null) continue;

			int[] blockAtlasCoordinates = TextureManager.get().getBlockAtlasCoordinates(iconName);
			blockAtlasCoordinates[0] *= 16;
			blockAtlasCoordinates[1] *= 16;

			renderer.drawGuiTextureClipped(
					textureManager.getBlockAtlasTexture(), //texture
					hotbarX + 7 + 24 * i, hotbarY + 7, // screen x, y
					16, 16, // screen width, height
					blockAtlasCoordinates[0], blockAtlasCoordinates[1], // texture x, y
					16, 16 // texture width, height
			);

		}

	}




	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public boolean isClean() {
		return true;
	}

	@Override
	public void clean() {

	}
}
