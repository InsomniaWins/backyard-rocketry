package wins.insomnia.backyardrocketry.entity.component;

import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.world.World;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class ComponentGravity extends Component {

	private final Entity ENTITY;
	private float weight = 0f;

	public ComponentGravity(Entity entity, float weight) {
		this.ENTITY = entity;
		this.weight = weight;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	@Override
	public void fixedUpdate() {

		World world = ENTITY.getWorld();

		ENTITY.getVelocity().add(0f, world.getGravity() * weight, 0f);

	}

	@Override
	public void update(double deltaTime) {

	}

}
