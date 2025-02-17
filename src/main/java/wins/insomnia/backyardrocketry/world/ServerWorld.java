package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ServerWorld extends World {

	private final HashMap<Integer, EntityServerPlayer> SERVER_PLAYER_HASH_MAP;

	public ServerWorld() {
		super();
		SERVER_PLAYER_HASH_MAP = new HashMap<>();
	}

	private List<EntityServerPlayer> getPlayersNearChunk(Chunk chunk) {
		List<EntityServerPlayer> returnList = new ArrayList<>();



		return returnList;
	}


	// DO NOT CALL DIRECTLY
	// only called on main-thread
	private void _loadChunk(ChunkPosition chunkPosition) {

		if (CHUNKS.get(chunkPosition) != null) return;
		ServerChunk chunk = new ServerChunk(this, chunkPosition);

		CHUNKS.put(chunkPosition, chunk);
		CHUNKS_CURRENTLY_LOADING.remove(chunkPosition);
		ENTITIES.put(chunkPosition, new ArrayList<>());

		// todo: send only to those who need it
		ServerController.sendReliable(chunk.createLoadPacket());

	}


	@Override
	public void updateChunksAroundPlayer(IPlayer player) {

		List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player, chunkLoadDistance);
		for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

			Chunk chunk = CHUNKS.get(chunkPosition);
			double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);

			if (chunk == null) {

				if (chunkDistance <= chunkLoadDistance) {
					queueChunkForLoading(chunkPosition);
				}
			}
		}

		/*

		TODO: implement chunk unloading

		for (Map.Entry<ChunkPosition, Chunk> chunkEntry : CHUNKS.entrySet()) {

			ChunkPosition chunkPosition = chunkEntry.getKey();
			Chunk chunk = chunkEntry.getValue();
			double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);

			chunk.setShouldProcess(chunkDistance <= chunkProcessDistance);

			if (chunkDistance >= chunkUnloadDistance) {
				chunk.ticksToLive -= 1;
			}

			if (chunk.isProcessing()) {
				// if chunk is processing, make it stay alive
				chunk.ticksToLive = Math.max(1, chunk.ticksToLive);
			} else {
				// check for chunk unloading
				if (chunk.ticksToLive <= 0) {
					queueChunkForUnloading(chunkPosition);
				}
			}

		}*/


	}

	public void setServerPlayer(int connectionId, EntityServerPlayer serverPlayer) {
		SERVER_PLAYER_HASH_MAP.put(connectionId, serverPlayer);
	}

	public EntityServerPlayer getServerPlayer(int connectionId) {

		return SERVER_PLAYER_HASH_MAP.get(connectionId);

	}


	@Override
	protected void loadChunk(ChunkPosition chunkPosition) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> _loadChunk(chunkPosition));
		} else {
			_loadChunk(chunkPosition);
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


	public boolean isPlayerInUnloadedChunk(EntityServerPlayer player) {

		List<Chunk> chunksTouchingPlayer = Collision.getChunksTouchingBoundingBox(this, player.getBoundingBox(), true);

		return chunksTouchingPlayer.contains(null);
	}


}
