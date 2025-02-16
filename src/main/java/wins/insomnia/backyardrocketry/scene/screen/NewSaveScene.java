package wins.insomnia.backyardrocketry.scene.screen;

import org.joml.Math;
import org.joml.primitives.Rectanglei;
import org.lwjgl.glfw.GLFW;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.gui.elements.Button;
import wins.insomnia.backyardrocketry.gui.elements.GuiElement;
import wins.insomnia.backyardrocketry.gui.elements.LineEdit;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.scene.Scene;
import wins.insomnia.backyardrocketry.scene.SceneManager;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.util.io.FileIO;
import wins.insomnia.backyardrocketry.util.io.device.KeyboardInput;
import wins.insomnia.backyardrocketry.util.io.device.MouseInput;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.World;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class NewSaveScene extends Scene {

	private final LineEdit SAVE_NAME_LINE_EDIT;
	private final LineEdit SAVE_SEED_LINE_EDIT;
	private final Button REFRESH_SEED_BUTTON;
	private final Button CREATE_SAVE_BUTTON;
	private final Button BACK_BUTTON;
	private Texture seedTexture;
	private int desiredPreviewCameraX;
	private int desiredPreviewCameraY;
	private float previewCameraX;
	private float previewCameraY;
	private final int PREVIEW_WIDTH = 91;
	private final int PREVIEW_HEIGHT = 91;
	private final int SEED_WIDTH = World.CHUNK_AMOUNT_X * Chunk.SIZE_X;
	private final int SEED_HEIGHT = World.CHUNK_AMOUNT_Z * Chunk.SIZE_Z;
	private final ByteBuffer SEED_TEXTURE_DATA = ByteBuffer.allocateDirect(4 * SEED_WIDTH * SEED_HEIGHT);
	private Thread seedPreviewThread;
	private boolean shouldUpdateSeedPreview = false;
	private final int SEED_PREVIEW_UNGENERATED = 0;
	private final int SEED_PREVIEW_GENERATING = 1;
	private final int SEED_PREVIEW_GENERATED = 2;
	private final AtomicInteger SEED_PREVIEW_STATUS = new AtomicInteger(0);
	private final Runnable GENERATE_SEED_PREVIEW_RUNNABLE = new Runnable() {
		@Override
		public void run() {
			long seed = getSeed();

			WorldGeneration.getWorldPreview(seed, SEED_TEXTURE_DATA);
			SEED_PREVIEW_STATUS.set(SEED_PREVIEW_GENERATED);

			Updater.get().queueMainThreadInstruction(() -> {

				if (seedTexture != null && !seedTexture.isClean()) {
					seedTexture.clean();
					seedTexture = null;
				}

				seedTexture = new Texture(SEED_TEXTURE_DATA, SEED_WIDTH, SEED_HEIGHT);

			});

		}
	};

	private final int SAVE_CREATION_GENERATING_CHUNKS = 0;
	private Thread saveCreationThread;
	private boolean creatingSave = false;
	private final AtomicInteger CURRENT_CREATION_STEP = new AtomicInteger(SAVE_CREATION_GENERATING_CHUNKS);
	private final AtomicInteger CREATION_PROGRESS = new AtomicInteger(0);
	private int creationTotalProgress = 1;


	public NewSaveScene() {

		BACK_BUTTON = new Button("Back", () -> SceneManager.get().changeScene(new SaveSelectionScene()));
		BACK_BUTTON.setSize(100, 20);
		BACK_BUTTON.setPosition(Renderer.get().getCenterAnchorX() - BACK_BUTTON.getWidth() / 2, Renderer.get().getBottomAnchor() - 25);


		int verticalObjectPosition = 15;
		int verticalObjectIndex = 0;
		int verticalObjectHeight = 36;

		SAVE_NAME_LINE_EDIT = new LineEdit("Name");
		SAVE_NAME_LINE_EDIT.setSize(150, 20);
		SAVE_NAME_LINE_EDIT.setPosition(
				Renderer.get().getCenterAnchorX() - SAVE_NAME_LINE_EDIT.getWidth() / 2,
				verticalObjectPosition + verticalObjectIndex * verticalObjectHeight
		);
		SAVE_NAME_LINE_EDIT.registerTextListener((text) -> makeValidSaveName());
		SAVE_NAME_LINE_EDIT.setMaximumCharacters(15);
		SAVE_NAME_LINE_EDIT.setHorizontalTextAlignment(LineEdit.HorizontalTextAlignment.CENTER);
		SAVE_NAME_LINE_EDIT.setText("New Save");

		verticalObjectIndex++;

		SAVE_SEED_LINE_EDIT = new LineEdit("Seed");
		SAVE_SEED_LINE_EDIT.setSize(150, 20);
		SAVE_SEED_LINE_EDIT.setPosition(
				Renderer.get().getCenterAnchorX() - SAVE_SEED_LINE_EDIT.getWidth() / 2,
				verticalObjectPosition + verticalObjectIndex * verticalObjectHeight
		);
		SAVE_SEED_LINE_EDIT.setMaximumCharacters(15);
		SAVE_SEED_LINE_EDIT.setHorizontalTextAlignment(LineEdit.HorizontalTextAlignment.CENTER);
		SAVE_SEED_LINE_EDIT.clearAllowedCharacters();
		for (int i = 0; i < 10; i++) {
			SAVE_SEED_LINE_EDIT.addAllowedCharacter((char) (i + (int) '0'));
		}
		randomizeSeed();
		SAVE_SEED_LINE_EDIT.registerTextListener((text) -> {
			updateSeedTexture();
		});

		REFRESH_SEED_BUTTON = new Button("Random", () -> {
			randomizeSeed();
		});
		REFRESH_SEED_BUTTON.setSize(60, SAVE_SEED_LINE_EDIT.getHeight());
		REFRESH_SEED_BUTTON.setXPosition(SAVE_SEED_LINE_EDIT.getXPosition() + SAVE_SEED_LINE_EDIT.getWidth() + 5);
		REFRESH_SEED_BUTTON.setYPosition(SAVE_SEED_LINE_EDIT.getYPosition());

		verticalObjectIndex += 3;

		CREATE_SAVE_BUTTON = new Button("Create Save", this::createSave);
		CREATE_SAVE_BUTTON.setSize(100, 20);
		CREATE_SAVE_BUTTON.setPosition(
				Renderer.get().getCenterAnchorX() - CREATE_SAVE_BUTTON.getWidth() / 2,
				verticalObjectPosition + verticalObjectIndex * verticalObjectHeight + 30
		);

		verticalObjectIndex++;


		updateSeedTexture();

	}

	private void randomizeSeed() {

		SAVE_SEED_LINE_EDIT.setText(String.valueOf(World.RANDOM.nextLong(0, 999999999999999L)));
		updateSeedTexture();

	}

	private void updateSeedTexture() {
		shouldUpdateSeedPreview = true;
	}

	private void drawBackground() {
		TextureRenderer.drawGuiTextureTiled(
				TextureManager.getTexture("menu_background"),
				0, 0,
				Renderer.get().getRightAnchor(), Renderer.get().getBottomAnchor()
		);
	}

	@Override
	public void render() {

		drawBackground();
		
		String versionString = BackyardRocketry.getVersionString();
		TextRenderer.drawTextOutline(
				versionString,
				Renderer.get().getRightAnchor() - TextRenderer.getTextPixelWidth(versionString),
				Renderer.get().getBottomAnchor() - TextRenderer.getTextPixelHeight(1),
				1,
				TextureManager.getTexture("font")
		);

		drawSeedPreview();

		if (creatingSave) {

			drawSaveCreationProgress();

		}
	}

	private void drawSaveCreationProgress() {

		drawBackground();

		int frame = (int) ((Updater.getCurrentTime() * 25f) % 12f);
		TextureRenderer.drawGuiTextureClipped(
				TextureManager.getTexture("buffering"),
				Renderer.get().getCenterAnchorX() - 8,
				Renderer.get().getCenterAnchorY() - 8,
				16,
				16,
				frame * 16,
				0,
				16,
				16
		);

		String loadingText = "Creating Save . . .";
		TextRenderer.drawText(
				loadingText,
				Renderer.get().getCenterAnchorX() - TextRenderer.getTextPixelWidth(loadingText) / 2,
				Renderer.get().getCenterAnchorY() - 32
		);


		Renderer renderer = Renderer.get();


		String stepText = "";
		switch (CURRENT_CREATION_STEP.get()) {
			case SAVE_CREATION_GENERATING_CHUNKS -> {

				stepText = "Generating Chunks . . .";

			}
		}
		TextRenderer.drawText(
				stepText,
				Renderer.get().getCenterAnchorX() - TextRenderer.getTextPixelWidth(stepText) / 2,
				Renderer.get().getCenterAnchorY() + 16
		);




		int progressBarScreenPositionY = renderer.getCenterAnchorY() + 32;

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


		int breakProgressPixels = Math.round(62f * (CREATION_PROGRESS.get() / (float) creationTotalProgress));

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


	private void drawSeedPreview() {

		int textureWidth = 64;
		int textureHeight = 64;
		int textureX = Renderer.get().getCenterAnchorX() - textureWidth / 2;
		int textureY = (int) CREATE_SAVE_BUTTON.getYPosition() - 90;

		if (seedTexture != null && !seedTexture.isClean()) {

			TextureRenderer.drawGuiTextureFit(
					seedTexture,
					textureX,
					textureY,
					textureWidth,
					textureHeight
			);

			int previewX = textureX / 2 - PREVIEW_WIDTH / 2;
			int previewY = textureY + 32 - PREVIEW_HEIGHT / 2;


			TextureRenderer.drawGuiTextureNineSlice(
					TextureManager.getTexture("line_edit"),
					previewX - 3,
					previewY - 3,
					PREVIEW_WIDTH + 6,
					PREVIEW_HEIGHT + 6,
					5
			);

			TextureRenderer.drawGuiTextureClipped(
					seedTexture,
					previewX,
					previewY,
					PREVIEW_WIDTH,
					PREVIEW_HEIGHT,
					(int) previewCameraX,
					(int) previewCameraY,
					PREVIEW_WIDTH,
					PREVIEW_HEIGHT
			);

			TextureRenderer.drawGuiTexture(
					TextureManager.getTexture("world_preview_crosshair"),
					previewX + PREVIEW_WIDTH / 2 - 2,
					previewY + PREVIEW_HEIGHT / 2 - 2
			);

			TextRenderer.drawText(
					"W,A,S,D",
					previewX + PREVIEW_WIDTH / 2 - TextRenderer.getTextPixelWidth("W,A,S,D") / 2,
					previewY - TextRenderer.getTextPixelHeight(1) - 3
			);


			TextureRenderer.drawGuiTexture(
					TextureManager.getTexture("world_preview_crosshair"),
					textureX - 2 + (int) ((previewCameraX + PREVIEW_WIDTH / 2) * (textureWidth / (float) seedTexture.getWidth())),
					textureY - 2 + (int) ((previewCameraY + PREVIEW_HEIGHT / 2) * (textureHeight / (float) seedTexture.getHeight()))
			);

		}


		if (SEED_PREVIEW_STATUS.get() != SEED_PREVIEW_GENERATED) {
			int frame = (int) ((Updater.getCurrentTime() * 25f) % 12f);
			TextureRenderer.drawGuiTextureClipped(
					TextureManager.getTexture("buffering"),
					Renderer.get().getCenterAnchorX() - 8,
					textureY + textureHeight / 2 - 8,
					16,
					16,
					frame * 16,
					0,
					16,
					16
			);
		}
	}

	private void handleSeedPreviewPanning() {
		KeyboardInput keyboardInput = KeyboardInput.get();

		int moveSpeed = 10;

		if (GuiElement.getFocusedElement() == null) {

			if (keyboardInput.isKeyPressed(GLFW.GLFW_KEY_D)) {
				desiredPreviewCameraX += moveSpeed;
			}

			if (keyboardInput.isKeyPressed(GLFW.GLFW_KEY_A)) {
				desiredPreviewCameraX -= moveSpeed;
			}

			if (keyboardInput.isKeyPressed(GLFW.GLFW_KEY_W)) {
				desiredPreviewCameraY -= moveSpeed;
			}

			if (keyboardInput.isKeyPressed(GLFW.GLFW_KEY_S)) {
				desiredPreviewCameraY += moveSpeed;
			}

		}

		if (MouseInput.get().isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {

			int mouseX = Window.get().getViewportMouseX();
			int mouseY = Window.get().getViewportMouseY();

			int textureWidth = 64;
			int textureHeight = 64;
			int textureX = Renderer.get().getCenterAnchorX() - textureWidth / 2;
			int textureY = (int) CREATE_SAVE_BUTTON.getYPosition() - 90;


			if (new Rectanglei(textureX, textureY, textureX + textureWidth, textureY + textureHeight).containsPoint(mouseX, mouseY)) {

				desiredPreviewCameraX = (int) (seedTexture.getWidth() * ((mouseX - textureX) / (float) textureWidth));
				desiredPreviewCameraY = (int) (seedTexture.getHeight() * ((mouseY - textureY) / (float) textureHeight));
				desiredPreviewCameraX -= PREVIEW_WIDTH / 2;
				desiredPreviewCameraY -= PREVIEW_HEIGHT / 2;

			}
		}

		desiredPreviewCameraX = Math.max(desiredPreviewCameraX, 0);
		desiredPreviewCameraY = Math.max(desiredPreviewCameraY, 0);
		desiredPreviewCameraX = Math.min(desiredPreviewCameraX, seedTexture.getWidth() - PREVIEW_WIDTH);
		desiredPreviewCameraY = Math.min(desiredPreviewCameraY, seedTexture.getWidth() - PREVIEW_HEIGHT);
	}

	@Override
	public void fixedUpdate() {

		if (seedTexture != null && !seedTexture.isClean()) {
			handleSeedPreviewPanning();
		}

	}

	@Override
	public void update(double deltaTime) {

		previewCameraX = (float) Math.lerp(previewCameraX, desiredPreviewCameraX, deltaTime * 10f);
		previewCameraY = (float) Math.lerp(previewCameraY, desiredPreviewCameraY, deltaTime * 10f);


		if (shouldUpdateSeedPreview) {

			switch (SEED_PREVIEW_STATUS.get()) {
				case SEED_PREVIEW_GENERATED, SEED_PREVIEW_UNGENERATED -> {

					SEED_PREVIEW_STATUS.set(SEED_PREVIEW_GENERATING);
					seedPreviewThread = new Thread(GENERATE_SEED_PREVIEW_RUNNABLE, "Seed-Preview-Thread");
					seedPreviewThread.start();
					shouldUpdateSeedPreview = false;

				}
			}
		}

	}


	@Override
	public void sceneRegistered() {

		registerGameObject(BACK_BUTTON);
		registerGameObject(CREATE_SAVE_BUTTON);
		registerGameObject(SAVE_NAME_LINE_EDIT);
		registerGameObject(SAVE_SEED_LINE_EDIT);
		registerGameObject(REFRESH_SEED_BUTTON);

	}

	@Override
	public void sceneUnregistered() {

		unregisterGameObject(BACK_BUTTON);
		unregisterGameObject(CREATE_SAVE_BUTTON);
		unregisterGameObject(SAVE_NAME_LINE_EDIT);
		unregisterGameObject(SAVE_SEED_LINE_EDIT);
		unregisterGameObject(REFRESH_SEED_BUTTON);

	}



	private void makeValidSaveName() {

		SAVE_NAME_LINE_EDIT.setText(SAVE_NAME_LINE_EDIT.getText().replaceAll("[^a-zA-Z0-9-_\\.]", "_"), false);

	}

	private long getSeed() {

		if (SAVE_SEED_LINE_EDIT.getText().isEmpty()) {
			return 0L;
		} else {
			return Long.parseLong(SAVE_SEED_LINE_EDIT.getText());
		}

	}

	private void createSave() {

		makeValidSaveName();

		String saveName = SAVE_NAME_LINE_EDIT.getText();

		Path savePath = FileIO.getPathForSave(saveName, false);
		if (Files.exists(savePath)) {

			//TODO: tell user save name already exists
			return;

		}

		try {
			Files.createDirectory(savePath);
		} catch (IOException e) {

			//TODO: tell user we failed to make a save directory

			return;
		}


		unregisterGameObject(SAVE_NAME_LINE_EDIT);
		unregisterGameObject(SAVE_SEED_LINE_EDIT);
		unregisterGameObject(BACK_BUTTON);
		unregisterGameObject(CREATE_SAVE_BUTTON);
		unregisterGameObject(REFRESH_SEED_BUTTON);

		creatingSave = true;

		CREATION_PROGRESS.set(0);
		creationTotalProgress = World.CHUNK_AMOUNT_X * World.CHUNK_AMOUNT_Y * World.CHUNK_AMOUNT_Z;

		long seed = getSeed();

		Path chunksPath = FileIO.getChunksPath(savePath);
		ChunkIO.setChunksPath(chunksPath);
		saveCreationThread = new Thread(
				() -> {

					FileOutputStream fos = null;

					try {
						int regionAmountX = World.CHUNK_AMOUNT_X / ChunkIO.REGION_SIZE;
						int regionAmountY = World.CHUNK_AMOUNT_Y / ChunkIO.REGION_SIZE;
						int regionAmountZ = World.CHUNK_AMOUNT_Z / ChunkIO.REGION_SIZE;

						for (int regionX = 0; regionX < regionAmountX; regionX++) {
							for (int regionY = 0; regionY < regionAmountY; regionY++) {
								for (int regionZ = 0; regionZ < regionAmountZ; regionZ++) {

									String regionName = ChunkIO.getRegionNameFromRegion(regionX, regionY, regionZ);
									Path regionFilePath = ChunkIO.getRegionPathFromRegionName(regionName);

									Files.createFile(regionFilePath);
									fos = new FileOutputStream(regionFilePath.toString());
									BufferedOutputStream bos = new BufferedOutputStream(fos);


									for (int i = 0; i < ChunkIO.REGION_SIZE; i++) {
										for (int j = 0; j < ChunkIO.REGION_SIZE; j++) {
											for (int k = 0; k < ChunkIO.REGION_SIZE; k++) {

												int chunkX = regionX * ChunkIO.REGION_SIZE + i;
												int chunkY = regionY * ChunkIO.REGION_SIZE + j;
												int chunkZ = regionZ * ChunkIO.REGION_SIZE + k;

												ChunkData chunkData = new ChunkData(seed, chunkX, chunkY, chunkZ);

												bos.write(ChunkData.serialize(chunkData));

												CREATION_PROGRESS.incrementAndGet();

											}
										}
									}

									bos.close();
									fos.close();

								}
							}
						}
					} catch (IOException e) {



						if (fos != null) {
							try {
								fos.close();
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}

						e.printStackTrace();

					}

					Updater.get().queueMainThreadInstruction(() -> {

						GameplayScene gameplayScene = new GameplayScene(GameplayScene.GameType.CLIENT_SERVER);
						SceneManager.get().changeScene(gameplayScene);

					});

					},
				"Save-Creation-Thread"
		);

		saveCreationThread.start();


	}

}
