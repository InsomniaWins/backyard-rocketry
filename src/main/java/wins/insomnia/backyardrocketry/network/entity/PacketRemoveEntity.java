package wins.insomnia.backyardrocketry.network.entity;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;

import java.util.UUID;

public class PacketRemoveEntity extends Packet {



	String uuid;

	public PacketRemoveEntity setUuid(UUID uuid) {
		this.uuid = uuid.toString();
		return this;
	}

	// call on main thread
	private void _removeEntity() {

		ClientWorld clientWorld = ClientWorld.getClientWorld();

		if (clientWorld == null) return;

		Entity entity = clientWorld.getEntity(UUID.fromString(uuid));

		if (entity == null) return;

		clientWorld.removeEntity(entity);

	}

	@Override
	public void received(SenderType senderType, Connection connection) {
		if (senderType != SenderType.SERVER) return;


		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(this::_removeEntity);
		} else {
			_removeEntity();
		}


	}
}
