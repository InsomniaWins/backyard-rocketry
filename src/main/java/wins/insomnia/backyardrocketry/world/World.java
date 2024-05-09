package wins.insomnia.backyardrocketry.world;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.util.IPlayer;

import java.util.*;

public class World {

    public static final int CHUNK_AMOUNT_X = 15;
    public static final int CHUNK_AMOUNT_Y = 6;
    public static final int CHUNK_AMOUNT_Z = 15;

    private static World instance;

    private final Map<ChunkPosition, Chunk> CHUNKS;
    public static final Random RANDOM = new Random();

    private long seed;

    public World() {

        seed = RANDOM.nextLong();
        CHUNKS = new HashMap<>();
        instance = this;
    }



    public Chunk getChunk(ChunkPosition chunkPosition) {

        // if chunk is loaded, return loaded chunk
        Chunk returnChunk = CHUNKS.get(chunkPosition);
        if (returnChunk != null) return returnChunk;


        // load and return chunk otherwise
        returnChunk = loadChunk(chunkPosition);
        return returnChunk;
    }

    private Chunk loadChunk(ChunkPosition chunkPosition) {

        Chunk chunk;

        // if chunk already generated, load
        // TODO: replace with actual chunk loading

        // otherwise, generate
        chunk = generateChunk(chunkPosition);


        CHUNKS.put(chunkPosition, chunk);


        return chunk;
    }

    private Chunk generateChunk(ChunkPosition chunkPosition) {

        return new Chunk(
                this,
                chunkPosition.getX(),
                chunkPosition.getY(),
                chunkPosition.getZ()
        );
    }

    public void updateChunksAroundPlayer(IPlayer player) {

        List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player);

        for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

            if (!CHUNKS.containsKey(chunkPosition)) {
                loadChunk(chunkPosition);
            }

        }

    }

    public List<ChunkPosition> getChunkPositionsBlockPosition(int x, int y, int z, int chunkRadius) {

        List<ChunkPosition> chunkPositions = new ArrayList<>();
        ChunkPosition originChunkPosition = getChunkPositionFromBlockPositionClamped(x, y, z);

        for (int xIterator = -chunkRadius; xIterator < chunkRadius; xIterator++) {
            for (int yIterator = -chunkRadius; yIterator < chunkRadius; yIterator++) {
                for (int zIterator = -chunkRadius; zIterator < chunkRadius; zIterator++) {

                    int chunkX = xIterator * 16 + originChunkPosition.getX();
                    if (chunkX < 0 || chunkX >= getSizeX()) continue;


                    int chunkY = yIterator * 16 + originChunkPosition.getY();
                    if (chunkY < 0 || chunkY >= getSizeY()) continue;


                    int chunkZ = zIterator * 16 + originChunkPosition.getZ();
                    if (chunkZ < 0 || chunkZ >= getSizeZ()) continue;

                    chunkPositions.add(new ChunkPosition(chunkX, chunkY, chunkZ));

                }
            }
        }


        return chunkPositions;

    }

    public List<ChunkPosition> getChunkPositionsAroundPlayer(IPlayer player) {

        Vector3i playerBlockPos = player.getBlockPosition();

        return getChunkPositionsBlockPosition(playerBlockPos.x, playerBlockPos.y, playerBlockPos.z, 4);
    }


    @Deprecated
    public void generate() {
        for (int y = 0; y < CHUNK_AMOUNT_Y; y++){
            for (int x = 0; x < CHUNK_AMOUNT_X; x++) {
                for (int z = 0; z < CHUNK_AMOUNT_Z; z++) {

                    int chunkPosX = x * Chunk.SIZE_X;
                    int chunkPosY = y * Chunk.SIZE_Y;
                    int chunkPosZ = z * Chunk.SIZE_Z;

                    ChunkPosition chunkPosition = new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ, false);

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

    public Chunk getChunkAt(ChunkPosition chunkPosition) {
        return CHUNKS.get(chunkPosition);
    }

    public Chunk getChunkAt(int chunkX, int chunkY, int chunkZ) {
        return CHUNKS.get(new ChunkPosition(chunkX, chunkY, chunkZ));
    }

    public int getBlock(int x, int y, int z) {

        // if out of world border
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

    public static World get() {
        return instance;
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
    }

    public Collection<Chunk> getChunks() {
        return CHUNKS.values();
    }
}
