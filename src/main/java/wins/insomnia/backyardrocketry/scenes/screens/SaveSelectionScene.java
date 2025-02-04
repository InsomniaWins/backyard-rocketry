package wins.insomnia.backyardrocketry.scenes.screens;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.gui.elements.Button;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextRenderer;
import wins.insomnia.backyardrocketry.render.TextureManager;
import wins.insomnia.backyardrocketry.scenes.GameplayScene;
import wins.insomnia.backyardrocketry.scenes.Scene;
import wins.insomnia.backyardrocketry.scenes.SceneManager;

public class SaveSelectionScene extends Scene {


	private final Button BACK_BUTTON;
	private final Button CREATE_BUTTON;
	private final Button LOAD_BUTTON;

	public SaveSelectionScene() {

		BACK_BUTTON = new Button("Back To Title", () -> SceneManager.get().changeScene(new TitleScene()));
		BACK_BUTTON.setSize(100, 20);
		BACK_BUTTON.setPosition(Renderer.get().getCenterAnchorX() - BACK_BUTTON.getWidth() / 2, Renderer.get().getBottomAnchor() - 25);

		CREATE_BUTTON = new Button("New Save", this::startNewSave);
		CREATE_BUTTON.setSize(100, 20);
		CREATE_BUTTON.setPosition(Renderer.get().getCenterAnchorX() - CREATE_BUTTON.getWidth() / 2, 30);

		LOAD_BUTTON = new Button("Load Save", () -> {});
		LOAD_BUTTON.setSize(100, 20);
		LOAD_BUTTON.setPosition(Renderer.get().getCenterAnchorX() - CREATE_BUTTON.getWidth() / 2, 55);
	}


	private void startNewSave() {

		GameplayScene gameplayScene = new GameplayScene(GameplayScene.GameType.CLIENT_SERVER);
		SceneManager.get().changeScene(gameplayScene);

	}


	@Override
	public void update(double deltaTime) {



	}

	@Override
	public void fixedUpdate() {



	}

	@Override
	public void render() {


		Renderer renderer = Renderer.get();

		renderer.drawGuiTextureTiled(
				TextureManager.getTexture("menu_background"),
				0, 0,
				renderer.getRightAnchor(), renderer.getBottomAnchor()
		);

		String titleString = "Saves";
		int textX = Renderer.get().getCenterAnchorX() - TextRenderer.getTextPixelWidth(titleString) / 2;

		TextRenderer.drawTextOutline(titleString, textX, 10, 1, TextureManager.getTexture("font"));

		String versionString = BackyardRocketry.getVersionString();
		TextRenderer.drawTextOutline(
				versionString,
				Renderer.get().getRightAnchor() - TextRenderer.getTextPixelWidth(versionString),
				Renderer.get().getBottomAnchor() - TextRenderer.getTextPixelHeight(1),
				1,
				TextureManager.getTexture("font")
		);

	}

	@Override
	public boolean shouldRender() {
		return true;
	}


	@Override
	public void sceneRegistered() {

		registerGameObject(BACK_BUTTON);
		registerGameObject(CREATE_BUTTON);
		registerGameObject(LOAD_BUTTON);

	}

	@Override
	public void sceneUnregistered() {

		unregisterGameObject(BACK_BUTTON);
		unregisterGameObject(CREATE_BUTTON);
		unregisterGameObject(LOAD_BUTTON);

	}

}
