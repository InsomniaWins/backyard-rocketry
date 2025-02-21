package wins.insomnia.backyardrocketry.network.world;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;


public class PacketLoadChunk extends Packet {


	private static final int CHUNK_DATA_SECTION_AMOUNT = 4;
	private static final int BYTES_PER_CHUNK_DATA_SECTION = Math.round(ChunkIO.BYTES_PER_CHUNK / (float) CHUNK_DATA_SECTION_AMOUNT);

	// is also compressed
	public byte[] serializedChunkData;


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
		if (senderType != SenderType.SERVER) return;

		ClientWorld clientWorld = World.getClientWorld();

		if (clientWorld == null) return;

		ChunkData chunkData = ChunkData.deserialize(serializedChunkData);
		ChunkPosition chunkPosition = chunkData.getChunkPosition(clientWorld);

		if (chunkPosition != null) {

			clientWorld.receivedChunkDataFromServer(chunkData);

		}

	}



}
