package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.EntityManager;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBoxRaycastResult;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.BlockAtlasTexture;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;

import java.util.Collection;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

public class PlayerGui implements IGuiRenderable, IUpdateListener {


	public static final float NORMAL_HOTBAR_ITEM_SCALE = 0.013f;
	public static final float SELECTED_HOTBAR_ITEM_SCALE = 0.015f;
	public static final Vector3f BLOCK_ITEM_ICON_ROTATION = new Vector3f();
	private float[] hotbarItemScales = new float[10];
	private float breakProgressRatio = 0.0f;
	private EntityClientPlayer player;
	private float desiredBreakProgress = 0f;

	public PlayerGui(EntityClientPlayer player) {
		this.player = player;
	}

	public static void renderItemIcon(Item item, int guiX, int guiY) {
		renderItemIcon(
				item,
				guiX,
				guiY,
				item instanceof BlockItem ? PlayerGui.NORMAL_HOTBAR_ITEM_SCALE : 1f
		);
	}

	public static void renderItemIcon(Item item, int guiX, int guiY, float scale) {

		Renderer renderer = Renderer.get();

		if (item instanceof BlockItem blockItem) {

			glBindTexture(GL_TEXTURE_2D, TextureManager.getTexture("block_atlas").getTextureHandle());

			Mesh hotbarSlotHandMesh = BlockModelData.getMeshFromBlock(blockItem.getBlock());

			if (hotbarSlotHandMesh == null || hotbarSlotHandMesh.isClean()) {
				return;
			}

			int resolutionWidth = Window.get().getResolutionFrameBuffer().getWidth();
			int resolutionHeight = Window.get().getResolutionFrameBuffer().getHeight();

			float modelAspectScale = (750f / resolutionHeight);

			Renderer.get().getModelMatrix().identity()
					.translate(0f, 0f, -1f)
					.scale(1f, 1f, 0f)
					.rotateX(BLOCK_ITEM_ICON_ROTATION.x)
					.rotateY(BLOCK_ITEM_ICON_ROTATION.y)
					.rotateZ(BLOCK_ITEM_ICON_ROTATION.z)
					.scale(
							scale * modelAspectScale * renderer.getGuiScale(),
							scale * modelAspectScale * renderer.getGuiScale(),
							scale * modelAspectScale * renderer.getGuiScale()
					)
					.translate(-0.5f, -0.5f, -0.5f);



			float gameWindowAspect = resolutionWidth / (float) resolutionHeight;

			Vector2f viewportOffset = new Vector2f(
					2f * (((renderer.getGuiScale() * guiX) / (float) resolutionWidth) - 0.5f),
					2f * (((renderer.getGuiScale() * guiY) / (float) resolutionHeight) - 0.5f)
			);

			Matrix4f projectionMatrix = new Matrix4f().setPerspective(70f, gameWindowAspect, 0.01f, 1f);
			projectionMatrix.m20(-viewportOffset.x);
			projectionMatrix.m21(viewportOffset.y);

			Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", projectionMatrix);
			Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", new Matrix4f().identity());
			Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			hotbarSlotHandMesh.render();
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getCamera().getProjectionMatrix());
			Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", Renderer.get().getCamera().getViewMatrix());


		}


	}

	@Override
	public void render() {


		Renderer renderer = Renderer.get();

		int hotbarX = renderer.getCenterAnchorX() - 139;
		int hotbarY = renderer.getBottomAnchor() - 41;
		int selectedHotbarSlotX = hotbarX + player.getCurrentHotbarSlot() * 28;
		TextureRenderer.drawGuiTexture(TextureManager.getTexture("hotbar"), hotbarX, hotbarY);
		TextureRenderer.drawGuiTexture(TextureManager.getTexture("selected_hotbar_slot"), selectedHotbarSlotX, hotbarY);



		// render hotbar items
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTexture("block_atlas").getTextureHandle());

		for (int i = 0; i < 10; i++) {

			int hotbarIndex = i == 9 ? 0 : i + 1;
			TextRenderer.drawText(Integer.toString(hotbarIndex), hotbarX + 11 + i * 28, hotbarY + 5);

			int guiX = hotbarX + 14 + 28 * i;
			int guiY = hotbarY + 27;
			float blockMeshScale = hotbarItemScales[i];

			renderItemIcon(
					Item.getBlockItem(player.getHotbarSlotContents(i)),
					guiX, guiY,
					blockMeshScale
			);




			/*     OLD 2D ICON RENDERING
			BlockModelData blockModelData = BlockModelData.getBlockModel(currentBlock, 0, 0, 0);

			if (blockModelData == null) continue;

			String iconName = blockModelData.getTextures().get("icon");

			if (iconName == null) {
				continue;
			}

			int[] blockAtlasCoordinates = TextureManager.getBlockAtlasCoordinates(iconName);
			blockAtlasCoordinates[0] *= 16;
			blockAtlasCoordinates[1] *= 16;

			renderer.drawGuiTextureClipped(
					TextureManager.getBlockAtlasTexture(), //texture
					hotbarX + 5 + 28 * i, hotbarY + 20, // screen x, y
					16, 16, // screen width, height
					blockAtlasCoordinates[0], blockAtlasCoordinates[1], // texture x, y
					16, 16 // texture width, height
			);

			 */



		}






		BlockRaycastResult targetBlock = player.getTargetBlock();
		if (targetBlock != null) {

			byte targetBlockId = targetBlock.getChunk().getBlock(
					targetBlock.getChunk().toLocalX(targetBlock.getBlockX()),
					targetBlock.getChunk().toLocalY(targetBlock.getBlockY()),
					targetBlock.getChunk().toLocalZ(targetBlock.getBlockZ())
			);
			
			if (targetBlockId != Block.AIR) {
				renderTargetBlock(targetBlockId);
			}

		} else {
			BoundingBoxRaycastResult targetEntity = player.getTargetEntity();

			if (targetEntity != null && targetEntity.getEntity() instanceof Entity entity) {

				renderTargetEntity(entity);

			}

		}



		// draw crosshair
		TextureRenderer.drawGuiTexture(TextureManager.getTexture("crosshair"), renderer.getCenterAnchorX() - 8, renderer.getCenterAnchorY() - 8);


		// render inventory
		if (player.getInventoryManager().isOpen()) {
			Texture inventoryTexture = TextureManager.getTexture("placeholder_inventory");
			int textureX = renderer.getCenterAnchorX() - inventoryTexture.getWidth() / 2;
			int textureY = renderer.getCenterAnchorY() - inventoryTexture.getHeight() / 2;
			TextureRenderer.drawGuiTexture(inventoryTexture, textureX, textureY);
		}



	}


	private void renderTargetBlock(byte targetBlockId) {

		Renderer renderer = Renderer.get();

		// render break progress
		if (player.getBreakProgress() > 0) {
			int progressBarScreenPositionY = renderer.getCenterAnchorY() + 16;

			TextureRenderer.drawGuiTextureClipped(
					TextureManager.getTexture("break_progress_bar_under"),
					renderer.getCenterAnchorX() - 34,
					progressBarScreenPositionY,
					68,
					8,
					0,
					0,
					68,
					8
			);


			int breakProgressPixels = Math.round(62f * breakProgressRatio);

			TextureRenderer.drawGuiTextureClipped(
					TextureManager.getTexture("break_progress_bar_progress"),
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
		String blockName = Block.getBlockName(targetBlockId);

		int wailaHeight = TextRenderer.getTextPixelHeight(2);
		int wailaPosX = 0;
		int wailaPosY = 0;

		renderItemIcon(
				Item.getBlockItem(targetBlockId),
				wailaPosX + (int) (wailaHeight / 2f),
				(int) (wailaHeight / 2f)
		);

		TextRenderer.drawTextOutline(
				blockName,
				wailaPosX + (int) (wailaHeight / 2f) + 14,
				wailaPosY + (int) (wailaHeight / 2f - TextRenderer.getTextPixelHeight(1) / 2f)
		);
	}


	private void renderTargetEntity(Entity entity) {

		Renderer renderer = Renderer.get();

		EntityManager.EntityRegistrationInformation entityInformation = EntityManager.getEntityInformation(entity.getClass());

		if (entityInformation == null) return;

		// render w.a.i.l.a gui

		Texture wailaTexture = TextureManager.getTexture("waila");

		if (entity instanceof EntityItem entityItem) {

			ItemStack itemStack = entityItem.getItemStack();
			if (itemStack != null) {

				String itemName = itemStack.getItem().getName() + " x " + (int) itemStack.getItemAmount();
				String volumeString = (itemStack.getVolume() / 1000f) + " Liters";

				int wailaHeight = TextRenderer.getTextPixelHeight(2);
				int wailaPosX = 0;
				int wailaPosY = 0;

				renderItemIcon(
						itemStack.getItem(),
						wailaPosX + (int) (wailaHeight / 2f),
						(int) (wailaHeight / 2f)
				);

				TextRenderer.drawTextOutline(
						itemName + "\n" + volumeString,
						wailaPosX + (int) (wailaHeight / 2f) + 14,
						wailaPosY + 3
				);

				return;



			}

		}

		String entityName = entityInformation.entityName();
		int textWidth = TextRenderer.getTextPixelWidth(entityName);

		int wailaTextPosX = 14;
		TextRenderer.drawText(entityName, wailaTextPosX, 2);
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

	@Override
	public boolean hasTransparency() {
		return true;
	}

	@Override
	public void update(double deltaTime) {

		BLOCK_ITEM_ICON_ROTATION.y += (float) (deltaTime * 1.5);
		BLOCK_ITEM_ICON_ROTATION.x = (float) Math.sin(Updater.getCurrentTime());

		while (BLOCK_ITEM_ICON_ROTATION.x >= 360) {
			BLOCK_ITEM_ICON_ROTATION.x -= 360;
		}

		while (BLOCK_ITEM_ICON_ROTATION.y >= 360) {
			BLOCK_ITEM_ICON_ROTATION.y -= 360;
		}


		float tickUpdateFactor = (float) (Updater.get().getTickDelta() / (1f / Updater.FIXED_UPDATES_PER_SECOND));



		BlockRaycastResult targetBlock = player.getTargetBlock();

		if (targetBlock != null) {
			byte targetBlockId = targetBlock.getChunk().getBlock(
					targetBlock.getChunk().toLocalX(targetBlock.getBlockX()),
					targetBlock.getChunk().toLocalY(targetBlock.getBlockY()),
					targetBlock.getChunk().toLocalZ(targetBlock.getBlockZ())
			);

			desiredBreakProgress = (player.getBreakProgress()) / (float) Block.getBlockHealth(targetBlockId);

			breakProgressRatio = desiredBreakProgress + (tickUpdateFactor / (float) Block.getBlockHealth(targetBlockId));

		} else {
			breakProgressRatio = 0f;
		}



		// update hotbar item scales
		for (int i = 0; i < 10; i++) {
			if (player.getCurrentHotbarSlot() == i) {
				hotbarItemScales[i] = (float) Math.lerp(hotbarItemScales[i], SELECTED_HOTBAR_ITEM_SCALE, deltaTime * 12f);
			} else {
				hotbarItemScales[i] = (float) Math.lerp(hotbarItemScales[i], NORMAL_HOTBAR_ITEM_SCALE, deltaTime * 12f);
			}
		}

	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}
}
