package wins.insomnia.backyardrocketry.gui.elements;

import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.util.update.Updater;

public class GuiElementPlayerInventory extends GuiElement {

	public GuiElementPlayerInventory() {




	}


	public void register() {

		Updater.get().registerUpdateListener(this);
		Updater.get().registerFixedUpdateListener(this);
		Renderer.get().addRenderable(this);


	}

	public void unregister() {

		Updater.get().unregisterUpdateListener(this);
		Updater.get().unregisterFixedUpdateListener(this);
		Renderer.get().removeRenderable(this);


	}


	@Override
	public void render() {

		Renderer renderer = Renderer.get();

		Texture inventoryTexture = TextureManager.getTexture("inventory");
		int textureX = renderer.getCenterAnchorX() - inventoryTexture.getWidth() / 2;
		int textureY = renderer.getCenterAnchorY() - inventoryTexture.getHeight() / 2;
		TextureRenderer.drawGuiTexture(inventoryTexture, textureX, textureY);

	}

	@Override
	public int getRenderPriority() {
		return 2;
	}


}
