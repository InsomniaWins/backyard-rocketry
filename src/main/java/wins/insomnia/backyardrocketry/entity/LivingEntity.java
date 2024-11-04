package wins.insomnia.backyardrocketry.entity;

import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.world.World;

public class LivingEntity extends Entity {

	public LivingEntity(World world) {
		super(world);
	}

	private float maxHealth = 20.0f;
	private float health = maxHealth;


	public float getMaxHealth() {
		return maxHealth;
	}

	public float getHealth() {
		return health;
	}

}
