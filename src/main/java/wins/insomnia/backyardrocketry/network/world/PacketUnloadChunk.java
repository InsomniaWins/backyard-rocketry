package wins.insomnia.backyardrocketry.network.world;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;


public class PacketUnloadChunk extends Packet {

	public int chunkX;
	public int chunkY;
	public int chunkZ;

	// only used for kryonet
	public PacketUnloadChunk() {}

	public PacketUnloadChunk(int chunkX, int chunkY, int chunkZ) {

		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;

	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.SERVER) {
			return;
		}

		Updater.get().queueMainThreadInstruction(() -> {

			ClientWorld clientWorld = World.getClientWorld();

			if (clientWorld == null) return;

			ChunkPosition chunkPosition = new ChunkPosition(chunkX, chunkY, chunkZ);

			Chunk chunk = clientWorld.getChunkSafe(chunkPosition);

			if (chunk == null) return;

			clientWorld.queueChunkForUnloading(chunkPosition);


		});

	}



}
