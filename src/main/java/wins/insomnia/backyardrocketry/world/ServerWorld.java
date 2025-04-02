package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.item.EntityServerItem;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.network.PacketDropItem;
import wins.insomnia.backyardrocketry.network.entity.PacketRemoveEntity;
import wins.insomnia.backyardrocketry.network.world.PacketUnloadChunk;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.util.*;
import java.util.logging.Level;

public class ServerWorld extends World {

	private final HashMap<Integer, EntityServerPlayer> SERVER_PLAYER_HASH_MAP;

	public ServerWorld() {
		super();
		SERVER_PLAYER_HASH_MAP = new HashMap<>();
	}


	public void removeEntity(Entity entity, boolean notifyClients) {

		super.removeEntity(entity);

		if (notifyClients) {

			ServerController.sendReliable(
					new PacketRemoveEntity()
							.setUuid(entity.getUUID())
			);

		}

	}

	public void dropItem(ItemStack itemStack, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {

		UUID uuid = UUID.randomUUID();

		EntityServerItem itemEntity = new EntityServerItem(itemStack, this, uuid);
		addEntity(itemEntity, x, y, z);
		itemEntity.getVelocity().set(velocityX, velocityY, velocityZ);

		int itemId = itemStack.getItem().getId();
		int itemAmount = itemStack.getAmount();

		ServerController.sendReliable(
				new PacketDropItem()
						.setUuid(uuid)
						.setItem(itemId)
						.setAmount(itemAmount)
		);

	}


	public void submitChunkTask(Runnable task) {
		CHUNK_MANAGEMENT_EXECUTOR_SERVICE.submit(task);
	}


	@Override
	public void setBlock(int x, int y, int z, byte block, byte blockState) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {

			//logInfo("Tried setting block in thread other than main thread! Queuing block placement instead.");

			Updater.get().queueMainThreadInstruction(() -> {
				setBlock(x, y, z, block, blockState);
			});
			return;
		}


		Chunk chunk = getChunkContainingBlock(x, y, z);

		if (chunk == null) {
			// TODO: implement queue
			return;
		}

		chunk.setBlock(chunk.toLocalX(x), chunk.toLocalY(y), chunk.toLocalZ(z), block, blockState);

	}


	public void loadChunkImmediate(ChunkPosition chunkPosition, ServerChunk.GenerationPass generationPass) {
		_loadChunk(chunkPosition, generationPass);
	}


	// DO NOT CALL DIRECTLY
	// only called on main-thread
	private void _loadChunk(ChunkPosition chunkPosition, ServerChunk.GenerationPass generationPass) {

		Chunk chunk = CHUNKS.get(chunkPosition);

		if (chunk != null) {

			ServerChunk serverChunk = (ServerChunk) chunk;

			if (serverChunk.getDesiredGenerationPassOrdinal() >= generationPass.ordinal()) {
				return;
			}

			serverChunk.setDesiredGenerationPass(generationPass);
			return;
		}

		ServerChunk serverChunk = new ServerChunk(this, chunkPosition);

		CHUNKS.put(chunkPosition, serverChunk);

	}

	public boolean isChunkLoaded(ChunkPosition chunkPosition, ServerChunk.GenerationPass pass) {

		Chunk chunk = CHUNKS.get(chunkPosition);
		if (chunk == null) return false;

		ServerChunk serverChunk = (ServerChunk) chunk;
		if (!serverChunk.hasFinishedPass(pass)) return false;

		return true;
	}


	@Override
	public void updateChunksAroundPlayer(IPlayer player) {

		List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player, chunkLoadDistance);
		for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

			double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);

			if (!isChunkLoaded(chunkPosition, ServerChunk.GenerationPass.DECORATION)) {
				if (chunkDistance <= chunkLoadDistance) {
					queueChunkForLoading(chunkPosition, ServerChunk.GenerationPass.DECORATION);
				}
			} else {

				Chunk chunk = getChunkSafe(chunkPosition);
				chunk.setTicksToLive(chunkLoadingTicksToLive);

			}
		}

/*
		for (ChunkPosition chunkPosition : CHUNKS.keySet()) {

			Chunk chunk = getChunk(chunkPosition);

			if (chunk.getTicksToLive() <= 0) queueChunkForUnloading(chunkPosition);

			chunk.decrementTicksToLive();

		}*/


	}

	public void setServerPlayer(int connectionId, EntityServerPlayer serverPlayer) {
		SERVER_PLAYER_HASH_MAP.put(connectionId, serverPlayer);
	}

	public EntityServerPlayer getServerPlayer(int connectionId) {

		return SERVER_PLAYER_HASH_MAP.get(connectionId);

	}

	public Collection<EntityServerPlayer> getServerPlayers() {
		return SERVER_PLAYER_HASH_MAP.values();
	}

	@Override
	public void loadChunk(ChunkPosition chunkPosition, ServerChunk.GenerationPass generationPass) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> _loadChunk(chunkPosition, generationPass));
		} else {
			_loadChunk(chunkPosition, generationPass);
		}

	}


	@Override
	protected void unloadChunk(ChunkPosition chunkPosition) {

		if (CHUNKS.get(chunkPosition) != null) {
			/*HashMap<UUID, Entity> entitiesInChunk = CHUNK_ENTITY_MAP.get(chunkPosition);
			CHUNK_ENTITY_MAP.remove(chunkPosition);


			Iterator<UUID> entityIterator = entitiesInChunk.keySet().iterator();
			while (entityIterator.hasNext()) {
				UUID uuid = entityIterator.next();
				Entity entity = entitiesInChunk.get(uuid);

				entity.removedFromWorld();

				ENTITY_MAP.remove(uuid);
				entityIterator.remove();
			}*/

			Chunk chunk = CHUNKS.get(chunkPosition);
			CHUNKS.remove(chunkPosition);

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


	@Override
	public void update(double deltaTime) {



		int chunkManagementQueueSize = CHUNK_MANAGEMENT_QUEUE.size();
		for (int i = 0; i < chunkManagementQueueSize; i++) {

			ChunkManagementData chunkManagementData = CHUNK_MANAGEMENT_QUEUE.poll();

			if (chunkManagementData == null) continue;


			ServerChunk chunk = (ServerChunk) CHUNKS.get(chunkManagementData.chunkPosition);


			switch (chunkManagementData.type) {
				case LOAD -> {

					if (chunk == null) {
						loadChunk(chunkManagementData.chunkPosition, chunkManagementData.pass);
					}

				}
				case UNLOAD -> {
					ChunkPosition chunkPosition = chunkManagementData.chunkPosition;

					if (chunk == null) continue;

					// if chunk is still loading, wait for it to finish loading before unloading it
					if (!chunk.hasFinishedDesiredGenerationPass()) {

						CHUNK_MANAGEMENT_QUEUE.offer(chunkManagementData);

					} else {
						unloadChunk(chunkPosition);
					}
				}
				default -> {

				}
			}


		}

	}


}
