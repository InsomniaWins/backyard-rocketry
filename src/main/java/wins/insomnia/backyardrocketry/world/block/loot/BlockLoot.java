package wins.insomnia.backyardrocketry.world.block.loot;

import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.util.io.LoadTask;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockLoot {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final HashMap<Byte, BlockLoot> BLOCK_LOOT_HASH_MAP = new HashMap<>();
	private HashMap<String, ArrayList<ArrayList<Object>>> lootTable;


	public BlockLoot() {
		lootTable = new HashMap<>();
	}

	public static List<LoadTask> makeLoadingTaskList() {

		List<LoadTask> taskList = new ArrayList<>();

		for (byte block : Block.getBlocks()) {

			String blockStateName = Block.getBlockStateName(block);

			taskList.add(new LoadTask("Loading block loot: " + blockStateName, () -> {
				BlockLoot blockLoot = loadBlockLoot(blockStateName);
				BLOCK_LOOT_HASH_MAP.put(block, blockLoot);
			}));

		}

		return taskList;
	}

	public static BlockLoot getBlockLoot(byte block) {
		return BLOCK_LOOT_HASH_MAP.get(block);
	}

	public static BlockLoot getBlockLoot(String blockStateName) {

		return BLOCK_LOOT_HASH_MAP.get(Block.getBlockIdFromSynonym(blockStateName));

	}


	public static BlockLoot loadBlockLoot(String lootFileName) {

		// get path to model
		URL src = BlockLoot.class.getResource("/loot/blocks/" + lootFileName + ".json");

		// if could not find file
		if (src == null) {
			System.err.println("Failed to load block loot: " + lootFileName);
			return null;
		}

		// load loot
		try {
			Map<String, ArrayList<ArrayList<Object>>> loot = MAPPER.readValue(src, Map.class);
			BlockLoot blockLoot = new BlockLoot();

			blockLoot.lootTable.putAll(loot);

			return blockLoot;
		} catch (IOException ignored) {

			ignored.printStackTrace();
			System.out.println("Failed to load block loot and got IOException: " + lootFileName);

			return null;
		}

	}

	public ArrayList<ArrayList<Object>> getLootOfType(String lootType) {
		return lootTable.get(lootType);
	}

	public static String getItemFromLootAtIndex(ArrayList<ArrayList<Object>> loot, int index) {

		return (String) loot.get(index).get(0);

	}

	public static int getItemVolumeFromLootAtIndex(ArrayList<ArrayList<Object>> loot, int index) {

		return (Integer) loot.get(index).get(1);

	}


	public ItemStack[] getBlockBreakLoot() {

		return null;

	}

}
