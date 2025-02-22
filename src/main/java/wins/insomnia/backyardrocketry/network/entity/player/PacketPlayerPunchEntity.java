package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ServerWorld;

import java.util.UUID;

public class PacketPlayerPunchEntity extends Packet {

	String uuid;

	public PacketPlayerPunchEntity setUuid(UUID uuid) {
		this.uuid = uuid.toString();
		return this;
	}

	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.CLIENT) return;

		Updater.get().queueMainThreadInstruction(() -> {

			ServerWorld serverWorld = ServerWorld.getServerWorld();

			if (serverWorld == null) return;

			Entity entity = serverWorld.getEntity(UUID.fromString(uuid));

			if (entity == null) return;

			serverWorld.removeEntity(entity, true);

		});

	}
}
