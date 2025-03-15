package wins.insomnia.backyardrocketry.world.block;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyFaceDirection;

public class Block {

	public static final BoundingBox DEFAULT_BLOCK_BOUNDING_BOX = new BoundingBox(
			0,0,0,
			1,1,1
	);

	private final String NAME;
	private final String BLOCK_STATE_FILE_NAME;
	private final Class<? extends BlockState> BLOCK_STATE;
	private final boolean TRANSPARENT;
	private final boolean HIDE_NEIGHBORING_FACES;
	private final int BLOCK_STRENGTH;
	private final BlockAudio BLOCK_AUDIO;

	public Block(String name, String blockStateFileName, Class<? extends BlockState> blockState, boolean transparent, boolean hideNeighboringFaces, int blockStrength) {
		this(name, blockStateFileName, blockState, transparent, hideNeighboringFaces, blockStrength, null);
	}

	public Block(String name, String blockStateFileName, Class<? extends BlockState> blockState, boolean transparent, boolean hideNeighboringFaces, int blockStrength, BlockAudio blockAudio) {
		NAME = name;
		BLOCK_STATE_FILE_NAME = blockStateFileName;
		BLOCK_STATE = blockState;
		TRANSPARENT = transparent;
		HIDE_NEIGHBORING_FACES = hideNeighboringFaces;
		BLOCK_STRENGTH = blockStrength;
		BLOCK_AUDIO = blockAudio;
	}

	public Class<? extends BlockState> getBlockState() {
		return BLOCK_STATE;
	}

	public String getName() {
		return NAME;
	}

	public String getBlockStateName() {
		return BLOCK_STATE_FILE_NAME;
	}

	public boolean isTransparent() {
		return TRANSPARENT;
	}

	public boolean shouldHideNeighboringFaces() {
		return HIDE_NEIGHBORING_FACES;
	}

	public int getBlockStrength() {
		return BLOCK_STRENGTH;
	}

	public BlockAudio getBlockAudio() {
		return BLOCK_AUDIO;
	}

	public BoundingBox getBlockCollision() {
		return new BoundingBox(DEFAULT_BLOCK_BOUNDING_BOX);
	}

	// returns the block-state index to place into the chunk (0 is default block-state)
	public byte onPlace(int localBlockX, int localBlockY, int localBlockZ, Blocks.Face face) {
		return 0;
	}

	public short getMinimumLightLevel(byte blockState, int localBlockX, int localBlockY, int localBlockZ) {
		// when overriding: CAST TO SHORT! (simulates unsigned short because java is dumb)
		// 0bRRRR_GGGG_BBBB_SSSS <- red, green, blue, sky (prob not a good idea to change sky from 0000)
		return 0b0000_0000_0000_0000;
	}

}
