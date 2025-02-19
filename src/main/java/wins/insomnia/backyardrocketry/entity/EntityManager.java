package wins.insomnia.backyardrocketry.entity;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;

import java.util.HashMap;

public class EntityManager {

	public enum EntitySide {
		CLIENT,
		SERVER
	}

	private static final HashMap<Class<? extends Entity>, EntityRegistrationInformation> ENTITY_MAP = new HashMap<>();
	private static final HashMap<String, EntityRegistrationInformation> ENTITY_ID_MAP = new HashMap<>();

	public static void registerEntities() {

		registerEntity(EntityItem.class, "item", "Item");

	}




	public static void registerEntity(Class<? extends Entity> entityClass, String entityId, String entityName) {

		EntityRegistrationInformation entityRegistrationInformation = new EntityRegistrationInformation(
				entityId,
				entityName
		);
		ENTITY_MAP.put(entityClass, entityRegistrationInformation);
		ENTITY_ID_MAP.put(entityId, entityRegistrationInformation);

	}

	public static EntityRegistrationInformation getEntityInformation(String entityId) {

		return ENTITY_ID_MAP.get(entityId);

	}

	public static EntityRegistrationInformation getEntityInformation(Class<? extends Entity> entityClass) {

		EntityRegistrationInformation information = ENTITY_MAP.get(entityClass);

		Class<?> classIterator = entityClass;
		while (information == null) {

			Class<?> superClass = classIterator.getSuperclass();

			if (superClass == null || superClass == Entity.class) return null;

			if (!Entity.class.isAssignableFrom(superClass)) {
				return null;
			}

			classIterator = superClass;
			information = ENTITY_MAP.get(classIterator);

		}

		return information;
	}


	public record EntityRegistrationInformation(
			String entityId,
			String entityName
	) {}

}
