package wins.insomnia.backyardrocketry.world.block;

public class BlockName {

	public static String get(int block) {
		switch (block) {

			case Block.AIR -> {
				return "Air";
			}

			case Block.GRASS -> {
				return "Grass";
			}

			case Block.DIRT -> {
				return "Dirt";
			}

			case Block.STONE -> {
				return "Stone";
			}

			case Block.COBBLESTONE -> {
				return "Cobblestone";
			}

			case Block.WORLD_BORDER -> {
				return "";
			}

			default -> {
				return "UNKNOWN BLOCK";
			}
		}
	}

}
