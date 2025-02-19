package wins.insomnia.backyardrocketry.entity.item;

import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;
import wins.insomnia.backyardrocketry.entity.component.ComponentGenericVelocityMovement;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.World;

import java.util.UUID;


public class EntityItem extends Entity implements IBoundingBoxEntity {

	private ItemStack itemStack;
	private final BoundingBox BOUNDING_BOX = new BoundingBox();
	private final double CREATION_TIME;
	private final ComponentGenericVelocityMovement VELOCITY_MOVEMENT_COMPONENT;
	protected final Transform PREVIOUS_TRANSFORM = new Transform();

	public EntityItem(ItemStack itemStack, World world, java.util.UUID uuid) {
		super(world, uuid);
		this.itemStack = itemStack;

		addEntityComponent(new ComponentGravity(this, 0.125f));
		VELOCITY_MOVEMENT_COMPONENT = new ComponentGenericVelocityMovement(this);
		addEntityComponent(VELOCITY_MOVEMENT_COMPONENT);
		CREATION_TIME = Updater.getCurrentTime();

		getVelocity().add(world.getRandom().nextFloat() - 0.5f, world.getRandom().nextFloat() * 0.1f + 0.05, world.getRandom().nextFloat() - 0.25f);
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
	public void removedFromWorld() {
		Updater.get().unregisterUpdateListener(this);
		Updater.get().unregisterFixedUpdateListener(this);
	}

	@Override
	public void addedToWorld() {
		Updater.get().registerUpdateListener(this);
		Updater.get().registerFixedUpdateListener(this);
	}

	private void updateBoundingBox() {
		BOUNDING_BOX.getMin().set(getPosition()).add(-0.4, -0.4, -0.4);
		BOUNDING_BOX.getMax().set(getPosition()).add(0.4, 0.4, 0.4);
	}

	@Override
	public void fixedUpdate() {

		super.fixedUpdate();

		getVelocity().x *= 0.5f;
		getVelocity().z *= -0.5f;

		updateBoundingBox();

		PREVIOUS_TRANSFORM.set(getTransform());
		VELOCITY_MOVEMENT_COMPONENT.move();

	}

	@Override
	public void update(double deltaTime) {
		super.update(deltaTime);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return BOUNDING_BOX;
	}

	public boolean hasTransparency() {
		return true;
	}



}
