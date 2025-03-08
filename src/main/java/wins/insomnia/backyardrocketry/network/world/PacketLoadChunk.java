package wins.insomnia.backyardrocketry.network.world;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;


public class PacketLoadChunk extends Packet {


	private static final int CHUNK_DATA_SECTION_AMOUNT = 4;
	private static final int BYTES_PER_CHUNK_DATA_SECTION = Math.round(ChunkIO.BYTES_PER_CHUNK / (float) CHUNK_DATA_SECTION_AMOUNT);


	public byte[] serializedChunkData;
	public int chunkX;
	public int chunkY;
	public int chunkZ;

	// only used for kryonet
	public PacketLoadChunk() {
		serializedChunkData = new byte[0];
	}


	public PacketLoadChunk(ChunkData chunkData) {

		serializedChunkData = ChunkData.serialize(chunkData);

	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		// clients can only be told by server to load chunks, no other way
		if (senderType != SenderType.SERVER) {

			Updater.get().queueMainThreadInstruction(() -> {

				ServerWorld serverWorld = World.getServerWorld();

				if (serverWorld == null) return;

				ChunkPosition chunkPosition = new ChunkPosition(chunkX, chunkY, chunkZ);

				ServerChunk serverChunk = (ServerChunk) serverWorld.getChunk(chunkPosition);

				if (serverChunk == null) return;

				if (!serverChunk.hasFinishedPass(ServerChunk.GenerationPass.DECORATION)) return;

				PacketLoadChunk packet = new PacketLoadChunk();

				while (!serverChunk.getChunkData().grabThreadOwnership()) {}

				packet.serializedChunkData = ChunkData.serialize(serverChunk.getChunkData());

				while (!serverChunk.getChunkData().loseThreadOwnership()) {}

				ServerController.sendReliable(
						connection.getID(),
						packet
				);

			});

			return;
		}

		Updater.get().queueMainThreadInstruction(() -> {

			ClientWorld clientWorld = World.getClientWorld();

			if (clientWorld == null) return;

			ChunkData chunkData = ChunkData.deserialize(serializedChunkData);
			ChunkPosition chunkPosition = chunkData.getChunkPosition(clientWorld);

			if (chunkPosition != null) {

				Chunk chunk = clientWorld.getChunk(chunkPosition);

				if (chunk == null) {
					clientWorld.receivedChunkDataFromServer(chunkData);
				} else {

					((ClientChunk) chunk).getChunkData().replaceBlocks(chunkData);

				}

			}

		});

	}



}
