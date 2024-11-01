package wins.insomnia.backyardrocketry.item;

public class BlockItem extends Item {

	private final byte BLOCK_ID;


	public BlockItem(byte blockId, int itemId, String itemName, String itemSynonym, int volumePerItem, int maxItemVolume) {
		super(itemId, itemName, itemSynonym, volumePerItem, maxItemVolume);
		BLOCK_ID = blockId;
	}


	public byte getBlock() {
		return BLOCK_ID;
	}

}
