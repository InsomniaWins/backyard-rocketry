package wins.insomnia.backyardrocketry.entity.item;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.render.IRenderable;
import wins.insomnia.backyardrocketry.render.Mesh;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.gui.DroppedItemIcon;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.World;

import java.util.UUID;

public class EntityClientItem extends EntityItem implements IRenderable {

	private boolean uniqueMesh = true;
	private Mesh mesh;
	private float modelYRotation = 0f;
	private float modelXRotation = 0f;
	private float visualInterpolationFactor = 0f;
	private DroppedItemIcon droppedItemIcon;

	public EntityClientItem(ItemStack itemStack, World world, java.util.UUID uuid) {
		super(itemStack, world, uuid);

		if (itemStack.getItem() instanceof BlockItem blockItem) {
			mesh = BlockModelData.getMeshFromBlock(blockItem.getBlock());
			uniqueMesh = false;
		} else {
			// make mesh for dropped item
		}

	}


	@Override
	public void teleport(double x, double y, double z, float rotX, float rotY, float rotZ) {
		super.teleport(x, y, z, rotX, rotY, rotZ);

		getInterpolatedTransform().setPosition(getPosition());
		getInterpolatedTransform().setRotation(getRotation());

		visualInterpolationFactor = 0f;
	}

	@Override
	public void render() {

		Transform transform = getInterpolatedTransform();

		Renderer.get().getModelMatrix().identity()
				.translate((float) transform.getPosition().x, (float) transform.getPosition().y, (float) transform.getPosition().z)
				.rotateZ(getRotation().z)
				.rotateY(getRotation().y + modelYRotation)
				.rotateX(getRotation().x + modelXRotation)
				.scale(0.5f)
				.translate(-0.5f, -0.5f, -0.5f);

		Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());
		mesh.render();
	}

	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public boolean isClean() {

		if (!hasUniqueMesh()) return true;

		return mesh == null || mesh.isClean();
	}

	@Override
	public void clean() {
		if (hasUniqueMesh() && mesh != null) {
			mesh.clean();
		}

		mesh = null;
	}

	@Override
	public int getRenderPriority() {
		return 0;
	}

	public boolean hasUniqueMesh() {
		return uniqueMesh;
	}

	@Override
	public void removedFromWorld() {
		super.removedFromWorld();
		Renderer.get().removeRenderable(this);
		clean();
	}

	@Override
	public void addedToWorld() {
		super.addedToWorld();
		Renderer.get().addRenderable(this);
	}

	@Override
	public void fixedUpdate() {
		super.fixedUpdate();
		visualInterpolationFactor = 0f;
	}

	@Override
	public void update(double deltaTime) {

		super.update(deltaTime);

		modelYRotation = (float) (modelYRotation + deltaTime);
		modelXRotation = (float) (modelXRotation + deltaTime);

		while (modelXRotation >= 360f) modelXRotation -= 360f;
		while (modelYRotation >= 360f) modelYRotation -= 360f;

		//modelYPosition = (float) Math.sin(Updater.getCurrentTime() - CREATION_TIME) * 0.1f;

		visualInterpolationFactor += (float) deltaTime / (1.0f / Updater.getFixedUpdatesPerSecond());
		visualInterpolationFactor = Math.min(visualInterpolationFactor, 1f);

	}

	public Transform getInterpolatedTransform() {

		// reset interpolation to t = 0 to begin interpolation
		Vector3f interpolatedRotation = new Vector3f(PREVIOUS_TRANSFORM.getRotation());
		Vector3d interpolatedPostion = new Vector3d(PREVIOUS_TRANSFORM.getPosition());

		// interpolate rotation and position
		interpolatedRotation.set(
				Transform.lerpAngle(interpolatedRotation.x, getRotation().x, visualInterpolationFactor),
				Transform.lerpAngle(interpolatedRotation.y, getRotation().y, visualInterpolationFactor),
				Transform.lerpAngle(interpolatedRotation.z, getRotation().z, visualInterpolationFactor)
		);
		interpolatedPostion.lerp(getPosition(), visualInterpolationFactor);

		return INTERPOLATED_TRANSFORM.setPosition(interpolatedPostion).setRotation(interpolatedRotation);
	}

	@Override
	public void registeredFixedUpdateListener() {
		droppedItemIcon = new DroppedItemIcon(getItemStack().getItem(), this);
		droppedItemIcon.register();
	}

	@Override
	public void unregisteredFixedUpdateListener() {
		droppedItemIcon.unregister();
		droppedItemIcon = null;
	}

}
