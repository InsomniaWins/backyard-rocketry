package wins.insomnia.backyardrocketry.scene;

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

		if (!currentScene.isClean()) {
			currentScene.clean();
		}

	}

	public void mainLoopFinished() {

		Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();

		if (!(caller.equals(Updater.get().getClass()))) {
			System.out.println("mainLoopFinished called from unauthorized class: " + caller);
			return;
		}

		unloadCurrentScene();

	}

	public static SceneManager get() {
		return BackyardRocketry.getInstance().getSceneManager();
	}

	public boolean isSceneChanging() {
		return sceneChanging;
	}
}
