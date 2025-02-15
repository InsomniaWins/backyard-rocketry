package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.Server;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;

import java.util.Arrays;
import java.util.List;

public abstract class Packet {

	public enum SenderType {
		SERVER,
		CLIENT
	}

	public static final List<Class<? extends Packet>> PACKET_LIST = Arrays.asList(
			TestPacket.class
	);



	public static void registerPackets(Kryo kryo) {

		for (Class<? extends Packet> packetClass : PACKET_LIST) {

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
