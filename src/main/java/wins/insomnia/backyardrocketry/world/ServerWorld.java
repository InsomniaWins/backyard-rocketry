package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ServerWorld extends World {


	private List<EntityServerPlayer> getPlayersNearChunk(Chunk chunk) {
		List<EntityServerPlayer> returnList = new ArrayList<>();



		return returnList;
	}


	// DO NOT CALL DIRECTLY
	// only called on main-thread
	private void _loadChunk(Chunk chunk) {

		ChunkPosition chunkPosition = chunk.getChunkPosition();

		CHUNKS.put(chunkPosition, chunk);
		CHUNKS_CURRENTLY_LOADING.remove(chunkPosition);
		ENTITIES.put(chunkPosition, new ArrayList<>());

		for (EntityServerPlayer player : getPlayersNearChunk(chunk)) {



		}

		//chunk.updateNeighborChunkMeshes();

	}


	@Override
	protected void loadChunk(ChunkPosition chunkPosition) {

		if (CHUNKS.get(chunkPosition) != null) return;

		Chunk chunk = new Chunk(this, chunkPosition);

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> _loadChunk(chunk));
		} else {
			_loadChunk(chunk);
		}

	}

	@Override
	protected void unloadChunk(ChunkPosition chunkPosition) {

		if (CHUNKS.get(chunkPosition) != null) {
			ArrayList<Entity> entitiesInChunk = ENTITIES.get(chunkPosition);
			ENTITIES.remove(chunkPosition);

			Chunk chunk = CHUNKS.get(chunkPosition);
			CHUNKS.remove(chunkPosition);

			Iterator<Entity> entityIterator = entitiesInChunk.iterator();
			while (entityIterator.hasNext()) {
				Entity entity = entityIterator.next();
				entity.removedFromWorld();

				ENTITY_LIST.remove(entity);
				entityIterator.remove();
			}

			Updater.get().unregisterUpdateListener(chunk);
			Updater.get().unregisterFixedUpdateListener(chunk);

		}

	}



	@Override
	public void logInfo(String info) {

		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) return;

		ServerController serverController = gameplayScene.getServer();

		if (serverController == null) return;

		serverController.getLogger().log(Level.INFO, info);

	}





}
