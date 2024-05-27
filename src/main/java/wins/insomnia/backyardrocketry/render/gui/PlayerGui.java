package wins.insomnia.backyardrocketry.render.gui;

import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextureManager;

public class PlayerGui implements IGuiRenderable {


	@Override
	public void render() {

		Renderer renderer = Renderer.get();
		TextureManager textureManager = TextureManager.get();

		renderer.drawGuiTexture(textureManager.HOTBAR_TEXTURE, renderer.getCenterAnchorX() - 123, renderer.getBottomAnchor() - 30);


	}




	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public boolean isClean() {
		return true;
	}

	@Override
	public void clean() {

	}
}
