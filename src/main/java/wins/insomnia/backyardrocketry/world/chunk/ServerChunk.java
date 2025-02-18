package wins.insomnia.backyardrocketry.world.chunk;

import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.EntityItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerBreakBlock;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerPlaceBlock;
import wins.insomnia.backyardrocketry.network.world.PacketLoadChunk;
import wins.insomnia.backyardrocketry.network.world.PacketUpdateBlock;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
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


	public void setBlock(int x, int y, int z, byte block, boolean updateClients) {
		super.setBlock(x, y, z, block);

		if (updateClients) {
			ServerController.sendReliable(
					new PacketUpdateBlock()
							.setWorldX(toGlobalX(x))
							.setWorldY(toGlobalY(y))
							.setWorldZ(toGlobalZ(z))
							.setBlock(getBlock(x, y, z))
			);
		}

	}

	public void setBlock(int x, int y, int z, byte block) {
		setBlock(x, y, z, block, true);
	}


	public void placeBlock(int x, int y, int z, byte block) {

		setBlock(x, y, z, block, false);

		ServerController.sendReliable(
				new PacketPlayerPlaceBlock()
						.setWorldX(toGlobalX(x))
						.setWorldY(toGlobalY(y))
						.setWorldZ(toGlobalZ(z))
						.setBlock(block)
		);

	}


	public void breakBlock(int x, int y, int z, boolean dropLoot) {

		byte blockBroken = getBlock(x, y, z);
		setBlock(x, y, z, Block.AIR, false);


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

				EntityItem itemEntity = new EntityItem(itemStack, getWorld());
				getWorld().addEntity(itemEntity, getX() + x + 0.5f, getY() + y + 0.5f, getZ() + z + 0.5f);
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
