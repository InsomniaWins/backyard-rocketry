package wins.insomnia.backyardrocketry.entity.item;

import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.network.entity.PacketRemoveEntity;
import wins.insomnia.backyardrocketry.network.entity.PacketUpdateEntityTransform;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.World;

public class EntityServerItem extends EntityItem {

	private int ticksToLive = 400;

	public EntityServerItem(ItemStack itemStack, World world, java.util.UUID uuid) {
		super(itemStack, world, uuid);
	}

	@Override
	public void fixedUpdate() {

		super.fixedUpdate();

		ServerController.sendUnreliable(
				new PacketUpdateEntityTransform()
						.setUuid(getUUID())
						.setTransform(getTransform())
		);

		ticksToLive--;

		if (ticksToLive <= 0) {
			ServerWorld serverWorld = ServerWorld.getServerWorld();
			serverWorld.removeEntity(this);
		}

	}

	@Override
	public void removedFromWorld() {

		ServerController.sendReliable(
				new PacketRemoveEntity()
						.setUuid(getUUID())
		);

	}
}
