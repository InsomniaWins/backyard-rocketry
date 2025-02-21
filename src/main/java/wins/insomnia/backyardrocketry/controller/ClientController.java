package wins.insomnia.backyardrocketry.controller;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.network.TestPacket;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;

import java.io.IOException;
import java.util.UUID;

public class ClientController extends GameController {

	private static final Listener CLIENT_LISTENER = new Listener() {
		public void received(Connection connection, Object object) {

			if (!(object instanceof Packet packet)) return;

			packet.received(Packet.SenderType.SERVER, connection);

		}


	};

	private ClientWorld world;
	private EntityClientPlayer clientPlayer;
	private Client client;


	private final String IP;
	private final int TCP_PORT;
	private final int UDP_PORT;



	// for local client-server architecture (offline single-player)
	public ClientController() {

		IP = "127.0.0.1";
		TCP_PORT = ServerController.TCP_PORT;
		UDP_PORT = ServerController.UDP_PORT;

	}

	// for connecting to server
	public ClientController(String ip, int tcpPort, int udpPort) {

		IP = ip;
		TCP_PORT = tcpPort;
		UDP_PORT = udpPort;

	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	protected void onStop() {

		disconnectFromServer();
		world.shutdown();

	}

	@Override
	protected void onStart() {


		try {
			connectToInternalServer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}



		world = new ClientWorld();

		clientPlayer = new EntityClientPlayer(world, UUID.randomUUID());
		double[] centerXZ = world.getCenterXZ();
		clientPlayer.getPosition().set(centerXZ[0], 164, centerXZ[1]);

		world.setClientPlayer(clientPlayer);
	}



	private void connectToInternalServer() throws IOException {

		connectToServer("127.0.0.1", ServerController.TCP_PORT, ServerController.UDP_PORT);

	}



	private void connectToServer(String ip, int tcpPort, int udpPort) throws IOException {


		client = new Client(CLIENT_WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		client.start();
		client.connect(5000, ip, tcpPort, udpPort);

		client.addListener(CLIENT_LISTENER);

		Packet.registerClasses(client.getKryo());

		TestPacket packet = new TestPacket();
		packet.string = "Hello, Server!";
		packet.sendToServer(true);

	}

	private void disconnectFromServer() {

		if (client != null) {
			client.stop();
			client = null;
		}

	}



	public ClientWorld getWorld() {
		return world;
	}

	public EntityClientPlayer getPlayer() {
		return clientPlayer;
	}

	public static ClientController get() {
		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) {
			throw new RuntimeException("Cannot get serverController when GameplayScene is not active!");
		}

		return gameplayScene.getClient();

	}





	public static void sendUnreliable(Packet packet) {

		send(packet, false);

	}

	public static void sendReliable(Packet packet) {

		send(packet, true);

	}

	public static void send(Packet packet, boolean reliable) {

		ClientController clientController = get();
		Client client = clientController.client;

		if (reliable) {

			client.sendTCP(packet);

		} else {

			client.sendUDP(packet);

		}

	}


}
