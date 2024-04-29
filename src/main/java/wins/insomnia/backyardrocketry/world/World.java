package wins.insomnia.backyardrocketry.world;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class World {

    public static final int CHUNK_AMOUNT_X = 5;
    public static final int CHUNK_AMOUNT_Y = 4;
    public static final int CHUNK_AMOUNT_Z = 5;

    private final ArrayList<Chunk> CHUNKS;
    public static final Random RANDOM = new Random();

    private long seed;

    public World() {

        seed = RANDOM.nextLong();

        CHUNKS = new ArrayList<>();


    }

    public void generate() {
        for (int y = 0; y < CHUNK_AMOUNT_Y; y++){
            for (int x = 0; x < CHUNK_AMOUNT_X; x++) {
                for (int z = 0; z < CHUNK_AMOUNT_Z; z++) {

                    CHUNKS.add(new Chunk(
                            this,
                            x * Chunk.SIZE_X,
                            y * Chunk.SIZE_Y,
                            z * Chunk.SIZE_Z
                    ));

                }
            }
        }
    }

    public Chunk getChunkAt(int chunkX, int chunkY, int chunkZ) {
        for (Chunk chunk : CHUNKS) {
            if (chunk.getX() == chunkX && chunk.getY() == chunkY && chunk.getZ() == chunkZ) {
                return chunk;
            }
        }
        return null;
    }

    public int getBlock(int x, int y, int z) {

        if (x > getSizeX()-1 || x < 0 || y > getSizeX()-1 || y < 0 || z > getSizeX()-1 || z < 0 ) {
            return Block.WORLD_BORDER;
        }

        Chunk chunk = getChunkContainingBlock(x, y, z);

        if (chunk == null) return Block.NULL;

        return chunk.getBlock(chunk.toLocalX(x), chunk.toLocalY(y), chunk.toLocalZ(z));

    }

    public double[] getCenterXZ() {

        return new double[]{
                getSizeX() * 0.5,
                getSizeZ() * 0.5
        };

    }

    public long getSeed() {
        return seed;
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

        if (x < 0 || y < 0 || z < 0) return null;

        int chunkPosX = Chunk.SIZE_X * (x / Chunk.SIZE_X);
        int chunkPosY = Chunk.SIZE_Y * (y / Chunk.SIZE_Y);
        int chunkPosZ = Chunk.SIZE_Z * (z / Chunk.SIZE_Z);

        for (Chunk chunk : CHUNKS) {
            if (chunk.getX() == chunkPosX && chunk.getY() == chunkPosY && chunk.getZ() == chunkPosZ) return chunk;
        }

        return null;
    }

    public List<Chunk> getChunks() {
        return CHUNKS;
    }
}
