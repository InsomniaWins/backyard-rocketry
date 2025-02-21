package wins.insomnia.backyardrocketry.controller;

import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.world.World;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.*;

public abstract class GameController implements IUpdateListener, IFixedUpdateListener {

	public static final int CLIENT_WRITE_BUFFER_SIZE = 16_384;
	public static final int SERVER_WRITE_BUFFER_SIZE = CLIENT_WRITE_BUFFER_SIZE * 2;
	public static final int OBJECT_BUFFER_SIZE = 20_000; // read buffer size
	private boolean started = false;
	private World world;
	private final Logger LOGGER;
	private final ConsoleHandler LOGGER_CONSOLE_HANDLER;
	private final Formatter LOGGER_CONSOLE_HANDLER_FORMATTER;
	private final SimpleDateFormat LOGGER_DATE_FORMAT;

	public GameController() {

		LOGGER_DATE_FORMAT = new SimpleDateFormat("[MM/dd/yyyy hh:mm a]");

		LOGGER = Logger.getLogger(this instanceof ClientController
				? "[CLIENT]"
				: "[SERVER]"
		);
		LOGGER.setUseParentHandlers(false);
		LOGGER_CONSOLE_HANDLER = new ConsoleHandler();
		LOGGER_CONSOLE_HANDLER_FORMATTER = new Formatter() {
			@Override
			public String format(LogRecord record) {

				Date date = new Date(record.getMillis());
				String time = LOGGER_DATE_FORMAT.format(date);

				String level = "[" + record.getLevel().getName() + "]";

				return record.getLoggerName() + " " + level + " " + time + " " + record.getMessage() + "\"\n";
			}
		};
		LOGGER_CONSOLE_HANDLER.setFormatter(LOGGER_CONSOLE_HANDLER_FORMATTER);
		Handler[] handlers = LOGGER.getHandlers();
		if (handlers != null) {
			for (Handler handler : handlers) {
				LOGGER.removeHandler(handler);
			}
		}
		LOGGER.addHandler(LOGGER_CONSOLE_HANDLER);

	}


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
