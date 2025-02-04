package wins.insomnia.backyardrocketry.gameframework;

import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.scenes.SceneManager;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;

public class ClientController extends GameController {

	private ClientWorld world;

	private TestPlayer clientPlayer;

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	protected void onStop() {

		world.shutdown();

	}

	@Override
	protected void onStart() {

		world = new ClientWorld();
		clientPlayer = new TestPlayer(world);
		double[] centerXZ = world.getCenterXZ();
		clientPlayer.getPosition().set(centerXZ[0], 164, centerXZ[1]);

	}

	public ClientWorld getWorld() {
		return world;
	}

	public TestPlayer getPlayer() {
		return clientPlayer;
	}
}
