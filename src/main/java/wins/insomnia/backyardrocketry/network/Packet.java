package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.network.world.LoadChunkPacket;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;

import java.util.Arrays;
import java.util.List;

public abstract class Packet {

	public enum SenderType {
		SERVER,
		CLIENT
	}

	public static final List<Class<?>> CLASS_REGISTRATION_LIST = Arrays.asList(
			TestPacket.class,
			LoadChunkPacket.class,
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





	public abstract void received(SenderType senderType, Connection connection);



}
