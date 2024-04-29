package wins.insomnia.backyardrocketry.world;

import org.joml.Math;
import org.joml.Vector2d;

import java.util.*;

public class World {

    public static final int CHUNK_AMOUNT_X = 5;
    public static final int CHUNK_AMOUNT_Y = 4;
    public static final int CHUNK_AMOUNT_Z = 5;

    private final Map<ChunkPosition, Chunk> CHUNKS;
    public static final Random RANDOM = new Random();

    private long seed;

    public World() {

        seed = RANDOM.nextLong();

        CHUNKS = new HashMap<>();

    }

    public void generate() {
        for (int y = 0; y < CHUNK_AMOUNT_Y; y++){
            for (int x = 0; x < CHUNK_AMOUNT_X; x++) {
                for (int z = 0; z < CHUNK_AMOUNT_Z; z++) {

                    int chunkPosX = x * Chunk.SIZE_X;
                    int chunkPosY = y * Chunk.SIZE_Y;
                    int chunkPosZ = z * Chunk.SIZE_Z;

                    ChunkPosition chunkPosition = new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);

                    Chunk chunk = new Chunk(
                            this,
                            chunkPosX,
                            chunkPosY,
                            chunkPosZ
                    );
                    CHUNKS.put(chunkPosition, chunk);
                }
            }
        }
    }

    public Chunk getChunkAt(int chunkX, int chunkY, int chunkZ) {
        return CHUNKS.get(new ChunkPosition(chunkX, chunkY, chunkZ));
        /*
        for (Chunk chunk : CHUNKS) {
            if (chunk.getX() == chunkX && chunk.getY() == chunkY && chunk.getZ() == chunkZ) {
                return chunk;
            }
        }
        return null;*/
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

    /**
     *
     * gets position of chunk containing block
     * If block is out of world bounds, returns chunk with coordinates clamped to world dimension.
     * (will never return null, and will always be a chunk in the world boundaries)
     *
     * @param blockX
     * @param blockY
     * @param blockZ
     *
     * @return ChunkPosition
     */
    public ChunkPosition getChunkPositionFromBlockPositionClamped(int blockX, int blockY, int blockZ) {

        int worldSizeX = getSizeX();
        int worldSizeY = getSizeY();
        int worldSizeZ = getSizeZ();

        blockX = Math.clamp(blockX, 0, worldSizeX-1);
        blockY = Math.clamp(blockY, 0, worldSizeY-1);
        blockZ = Math.clamp(blockZ, 0, worldSizeZ-1);

        int chunkPosX = Chunk.SIZE_X * (blockX / Chunk.SIZE_X);
        int chunkPosY = Chunk.SIZE_Y * (blockY / Chunk.SIZE_Y);
        int chunkPosZ = Chunk.SIZE_Z * (blockZ / Chunk.SIZE_Z);

        return new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);
    }

    /**
     *
     * gets position of chunk containing block
     * if block is out of world bounds, returns null
     *
     * @param blockX
     * @param blockY
     * @param blockZ
     *
     * @return ChunkPosition
     */
    public ChunkPosition getChunkPositionFromBlockPosition(int blockX, int blockY, int blockZ) {

        if (blockX < 0 || blockY < 0 || blockZ < 0) return null;
        if (blockX > getSizeX()-1 || blockY > getSizeY()-1 || blockZ > getSizeZ()-1) return null;

        int chunkPosX = Chunk.SIZE_X * (blockX / Chunk.SIZE_X);
        int chunkPosY = Chunk.SIZE_Y * (blockY / Chunk.SIZE_Y);
        int chunkPosZ = Chunk.SIZE_Z * (blockZ / Chunk.SIZE_Z);

        return new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);
    }

    public Chunk getChunkContainingBlock(int x, int y, int z) {

        if (x < 0 || y < 0 || z < 0) return null;

        int chunkPosX = Chunk.SIZE_X * (x / Chunk.SIZE_X);
        int chunkPosY = Chunk.SIZE_Y * (y / Chunk.SIZE_Y);
        int chunkPosZ = Chunk.SIZE_Z * (z / Chunk.SIZE_Z);

        return CHUNKS.get(new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ));

        /*
        for (Chunk chunk : CHUNKS) {
            if (chunk.getX() == chunkPosX && chunk.getY() == chunkPosY && chunk.getZ() == chunkPosZ) return chunk;
        }

        return null;*/
    }

    public Collection<Chunk> getChunks() {
        return CHUNKS.values();
    }
}
