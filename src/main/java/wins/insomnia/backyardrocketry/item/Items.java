package wins.insomnia.backyardrocketry.item;

import wins.insomnia.backyardrocketry.item.types.block.*;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import java.util.HashMap;

public class Items {

	private static int nextAvailableItemIdForRegistration = Integer.MIN_VALUE;
	private static final HashMap<Integer, Item> ITEM_HASHMAP = new HashMap<>();
	private static final HashMap<String, Integer> ITEM_SYNONYM_MAP = new HashMap<>();
	public static final HashMap<Byte, BlockItem> BLOCK_ITEM_MAP = new HashMap<>();


	public static final Item COBBLESTONE = registerItem(new BlockItemCobblestone());
	public static final Item GRASS = registerItem(new BlockItemGrass());
	public static final Item DIRT = registerItem(new BlockItemDirt());
	public static final Item LEAVES = registerItem(new BlockItemLeaves());
	public static final Item STONE = registerItem(new BlockItemStone());
	public static final Item WOODEN_PLANKS = registerItem(new BlockItemWoodenPlanks());
	public static final Item WOOD = registerItem(new BlockItemWood());
	public static final Item LOG = registerItem(new BlockItemLog());
	public static final Item GLASS = registerItem(new BlockItemGlass());
	public static final Item BRICKS = registerItem(new BlockItemBricks());
	public static final Item LIMESTONE = registerItem(new BlockItemLimestone());


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

	public static Item registerItem(Item item) {
		int itemId = nextAvailableItemIdForRegistration++;

		if (item instanceof BlockItem blockItem) {
			BLOCK_ITEM_MAP.put(blockItem.getBlock(), blockItem);
		}

		ITEM_HASHMAP.put(itemId, item);
		ITEM_SYNONYM_MAP.put(item.getIdSynonym(), itemId);

		return item;
	}

	public static BlockItem getBlockItem(byte blockId) {
		return BLOCK_ITEM_MAP.get(blockId);
	}

}
