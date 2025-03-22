package wins.insomnia.backyardrocketry.controller;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ServerController extends GameController {

	public static final int TCP_PORT = 54555;
	public static final int UDP_PORT = 54777;


	private static final Listener SERVER_LISTENER = new Listener() {
		@Override
		public void received(Connection connection, Object object) {

			if (!(object instanceof Packet packet)) return;


			packet.received(Packet.SenderType.CLIENT, connection);


		}

		@Override
		public void connected(Connection connection) {


			ServerController serverController = ServerController.get();
			ServerWorld serverWorld = serverController.getWorld();

			EntityServerPlayer serverPlayer = new EntityServerPlayer(connection.getID(), serverWorld, UUID.randomUUID());

			double[] worldCenter = serverWorld.getCenterXZ();

			serverPlayer.getTransform().setPosition(new Vector3d(worldCenter[0], 100, worldCenter[1]));
			List<ChunkPosition> chunkPositionsAroundPlayer = serverWorld.getChunkPositionsAroundPlayer(serverPlayer, ServerWorld.chunkLoadDistance);


			Updater.get().queueMainThreadInstruction(() -> {
				for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {
					double chunkDistance = serverWorld.getChunkDistanceToPlayer(chunkPosition, serverPlayer);
					if (!serverWorld.isChunkLoaded(chunkPosition, ServerChunk.GenerationPass.DECORATION)) {

						if (chunkDistance <= ServerWorld.chunkLoadDistance) {
							serverWorld.queueChunkForLoading(chunkPosition, ServerChunk.GenerationPass.DECORATION);
						}
					}
				}
			});


			// wait for chunks to load
			for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

				double chunkDistance = serverWorld.getChunkDistanceToPlayer(chunkPosition, serverPlayer);

				if (chunkDistance <= ServerWorld.chunkLoadDistance) {
					while (!serverWorld.isChunkLoaded(chunkPosition, ServerChunk.GenerationPass.DECORATION)) {

					}
				}

			}

			serverPlayer.getTransform().getPosition().y = WorldGeneration.getGroundHeight(
					serverWorld.getSeed(),
					(int) serverPlayer.getTransform().getPosition().x,
					(int) serverPlayer.getTransform().getPosition().z
			) + 2;

			Updater.get().registerUpdateListener(serverPlayer);
			Updater.get().registerFixedUpdateListener(serverPlayer);
			serverWorld.setServerPlayer(connection.getID(), serverPlayer);


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

		try {
			if (reliable) {

				server.sendToAllTCP(packet);


			} else {

				server.sendToAllUDP(packet);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
