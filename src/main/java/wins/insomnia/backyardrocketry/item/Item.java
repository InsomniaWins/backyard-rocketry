package wins.insomnia.backyardrocketry.item;

import wins.insomnia.backyardrocketry.world.block.Block;

import java.util.HashMap;
import java.util.List;

public class Item {

	private final int ID;
	private final String NAME;
	private final String ID_SYNONYM;
	private final int MAX_VOLUME;
	private final int VOLUME_PER_ITEM;
	private static int nextAvailableItemIdForRegistration = Integer.MIN_VALUE;
	private static final HashMap<Integer, Item> ITEM_HASHMAP = new HashMap<>();
	private static final HashMap<String, Integer> ITEM_SYNONYM_MAP = new HashMap<>();
	public static final HashMap<Byte, BlockItem> BLOCK_ITEM_MAP = new HashMap<>();

	public static final BlockItem COBBLESTONE = registerBlockItem(
			Block.COBBLESTONE,
			"Cobblestone",
			"cobblestone",
			1000,
			99000
	);

	public static final Item GRASS = registerBlockItem(
			Block.GRASS,
			"Grass",
			"grass",
			1000,
			99000
	);

	public static final Item DIRT = registerBlockItem(
			Block.DIRT,
			"Dirt",
			"dirt",
			1000,
			99000
	);

	public static final Item LEAVES = registerBlockItem(
			Block.LEAVES,
			"Leaves",
			"leaves",
			1000,
			99000
	);

	public static final Item STONE = registerBlockItem(
			Block.STONE,
			"Stone",
			"stone",
			1000,
			99000
	);

	public static final Item WOODEN_PLANKS = registerBlockItem(
			Block.WOODEN_PLANKS,
			"Wooden Planks",
			"wooden_planks",
			1000,
			99000
	);

	public static final Item WOOD = registerBlockItem(
			Block.WOOD,
			"Wood",
			"wood",
			1000,
			99000
	);

	public static final Item LOG = registerBlockItem(
			Block.LOG,
			"Log",
			"log",
			1000,
			99000
	);

	public static final Item GLASS = registerBlockItem(
			Block.GLASS,
			"Glass",
			"glass",
			1000,
			99000
	);

	public static final Item BRICKS = registerBlockItem(
			Block.BRICKS,
			"Bricks",
			"bricks",
			1000,
			99000
	);




	public Item(int itemId, String itemName, String itemSynonym, int volumePerItem, int maxItemVolume) {
		this.ID = itemId;
		this.NAME = itemName;
		this.ID_SYNONYM = itemSynonym;
		this.MAX_VOLUME = maxItemVolume;
		this.VOLUME_PER_ITEM = volumePerItem;
	}

	public int getMaxVolume() {
		return MAX_VOLUME;
	}

	public int getVolumePerItem() {
		return VOLUME_PER_ITEM;
	}

	public String getIdSynonym() {
		return ID_SYNONYM;
	}

	public String getName() {
		return NAME;
	}

	public int getId() {
		return ID;
	}

	public static Item getItem(int itemId) {
		return ITEM_HASHMAP.get(itemId);
	}

	public static int getItemIdFromSynonym(String itemSynonym) {
		return ITEM_SYNONYM_MAP.get(itemSynonym);
	}

	public static Item getItem(String itemSynonym) {

		int itemId = getItemIdFromSynonym(itemSynonym);
		return ITEM_HASHMAP.get(itemId);

	}

	public static Item registerItem(String itemName, String itemSynonym, int volumePerItem, int maxVolume) {
		int itemId = nextAvailableItemIdForRegistration++;
		Item item = new Item(
				itemId,
				itemName,
				itemSynonym,
				volumePerItem,
				maxVolume
		);

		ITEM_HASHMAP.put(itemId, item);
		ITEM_SYNONYM_MAP.put(itemSynonym, itemId);

		return item;
	}

	public static BlockItem registerBlockItem(byte blockId, String itemName, String itemSynonym, int volumePerItem, int maxVolume) {
		int itemId = nextAvailableItemIdForRegistration++;
		BlockItem item = new BlockItem(
				blockId,
				itemId,
				itemName,
				itemSynonym,
				volumePerItem,
				maxVolume
		);

		BLOCK_ITEM_MAP.put(blockId, item);

		ITEM_HASHMAP.put(itemId, item);
		ITEM_SYNONYM_MAP.put(itemSynonym, itemId);

		return item;
	}

	public static BlockItem getBlockItem(byte blockId) {
		return BLOCK_ITEM_MAP.get(blockId);
	}
}
