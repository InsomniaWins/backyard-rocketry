package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.blockproperty.BlockGrassProperties;

public class Block {

    public enum Face {
        NULL,
        NEG_X,
        NEG_Y,
        NEG_Z,
        POS_X,
        POS_Y,
        POS_Z
    }

    public static BoundingBox DEFAULT_BLOCK_BOUNDING_BOX = new BoundingBox(
            0,0,0,
            1,1,1
    );

    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int COBBLESTONE = 2;
    public static final int DIRT = 3;
    public static final int STONE = 4;

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

    public static BoundingBox getBlockCollision(int block) {

        switch (block) {
            case AIR -> {
                return null;
            }
            default -> {
                return new BoundingBox(DEFAULT_BLOCK_BOUNDING_BOX);
            }
        }

    }

}
