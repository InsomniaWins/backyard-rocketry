package wins.insomnia.backyardrocketry.render.gui;

import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
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

		int hotbarX = renderer.getCenterAnchorX() - 139;
		int hotbarY = renderer.getBottomAnchor() - 41;
		int selectedHotbarSlotX = hotbarX + player.getCurrentHotbarSlot() * 28;
		renderer.drawGuiTexture(textureManager.HOTBAR_TEXTURE, hotbarX, hotbarY);
		renderer.drawGuiTexture(textureManager.HOTBAR_SLOT_TEXTURE, selectedHotbarSlotX, hotbarY);



		// render hotbar items
		for (int i = 0; i < 10; i++) {

			int hotbarIndex = i == 9 ? 0 : i + 1;
			TextRenderer.drawText(Integer.toString(hotbarIndex), hotbarX + 11 + i * 28, hotbarY + 5);

			byte currentBlock = player.getHotbarSlotContents(i);

			BlockModelData blockModelData = BlockModelData.getBlockModel(currentBlock, 0, 0, 0);

			if (blockModelData == null) continue;

			String iconName = blockModelData.getTextures().get("icon");

			if (iconName == null) continue;

			int[] blockAtlasCoordinates = TextureManager.get().getBlockAtlasCoordinates(iconName);
			blockAtlasCoordinates[0] *= 16;
			blockAtlasCoordinates[1] *= 16;

			renderer.drawGuiTextureClipped(
					textureManager.getBlockAtlasTexture(), //texture
					hotbarX + 5 + 28 * i, hotbarY + 20, // screen x, y
					16, 16, // screen width, height
					blockAtlasCoordinates[0], blockAtlasCoordinates[1], // texture x, y
					16, 16 // texture width, height
			);

		}



		BlockRaycastResult targetBlock = player.getTargetBlock();
		if (targetBlock != null) {

			byte targetBlockId = targetBlock.getChunk().getBlock(
					targetBlock.getChunk().toLocalX(targetBlock.getBlockX()),
					targetBlock.getChunk().toLocalY(targetBlock.getBlockY()),
					targetBlock.getChunk().toLocalZ(targetBlock.getBlockZ())
			);

			// render break progress
			if (player.getBreakProgress() > 0) {
				int progressBarScreenPositionY = renderer.getCenterAnchorY() + 16;

				renderer.drawGuiTextureClipped(
						textureManager.BREAK_PROGRESS_BAR_UNDER_TEXTURE,
						renderer.getCenterAnchorX() - 34,
						progressBarScreenPositionY,
						68,
						8,
						0,
						0,
						68,
						8
				);

				float breakProgressRatio = player.getBreakProgress();
				breakProgressRatio = breakProgressRatio / (float) Block.getBlockHealth(targetBlockId);
				int breakProgressPixels = Math.round(62f * breakProgressRatio);

				renderer.drawGuiTextureClipped(
						textureManager.BREAK_PROGRESS_BAR_PROGRESS_TEXTURE,
						renderer.getCenterAnchorX() - 31,
						progressBarScreenPositionY + 3,
						breakProgressPixels,
						2,
						0,
						0,
						breakProgressPixels,
						2
				);
			}

			// render w.a.i.l.a gui
			Texture wailaTexture = TextureManager.get().WAILA_BACKGROUND_TEXTURE;
			int wailaPosY = 0;
			renderer.drawGuiTexture(
					wailaTexture,
					renderer.getCenterAnchorX() - wailaTexture.getWidth() / 2,
					wailaPosY
					);

			String blockName = Block.getBlockName(targetBlockId);
			int textWidth = TextRenderer.getTextPixelWidth(blockName);
			int wailaTextPosX = renderer.getCenterAnchorX() - textWidth / 2;
			TextRenderer.drawText(blockName, wailaTextPosX, wailaPosY + 2);

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

	@Override
	public int getRenderPriority() {
		return 0;
	}
}
