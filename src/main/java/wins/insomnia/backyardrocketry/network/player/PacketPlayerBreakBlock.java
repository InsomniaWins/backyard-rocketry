package wins.insomnia.backyardrocketry.network.player;

import com.esotericsoftware.kryonet.Connection;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.network.world.PacketUpdateBlock;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

public class PacketPlayerBreakBlock extends Packet {


	int worldX;
	int worldY;
	int worldZ;


	public PacketPlayerBreakBlock setWorldX(int x) {
		this.worldX = x;
		return this;
	}

	public PacketPlayerBreakBlock setWorldY(int y) {
		this.worldY = y;
		return this;
	}

	public PacketPlayerBreakBlock setWorldZ(int z) {
		this.worldZ = z;
		return this;
	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.CLIENT) {

			ClientWorld clientWorld = ClientWorld.getClientWorld();

			if (clientWorld == null) return;

			Chunk chunk = clientWorld.getChunkContainingBlock(worldX, worldY, worldZ);

			if (chunk == null) return;

			if (!(chunk instanceof ClientChunk clientChunk)) return;

			int localX = clientChunk.toLocalX(worldX);
			int localY = clientChunk.toLocalY(worldY);
			int localZ = clientChunk.toLocalZ(worldZ);

			clientChunk.breakBlock(localX, localY, localZ, true);

			return;
		}

		ServerWorld serverWorld = ServerWorld.getServerWorld();

		if (serverWorld == null) return;

		Chunk chunk = serverWorld.getChunkContainingBlock(worldX, worldY, worldZ);

		if (chunk == null) return;

		if (!(chunk instanceof ServerChunk serverChunk)) return;

		int localX = serverChunk.toLocalX(worldX);
		int localY = serverChunk.toLocalY(worldY);
		int localZ = serverChunk.toLocalZ(worldZ);

		serverChunk.breakBlock(localX, localY, localZ, false);

	}
}
