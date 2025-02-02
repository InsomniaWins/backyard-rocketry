package wins.insomnia.backyardrocketry.scenes;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.update.Updater;

public class SceneManager {

	private Scene currentScene;
	private boolean sceneChanging;


	public Scene getCurrentScene() {
		return currentScene;
	}


	public void changeScene(Scene nextScene) {
		sceneChanging = true;

		unloadCurrentScene();
		setCurrentScene(nextScene);

		sceneChanging = false;
	}

	private void setCurrentScene(Scene scene) {

		currentScene = scene;
		Updater.get().registerUpdateListener(currentScene);
		Updater.get().registerFixedUpdateListener(currentScene);
		Renderer.get().addRenderable(currentScene);

		currentScene.sceneRegistered();

	}


	private void unloadCurrentScene() {

		if (currentScene == null) return;

		Updater.get().unregisterUpdateListener(currentScene);
		Updater.get().unregisterFixedUpdateListener(currentScene);
		Renderer.get().removeRenderable(currentScene);

		currentScene.sceneUnregistered();
	}


	public static SceneManager get() {
		return BackyardRocketry.getInstance().getSceneManager();
	}

	public boolean isSceneChanging() {
		return sceneChanging;
	}
}
