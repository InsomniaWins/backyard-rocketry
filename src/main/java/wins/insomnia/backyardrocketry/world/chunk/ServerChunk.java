package wins.insomnia.backyardrocketry.world.chunk;

import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.item.EntityItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.network.entity.player.PacketPlayerBreakBlock;
import wins.insomnia.backyardrocketry.network.entity.player.PacketPlayerPlaceBlock;
import wins.insomnia.backyardrocketry.network.world.PacketLoadChunk;
import wins.insomnia.backyardrocketry.network.world.PacketUpdateBlock;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;

import java.util.ArrayList;

public class ServerChunk extends Chunk {


	public ServerChunk(World world, ChunkPosition chunkPosition) {
		super(world, chunkPosition);
		chunkData = ChunkIO.loadChunk(chunkPosition);
	}

	public PacketLoadChunk createLoadPacket() {

		return new PacketLoadChunk(chunkData);

	}


	public void setBlock(int x, int y, int z, byte block, byte blockState, boolean updateClients) {
		super.setBlock(x, y, z, block, blockState);

		if (updateClients) {
			ServerController.sendReliable(
					new PacketUpdateBlock()
							.setWorldX(toGlobalX(x))
							.setWorldY(toGlobalY(y))
							.setWorldZ(toGlobalZ(z))
							.setBlock(getBlock(x, y, z))
							.setBlockState(blockState)
			);
		}

	}

	public void setBlock(int x, int y, int z, byte block, byte blockState) {
		setBlock(x, y, z, block, blockState, true);
	}


	public void placeBlock(int x, int y, int z, byte block, byte blockState) {

		setBlock(x, y, z, block, blockState, false);

		ServerController.sendReliable(
				new PacketPlayerPlaceBlock()
						.setWorldX(toGlobalX(x))
						.setWorldY(toGlobalY(y))
						.setWorldZ(toGlobalZ(z))
						.setBlock(block)
						.setBlockState(blockState)
		);

	}


	public void breakBlock(int x, int y, int z, boolean dropLoot) {

		if (!isBlockInBoundsLocal(x, y, z)) return;

		byte blockBroken = getBlock(x, y, z);

		if (Block.getBlockHealth(blockBroken) < 0) return;

		setBlock(x, y, z, Block.AIR, (byte) 0, false);


		if (dropLoot) {

			BlockLoot blockLoot = BlockLoot.getBlockLoot(blockBroken);

			if (blockLoot == null) {
				return;
			}

			ArrayList<ArrayList<Object>> defaultLoot = blockLoot.getLootOfType("default");
			for (ArrayList<Object> lootEntry : defaultLoot) {

				String itemSynonym = (String) lootEntry.get(0);
				int volume = (Integer) lootEntry.get(1);

				Item item = Item.getItem(itemSynonym);
				ItemStack itemStack = new ItemStack(item, volume);

				ServerWorld serverWorld = ServerWorld.getServerWorld();
				if (serverWorld != null) {

					serverWorld.dropItem(
							itemStack,
							getX() + x + 0.5f,
							getY() + y + 0.5f,
							getZ() + z + 0.5f,
							0.0,
							0.0,
							0.0
					);

				}

			}

		}

		ServerController.sendReliable(
				new PacketPlayerBreakBlock()
						.setWorldX(toGlobalX(x))
						.setWorldY(toGlobalY(y))
						.setWorldZ(toGlobalZ(z))
		);

	}

}
