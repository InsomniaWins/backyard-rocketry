package wins.insomnia.backyardrocketry.controller;

import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.world.World;

import java.util.logging.Logger;

public abstract class GameController implements IUpdateListener, IFixedUpdateListener {


	private boolean started = false;
	private World world;
	private final Logger LOGGER = Logger.getLogger(getClass().getName());

	public Logger getLogger() {

		return LOGGER;

	}

	public boolean isServer() {
		return false;
	}

	public boolean isClient() {
		return false;
	}

	public boolean isValidController() {
		return !((!isServer() && !isClient()) || (isServer() && isClient()));
	}

	@Override
	public void fixedUpdate() {

	}

	@Override
	public void registeredFixedUpdateListener() {

	}

	@Override
	public void unregisteredFixedUpdateListener() {


	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}

	public boolean isStarted() {
		return started;
	}

	public void start() {

		if (isStarted()) return;
		started = true;

		onStart();
	}

	public void stop() {


		if (!isStarted()) return;
		started = false;

		onStop();

	}

	public World getWorld() {
		return world;
	}

	protected abstract void onStop();

	protected abstract void onStart();
}
