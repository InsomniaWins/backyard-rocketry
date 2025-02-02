package wins.insomnia.backyardrocketry.scenes.screens;

import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.gui.elements.Button;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.scenes.Scene;
import wins.insomnia.backyardrocketry.scenes.SceneManager;
import wins.insomnia.backyardrocketry.world.block.Block;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class TitleScene extends Scene {

	private final Button OFFLINE_BUTTON;
	private final Button QUIT_BUTTON;



	public TitleScene() {

		OFFLINE_BUTTON = new Button("Play Offline", () -> SceneManager.get().changeScene(new SaveSelectionScene()));
		OFFLINE_BUTTON.setSize(120, 20);
		OFFLINE_BUTTON.setPosition(
				Renderer.get().getCenterAnchorX() - OFFLINE_BUTTON.getWidth() / 2,
				Renderer.get().getCenterAnchorY() - OFFLINE_BUTTON.getHeight() / 2
		);

		QUIT_BUTTON = new Button("Quit Game", () -> glfwSetWindowShouldClose(Window.get().getWindowHandle(), true));
		QUIT_BUTTON.setSize(120, 22);
		QUIT_BUTTON.setPosition(
				Renderer.get().getCenterAnchorX() - QUIT_BUTTON.getWidth() / 2,
				Renderer.get().getCenterAnchorY() - QUIT_BUTTON.getHeight() / 2 + QUIT_BUTTON.getHeight() + 4
		);



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

		String titleString = "Backyard Rocketry";
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



		Mesh blockMesh = BlockModelData.getMeshFromBlock(Block.GRASS);
		blockMesh.render();




	}

	@Override
	public boolean shouldRender() {
		return true;
	}


	@Override
	public void sceneRegistered() {

		Window.get().setFullscreen(true);

		registerGameObject(OFFLINE_BUTTON);
		registerGameObject(QUIT_BUTTON);

	}

	@Override
	public void sceneUnregistered() {

		unregisterGameObject(OFFLINE_BUTTON);
		unregisterGameObject(QUIT_BUTTON);

	}

}
