package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerJump;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerMovementInputs;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerTransform;
import wins.insomnia.backyardrocketry.network.world.PacketLoadChunk;
import wins.insomnia.backyardrocketry.util.Transform;

import java.util.Arrays;
import java.util.List;

public abstract class Packet {

	public enum SenderType {
		SERVER,
		CLIENT
	}

	public static final List<Class<?>> CLASS_REGISTRATION_LIST = Arrays.asList(
			TestPacket.class,
			PacketLoadChunk.class,
			PacketPlayerTransform.class,
			PacketPlayerJump.class,
			PacketPlayerMovementInputs.class,
			Transform.class,
			Vector3f.class,
			Vector3d.class,
			boolean[].class,
			byte[].class,
			byte[][].class
	);



	public static void registerClasses(Kryo kryo) {

		for (Class<?> packetClass : CLASS_REGISTRATION_LIST) {

			kryo.register(packetClass);

		}

	}



	public void sendToServer(boolean reliable) {
		ClientController.send(this, reliable);
	}

	public void sendToClient(int id, boolean reliable) {
		ServerController.send(id, this, reliable);
	}





	// called on main thread
	public abstract void received(SenderType senderType, Connection connection);



}
