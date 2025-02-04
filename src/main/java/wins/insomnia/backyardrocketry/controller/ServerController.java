package wins.insomnia.backyardrocketry.controller;

import wins.insomnia.backyardrocketry.world.ServerWorld;

public class ServerController extends GameController {

	private ServerWorld world;

	@Override
	public boolean isServer() {
		return true;
	}

	@Override
	protected void onStop() {

		world.shutdown();

	}


	@Override
	protected void onStart() {

		world = new ServerWorld();

	}

	public ServerWorld getWorld() {
		return world;
	}
}
