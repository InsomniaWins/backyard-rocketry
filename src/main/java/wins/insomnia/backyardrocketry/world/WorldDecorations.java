package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.block.Block;

public class WorldDecorations {

	public enum PlacementBehavior {

		ONLY_REPLACE_AIR,
		REPLACE_ANY_BLOCK,
		REPLACE_ANY_BREAKABLE_BLOCK

	}

	public static final PlacementBehavior[] PLACEMENT_BEHAVIOR_ENUM_VALUES = WorldDecorations.PlacementBehavior.values();

	public static final Decoration TREE = new Decoration(
			new BoundingBox(-1, 0, -1,   1, 8, 1),
			new int[][] {
					{0,0,0, Block.LOG, PlacementBehavior.REPLACE_ANY_BREAKABLE_BLOCK.ordinal()},


					{0,1,0, Block.LOG, PlacementBehavior.REPLACE_ANY_BREAKABLE_BLOCK.ordinal()},


					{0,2,0, Block.LOG, PlacementBehavior.REPLACE_ANY_BREAKABLE_BLOCK.ordinal()},


					{0,3,0, Block.LOG, PlacementBehavior.REPLACE_ANY_BREAKABLE_BLOCK.ordinal()},
					{1,3,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,3,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,3,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,3,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,3,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,3,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,3,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,3,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},


					{0,4,0, Block.LOG, PlacementBehavior.REPLACE_ANY_BREAKABLE_BLOCK.ordinal()},
					{1,4,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,4,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,4,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,4,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,4,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,4,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,4,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,4,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},

					{0,5,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,5,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,5,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,5,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,5,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{1,5,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{-1,5,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,5,1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},
					{0,5,-1, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},

					{0,6,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},


					{0,7,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()},


					{0,8,0, Block.LEAVES, PlacementBehavior.ONLY_REPLACE_AIR.ordinal()}
			}
	);


}
