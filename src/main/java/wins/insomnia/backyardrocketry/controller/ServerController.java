package wins.insomnia.backyardrocketry.controller;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.network.TestPacket;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.world.ServerWorld;

import java.io.IOException;

public class ServerController extends GameController {

	public static final int TCP_PORT = 54555;
	public static final int UDP_PORT = 54777;
	private static final Listener SERVER_LISTENER = new Listener() {
		public void received(Connection connection, Object object) {

			if (!(object instanceof Packet packet)) return;

			packet.received(Packet.SenderType.CLIENT, connection);


		}


	};

	// used to determine if the server is actually a server or an internal server for single-player
	private final boolean INTERNAL;
	private ServerWorld world;
	private Server server;


	public ServerController(boolean isInternal) {
		super();

		INTERNAL = isInternal;

	}


	@Override
	public boolean isServer() {
		return true;
	}

	@Override
	protected void onStop() {

		stopServer();

	}


	@Override
	protected void onStart() {


		startServer();


		if (isInternalServer()) {

		} else {

		}

		world = new ServerWorld();
	}


	private void startServer() {

		server = new Server();
		server.start();

		try {
			server.bind(TCP_PORT, UDP_PORT);
		} catch (IOException e) {
			e.printStackTrace();

			server.removeListener(SERVER_LISTENER);
			server.stop();
			server = null;
			stop();

			return;

		}

		server.addListener(SERVER_LISTENER);

		Packet.registerPackets(server.getKryo());

	}

	private void stopServer() {
		if (getWorld() != null) {
			getWorld().shutdown();
		}


		if (server != null) {
			server.stop();
		}
	}

	public ServerWorld getWorld() {
		return world;
	}

	public boolean isInternalServer() {
		return INTERNAL;
	}


	public static ServerController get() {
		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) {
			throw new RuntimeException("Cannot get serverController when GameplayScene is not active!");
		}

		return gameplayScene.getServer();

	}

	public static void sendUnreliable(int receiverId, Packet packet) {

		send(receiverId, packet, false);

	}

	public static void sendReliable(int receiverId, Packet packet) {

		send(receiverId, packet, true);

	}

	public static void send(int receiverId, Packet packet, boolean reliable) {

		ServerController serverController = get();
		Server server = serverController.server;

		if (reliable) {
			server.sendToTCP(receiverId, packet);
		} else {
			server.sendToUDP(receiverId, packet);
		}

	}



}
