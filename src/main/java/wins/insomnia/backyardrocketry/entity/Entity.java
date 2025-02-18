package wins.insomnia.backyardrocketry.entity;

import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.entity.component.Component;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Entity implements IUpdateListener, IFixedUpdateListener {

	private final World WORLD;
	private final List<Component> COMPONENTS = new ArrayList<>();
	private final Transform TRANSFORM = new Transform();
	private final Vector3d VELOCITY = new Vector3d();
	private final UUID UUID;

	public Entity(World world, UUID uuid) {
		WORLD = world;
		UUID = uuid;
	}

	public UUID getUUID() {
		return UUID;
	}

	@Override
	public void fixedUpdate() {
		for (Component component : COMPONENTS) {
			component.fixedUpdate();
		}
	}

	@Override
	public void registeredFixedUpdateListener() {

	}

	@Override
	public void unregisteredFixedUpdateListener() {

	}

	@Override
	public void update(double deltaTime) {
		for (Component component : COMPONENTS) {
			component.update(deltaTime);
		}
	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}

	public boolean hasEntityComponent(Class<? extends Component> componentClass) {

		return COMPONENTS.stream().anyMatch(c -> c.getClass() == componentClass);

	}

	public void addEntityComponent(Component component) {
		COMPONENTS.add(component);
	}

	public void removeEntityComponent(Component component) {
		COMPONENTS.remove(component);
	}

	public World getWorld() {
		return WORLD;
	}

	public Transform getTransform() {
		return TRANSFORM;
	}

	public Vector3d getPosition() {
		return TRANSFORM.getPosition();
	}

	public Vector3f getRotation() {
		return TRANSFORM.getRotation();
	}

	public Vector3d getVelocity() {
		return VELOCITY;
	}

	public void addedToWorld() {

	}

	public void removedFromWorld() {

	}

	public void teleport(double x, double y, double z, float rotX, float rotY, float rotZ) {

		getPosition().set(x, y, z);
		getRotation().set(rotX, rotY, rotZ);

	}
}
