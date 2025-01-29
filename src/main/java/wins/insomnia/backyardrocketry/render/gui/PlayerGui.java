package wins.insomnia.backyardrocketry.render.gui;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Block;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

public class PlayerGui implements IGuiRenderable, IUpdateListener {


	private final float NORMAL_HOTBAR_ITEM_SCALE = 0.015f;
	private final float SELECTED_HOTBAR_ITEM_SCALE = 0.02f;
	private float[] hotbarItemScales = new float[10];

	private float breakProgressRatio = 0.0f;
	private TestPlayer player;
	private double previousDeltaTime = 0.0;
	private Vector3d blockMeshRotationValue = new Vector3d();
	private float desiredBreakProgress = 0f;

	public PlayerGui(TestPlayer player) {
		this.player = player;
	}



	@Override
	public void render() {


		double deltaTime = Updater.getCurrentTime() - previousDeltaTime;
		previousDeltaTime = Updater.getCurrentTime();


		blockMeshRotationValue.x += deltaTime * 1.5;
		blockMeshRotationValue.y += deltaTime * 15.0;


		while (blockMeshRotationValue.x >= 360) {
			blockMeshRotationValue.x -= 360;
		}
		while (blockMeshRotationValue.y >= 360) {
			blockMeshRotationValue.y -= 360;
		}


		Renderer renderer = Renderer.get();

		int hotbarX = renderer.getCenterAnchorX() - 139;
		int hotbarY = renderer.getBottomAnchor() - 41;
		int selectedHotbarSlotX = hotbarX + player.getCurrentHotbarSlot() * 28;
		renderer.drawGuiTexture(TextureManager.getTexture("hotbar"), hotbarX, hotbarY);
		renderer.drawGuiTexture(TextureManager.getTexture("selected_hotbar_slot"), selectedHotbarSlotX, hotbarY);



		// render hotbar items

		int resolutionWidth = renderer.getResolutionFrameBuffer().getWidth();
		int resolutionHeight = renderer.getResolutionFrameBuffer().getHeight();

		float gameWindowAspect = resolutionWidth / (float) resolutionHeight;
		float modelAspectScale = (750f / resolutionHeight);
		glBindTexture(GL_TEXTURE_2D, TextureManager.getTexture("block_atlas").getTextureHandle());

		for (int i = 0; i < 10; i++) {

			int hotbarIndex = i == 9 ? 0 : i + 1;
			TextRenderer.drawText(Integer.toString(hotbarIndex), hotbarX + 11 + i * 28, hotbarY + 5);

			byte currentBlock = player.getHotbarSlotContents(i);


			Mesh hotbarSlotHandMesh = BlockModelData.getMeshFromBlock(currentBlock);

			if (hotbarSlotHandMesh == null || hotbarSlotHandMesh.isClean()) {
				continue;
			}

			float blockMeshScale = hotbarItemScales[i] * modelAspectScale * renderer.getGuiScale();

			Renderer.get().getModelMatrix().identity()
					.translate(0f, 0f, -1f)
					.scale(1f, 1f, 0f)
					.rotateX((float) Math.sin(blockMeshRotationValue.x) * 0.3f)
					.rotateY((float) Math.toRadians(blockMeshRotationValue.y))
					.scale(
							blockMeshScale,
							blockMeshScale,
							blockMeshScale
					)
					.translate(-0.5f, -0.5f, -0.5f);


			int guiX = hotbarX + 13 + 28 * i;
			int guiY = hotbarY + 28;
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

		}


		// draw crosshair
		renderer.drawGuiTexture(TextureManager.getTexture("crosshair"), renderer.getCenterAnchorX() - 8, renderer.getCenterAnchorY() - 8);


		// render inventory
		if (player.getInventoryManager().isOpen()) {
			Texture inventoryTexture = TextureManager.getTexture("placeholder_inventory");
			int textureX = renderer.getCenterAnchorX() - inventoryTexture.getWidth() / 2;
			int textureY = renderer.getCenterAnchorY() - inventoryTexture.getHeight() / 2;
			renderer.drawGuiTexture(inventoryTexture, textureX, textureY);
		}



	}


	private void renderTargetBlock(byte targetBlockId) {

		Renderer renderer = Renderer.get();

		// render break progress
		if (player.getBreakProgress() > 0) {
			int progressBarScreenPositionY = renderer.getCenterAnchorY() + 16;

			renderer.drawGuiTextureClipped(
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

			renderer.drawGuiTextureClipped(
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
		Texture wailaTexture = TextureManager.getTexture("waila");
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
