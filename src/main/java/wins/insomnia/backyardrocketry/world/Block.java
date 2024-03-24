package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.world.blockproperty.BlockGrassProperties;

public class Block {
    public static final int AIR = 0;
    public static final int GRASS = 1;

    public static IBlockProperties  createBlockProperties(int block) {
        switch (block) {
            case GRASS -> {
                return new BlockGrassProperties();
            }
            default -> {
                return null;
            }
        }
    }

}
