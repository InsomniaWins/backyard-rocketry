package wins.insomnia.backyardrocketry.render.gui;

import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextureManager;
import wins.insomnia.backyardrocketry.util.TestPlayer;

public class PlayerGui implements IGuiRenderable {

	private TestPlayer player;

	public PlayerGui(TestPlayer player) {
		this.player = player;
	}

	@Override
	public void render() {

		Renderer renderer = Renderer.get();
		TextureManager textureManager = TextureManager.get();

		int hotbarX = renderer.getCenterAnchorX() - 123;
		int selectedHotbarSlotX = hotbarX + player.getCurrentHotbarSlot() * 24;
		renderer.drawGuiTexture(textureManager.HOTBAR_TEXTURE, hotbarX, renderer.getBottomAnchor() - 30);
		renderer.drawGuiTexture(textureManager.HOTBAR_SLOT_TEXTURE, selectedHotbarSlotX, renderer.getBottomAnchor() - 30);


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
