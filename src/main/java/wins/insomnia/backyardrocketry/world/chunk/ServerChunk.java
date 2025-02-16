package wins.insomnia.backyardrocketry.world.chunk;

import wins.insomnia.backyardrocketry.network.world.LoadChunkPacket;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;

public class ServerChunk extends Chunk {


	public ServerChunk(World world, ChunkPosition chunkPosition) {
		super(world, chunkPosition);
		chunkData = ChunkIO.loadChunk(chunkPosition);
	}

	public LoadChunkPacket createLoadPacket() {

		return new LoadChunkPacket(chunkData);

	}

}
