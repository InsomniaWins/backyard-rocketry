package wins.insomnia.backyardrocketry.entity;

import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.world.World;

import java.util.UUID;

public class LivingEntity extends Entity {

	public LivingEntity(World world, java.util.UUID uuid) {
		super(world, uuid);
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
