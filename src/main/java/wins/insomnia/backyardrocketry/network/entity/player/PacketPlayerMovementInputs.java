package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.player.EntityPlayer;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.update.Updater;
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


	// call on main thread
	private void _updatePlayerMovementInputs(int connectionId) {

		ServerWorld serverWorld = ServerWorld.getServerWorld();

		if (serverWorld == null) return;

		EntityServerPlayer serverPlayer = serverWorld.getServerPlayer(connectionId);

		if (serverPlayer == null) return;

		serverPlayer.setMovementInputs(movementInputs);

		if (rotation != null) {
			serverPlayer.getTransform().setRotation(rotation);
		}

	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (movementInputs == null) return;

		if (movementInputs.length != EntityPlayer.MOVEMENT_INPUT_SIZE) return;

		if (senderType != SenderType.CLIENT) return;

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> {
				_updatePlayerMovementInputs(connection.getID());
			});
		} else {
			_updatePlayerMovementInputs(connection.getID());
		}
	}
}
