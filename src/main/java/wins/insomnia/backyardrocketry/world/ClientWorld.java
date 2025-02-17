package wins.insomnia.backyardrocketry.world;


import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.player.EntityClientPlayer;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

public class ClientWorld extends World {

	private EntityClientPlayer clientPlayer;


	public void setClientPlayer(EntityClientPlayer clientPlayer) {
		this.clientPlayer = clientPlayer;
	}

	public EntityClientPlayer getClientPlayer() {
		return clientPlayer;
	}

	@Override
	public void updateChunksAroundPlayer(IPlayer player) {

	}


	@Override
	public void shutdown() {

		super.shutdown();
		ClientChunk.chunkMeshGenerationExecutorService.shutdown();

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
	protected void loadChunk(ChunkPosition chunkPosition) {

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

	public void receivedChunkDataFromServer(ChunkData chunkData) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> _loadChunk(chunkData));
		} else {
			_loadChunk(chunkData);
		}


	}

}
