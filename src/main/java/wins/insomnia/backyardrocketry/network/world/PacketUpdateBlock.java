package wins.insomnia.backyardrocketry.network.world;

import com.esotericsoftware.kryonet.Connection;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;

public class PacketUpdateBlock extends Packet {


	int worldX;
	int worldY;
	int worldZ;
	byte block;
	byte blockState;

	public PacketUpdateBlock setBlock(byte block) {

		this.block = block;
		return this;

	}

	public PacketUpdateBlock setBlockState(byte blockState) {
		this.blockState = blockState;
		return this;
	}

	public PacketUpdateBlock setWorldX(int x) {
		this.worldX = x;
		return this;
	}

	public PacketUpdateBlock setWorldY(int y) {
		this.worldY = y;
		return this;
	}

	public PacketUpdateBlock setWorldZ(int z) {
		this.worldZ = z;
		return this;
	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.SERVER) return;

		Updater.get().queueMainThreadInstruction(() -> {

			ClientWorld clientWorld = ClientWorld.getClientWorld();

			if (clientWorld == null) return;

			Chunk chunk = clientWorld.getChunkContainingBlock(worldX, worldY, worldZ);

			if (!(chunk instanceof ClientChunk clientChunk)) return;


			clientChunk.setBlock(
					chunk.toLocalX(worldX),
					chunk.toLocalY(worldY),
					chunk.toLocalZ(worldZ),
					block,
					blockState,
					true,
					true
			);

		});




	}
}
