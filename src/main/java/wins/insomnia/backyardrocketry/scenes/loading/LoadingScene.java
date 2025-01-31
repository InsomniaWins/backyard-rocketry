package wins.insomnia.backyardrocketry.scenes.loading;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.scenes.Scene;
import wins.insomnia.backyardrocketry.util.loading.AssetLoader;
import wins.insomnia.backyardrocketry.util.loading.LoadTask;
import wins.insomnia.backyardrocketry.util.update.DelayedMainThreadInstruction;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;

import java.util.List;

public class LoadingScene extends Scene {


	private final AssetLoader ASSET_LOADER;

	public LoadingScene() {
		ASSET_LOADER = new AssetLoader();
	}

	@Override
	public void sceneRegistered() {

		if (ASSET_LOADER.areAssetsCurrentlyLoading()) {
			return;
		}

		ASSET_LOADER.addLoadTask(new LoadTask("Loading block models . . .", BlockModelData::loadBlockModels));
		ASSET_LOADER.addLoadTask(new LoadTask("Loading block states . . .", BlockModelData::loadBlockStates));
		ASSET_LOADER.addLoadTask(new LoadTask("Registering block meshes . . .", BlockModelData::registerBlockMeshes));

		List<LoadTask> blockLootTaskList = BlockLoot.makeLoadingTaskList();
		for (LoadTask task : blockLootTaskList) {
			ASSET_LOADER.addLoadTask(task);
		}

	}


	@Override
	public boolean shouldRender() {
		return true;
	}

	private void loadNextAsset() {
		if (ASSET_LOADER.areAssetsLoaded()) return;

		if (ASSET_LOADER.isProcessingTask()) return;

		ASSET_LOADER.loadNextAsset();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(double deltaTime) {

		loadNextAsset();

	}

	@Override
	public void render() {
		int progress = ASSET_LOADER.getProgress();
		int totalProgress = ASSET_LOADER.getTotalProgress();

		Renderer renderer = Renderer.get();

		int centerX = renderer.getCenterAnchorX();
		int centerY = renderer.getCenterAnchorY();


		String loadingText = "Loading game . . ." + (int) ((progress / (double) totalProgress) * 100) + "%";
		int textPixelWidth = TextRenderer.getTextPixelWidth(loadingText);
		int textPixelHeight = TextRenderer.getTextPixelHeight(1);

		int textX = centerX - textPixelWidth / 2;
		int textY = centerY - textPixelHeight / 2;

		TextRenderer.drawTextOutline(loadingText, textX, textY, 1, TextureManager.getTexture("font"));



		Texture underTexture = TextureManager.getTexture("break_progress_bar_under");
		renderer.drawGuiTexture(underTexture, centerX - underTexture.getWidth() / 2, centerY - underTexture.getHeight() / 2 + 16);

	}


}
