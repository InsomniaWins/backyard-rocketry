package wins.insomnia.backyardrocketry.item;

public class BlockItem extends Item {

	private final byte BLOCK_ID;

	public BlockItem(byte blockId, String itemName, String itemSynonym) {
		super(itemName, itemSynonym);
		BLOCK_ID = blockId;
	}

	public BlockItem(byte blockId, String itemName, String itemSynonym, int maxStackSize) {
		super(itemName, itemSynonym, maxStackSize);
		BLOCK_ID = blockId;
	}


	public byte getBlock() {
		return BLOCK_ID;
	}

}
