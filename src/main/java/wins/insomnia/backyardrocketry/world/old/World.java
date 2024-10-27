package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import org.joml.Math;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.render.TextRenderer;
import wins.insomnia.backyardrocketry.render.TextureManager;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.util.*;
import java.util.Random;
import java.util.concurrent.*;

public class World implements IFixedUpdateListener, IUpdateListener {

    private enum ChunkManagementType {
        LOAD,
        UNLOAD
    }

    public static final int CHUNK_AMOUNT_X = 45;
    public static final int CHUNK_AMOUNT_Y = 15;
    public static final int CHUNK_AMOUNT_Z = 45;
    private static World instance;

    public static int chunkLoadDistance = 3;
    public static int chunkProcessDistance = 100;//in block units NOT chunk positional units

    private final ConcurrentHashMap<ChunkPosition, Chunk> CHUNKS;
    public static final Random RANDOM = new Random();
    private final ExecutorService CHUNK_MANAGEMENT_EXECUTOR_SERVICE;
    public final ArrayList<ChunkPosition> CHUNKS_CURRENTLY_LOADING;
    private final Queue<ChunkManagementData> CHUNK_MANAGEMENT_QUEUE;

    private long seed;

    public World() {

        seed = RANDOM.nextLong();
        CHUNKS = new ConcurrentHashMap<>();
        CHUNK_MANAGEMENT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
        CHUNK_MANAGEMENT_QUEUE = new LinkedList<>();
        CHUNKS_CURRENTLY_LOADING = new ArrayList<>();
        instance = this;

        Updater.get().registerFixedUpdateListener(this);
        Updater.get().registerUpdateListener(this);
    }

    public boolean isChunkPositionInWorldBorder(ChunkPosition chunkPosition) {
        return isBlockInWorldBorder(
                chunkPosition.getX() * Chunk.SIZE_X,
                chunkPosition.getY() * Chunk.SIZE_Y,
                chunkPosition.getZ() * Chunk.SIZE_Z

                );
    }

    public boolean isBlockInWorldBorder(int x, int y, int z) {

        if (x < 0) return false;
        if (x > World.get().getSizeX()-1) return false;

        if (y < 0) return false;
        if (y > World.get().getSizeY()-1) return false;

        if (z < 0) return false;
        if (z > World.get().getSizeZ()-1) return false;

        return true;
    }

    public boolean isPlayerInUnloadedChunk(TestPlayer player) {

        List<Chunk> chunksTouchingPlayer = Collision.getChunksTouchingBoundingBox(player.getBoundingBox(), true);

		return chunksTouchingPlayer.contains(null);
	}

    public List<ChunkPosition> getChunkPositionsAroundBlockPosition(int x, int y, int z, int chunkRadius) {

        List<ChunkPosition> chunkPositions = new ArrayList<>();
        ChunkPosition originChunkPosition = getChunkPositionFromBlockPositionClamped(x, y, z);

        for (int xIterator = -chunkRadius; xIterator <= chunkRadius; xIterator++) {
            for (int yIterator = -chunkRadius; yIterator <= chunkRadius; yIterator++) {
                for (int zIterator = -chunkRadius; zIterator <= chunkRadius; zIterator++) {

                    int chunkX = xIterator + originChunkPosition.getX();
                    if (chunkX < 0 || chunkX >= CHUNK_AMOUNT_X) continue;


                    int chunkY = yIterator + originChunkPosition.getY();
                    if (chunkY < 0 || chunkY >= CHUNK_AMOUNT_Y) continue;


                    int chunkZ = zIterator + originChunkPosition.getZ();
                    if (chunkZ < 0 || chunkZ >= CHUNK_AMOUNT_Z) continue;

                    ChunkPosition chunkPosition = new ChunkPosition(chunkX, chunkY, chunkZ);

                    chunkPositions.add(chunkPosition);

                }
            }
        }


        return chunkPositions;

    }

    public List<ChunkPosition> getChunkPositionsAroundPlayer(IPlayer player) {
        Vector3i playerBlockPos = player.getBlockPosition();
        return getChunkPositionsAroundBlockPosition(playerBlockPos.x, playerBlockPos.y, playerBlockPos.z, chunkLoadDistance);
    }

    // thread-safe
    public Chunk getChunkAt(ChunkPosition chunkPosition) {
        return CHUNKS.get(chunkPosition);
    }

    public int getBlock(int x, int y, int z) {

        // if out of world border
        if (x > getSizeX()-1 || x < 0 || y > getSizeY()-1 || y < 0 || z > getSizeZ()-1 || z < 0 ) {
            return Block.WORLD_BORDER;
        }


        Chunk chunk = getChunkContainingBlock(x, y, z);
        if (chunk == null) return Block.NULL;

        if (!chunk.isBlockInBounds(x, y, z)) {
            System.out.println("failed at: " + chunk.getChunkPosition() + " : " + x + ", " + y + ", " + z + " : " + getChunkPositionFromBlockPosition(x, y, z));
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                System.out.println(element);
            }
            System.exit(1);
        }

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

    public int getBlockState(int x, int y, int z) {

        Chunk chunk = getChunkContainingBlock(x, y, z);

        if (chunk == null) {
            return BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.NULL);
        }

        return chunk.getBlockState(x, y, z);
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

        int chunkPosX = blockX / (Chunk.SIZE_X);
        int chunkPosY = blockY / (Chunk.SIZE_Y);
        int chunkPosZ = blockZ / (Chunk.SIZE_Z);

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

        int chunkPosX = blockX / (Chunk.SIZE_X);
        int chunkPosY = blockY / (Chunk.SIZE_Y);
        int chunkPosZ = blockZ / (Chunk.SIZE_Z);

        return new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);
    }

    public Chunk getChunkContainingBlock(Vector3i blockPos) {
        return getChunkContainingBlock(blockPos.x, blockPos.y, blockPos.z);
    }

    public Chunk getChunkContainingBlock(int x, int y, int z) {

        ChunkPosition chunkPosition = getChunkPositionFromBlockPosition(x, y, z);
        if (chunkPosition == null) {
            return null;
        }

        return CHUNKS.get(chunkPosition);
    }

    public Collection<Chunk> getChunks() {
        return CHUNKS.values();
    }



    /**
     *
     * Queues a chunk at chunkPosition to be loaded.
     *
     * <p>
     *     >> Should be thread safe.
     * </p>
     *
     * @param chunkPosition
     */
    public void queueChunkForLoading(ChunkPosition chunkPosition) {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            Updater.get().queueMainThreadInstruction(() -> _queueChunkForLoading(chunkPosition));
        } else {
            _queueChunkForLoading(chunkPosition);
        }

    }

    private void _queueChunkForLoading(ChunkPosition chunkPosition) {

        // check already loaded
        if (getChunkAt(chunkPosition) != null) {
            return;
        }

        // check already queued
        for (ChunkManagementData chunkManagementData : CHUNK_MANAGEMENT_QUEUE) {
            if (chunkManagementData.managementType == ChunkManagementType.LOAD &&
                    chunkManagementData.chunkPosition.equals(chunkPosition)) {
                return;
            }
        }

        CHUNK_MANAGEMENT_QUEUE.offer(new ChunkManagementData(ChunkManagementType.LOAD, chunkPosition));
    }




    /**
     *
     * Queues a chunk at chunkPosition to be unloaded.
     *
     * <p>
     *     >> Should be thread safe.
     * </p>
     *
     * @param chunkPosition
     */
    public void queueChunkForUnloading(ChunkPosition chunkPosition) {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            Updater.get().queueMainThreadInstruction(() -> _queueChunkForUnloading(chunkPosition));
        } else {
            _queueChunkForUnloading(chunkPosition);
        }

    }

    private void _queueChunkForUnloading(ChunkPosition chunkPosition) {
        CHUNK_MANAGEMENT_QUEUE.offer(new ChunkManagementData(
                ChunkManagementType.UNLOAD,
                chunkPosition
        ));
    }


    public void updateChunksAroundPlayer(IPlayer player) {

        List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player);

        for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {
            if (CHUNKS.get(chunkPosition) == null) {
                queueChunkForLoading(chunkPosition);
            }
        }

        // loop through chunks
        for (Map.Entry<ChunkPosition, Chunk> chunkEntry : CHUNKS.entrySet()) {

            Chunk chunk = chunkEntry.getValue();
            ChunkPosition chunkPosition = chunkEntry.getKey();

            // set if chunk should process or not
            chunk.setShouldProcess(
                    chunkProcessDistance >= new Vector3d(chunkPosition.getVector()).distance(player.getPosition())
            );

            if (!chunkPositionsAroundPlayer.contains(chunk.getChunkPosition())) {
                chunk.ticksToLive -= 1;
            }

            if (chunk.isProcessing()) {
                chunk.ticksToLive = Math.max(1, chunk.ticksToLive);
            } else {
                if (chunk.ticksToLive <= 0) {
                    queueChunkForUnloading(chunkPosition);
                }
            }

        }
    }


    /**
     *
     * DO NOT USE DIRECTLY! This method is used ONLY in the World.update(double deltaTime) method.
     *
     *
     * @param chunkPosition
     */
    private void loadChunk(ChunkPosition chunkPosition) {

        if (CHUNKS.get(chunkPosition) != null) return;

        Chunk chunk = new Chunk(this, chunkPosition);
        chunk.generateLand();

        CHUNKS.put(chunkPosition, chunk);
        Updater.get().queueMainThreadInstruction(() -> {
            CHUNKS_CURRENTLY_LOADING.remove(chunkPosition);
        });
    }


    /**
     *
     * DO NOT USE DIRECTLY! This method is used ONLY in the World.update(double deltaTime) method.
     *
     *
     * @param chunkPosition
     */
    private void unloadChunk(ChunkPosition chunkPosition) {

        if (CHUNKS.get(chunkPosition) != null) {
            Chunk chunk = CHUNKS.get(chunkPosition);
            CHUNKS.remove(chunkPosition);

            Updater.get().unregisterUpdateListener(chunk);
            Updater.get().unregisterFixedUpdateListener(chunk);
        }

    }

    @Override
    public void fixedUpdate() {



    }

    @Override
    public void registeredFixedUpdateListener() {

    }

    @Override
    public void unregisteredFixedUpdateListener() {

    }

    @Override
    public void update(double deltaTime) {

        int chunkManagementQueueSize = CHUNK_MANAGEMENT_QUEUE.size();
        for (int i = 0; i < chunkManagementQueueSize; i++) {

            ChunkManagementData chunkManagementData = CHUNK_MANAGEMENT_QUEUE.poll();

            if (chunkManagementData == null) continue;

            switch (chunkManagementData.managementType) {
                case LOAD -> {

                    if (!CHUNKS_CURRENTLY_LOADING.contains(chunkManagementData.chunkPosition)) {
                        if (CHUNKS.get(chunkManagementData.chunkPosition) == null) {
                            CHUNKS_CURRENTLY_LOADING.add(chunkManagementData.chunkPosition);
                            CHUNK_MANAGEMENT_EXECUTOR_SERVICE.submit(() -> loadChunk(chunkManagementData.chunkPosition));
                        }
                    }

                }
                case UNLOAD -> {
                    ChunkPosition chunkPosition = chunkManagementData.chunkPosition;

                    // if chunk is still loading, wait for it to finish loading before unloading it
                    if (CHUNKS_CURRENTLY_LOADING.contains(chunkPosition)) {

                        CHUNK_MANAGEMENT_QUEUE.offer(chunkManagementData);

                    } else {
                        unloadChunk(chunkPosition);
                    }
                }
                default -> {

                }
            }


        }

    }

    @Override
    public void registeredUpdateListener() {

    }

    @Override
    public void unregisteredUpdateListener() {

    }


    // called at unload of world
    public void shutdown() {
        CHUNK_MANAGEMENT_EXECUTOR_SERVICE.shutdown();
        Chunk.chunkMeshGenerationExecutorService.shutdown();
    }

    public static List<ChunkPosition> getChunkPositionsTouchingBoundingBox(BoundingBox boundingBox, boolean includeUnloadedChunks) {
        World world = BackyardRocketry.getInstance().getPlayer().getWorld();

        List<ChunkPosition> chunks = new ArrayList<>();


        // get min chunk position, and get range for chunk loops

        ChunkPosition currentChunkPosition = world.getChunkPositionFromBlockPositionClamped(
                (int) boundingBox.getMax().x,
                (int) boundingBox.getMax().y,
                (int) boundingBox.getMax().z
        );

        int xRange = currentChunkPosition.getX();
        int yRange = currentChunkPosition.getY();
        int zRange = currentChunkPosition.getZ();

        currentChunkPosition = world.getChunkPositionFromBlockPositionClamped(
                (int) boundingBox.getMin().x,
                (int) boundingBox.getMin().y,
                (int) boundingBox.getMin().z
        );

        int minChunkX = currentChunkPosition.getX();
        int minChunkY = currentChunkPosition.getY();
        int minChunkZ = currentChunkPosition.getZ();

        xRange -= currentChunkPosition.getX();
        yRange -= currentChunkPosition.getY();
        zRange -= currentChunkPosition.getZ();


        // loop through chunks to find loaded chunks colliding
        for (int x = 0; x <= xRange; x++) {
            for (int y = 0; y <= yRange; y++) {
                for (int z = 0; z <= zRange; z++) {

                    currentChunkPosition.set(minChunkX + x, minChunkY + y, minChunkZ + z);

                    Chunk chunk = world.getChunkAt(currentChunkPosition);

                    if (!includeUnloadedChunks) {

                        if (chunk == null) {
                            continue;
                        }

                    } else {

                        // if we are including null chunks,
                        // check to see if chunk is in world border
                        // if it's not, then it will never exist, so continue and dont add null to list
                        if (!Collision.isBlockInWorldBorder(currentChunkPosition.getX(), currentChunkPosition.getY(), currentChunkPosition.getZ())) {
                            continue;
                        }

                    }


                    chunks.add(new ChunkPosition(currentChunkPosition));

                }
            }
        }

        return chunks;
    }

    private record ChunkManagementData(ChunkManagementType managementType, ChunkPosition chunkPosition) {

    }
}
