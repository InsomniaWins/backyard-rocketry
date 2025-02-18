package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.entity.player.EntityPlayer;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.world.ServerWorld;

public class PacketPlayerMovementInputs extends Packet {

	boolean[] movementInputs;
	Vector3f rotation;

	public PacketPlayerMovementInputs setMovementInputs(boolean[] movementInputs) {
		this.movementInputs = movementInputs;
		return this;
	}

	public PacketPlayerMovementInputs setRotation(Vector3f rotation) {
		this.rotation = rotation;
		return this;
	}



	@Override
	public void received(SenderType senderType, Connection connection) {

		if (movementInputs == null) return;

		if (movementInputs.length != EntityPlayer.MOVEMENT_INPUT_SIZE) return;

		if (senderType != SenderType.CLIENT) return;

		ServerWorld serverWorld = ServerWorld.getServerWorld();

		if (serverWorld == null) return;

		EntityServerPlayer serverPlayer = serverWorld.getServerPlayer(connection.getID());

		if (serverPlayer == null) return;

		serverPlayer.setMovementInputs(movementInputs);

		if (rotation != null) {
			serverPlayer.getTransform().setRotation(rotation);
		}

	}
}
