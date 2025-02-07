package wins.insomnia.backyardrocketry.scenes.screens;

import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.scenes.Scene;
import wins.insomnia.backyardrocketry.scenes.SceneManager;
import wins.insomnia.backyardrocketry.util.loading.AssetLoader;
import wins.insomnia.backyardrocketry.util.loading.LoadTask;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;

import java.util.List;

public class LoadingScene extends Scene {

	private boolean loading = false;
	private boolean finishedLoading = false;
	private String currentTaskName = "";
	private final AssetLoader ASSET_LOADER;


	public LoadingScene() {
		ASSET_LOADER = new AssetLoader();
	}

	@Override
	public void sceneRegistered() {

		if (ASSET_LOADER.areAssetsCurrentlyLoading()) {
			return;
		}

		ASSET_LOADER.addLoadTask(new LoadTask("Initializing Audio Manager . . .", () -> {

			try {
				AudioManager.get().init();
			} catch (Exception e) {
				System.err.println("Failed to initialize audio manager!");
			}

		}));

		List<LoadTask> audioBufferTaskList = AudioManager.makeAudioBufferLoadingTaskList();
		for (LoadTask task : audioBufferTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}


		List<LoadTask> blockModelTaskList = BlockModelData.makeBlockModelLoadingTaskList();
		for (LoadTask task : blockModelTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}

		List<LoadTask> blockStateTaskList = BlockModelData.makeBlockStateLoadingTaskList();
		for (LoadTask task : blockStateTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}

		List<LoadTask> blockMeshTaskList = BlockModelData.makeBlockMeshLoadingTaskList();
		for (LoadTask task : blockMeshTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}

		List<LoadTask> blockLootTaskList = BlockLoot.makeLoadingTaskList();
		for (LoadTask task : blockLootTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}

		loading = true;

	}


	@Override
	public boolean shouldRender() {
		return true;
	}

	private void loadNextAsset() {
		if (ASSET_LOADER.areAssetsLoaded()) {

			loading = false;
			finishedLoading = true;

			return;
		}

		if (ASSET_LOADER.isProcessingTask()) return;


		currentTaskName = ASSET_LOADER.peekTask() == null ? "" : ASSET_LOADER.peekTask().taskName();

		ASSET_LOADER.loadNextAsset();
	}

	@Override
	public void update(double deltaTime) {

		if (loading) {
			loadNextAsset();
		} else if (finishedLoading) {

			goToTitleScene();

		}

	}

	private void goToTitleScene() {

		if (SceneManager.get().isSceneChanging()) return;

		SceneManager.get().changeScene(new TitleScene());

	}

	@Override
	public void render() {

		Renderer renderer = Renderer.get();


		renderer.drawGuiTextureTiled(
				TextureManager.getTexture("menu_background"),
				0, 0,
				renderer.getRightAnchor(), renderer.getBottomAnchor()
		);


		int progress = ASSET_LOADER.getProgress();
		int totalProgress = ASSET_LOADER.getTotalProgress();

		int centerX = renderer.getCenterAnchorX();
		int centerY = renderer.getCenterAnchorY();


		String loadingText = "Loading game: " + (int) ((progress / (double) totalProgress) * 100) + "%";
		int textPixelWidth = TextRenderer.getTextPixelWidth(loadingText);
		int textPixelHeight = TextRenderer.getTextPixelHeight(1);

		int textX = centerX - textPixelWidth / 2;
		int textY = centerY - textPixelHeight / 2;

		TextRenderer.drawTextOutline(loadingText, textX, textY, 1, TextureManager.getTexture("font"));



		textPixelWidth = TextRenderer.getTextPixelWidth(currentTaskName);
		textPixelHeight = TextRenderer.getTextPixelHeight(1);

		textX = centerX - textPixelWidth / 2;
		textY = centerY - textPixelHeight / 2 + 32;

		TextRenderer.drawTextOutline(currentTaskName, textX, textY, 1, TextureManager.getTexture("font"));



		Texture underTexture = TextureManager.getTexture("break_progress_bar_under");
		renderer.drawGuiTexture(underTexture, centerX - underTexture.getWidth() / 2, centerY - underTexture.getHeight() / 2 + 16);

		Texture progressTexture = TextureManager.getTexture("break_progress_bar_progress");
		renderer.drawGuiTextureClipped(
				progressTexture,
				centerX - progressTexture.getWidth() / 2, centerY - progressTexture.getHeight() / 2 + 16,
				(int) (progressTexture.getWidth() * (progress / (double) totalProgress)), progressTexture.getHeight(),
				0, 0,
				(int) (progressTexture.getWidth() * (progress / (double) totalProgress)), progressTexture.getHeight()
		);

	}


}
