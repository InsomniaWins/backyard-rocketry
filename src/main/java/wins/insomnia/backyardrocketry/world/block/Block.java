package wins.insomnia.backyardrocketry.world.block;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BlockBoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockPropertiesDirt;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockPropertiesGrass;

import java.util.HashMap;
import java.util.Map;
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


    private enum BlockDetail {
        NAME,
        TRANSPARENCY,
        PROPERTIES,
        STATE,
        HEALTH,
        HIDE_NEIGHBORING_FACES
    }


    private static final HashMap<Byte, HashMap<BlockDetail, Object>> BLOCK_DETAILS_MAP = new HashMap<>();
    private static final HashMap<String, Byte> BLOCK_SYNONYM_MAP = new HashMap<>();
    private static byte nextAvailableBlockIdForRegistration = Byte.MIN_VALUE;
    public static final byte WORLD_BORDER = registerBlock(
            "WORLD BORDER",
            true,
            true,
            null,
            null,
            -1
    );
    public static final byte NULL = registerBlock(
            "NULL",
            true,
            true,
            null,
            null,
            -1
    );
    public static final byte AIR = registerBlock(
            "Air",
            true,
            false,
            null,
            null,
            -1
    );
    public static final byte GRASS = registerBlock(
            "Grass",
            false,
            true,
            null,
            "grass_block",
            40
    );
    public static final byte COBBLESTONE = registerBlock(
            "Cobblestone",
            false,
            true,
            null,
            "cobblestone",
            120
    );
    public static final byte DIRT = registerBlock(
            "Dirt",
            false,
            true,
            null,
            "dirt",
            30
    );
    public static final byte STONE = registerBlock(
            "Stone",
            false,
            true,
            null,
            "stone",
            120
    );
    public static final byte LOG = registerBlock(
            "Log",
            false,
            true,
            null,
            "log",
            90
    );

    public static final byte WOOD = registerBlock(
            "Wood",
            false,
            true,
            null,
            "wood",
            90
    );

    public static final byte LEAVES = registerBlock(
            "Leaves",
            true,
            true,
            null,
            "leaves",
            20
    );
    public static final byte WOODEN_PLANKS = registerBlock(
            "Wooden Planks",
            false,
            true,
            null,
            "wooden_planks",
            90
    );

    public static final byte GLASS = registerBlock(
            "Glass",
            true,
            true,
            null,
            "glass",
            20
    );

    public static final byte BRICKS = registerBlock(
            "Bricks",
            false,
            true,
            null,
            "bricks",
            120
    );



    static {
        for (Map.Entry<Byte, ?> entry: BLOCK_DETAILS_MAP.entrySet()) {
            System.out.println("Loaded block: " + entry.getKey() + " (" + ((HashMap<?, ?>) entry.getValue()).get(BlockDetail.NAME) + "): " + entry.getValue());
        }
    }


    public static byte getBlockIdFromSynonym(String blockSynonym) {
        return BLOCK_SYNONYM_MAP.get(blockSynonym);
    }

    public static byte registerBlock(String blockName, boolean isTransparent, boolean hideNeighboringFaces, BlockProperties blockProperties, String blockStateFileName, int blockHealth) {

        byte blockId = nextAvailableBlockIdForRegistration++;

        HashMap<BlockDetail, Object> detailsMap = new HashMap<>();



        detailsMap.put(BlockDetail.NAME, blockName);

        detailsMap.put(BlockDetail.TRANSPARENCY, isTransparent);

        detailsMap.put(BlockDetail.HIDE_NEIGHBORING_FACES, hideNeighboringFaces);

        if (blockProperties != null) {
            detailsMap.put(BlockDetail.PROPERTIES, blockProperties);
        }

        if (blockStateFileName != null) {
            detailsMap.put(BlockDetail.STATE, blockStateFileName);
            BLOCK_SYNONYM_MAP.put(blockStateFileName, blockId);
        }

        detailsMap.put(BlockDetail.HEALTH, blockHealth);



        BLOCK_DETAILS_MAP.put(blockId, detailsMap);

        return blockId;
    }

    public static BlockProperties getBlockProperties(byte block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) blockDetails = BLOCK_DETAILS_MAP.get(NULL);

        BlockProperties result = (BlockProperties) blockDetails.get(BlockDetail.PROPERTIES);

        if (result == null) {
            return (BlockProperties) BLOCK_DETAILS_MAP.get(NULL).get(BlockDetail.PROPERTIES);
        }

        return result;
    }

    public static boolean shouldHideNeighboringFaces(int block) {

        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return true;

        Boolean result = (Boolean) blockDetails.get(BlockDetail.HIDE_NEIGHBORING_FACES);

        if (result == null) {
            return true;
        }

        return result;
    }

    public static int getBlockHealth(byte block) {

        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return -1;

        Integer result = (Integer) blockDetails.get(BlockDetail.HEALTH);

        if (result == null) {
            return -1;
        }

        return result;
    }

    public static Set<Byte> getBlocks() {
        return BLOCK_DETAILS_MAP.keySet();
    }

    public static String getBlockStateName(byte block) {


        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return null;

        return (String) blockDetails.get(BlockDetail.STATE);

    }

    public static BlockBoundingBox getBlockBoundingBox(Chunk chunk, Vector3i blockPosition, byte block) {
        BoundingBox boundingBox = getBlockCollision(block);

        if (boundingBox == null) return null;

        return new BlockBoundingBox(boundingBox, chunk, blockPosition);
    }

    public static BoundingBox getBlockCollision(byte block) {
        if (block == AIR) {
            return null;
        } else {
            return new BoundingBox(DEFAULT_BLOCK_BOUNDING_BOX);
        }

    }

    public static String getBlockName(byte block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return "UNKNOWN BLOCK";

        String result = (String) blockDetails.get(BlockDetail.NAME);

        if (result == null) {
            return "UNKNOWN BLOCK";
        }

        return result;
    }

    public static boolean isBlockTransparent(byte block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return false;

        Boolean result = (Boolean) blockDetails.get(BlockDetail.TRANSPARENCY);

        if (result == null) {
            return false;
        }

        return result;
    }
}
