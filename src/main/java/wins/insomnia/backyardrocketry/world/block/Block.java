package wins.insomnia.backyardrocketry.world.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BlockBoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockPropertiesGrass;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

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


    private static final HashMap<Integer, String> BLOCK_NAME_MAP = new HashMap<>();
    private static final HashMap<Integer, Boolean> BLOCK_TRANSPARENCY_MAP = new HashMap<>();
    private static final HashMap<Integer, BlockProperties> BLOCK_PROPERTIES_MAP = new HashMap<>();
    static {
        BLOCK_PROPERTIES_MAP.put(-1, new BlockProperties());

    }
    private static final HashMap<Integer, String> BLOCK_STATE_NAME_MAP = new HashMap<>();
    private static final HashMap<Integer, Integer> BLOCK_HEALTH_MAP = new HashMap<>();
    private static final HashMap<Integer, Boolean> HIDE_NEIGHBORING_FACES_MAP = new HashMap<>();

    public static final int WORLD_BORDER = -2;
    public static final int NULL = -1;



    public static final int AIR = registerBlock(
            0,
            "Air",
            true,
            false,
            null,
            null,
            -1
    );
    public static final int GRASS = registerBlock(
            1,
            "Grass",
            false,
            true,
            new BlockPropertiesGrass(),
            "grass_block",
            40
    );
    public static final int COBBLESTONE = registerBlock(2,
            "Cobblestone",
            false,
            true,
            null,
            "cobblestone",
            120
    );
    public static final int DIRT = registerBlock(3,
            "Dirt",
            false,
            true,
            null,
            "dirt",
            30
    );
    public static final int STONE = registerBlock(4,
            "Stone",
            false,
            true,
            null,
            "stone",
            120
    );
    public static final int LOG = registerBlock(
            5,
            "Log",
            false,
            true,
            null,
            "log",
            90
    );
    public static final int LEAVES = registerBlock(
            6,
            "Leaves",
            true,
            true,
            null,
            "leaves",
            20
    );
    public static final int WOODEN_PLANKS = registerBlock(
            7,
            "Wooden Planks",
            false,
            true,
            null,
            "wooden_planks",
            90
    );

    public static final int GLASS = registerBlock(
            8,
            "Glass",
            true,
            true,
            null,
            "glass",
            20
    );



    public static int registerBlock(int blockId, String blockName, boolean isTransparent, boolean hideNeighboringFaces, BlockProperties blockProperties, String blockStateFileName, int blockHealth) {

		BLOCK_NAME_MAP.put(blockId, blockName);
        BLOCK_TRANSPARENCY_MAP.put(blockId, isTransparent);

        if (blockProperties != null) {
            BLOCK_PROPERTIES_MAP.put(blockId, blockProperties);
        }

        if (blockStateFileName != null) {
            BLOCK_STATE_NAME_MAP.put(blockId, blockStateFileName);
        }

        BLOCK_HEALTH_MAP.put(blockId, blockHealth);
        HIDE_NEIGHBORING_FACES_MAP.put(blockId, hideNeighboringFaces);

		return blockId;
    }

    public static boolean shouldHideNeighboringFaces(int block) {

        Boolean result = HIDE_NEIGHBORING_FACES_MAP.get(block);

        if (result == null) {
            return true;
        }

        return result;
    }

    public static int getBlockHealth(int block) {
        Integer blockHealth = BLOCK_HEALTH_MAP.get(block);

        if (blockHealth == null) {
            return -1;
        }

        return blockHealth;
    }

    public static Set<Integer> getBlocks() {
        return BLOCK_NAME_MAP.keySet();
    }

    public static String getBlockStateName(int block) {
        return BLOCK_STATE_NAME_MAP.get(block);
    }

    public static BlockBoundingBox getBlockBoundingBox(Chunk chunk, Vector3i blockPosition, int block) {
        BoundingBox boundingBox = getBlockCollision(block);

        if (boundingBox == null) return null;

		return new BlockBoundingBox(boundingBox, chunk, blockPosition);
    }

    public static BoundingBox getBlockCollision(int block) {
        if (block == AIR) {
            return null;
        } else {
            return new BoundingBox(DEFAULT_BLOCK_BOUNDING_BOX);
        }

    }

    public static BlockProperties getBlockPropertiesFromBlockState(int blockState) {

        int block = BitHelper.getBlockIdFromBlockState(blockState);

        BlockProperties blockProperties = BLOCK_PROPERTIES_MAP.get(block);

        if (blockProperties == null) blockProperties = BLOCK_PROPERTIES_MAP.get(NULL);

        return blockProperties;
    }

    public static String getBlockName(int block) {
        return BLOCK_NAME_MAP.get(block);
    }

    public static boolean isBlockTransparent(int block) {
        return BLOCK_TRANSPARENCY_MAP.get(block);
    }
}
