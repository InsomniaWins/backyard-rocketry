package wins.insomnia.backyardrocketry.world;


import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.entity.player.EntityServerPlayer;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.render.FogManager;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class ClientWorld extends World {

	private EntityClientPlayer clientPlayer;
	public final FogManager FOG_MANAGER;

	public ClientWorld() {
		super();
		FOG_MANAGER = new FogManager();
	}

	public void setClientPlayer(EntityClientPlayer clientPlayer) {
		this.clientPlayer = clientPlayer;
	}

	public EntityClientPlayer getClientPlayer() {
		return clientPlayer;
	}

	@Override
	public void updateChunksAroundPlayer(IPlayer player) {

		/*
		if (player instanceof EntityServerPlayer serverPlayer) {
			if (serverPlayer.isLoadingTerrain()) return;
		}

		List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player, chunkLoadDistance);
		for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

			double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);


			if (!isChunkLoaded(chunkPosition)) {

				if (chunkDistance <= chunkLoadDistance) {
					queueChunkForLoading(chunkPosition, ServerChunk.GenerationPass.DECORATION);
				}
			} else {

				Chunk chunk = getChunkSafe(chunkPosition);
				chunk.setTicksToLive(chunkLoadingTicksToLive);

			}
		}*/

		for (Chunk chunk : getChunks()) {
			chunk.setTicksToLive(chunkLoadingTicksToLive);
		}


	}


	@Override
	public void shutdown() {

		super.shutdown();
		ClientChunk.CHUNK_MESH_GENERATION_EXECUTOR_SERVICE.shutdown();

	}


	@Override
	public void logInfo(String info) {

		GameplayScene gameplayScene = GameplayScene.get();

		if (gameplayScene == null) return;

		ClientController clientController = gameplayScene.getClient();

		if (clientController == null) return;

		clientController.getLogger().info( info);

	}


	@Override
	protected void loadChunk(ChunkPosition chunkPosition, ServerChunk.GenerationPass pass) {

		// TODO: implement or make exclusive to ServerWorld???

	}


	private void _loadChunk(ChunkData chunkData) {

		ChunkPosition chunkPosition = chunkData.getChunkPosition(this);

		Chunk chunk = CHUNKS.get(chunkPosition);

		// if chunk already loaded/exists
		if (CHUNKS.get(chunkPosition) != null && chunk instanceof ClientChunk clientChunk) {

			// update chunk data
			clientChunk.gotChunkDataFromServer(chunkData);

			return;
		}

		// make new chunk object
		chunk = new ClientChunk(this, chunkData);
		CHUNKS.put(chunkPosition, chunk);
	}


	@Override
	protected void unloadChunk(ChunkPosition chunkPosition) {

		if (CHUNKS.get(chunkPosition) != null) {

			Chunk chunk = CHUNKS.get(chunkPosition);
			CHUNKS.remove(chunkPosition);

			Updater.get().unregisterUpdateListener(chunk);
			Updater.get().unregisterFixedUpdateListener(chunk);

		}

	}




	public void receivedChunkDataFromServer(ChunkData chunkData) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> _loadChunk(chunkData));
		} else {
			_loadChunk(chunkData);
		}


	}

	@Override
	public void update(double deltaTime) {

		final PriorityQueue<ClientChunk.ChunkMeshGenerationData> CHUNK_MESH_GENERATION_QUEUE = ClientChunk.CHUNK_MESH_PRIORITY_QUEUE;
		while (!CHUNK_MESH_GENERATION_QUEUE.isEmpty()) {

			ClientChunk.ChunkMeshGenerationData chunkMeshGenerationData = CHUNK_MESH_GENERATION_QUEUE.poll();
			ClientChunk.CHUNK_MESH_GENERATION_EXECUTOR_SERVICE.submit(() -> {
				// todo: combine transparent and opaque chunk generation

				ChunkData chunkData = chunkMeshGenerationData.chunk.getChunkData();

				chunkMeshGenerationData.chunk.getChunkMesh().generateMesh(
						chunkData.getBlocks(),
						chunkData.getBlockStates(),
						chunkMeshGenerationData.isDelayed
				);
				chunkMeshGenerationData.chunk.getTransparentChunkMesh().generateMesh(
						chunkData.getBlocks(),
						chunkData.getBlockStates(),
						chunkMeshGenerationData.isDelayed
				);
			});

		}

		int chunkManagementQueueSize = CHUNK_MANAGEMENT_QUEUE.size();
		for (int i = 0; i < chunkManagementQueueSize; i++) {

			ChunkManagementData chunkManagementData = CHUNK_MANAGEMENT_QUEUE.poll();

			if (chunkManagementData == null) continue;


			ClientChunk chunk = (ClientChunk) CHUNKS.get(chunkManagementData.chunkPosition);


			switch (chunkManagementData.type) {
				case LOAD -> {

					continue;

				}
				case UNLOAD -> {

					if (chunk == null) continue;

					ChunkPosition chunkPosition = chunkManagementData.chunkPosition;
					//unloadChunk(chunkPosition);
				}
				default -> {

				}
			}


		}


		/*

		int chunkManagementQueueSize = CHUNK_MANAGEMENT_QUEUE.size();
		for (int i = 0; i < chunkManagementQueueSize; i++) {

			ChunkManagementData chunkManagementData = CHUNK_MANAGEMENT_QUEUE.poll();

			if (chunkManagementData == null) continue;


			ServerChunk chunk = (ServerChunk) CHUNKS.get(chunkManagementData.chunkPosition);


			switch (chunkManagementData.type) {
				case LOAD -> {

					if (chunk == null) {
						CHUNK_MANAGEMENT_EXECUTOR_SERVICE.submit(() -> loadChunk(chunkManagementData.chunkPosition, chunkManagementData.pass));
					}

				}
				case UNLOAD -> {
					ChunkPosition chunkPosition = chunkManagementData.chunkPosition;

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



		 */



	}
}
