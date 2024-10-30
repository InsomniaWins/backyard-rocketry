package wins.insomnia.backyardrocketry.entity;

import wins.insomnia.backyardrocketry.entity.Entity;

public class LivingEntity extends Entity {

	private float maxHealth = 20.0f;
	private float health = maxHealth;


	public float getMaxHealth() {
		return maxHealth;
	}

	public float getHealth() {
		return health;
	}

}
