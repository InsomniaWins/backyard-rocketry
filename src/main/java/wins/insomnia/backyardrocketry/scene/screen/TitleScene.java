package wins.insomnia.backyardrocketry.scene.screen;

import org.joml.Math;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.gui.elements.Button;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.scene.Scene;
import wins.insomnia.backyardrocketry.scene.SceneManager;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class TitleScene extends Scene {

	private final Button OFFLINE_BUTTON;
	private final Button QUIT_BUTTON;
	private final IRenderable POST_RENDER_OBJECT;
	private final String TITLE_STRING = "Backyard Rocketry";
	private final int TITLE_X = Renderer.get().getCenterAnchorX() - TextRenderer.getTextPixelWidth(TITLE_STRING) / 2;
	private final int TITLE_Y = 10;
	private final int CHANGE_BLOCK_TIMER_WAIT_TIME = Updater.FIXED_UPDATES_PER_SECOND;
	private int changeBlockTimer = CHANGE_BLOCK_TIMER_WAIT_TIME;
	private byte block = Blocks.COBBLESTONE;

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

		POST_RENDER_OBJECT = new IRenderable() {
			@Override
			public void render() {
				Renderer renderer = Renderer.get();

				renderer.drawGuiMesh(
						BlockModelData.getMeshFromBlock(block),
						TextureManager.getTexture("block_atlas"),
						TITLE_X - 20,
						TITLE_Y + 5,
						(float) Math.sin(Updater.getCurrentTime()), (float) Math.cos(Updater.getCurrentTime()), 0f,
						1f / 48f,
						-0.5f, -0.5f, -0.5f
				);

				renderer.drawGuiMesh(
						BlockModelData.getMeshFromBlock(block),
						TextureManager.getTexture("block_atlas"),
						TITLE_X + 20 + TextRenderer.getTextPixelWidth(TITLE_STRING),
						TITLE_Y + 5,
						(float) Math.sin(Updater.getCurrentTime()), (float) Math.cos(Updater.getCurrentTime()), 0f,
						1f / 48f,
						-0.5f, -0.5f, -0.5f
				);
			}

			@Override
			public boolean shouldRender() {
				return true;
			}

			@Override
			public boolean isClean() {
				return false;
			}

			@Override
			public void clean() {

			}

			@Override
			public int getRenderPriority() {
				return 1;
			}

			@Override
			public boolean hasTransparency() {
				return false;
			}
		};

	}

	@Override
	public void update(double deltaTime) {



	}

	private void changeBlock() {

		byte previousBlock = block;
		block = Blocks.getRandomBlock();

		while (block == previousBlock || BlockModelData.getMeshFromBlock(block) == null) {
			block = Blocks.getRandomBlock();
		}

	}

	@Override
	public void fixedUpdate() {

		changeBlockTimer -= 1;
		if (changeBlockTimer <= 0) {
			changeBlockTimer = CHANGE_BLOCK_TIMER_WAIT_TIME;

			changeBlock();

		}

	}

	@Override
	public void render() {

		Renderer renderer = Renderer.get();

		TextureRenderer.drawGuiTextureTiled(
				TextureManager.getTexture("menu_background"),
				0, 0,
				renderer.getRightAnchor(), renderer.getBottomAnchor()
		);

		TextRenderer.drawTextOutline(TITLE_STRING, TITLE_X, TITLE_Y);

		String versionString = BackyardRocketry.getVersionString();
		TextRenderer.drawTextOutline(
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
		registerGameObject(POST_RENDER_OBJECT);




	}

	@Override
	public void sceneUnregistered() {

		unregisterGameObject(OFFLINE_BUTTON);
		unregisterGameObject(QUIT_BUTTON);
		unregisterGameObject(POST_RENDER_OBJECT);

	}

}
