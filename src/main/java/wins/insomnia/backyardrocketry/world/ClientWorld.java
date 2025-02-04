package wins.insomnia.backyardrocketry.world;


import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.scenes.GameplayScene;

public class ClientWorld extends World {



	@Override
	public void logInfo(String info) {

		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) return;

		ClientController clientController = gameplayScene.getClient();

		if (clientController == null) return;

		clientController.getLogger().info( info);

	}


}
