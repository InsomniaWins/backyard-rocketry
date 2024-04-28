package wins.insomnia.backyardrocketry.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class World {

    public static final int CHUNK_AMOUNT_X = 5;
    public static final int CHUNK_AMOUNT_Y = 5;
    public static final int CHUNK_AMOUNT_Z = 5;

    private final ArrayList<Chunk> CHUNKS;
    public static final Random RANDOM = new Random();

    public World() {

        CHUNKS = new ArrayList<>();

        CHUNKS.add(new Chunk(0,0,0));
        CHUNKS.add(new Chunk(0,16,0));

    }

    public int getSizeX() {
        return Chunk.SIZE_X * CHUNK_AMOUNT_X;
    }

    public int getSizeY() {
        return Chunk.SIZE_Y * CHUNK_AMOUNT_Y;
    }

    public int getSizeZ() {
        return Chunk.SIZE_Z * CHUNK_AMOUNT_Z;
    }

    public BlockState getBlockState(int x, int y, int z) {

        Chunk chunk = getChunkContainingBlock(x, y, z);
        if (chunk == null) {
            return null;
        }

        return chunk.getBlockState(chunk.toLocalX(x), chunk.toLocalY(y), chunk.toLocalZ(z));

    }

    public Chunk getChunkContainingBlock(int x, int y, int z) {

        int chunkPosX = 16 * (x / 16);
        int chunkPosY = 16 * (y / 16);
        int chunkPosZ = 16 * (z / 16);

        for (Chunk chunk : CHUNKS) {
            if (chunk.getX() == chunkPosX && chunk.getY() == chunkPosY && chunk.getZ() == chunkPosZ) return chunk;
        }

        return null;
    }

    public List<Chunk> getChunks() {
        return CHUNKS;
    }
}
