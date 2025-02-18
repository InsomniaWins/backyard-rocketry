package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.world.ServerWorld;

public class PacketPlayerJump extends Packet {

	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.CLIENT) return;

		ServerWorld serverWorld = ServerWorld.getServerWorld();

		if (serverWorld == null) return;

		EntityServerPlayer serverPlayer = serverWorld.getServerPlayer(connection.getID());

		if (serverPlayer == null) return;

		serverPlayer.jump();

	}
}
