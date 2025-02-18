package wins.insomnia.backyardrocketry.entity;

import wins.insomnia.backyardrocketry.entity.item.EntityClientItem;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;
import wins.insomnia.backyardrocketry.entity.item.EntityServerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EntityManager {

	public enum EntitySide {
		CLIENT,
		SERVER
	}

	private static final HashMap<String, List<Class<? extends Entity>>> ENTITY_HASH_MAP = new HashMap<>();



	public static void registerEntities() {

		registerEntity("item", EntityServerItem.class, EntityClientItem.class);


	}




	public static void registerEntity(String entityId, Class<? extends Entity> serverEntityClass, Class<? extends Entity> clientEntityClass) {

		ENTITY_HASH_MAP.put(entityId, Arrays.asList(serverEntityClass, clientEntityClass));

	}


	public static Class<? extends Entity> getEntityClass(String entityId, EntitySide side) {

		List<Class<? extends Entity>> entityList = ENTITY_HASH_MAP.get(entityId);

		if (entityList == null) return null;

		if (side == EntitySide.SERVER) return entityList.get(0);

		return entityList.get(1);

	}

}
