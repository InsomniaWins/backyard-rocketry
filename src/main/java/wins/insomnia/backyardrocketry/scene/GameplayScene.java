package wins.insomnia.backyardrocketry.scene;

import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.world.World;

public class GameplayScene extends Scene {


	public enum GameType {

		SERVER, // hosted server, no client
		CLIENT, // client connected to a server
		CLIENT_SERVER // client playing self-hosted OFFLINE save (not public server; is internal server)

	}

	private final GameType GAME_TYPE;
	private final ClientController CLIENT_CONTROLLER;
	private final ServerController SERVER_CONTROLLER;



	public GameplayScene(GameType gameType) {

		switch (gameType) {
			case CLIENT -> {

				String ip = "";
				int tcpPort = 0;
				int udpPort = 0;

				CLIENT_CONTROLLER = new ClientController(ip, tcpPort, udpPort);
				SERVER_CONTROLLER = null;

			}

			case SERVER -> {

				CLIENT_CONTROLLER = null;
				SERVER_CONTROLLER = new ServerController(false);

			}

			case CLIENT_SERVER -> {

				CLIENT_CONTROLLER = new ClientController();
				SERVER_CONTROLLER = new ServerController(true);

			}

			default -> throw new RuntimeException("Failed to initialize gameplay scene without client or server controllers.");
		}

		GAME_TYPE = gameType;


	}




	public ServerController getServer() {
		return SERVER_CONTROLLER;
	}

	public ClientController getClient() {
		return CLIENT_CONTROLLER;
	}

	public GameType getGameType() {
		return GAME_TYPE;
	}

	public boolean isServer() {
		return getGameType() == GameType.SERVER;
	}

	public boolean isClient() {
		return getGameType() == GameType.CLIENT;
	}

	public boolean isClientServer() {
		return getGameType() == GameType.CLIENT_SERVER;
	}

	@Override
	public void update(double delta) {

	}

	@Override
	public void fixedUpdate() {

	}

	@Override
	public void sceneRegistered() {

		if (SERVER_CONTROLLER != null) {
			registerGameObject(SERVER_CONTROLLER);
			SERVER_CONTROLLER.start();

			if (CLIENT_CONTROLLER != null) {

				registerGameObject(CLIENT_CONTROLLER);
				CLIENT_CONTROLLER.start();

			}

			return;
		}



		registerGameObject(CLIENT_CONTROLLER);
		CLIENT_CONTROLLER.start();


	}

	@Override
	public void sceneUnregistered() {

		if (CLIENT_CONTROLLER != null) {
			unregisterGameObject(CLIENT_CONTROLLER);
			CLIENT_CONTROLLER.stop();
		}

		if (SERVER_CONTROLLER != null) {
			unregisterGameObject(SERVER_CONTROLLER);
			SERVER_CONTROLLER.stop();
		}

	}

	public static boolean hasClient() {

		GameplayScene gameplayScene = get();

		if (gameplayScene == null) return false;

		return gameplayScene.getClient() != null;

	}

	public static GameplayScene get() {
		if (!(SceneManager.get().getCurrentScene() instanceof GameplayScene gameplayScene)) {
			return null;
		}

		return gameplayScene;
	}

	// always returns server world UNLESS called from client
	// if called from a client_server, the server world will be returned rather than the client world
	public static World getWorld() {
		return getWorld(false);
	}

	public static EntityClientPlayer getClientPlayer() {

		GameplayScene scene = get();

		if (scene == null) return null;

		if (scene.getGameType() == null) return null;

		ClientController clientController = scene.getClient();

		if (clientController == null) return null;

		return clientController.getPlayer();
	}

	public static World getWorld(boolean preferClientWorld) {

		GameplayScene scene = get();

		if (scene == null) return null;

		if (scene.getGameType() == null) return null;

		ClientController clientController = scene.getClient();
		ServerController serverController = scene.getServer();

		// for client game type
		if (scene.getGameType() == GameType.CLIENT) {
			if (clientController == null) return null;
			return clientController.getWorld();
		}

		// for client_server preferring client world
		if (preferClientWorld && clientController != null) return clientController.getWorld();

		// for server or client_server preferring server world
		if (serverController == null) return null;

		return serverController.getWorld();
	}

}
