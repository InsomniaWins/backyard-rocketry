package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryonet.Connection;

public class TestPacket extends Packet {

	public String string;


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType == SenderType.SERVER) {

			System.out.println("Got response from server " + connection.getID() + ": " + string);

		} else {

			System.out.println("Got packet from client " + connection.getID() + ": " + string);

			TestPacket packet = new TestPacket();
			packet.string = "Hello!!!!!";
			packet.sendToClient(connection.getID(), true);

		}


	}
}
