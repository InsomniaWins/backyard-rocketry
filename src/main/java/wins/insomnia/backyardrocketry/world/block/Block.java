package wins.insomnia.backyardrocketry.world.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BlockBoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockPropertiesDirt;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockPropertiesGrass;

import java.io.IOException;
import java.security.KeyPair;
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


    private static final HashMap<Integer, HashMap<BlockDetail, Object>> BLOCK_DETAILS_MAP = new HashMap<>();


    public static final int WORLD_BORDER = registerBlock(
            254,
            "WORLD BORDER",
            true,
            true,
            null,
            "cobblestone",
            -1
    );
    public static final int NULL = registerBlock(
            255,
            "NULL",
            true,
            true,
            new BlockProperties(),
            "cobblestone",
            -1
    );



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
    public static final int COBBLESTONE = registerBlock(
            2,
            "Cobblestone",
            false,
            true,
            null,
            "cobblestone",
            120
    );
    public static final int DIRT = registerBlock(
            3,
            "Dirt",
            false,
            true,
            new BlockPropertiesDirt(),
            "dirt",
            30
    );
    public static final int STONE = registerBlock(
            4,
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
    static {
        for (Map.Entry<Integer, ?> entry: BLOCK_DETAILS_MAP.entrySet()) {
            System.out.println("Loaded block: " + entry.getKey() + " (" + ((HashMap) entry.getValue()).get(BlockDetail.NAME) + "): " + entry.getValue());
        }
    }


    public static int registerBlock(int blockId, String blockName, boolean isTransparent, boolean hideNeighboringFaces, BlockProperties blockProperties, String blockStateFileName, int blockHealth) {

        HashMap<BlockDetail, Object> detailsMap = new HashMap<>();



        detailsMap.put(BlockDetail.NAME, blockName);

        detailsMap.put(BlockDetail.TRANSPARENCY, isTransparent);

        detailsMap.put(BlockDetail.HIDE_NEIGHBORING_FACES, hideNeighboringFaces);

        if (blockProperties != null) {
            detailsMap.put(BlockDetail.PROPERTIES, blockProperties);
        }

        if (blockStateFileName != null) {
            detailsMap.put(BlockDetail.STATE, blockStateFileName);
        }

        detailsMap.put(BlockDetail.HEALTH, blockHealth);



        BLOCK_DETAILS_MAP.put(blockId, detailsMap);

        return blockId;
    }

    public static BlockProperties getBlockProperties(int block) {
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

    public static int getBlockHealth(int block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return -1;

        Integer result = (Integer) blockDetails.get(BlockDetail.HEALTH);

        if (result == null) {
            return -1;
        }

        return result;
    }

    public static Set<Integer> getBlocks() {
        return BLOCK_DETAILS_MAP.keySet();
    }

    public static String getBlockStateName(int block) {


        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return null;

        return (String) blockDetails.get(BlockDetail.STATE);

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

        return getBlockProperties(block);
    }

    public static String getBlockName(int block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return "UNKNOWN BLOCK";

        String result = (String) blockDetails.get(BlockDetail.NAME);

        if (result == null) {
            return "UNKNOWN BLOCK";
        }

        return result;
    }

    public static boolean isBlockTransparent(int block) {
        HashMap<BlockDetail, Object> blockDetails = BLOCK_DETAILS_MAP.get(block);

        if (blockDetails == null) return false;

        Boolean result = (Boolean) blockDetails.get(BlockDetail.TRANSPARENCY);

        if (result == null) {
            return false;
        }

        return result;
    }
}
