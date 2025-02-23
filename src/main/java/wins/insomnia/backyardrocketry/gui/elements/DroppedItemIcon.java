package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;
import wins.insomnia.backyardrocketry.gui.elements.PlayerGui;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;

public class DroppedItemIcon implements IGuiRenderable, IPositionOwner {

	private final Vector3d POSITION = new Vector3d();
	private final EntityItem POSITION_OWNER;
	private Item item;
	private boolean shouldShowDistance = true;

	public DroppedItemIcon(Item item, EntityItem positionOwner) {
		POSITION_OWNER = positionOwner;
		this.item = item;
	}

	public DroppedItemIcon(Item item, double x, double y, double z) {
		POSITION.set(x, y, z);
		POSITION_OWNER = null;
		this.item = item;
	}

	public void showDistance() {
		shouldShowDistance = true;
	}

	public void hideDistance() {
		shouldShowDistance = false;
	}

	public boolean isShowingDistance() {
		return shouldShowDistance;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public void register() {
		Renderer.get().addRenderable(this);
	}

	public void unregister() {
		Renderer.get().removeRenderable(this);
	}

	public void setPosition(double x, double y, double z) {

		if (POSITION_OWNER != null) return;

		POSITION.set(x, y, z);

	}

	@Override
	public Vector3d getPosition() {
		if (POSITION_OWNER == null) {
			return POSITION;
		}

		return POSITION_OWNER.getPosition();
	}

	@Override
	public void render() {

		Camera camera = Renderer.get().getCamera();
		Vector3f position = new Vector3f(getPosition());

		Vector3i guiPosition = Renderer.get().worldPositionToGuiPosition(camera, position.x, position.y, position.z);

		if (guiPosition.z == 0) {
			TextureRenderer.drawGuiTextureNineSlice(
					TextureManager.getTexture("dropped_item"),
					guiPosition.x - 12,
					guiPosition.y - 12,
					24, 24,
					3, true
			);

			PlayerGui.renderItemIcon(item, guiPosition.x + 1, guiPosition.y - 1);

			String distanceText = String.valueOf((int) camera.getTransform().getPosition().distance(position.x, position.y, position.z));
			TextRenderer.drawTextOutline(
					distanceText,
					guiPosition.x - (TextRenderer.getTextPixelWidth(distanceText) / 2),
					guiPosition.y - 24
			);

		}

	}

	@Override
	public boolean shouldRender() {
		return getPosition().distance(Renderer.get().getCamera().getTransform().getPosition()) > 10f;
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
		return -1;
	}

	@Override
	public boolean hasTransparency() {
		return false;
	}
}
