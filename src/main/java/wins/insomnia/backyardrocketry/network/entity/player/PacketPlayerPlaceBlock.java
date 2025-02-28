package wins.insomnia.backyardrocketry.network.entity.player;

import com.esotericsoftware.kryonet.Connection;
import org.joml.Math;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;
import wins.insomnia.backyardrocketry.entity.LivingEntity;
import wins.insomnia.backyardrocketry.entity.player.EntityPlayer;
import wins.insomnia.backyardrocketry.network.Packet;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

public class PacketPlayerPlaceBlock extends Packet {
	int worldX;
	int worldY;
	int worldZ;
	byte block;
	byte blockState;
	int face;

	public PacketPlayerPlaceBlock setFace(int face) {
		this.face = face;
		return this;
	}

	public PacketPlayerPlaceBlock setWorldX(int x) {
		this.worldX = x;
		return this;
	}

	public PacketPlayerPlaceBlock setWorldY(int y) {
		this.worldY = y;
		return this;
	}

	public PacketPlayerPlaceBlock setWorldZ(int z) {
		this.worldZ = z;
		return this;
	}

	public PacketPlayerPlaceBlock setBlock(byte block) {
		this.block = block;
		return this;
	}

	public PacketPlayerPlaceBlock setBlockState(byte blockState) {
		this.blockState = blockState;
		return this;
	}


	@Override
	public void received(SenderType senderType, Connection connection) {

		if (senderType != SenderType.CLIENT) {

			Updater.get().queueMainThreadInstruction(() -> {

				ClientWorld clientWorld = ClientWorld.getClientWorld();

				if (clientWorld == null) return;
				Chunk chunk = clientWorld.getChunkContainingBlock(worldX, worldY, worldZ);

				if (!(chunk instanceof ClientChunk clientChunk)) return;

				clientChunk.placeBlock(
						clientChunk.toLocalX(worldX),
						clientChunk.toLocalY(worldY),
						clientChunk.toLocalZ(worldZ),
						block,
						blockState
				);
			});


			return;

		}

		Updater.get().queueMainThreadInstruction(() -> {

			ServerWorld serverWorld = ServerWorld.getServerWorld();

			if (serverWorld == null) return;


			BoundingBox boundingBox = Blocks.getBlockCollision(block);
			if (boundingBox != null) {

				boundingBox.getMin().add(worldX, worldY, worldZ);
				boundingBox.getMax().add(worldX, worldY, worldZ);

				// todo: replace with better system (checking if block is colliding with entity before placing)
				for (Entity entity : serverWorld.getEntityList()) {
					if (!(entity instanceof LivingEntity)) continue;
					if (!(entity instanceof IBoundingBoxEntity boundingBoxEntity)) continue;

					if (boundingBox.collideWithBoundingBoxExclusive(boundingBoxEntity.getBoundingBox()) != Collision.AABBCollisionResultType.OUTSIDE) {
						return;
					}

				}

				for (EntityPlayer player : serverWorld.getServerPlayers()) {
					if (boundingBox.collideWithBoundingBoxExclusive(player.getBoundingBox()) != Collision.AABBCollisionResultType.OUTSIDE) {
						return;
					}
				}

			}



			Chunk chunk = serverWorld.getChunkContainingBlock(worldX, worldY, worldZ);

			if (!(chunk instanceof ServerChunk serverChunk)) return;

			serverChunk.placeBlock(
					serverChunk.toLocalX(worldX),
					serverChunk.toLocalY(worldY),
					serverChunk.toLocalZ(worldZ),
					block,
					Blocks.Face.FACE_PRESETS[Math.clamp(face, 0, Blocks.Face.FACE_PRESETS.length - 1)]
			);

		});

	}
}
