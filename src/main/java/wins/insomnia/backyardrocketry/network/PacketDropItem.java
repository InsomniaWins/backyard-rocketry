package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.entity.item.EntityClientItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.item.Items;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;

import java.util.UUID;

public class PacketDropItem extends Packet {

	String uuid;
	int itemId;
	int itemVolume;

	public PacketDropItem setUuid(UUID uuid) {
		this.uuid = uuid.toString();
		return this;
	}

	public PacketDropItem setItem(int itemId) {
		this.itemId = itemId;
		return this;
	}

	public PacketDropItem setVolume(int volume) {
		this.itemVolume = volume;
		return this;
	}

	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.SERVER) return;

		Updater.get().queueMainThreadInstruction(() -> {

			ClientWorld world = ClientWorld.getClientWorld();
			if (world == null) return;

			ItemStack itemStack = new ItemStack(Items.getItem(itemId), itemVolume);

			EntityClientItem entity = new EntityClientItem(
					itemStack,
					world,
					UUID.fromString(uuid)
			);

			world.addEntity(entity, 0, 0, 0);

		});

	}
}
