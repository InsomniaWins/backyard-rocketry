package wins.insomnia.backyardrocketry.network.entity;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;

import java.util.UUID;

public class PacketUpdateEntityTransform extends Packet {

	String uuid;
	Transform transform;

	public PacketUpdateEntityTransform setUuid(UUID uuid) {
		this.uuid = uuid.toString();
		return this;
	}

	public PacketUpdateEntityTransform setTransform(Transform transform) {
		this.transform = transform;
		return this;
	}

	// call on main thread
	private void _updateEntityTransform() {

		ClientWorld clientWorld = ClientWorld.getClientWorld();

		if (clientWorld == null) return;

		Entity entity = clientWorld.getEntity(UUID.fromString(uuid));

		if (entity == null) return;

		entity.getTransform().set(transform);

	}

	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.SERVER) return;

		if (Main.MAIN_THREAD != Thread.currentThread()) {
			Updater.get().queueMainThreadInstruction(this::_updateEntityTransform);
		} else {
			_updateEntityTransform();
		}

	}



}
