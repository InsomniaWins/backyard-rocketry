package wins.insomnia.backyardrocketry.world.block.types;

import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateLog;

public class BlockLog extends Block {
	public BlockLog() {
		super("Log", "log", BlockStateLog.class, false, true, 90, BlockAudio.GENERIC_WOOD);
	}

	@Override
	public byte onPlace(int localBlockX, int localBlockY, int localBlockZ, Blocks.Face face) {
		return BlockStateManager.getBlockStateIndex(Blocks.LOG, BlockStateLog.getFromFace(face));
	}
}
