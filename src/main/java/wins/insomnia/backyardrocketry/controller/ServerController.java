package wins.insomnia.backyardrocketry.controller;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ServerWorld;

import java.io.IOException;

public class ServerController extends GameController {

	public static final int TCP_PORT = 54555;
	public static final int UDP_PORT = 54777;


	private static final Listener SERVER_LISTENER = new Listener() {
		@Override
		public void received(Connection connection, Object object) {

			if (!(object instanceof Packet packet)) return;

			Updater.get().queueMainThreadInstruction(() ->
					packet.received(Packet.SenderType.CLIENT, connection));


		}

		@Override
		public void connected(Connection connection) {

			Updater.get().queueMainThreadInstruction(() -> {

				ServerController serverController = ServerController.get();
				ServerWorld serverWorld = serverController.world;

				EntityServerPlayer serverPlayer = new EntityServerPlayer(connection.getID(), serverWorld);
				serverPlayer.getTransform().setPosition(new Vector3d(100, 100, 100));

				serverWorld.setServerPlayer(connection.getID(), serverPlayer);

			});



		}

		@Override
		public void disconnected(Connection connection) {



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

		world = new ServerWorld();
		startServer();

	}


	private void startServer() {

		server = new Server(SERVER_WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
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

		Packet.registerClasses(server.getKryo());

	}

	private void stopServer() {
		if (getWorld() != null) {
			getWorld().shutdown();
		}


		if (server != null) {
			server.stop();
			server = null;
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


	// sends to every client
	public static void sendUnreliable(Packet packet) {

		send(packet, false);

	}

	// sends to every client
	public static void sendReliable(Packet packet) {

		send(packet, true);

	}

	// sends to every client
	public static void send(Packet packet, boolean reliable) {

		ServerController serverController = get();
		Server server = serverController.server;

		if (reliable) {

			server.sendToAllTCP(packet);


		} else {

			server.sendToAllUDP(packet);

		}

	}

}
