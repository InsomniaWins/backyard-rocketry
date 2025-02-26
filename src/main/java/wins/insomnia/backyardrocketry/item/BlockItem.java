package wins.insomnia.backyardrocketry.item;

public class BlockItem extends Item {

	private final byte BLOCK_ID;


	public BlockItem(byte blockId, String itemName, String itemSynonym, int volumePerItem, int maxItemVolume, double kilosPerLiter) {
		super(itemName, itemSynonym, volumePerItem, maxItemVolume, kilosPerLiter);
		BLOCK_ID = blockId;
	}


	public byte getBlock() {
		return BLOCK_ID;
	}

}
