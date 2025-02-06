package wins.insomnia.backyardrocketry.render.gui;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.entity.EntityItem;
import wins.insomnia.backyardrocketry.gui.elements.PlayerGui;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.IPositionOwner;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.TextureManager;

public class DroppedItemIcon implements IGuiRenderable, IPositionOwner {

	private final Vector3d POSITION = new Vector3d();
	private final EntityItem POSITION_OWNER;
	private Item item;

	public DroppedItemIcon(Item item, EntityItem positionOwner) {
		POSITION_OWNER = positionOwner;
		this.item = item;
	}

	public DroppedItemIcon(Item item, double x, double y, double z) {
		POSITION.set(x, y, z);
		POSITION_OWNER = null;
		this.item = item;
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
			Renderer.get().drawGuiTextureNineSlice(
					TextureManager.getTexture("dropped_item"),
					guiPosition.x - 12,
					guiPosition.y - 12,
					24, 24,
					3, true
			);

			PlayerGui.renderItemIcon(item, guiPosition.x + 1, guiPosition.y - 1);
		}

	}

	@Override
	public boolean shouldRender() {
		return getPosition().distance(Renderer.get().getCamera().getTransform().getPosition()) > 4f;
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
