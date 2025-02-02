package wins.insomnia.backyardrocketry.scenes.screens;

import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.gui.elements.Button;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextRenderer;
import wins.insomnia.backyardrocketry.render.TextureManager;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.scenes.Scene;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class TitleScene extends Scene {

	private final Button OFFLINE_BUTTON;
	private final Button QUIT_BUTTON;

	public TitleScene() {

		OFFLINE_BUTTON = new Button("SINGLE PLAYER", () -> System.out.println("Pressed offline button."));
		OFFLINE_BUTTON.setSize(120, 20);
		OFFLINE_BUTTON.setPosition(
				Renderer.get().getCenterAnchorX() - OFFLINE_BUTTON.getWidth() / 2,
				Renderer.get().getCenterAnchorY() - OFFLINE_BUTTON.getHeight() / 2
		);

		QUIT_BUTTON = new Button("QUIT GAME", () -> glfwSetWindowShouldClose(Window.get().getWindowHandle(), true));
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

		String titleString = "Backyard Rocketry";
		int textX = Renderer.get().getCenterAnchorX() - TextRenderer.getTextPixelWidth(titleString) / 2;

		TextRenderer.drawText(titleString, textX, 10, 1, TextureManager.getTexture("font"));

		String versionString = BackyardRocketry.getVersionString();
		TextRenderer.drawText(
				versionString,
				Renderer.get().getRightAnchor() - TextRenderer.getTextPixelWidth(versionString),
				Renderer.get().getBottomAnchor() - TextRenderer.getTextPixelHeight(1)
		);

	}

	@Override
	public boolean shouldRender() {
		return true;
	}


	@Override
	public void sceneRegistered() {


		registerGameObject(OFFLINE_BUTTON);
		registerGameObject(QUIT_BUTTON);

	}

	@Override
	public void sceneUnregistered() {

		unregisterGameObject(OFFLINE_BUTTON);
		unregisterGameObject(QUIT_BUTTON);

	}

}
