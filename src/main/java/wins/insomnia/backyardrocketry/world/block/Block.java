package wins.insomnia.backyardrocketry.world.block;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BlockBoundingBox;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Block {

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
        HIDE_NEIGHBORING_FACES,
        AUDIO
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
            "grass",
            40,
            BlockAudio.GENERIC_DIRT
    );
    public static final byte COBBLESTONE = registerBlock(
            "Cobblestone",
            false,
            true,
            null,
            "cobblestone",
            120,
            BlockAudio.GENERIC_STONE
    );
    public static final byte DIRT = registerBlock(
            "Dirt",
            false,
            true,
            null,
            "dirt",
            30,
            BlockAudio.GENERIC_DIRT
    );
    public static final byte STONE = registerBlock(
            "Stone",
            false,
            true,
            null,
            "stone",
            120,
            BlockAudio.GENERIC_STONE
    );
    public static final byte LOG = registerBlock(
            "Log",
            false,
            true,
            null,
            "log",
            90,
            BlockAudio.GENERIC_WOOD
    );

    public static final byte WOOD = registerBlock(
            "Wood",
            false,
            true,
            null,
            "wood",
            90,
            BlockAudio.GENERIC_WOOD
    );

    public static final byte LEAVES = registerBlock(
            "Leaves",
            true,
            true,
            null,
            "leaves",
            20,
            BlockAudio.GENERIC_LEAVES
    );
    public static final byte WOODEN_PLANKS = registerBlock(
            "Wooden Planks",
            false,
            true,
            null,
            "wooden_planks",
            90,
            BlockAudio.GENERIC_WOOD
    );

    public static final byte GLASS = registerBlock(
            "Glass",
            true,
            true,
            null,
            "glass",
            20,
            BlockAudio.GENERIC_GLASS
    );

    public static final byte BRICKS = registerBlock(
            "Bricks",
            false,
            true,
            null,
            "bricks",
            120,
            BlockAudio.GENERIC_STONE
    );

    public static final byte WATER = registerBlock(
            "Water",
            true,
            true,
            null,
            "water",
            -1
    );



    static {
        for (Map.Entry<Byte, ?> entry: BLOCK_DETAILS_MAP.entrySet()) {
            System.out.println("Loaded block: " + entry.getKey() + " (" + ((HashMap<?, ?>) entry.getValue()).get(BlockDetail.NAME) + "): " + entry.getValue());
        }
    }


    public static byte getBlockIdFromSynonym(String blockSynonym) {
        return BLOCK_SYNONYM_MAP.get(blockSynonym);
    }

    public static byte registerBlock(
            String blockName,
            boolean isTransparent,
            boolean hideNeighboringFaces,
            BlockProperties blockProperties,
            String blockStateFileName,
            int blockHealth
    ) {
        return registerBlock(blockName, isTransparent, hideNeighboringFaces, blockProperties, blockStateFileName, blockHealth, null);
    }

    public static byte registerBlock(
            String blockName,
            boolean isTransparent,
            boolean hideNeighboringFaces,
            BlockProperties blockProperties,
            String blockStateFileName,
            int blockHealth,
            BlockAudio blockAudio
        ) {

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
        detailsMap.put(BlockDetail.AUDIO, blockAudio);


        BLOCK_DETAILS_MAP.put(blockId, detailsMap);

        return blockId;
    }

    public static BlockAudio getBlockAudio(byte block) {

        if (BLOCK_DETAILS_MAP.get(block) == null) return null;

        return (BlockAudio) BLOCK_DETAILS_MAP.get(block).get(BlockDetail.AUDIO);

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
        if (block == AIR || block == WATER) {
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

    public static byte getRandomBlock() {

        Object[] blocks = BLOCK_DETAILS_MAP.keySet().toArray();
        int index = World.RANDOM.nextInt(blocks.length);

        Byte block;

        try {
            block = (Byte) blocks[index];
        } catch (Exception e) {
            block = GRASS;
		}

		return block;

    }

    public static class Face {

        public static final Face NULL = null;
        public static final Face NEG_X = new Face(-1, 0, 0);
        public static final Face POS_X = new Face(1, 0, 0);
        public static final Face NEG_Y = new Face(0, -1, 0);
        public static final Face POS_Y = new Face(0, 1, 0);
        public static final Face NEG_Z = new Face(0, 0, -1);
        public static final Face POS_Z = new Face(0, 0, 1);

        private final int X;
        private final int Y;
        private final int Z;

        public Face(int x, int y, int z) {

            X = x;
            Y = y;
            Z = z;

        }

        public int getX() {
            return X;
        }

        public int getY() {
            return Y;
        }

        public int getZ() {
            return Z;
        }

    }

}
