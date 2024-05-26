package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BlockBoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBox;

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

    public static final int WORLD_BORDER = -2;
    public static final int NULL = -1;
    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int COBBLESTONE = 2;
    public static final int DIRT = 3;
    public static final int STONE = 4;

    public static BlockBoundingBox getBlockBoundingBox(Chunk chunk, Vector3i blockPosition, int block) {
        BoundingBox boundingBox = getBlockCollision(block);

        if (boundingBox == null) return null;

		return new BlockBoundingBox(boundingBox, chunk, blockPosition);
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
