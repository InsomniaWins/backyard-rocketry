package wins.insomnia.backyardrocketry.world;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.util.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.IPlayer;
import wins.insomnia.backyardrocketry.util.IUpdateListener;
import wins.insomnia.backyardrocketry.util.Updater;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World implements IFixedUpdateListener, IUpdateListener {

    public static final int CHUNK_AMOUNT_X = 128;
    public static final int CHUNK_AMOUNT_Y = 6;
    public static final int CHUNK_AMOUNT_Z = 128;
    private static World instance;

    public static int chunkLoadDistance = 5;

    private final Map<ChunkPosition, Chunk> CHUNKS;
    private final Queue<ChunkPosition> UNLOAD_CHUNK_QUEUE;
    private final Queue<ChunkPosition> LOAD_CHUNK_QUEUE;
    public static final Random RANDOM = new Random();


    private final ExecutorService chunkManagerExecutorService = Executors.newFixedThreadPool(10);

    private long seed;

    public World() {

        seed = RANDOM.nextLong();
        CHUNKS = new HashMap<>();
        LOAD_CHUNK_QUEUE = new LinkedList<>();
        UNLOAD_CHUNK_QUEUE = new LinkedList<>();
        instance = this;

        Updater.get().registerFixedUpdateListener(this);
        Updater.get().registerUpdateListener(this);
    }

    public static Vector3i getBlockPositionFromPosition(Vector3d position) {
        return new Vector3i(
                (int) position.x,
                (int) position.y,
                (int) position.z
        );
    }

    private void loadChunk(ChunkPosition chunkPosition) {

        if (CHUNKS.containsKey(chunkPosition)) {
            return;
        }

        if (LOAD_CHUNK_QUEUE.contains(chunkPosition)) {
            return;
        }

        LOAD_CHUNK_QUEUE.add(chunkPosition);


        chunkManagerExecutorService.submit(() -> {

            // otherwise, generate
            Chunk chunk = generateChunk(chunkPosition);

            synchronized (this) {
                CHUNKS.put(chunkPosition, chunk);
                LOAD_CHUNK_QUEUE.remove(chunkPosition);
            }

        });
    }

    // happens on chunk generation thread -- NOT MAIN THREAD!
    private Chunk generateChunk(ChunkPosition chunkPosition) {
        return new Chunk(
                this,
                chunkPosition.getX(),
                chunkPosition.getY(),
                chunkPosition.getZ()
        );
    }

    public void queueUnloadChunk(ChunkPosition chunkPosition) {

        UNLOAD_CHUNK_QUEUE.offer(chunkPosition);

    }

    private void unloadChunk(ChunkPosition chunkPosition) {

        synchronized (this) {
            Chunk chunk = CHUNKS.get(chunkPosition);
            CHUNKS.remove(chunkPosition);
            chunk.clean();
        }

    }

    public void updateChunksAroundPlayer(IPlayer player) {

        List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player);

        synchronized (this) {
            for (Map.Entry<ChunkPosition, Chunk> entry : CHUNKS.entrySet()) {
                if (chunkPositionsAroundPlayer.contains(entry.getKey())) {
                    // remove from positions around player: loading is not needed
                    chunkPositionsAroundPlayer.remove(entry.getKey());
                } else {
                    // unload chunk
                    queueUnloadChunk(entry.getKey());
                }
            }

            // load chunks
            for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {
                if (!CHUNKS.containsKey(chunkPosition)) {
                    loadChunk(chunkPosition);
                }
            }

            // unload chunks
            while (!UNLOAD_CHUNK_QUEUE.isEmpty()) {
                ChunkPosition chunkPosition = UNLOAD_CHUNK_QUEUE.poll();
                unloadChunk(chunkPosition);
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

        return getChunkPositionsBlockPosition(playerBlockPos.x, playerBlockPos.y, playerBlockPos.z, chunkLoadDistance);
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


    public int getBlockState(Vector3i blockPos) {
        return getBlockState(blockPos.x, blockPos.y, blockPos.z);
    }

    public int getBlockState(int x, int y, int z) {

        Chunk chunk = getChunkContainingBlock(x, y, z);

        if (chunk == null) {
            throw new RuntimeException("could not find chunk containing block at " + x + ", " + y + ", " + z);
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

    public Chunk getChunkContainingBlock(Vector3i blockPos) {
        return getChunkContainingBlock(blockPos.x, blockPos.y, blockPos.z);
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

    @Override
    public void fixedUpdate() {


    }

    @Override
    public void update(double deltaTime) {


    }

    // called at unload of world
    public void shutdown() {
        chunkManagerExecutorService.shutdown();
        Chunk.chunkMeshGenerationExecutorService.shutdown();
    }
}
