package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.world.ClientWorld;

public class PacketPlayerTransform extends Packet {

	public Transform transform;

	public PacketPlayerTransform setTransform(Transform transform) {
		this.transform = transform;
		return this;
	}

	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.SERVER) {
			return;
		}

		ClientWorld clientWorld = ClientWorld.getClientWorld();

		if (clientWorld == null) {
			return;
		}

		EntityClientPlayer clientPlayer = clientWorld.getClientPlayer();

		if (clientPlayer == null) {
			return;
		}

		clientPlayer.gotTransformFromServer(transform);

	}
}
