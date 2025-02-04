package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.scenes.GameplayScene;

import java.util.logging.Level;

public class ServerWorld extends World {



	@Override
	public void logInfo(String info) {

		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) return;

		ServerController serverController = gameplayScene.getServer();

		if (serverController == null) return;

		serverController.getLogger().log(Level.INFO, info);

	}






}
