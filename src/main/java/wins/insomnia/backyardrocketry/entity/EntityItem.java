package wins.insomnia.backyardrocketry.entity;

import org.joml.Math;
import org.joml.Random;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.entity.component.ComponentGenericVelocityMovement;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.World;


public class EntityItem extends Entity implements IRenderable, IBoundingBoxEntity {

	private boolean uniqueMesh = true;
	private Mesh mesh;
	private ItemStack itemStack;
	private final BoundingBox BOUNDING_BOX = new BoundingBox();
	private float modelYRotation = 0f;
	private float modelYPosition = 0f;
	private final double CREATION_TIME;
	private final ComponentGenericVelocityMovement VELOCITY_MOVEMENT_COMPONENT;
	private float visualInterpolationFactor = 0f;
	private final Transform PREVIOUS_TRANSFORM = new Transform();
	private final Transform INTERPOLATED_TRANSFORM = new Transform();

	public EntityItem(ItemStack itemStack, World world) {
		super(world);
		this.itemStack = itemStack;


		if (itemStack.getItem() instanceof BlockItem blockItem) {
			mesh = BlockModelData.getMeshFromBlock(blockItem.getBlock());
			uniqueMesh = false;
		} else {
			// make mesh for dropped item
		}

		addEntityComponent(new ComponentGravity(this, 0.125f));
		VELOCITY_MOVEMENT_COMPONENT = new ComponentGenericVelocityMovement(this);
		addEntityComponent(VELOCITY_MOVEMENT_COMPONENT);
		CREATION_TIME = Updater.getCurrentTime();


		getVelocity().add(world.getRandom().nextFloat() - 0.5f, world.getRandom().nextFloat() * 0.1f + 0.05, world.getRandom().nextFloat() - 0.25f);
	}

	@Override
	public void teleport(double x, double y, double z, float rotX, float rotY, float rotZ) {
		super.teleport(x, y, z, rotX, rotY, rotZ);

		INTERPOLATED_TRANSFORM.setPosition(getPosition());
		INTERPOLATED_TRANSFORM.setRotation(getRotation());

		visualInterpolationFactor = 0f;
	}

	// returns the item-stack previously stored by this item entity
	public ItemStack setItemStack(ItemStack itemStack) {
		ItemStack previousItemStack = this.itemStack;
		this.itemStack = itemStack;
		return previousItemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	@Override
	public void render() {

		Transform transform = getInterpolatedTransform();

		Renderer.get().getModelMatrix().identity()
				.translate((float) transform.getPosition().x, (float) transform.getPosition().y + modelYPosition, (float) transform.getPosition().z)
				.rotateZ(getRotation().z)
				.rotateY(getRotation().y + modelYRotation)
				.rotateX(getRotation().x)
				.scale(0.25f)
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
		Renderer.get().removeRenderable(this);
		clean();
		Updater.get().unregisterUpdateListener(this);
		Updater.get().unregisterFixedUpdateListener(this);
	}

	@Override
	public void addedToWorld() {
		Renderer.get().addRenderable(this);
		Updater.get().registerUpdateListener(this);
		Updater.get().registerFixedUpdateListener(this);
	}

	private void updateBoundingBox() {
		BOUNDING_BOX.getMin().set(getPosition()).add(-0.25, -0.25, -0.25);
		BOUNDING_BOX.getMax().set(getPosition()).add(0.25, 0.25, 0.25);
	}

	@Override
	public void fixedUpdate() {

		super.fixedUpdate();

		getVelocity().x *= 0.5f;
		getVelocity().z *= -0.5f;

		updateBoundingBox();

		PREVIOUS_TRANSFORM.set(getTransform());
		VELOCITY_MOVEMENT_COMPONENT.move();

		visualInterpolationFactor = 0f;

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
	public void update(double deltaTime) {
		super.update(deltaTime);

		modelYRotation = (float) (modelYRotation + deltaTime);
		modelYPosition = (float) Math.sin(Updater.getCurrentTime() - CREATION_TIME) * 0.1f;

		visualInterpolationFactor += (float) deltaTime / (1.0f / Updater.getFixedUpdatesPerSecond());
		visualInterpolationFactor = Math.min(visualInterpolationFactor, 1f);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return BOUNDING_BOX;
	}

	public boolean hasTransparency() {
		return true;
	}
}
