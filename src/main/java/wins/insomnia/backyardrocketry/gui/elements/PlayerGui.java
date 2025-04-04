package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.EntityManager;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.item.Items;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.physics.BoundingBoxRaycastResult;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.render.mesh.Mesh;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.BlockAtlasTexture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Blocks;

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


	private float itemToolTipAlpha = 1f;

	public PlayerGui(EntityClientPlayer player) {
		this.player = player;
	}

	public void setItemToolTipAlpha(float alpha) {
		itemToolTipAlpha = alpha;
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


		// render under water overlay
		if (player.isUnderWater()) {

			ShaderProgram guiShaderProgram = renderer.getShaderProgram("gui");
			guiShaderProgram.use();
			guiShaderProgram.setUniform("fs_alpha", 0.9f);

			TextureRenderer.drawGuiTextureClipped(
					BlockAtlasTexture.get(),
					0, 0,
					Window.get().getResolutionFrameBuffer().getWidth(), Window.get().getResolutionFrameBuffer().getHeight(),
					109, 1,
					1, 1
			);

			guiShaderProgram.use();
			guiShaderProgram.setUniform("fs_alpha", 1f);
		}


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

			int guiX = hotbarX + 13 + 28 * i;
			int guiY = hotbarY + 28;
			float blockMeshScale = hotbarItemScales[i];

			ItemStack slotItemStack = player.getHotbarSlotContents(i);


			if (slotItemStack != null && slotItemStack.getAmount() > 0) {
				renderItemIcon(
						slotItemStack.getItem(),
						guiX, guiY,
						blockMeshScale
				);


				// tooltip
				if (i == player.getCurrentHotbarSlot()) {

					String itemName = slotItemStack.getItem().getName();

					TextRenderer.setFontAlpha(itemToolTipAlpha);
					TextRenderer.drawTextOutline(
							itemName,
							renderer.getCenterAnchorX() - TextRenderer.getTextPixelWidth(itemName) / 2,
							renderer.getBottomAnchor() - 60
					);
					TextRenderer.setFontAlpha(1f);
				}

			}

		}






		BlockRaycastResult targetBlock = player.getTargetBlock();
		if (targetBlock != null) {

			byte targetBlockId = targetBlock.getChunk().getBlock(
					targetBlock.getChunk().toLocalX(targetBlock.getBlockX()),
					targetBlock.getChunk().toLocalY(targetBlock.getBlockY()),
					targetBlock.getChunk().toLocalZ(targetBlock.getBlockZ())
			);
			
			if (targetBlockId != Blocks.AIR) {
				renderTargetBlock(targetBlockId);
			}

		}

		BoundingBoxRaycastResult targetEntity = player.getTargetEntity();
		if (targetEntity != null && targetEntity.getEntity() instanceof Entity entity) {

			renderTargetEntity(entity);

		}



		// draw crosshair
		TextureRenderer.drawGuiTexture(TextureManager.getTexture("crosshair"), renderer.getCenterAnchorX() - 8, renderer.getCenterAnchorY() - 8);

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
		String blockName = Blocks.getBlockName(targetBlockId);

		int wailaHeight = TextRenderer.getTextPixelHeight(2);
		int wailaPosX = 0;
		int wailaPosY = 0;

		renderItemIcon(
				Items.getBlockItem(targetBlockId),
				wailaPosX + (int) (wailaHeight / 2f),
				wailaPosY + (int) (wailaHeight / 2f)
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

		if (entity instanceof EntityItem entityItem) {

			ItemStack itemStack = entityItem.getItemStack();
			if (itemStack != null) {

				String itemName = itemStack.getItem().getName() + " x " + itemStack.getAmount();

				int wailaHeight = TextRenderer.getTextPixelHeight(2);
				int wailaPosX = 0;
				int wailaPosY = 32;

				renderItemIcon(
						itemStack.getItem(),
						wailaPosX + (int) (wailaHeight / 2f),
						wailaPosY + (int) (wailaHeight / 2f)
				);

				TextRenderer.drawTextOutline(
						itemName,
						wailaPosX + (int) (wailaHeight / 2f) + 14,
						wailaPosY + 3
				);

				return;



			}

		}

		String entityName = entityInformation.entityName();

		int wailaTextPosX = 14;
		TextRenderer.drawText(entityName, wailaTextPosX, 2, Math.max(1, Renderer.get().getGuiScale() - 1));




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

		itemToolTipAlpha = Math.max(0f, (float) (itemToolTipAlpha - 0.25f * deltaTime));

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

			desiredBreakProgress = (player.getBreakProgress()) / (float) Blocks.getBlockStrength(targetBlockId);

			breakProgressRatio = desiredBreakProgress + (tickUpdateFactor / (float) Blocks.getBlockStrength(targetBlockId));

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
