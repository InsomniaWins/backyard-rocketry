package wins.insomnia.backyardrocketry.world.block;

import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.block.types.*;
import wins.insomnia.backyardrocketry.world.World;

import java.util.HashMap;
import java.util.Set;

public class Blocks {

    private static final HashMap<Byte, Block> BLOCK_MAP = new HashMap<>();
    private static final HashMap<String, Byte> BLOCK_SYNONYM_MAP = new HashMap<>();
    private static byte nextAvailableBlockIdForRegistration = Byte.MIN_VALUE;


    public static final byte WORLD_BORDER = registerBlock(new BlockWorldBorder());
    public static final byte NULL = registerBlock(new BlockNull());
    public static final byte AIR = registerBlock(new BlockAir());
    public static final byte GRASS = registerBlock(new BlockGrass());
    public static final byte COBBLESTONE = registerBlock(new BlockCobblestone());
    public static final byte DIRT = registerBlock(new BlockDirt());
    public static final byte STONE = registerBlock(new BlockStone());
    public static final byte LOG = registerBlock(new BlockLog());
    public static final byte WOOD = registerBlock(new BlockWood());
    public static final byte LEAVES = registerBlock(new BlockLeaves());
    public static final byte WOODEN_PLANKS = registerBlock(new BlockWoodenPlanks());
    public static final byte GLASS = registerBlock(new BlockGlass());
    public static final byte BRICKS = registerBlock(new BlockBricks());
    public static final byte WATER = registerBlock(new BlockWater());


    public static byte getBlockIdFromSynonym(String blockSynonym) {
        return BLOCK_SYNONYM_MAP.get(blockSynonym);
    }

    public static byte registerBlock(Block block) {
        byte id = nextAvailableBlockIdForRegistration++;
        BLOCK_MAP.put(id, block);
        BLOCK_SYNONYM_MAP.put(block.getBlockStateName(), id);

        return id;
    }

    public static BlockAudio getBlockAudio(byte block) {

        if (BLOCK_MAP.get(block) == null) return null;

        return BLOCK_MAP.get(block).getBlockAudio();

    }


    public static boolean shouldHideNeighboringFaces(byte block) {

        if (BLOCK_MAP.get(block) == null) return true;

        return BLOCK_MAP.get(block).shouldHideNeighboringFaces();
    }

    public static int getBlockStrength(byte block) {

        if (BLOCK_MAP.get(block) == null) return -1;

        return BLOCK_MAP.get(block).getBlockStrength();
    }

    public static Block getBlock(byte block) {
        return BLOCK_MAP.get(block);
    }

    public static Set<Byte> getBlocks() {
        return BLOCK_MAP.keySet();
    }

    public static String getBlockStateName(byte block) {

        if (BLOCK_MAP.get(block) == null) return null;

        return BLOCK_MAP.get(block).getBlockStateName();

    }

    public static BoundingBox getBlockCollision(byte block) {
        if (BLOCK_MAP.get(block) == null) return null;

        return BLOCK_MAP.get(block).getBlockCollision();
    }

    public static String getBlockName(byte block) {
        if (BLOCK_MAP.get(block) == null) return "UNKNOWN BLOCK";

        return BLOCK_MAP.get(block).getName();
    }

    public static boolean isBlockTransparent(byte block) {

        if (BLOCK_MAP.get(block) == null) return false;

        return BLOCK_MAP.get(block).isTransparent();
    }

    public static byte getRandomBlock() {

        Object[] blocks = BLOCK_MAP.keySet().toArray();
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
