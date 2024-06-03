package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import org.joml.Math;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.util.*;
import java.util.Random;
import java.util.concurrent.*;

public class World implements IFixedUpdateListener, IUpdateListener {

    public static final int CHUNK_AMOUNT_X = 128;
    public static final int CHUNK_AMOUNT_Y = 6;
    public static final int CHUNK_AMOUNT_Z = 128;
    private static World instance;

    public static int chunkLoadDistance = 5;
    public static int chunkProcessDistance = 100;//in block units NOT chunk positional units

    private final ConcurrentHashMap<ChunkPosition, Chunk> CHUNKS;
    protected final ConcurrentLinkedQueue<ChunkPosition> UNLOAD_CHUNK_QUEUE;
    protected final ConcurrentLinkedQueue<ChunkPosition> LOAD_CHUNK_QUEUE;
    protected final ConcurrentLinkedQueue<ChunkPosition> DECORATE_CHUNK_QUEUE;
    protected final ConcurrentLinkedQueue<Future> FUTURE_LIST;
    public static final Random RANDOM = new Random();


    private final ExecutorService chunkManagerExecutorService = Executors.newFixedThreadPool(10);

    private long seed;

    public World() {

        seed = RANDOM.nextLong();
        CHUNKS = new ConcurrentHashMap<>();
        LOAD_CHUNK_QUEUE = new ConcurrentLinkedQueue<>();
        UNLOAD_CHUNK_QUEUE = new ConcurrentLinkedQueue<>();
        DECORATE_CHUNK_QUEUE = new ConcurrentLinkedQueue<>();
        FUTURE_LIST = new ConcurrentLinkedQueue<>();
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

    public void setBlocks(ArrayList<Integer[]> blockList) {

        for (Integer[] blockData : blockList) {

            setBlock(blockData[0], blockData[1], blockData[2], blockData[3]);

        }

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

    public void setBlock(int x, int y, int z, int blockId) {

        if (!isBlockInWorldBorder(x, y, z)) {
            return;
        }

        ChunkPosition chunkPosition = getChunkPositionFromBlockPosition(x, y, z);
        Chunk chunk = getChunkAt(chunkPosition);

        if (LOAD_CHUNK_QUEUE.contains(chunkPosition)) {
            while (chunk == null) {
                chunk = getChunkAt(chunkPosition);
            }
        }

        if (chunk == null) {
            chunk = loadChunk(chunkPosition, false, true);
        }

        if (chunk == null) {
            System.out.println("could not load chunk: " + chunkPosition);
        }

        chunk.setBlock(
                chunk.toLocalX(x),
                chunk.toLocalY(y),
                chunk.toLocalZ(z),
                blockId
        );

    }

    // returns loaded chunk if generateOnSameThread is true;
    // otherwise, returns null
    protected Chunk loadChunk(ChunkPosition chunkPosition, boolean shouldDecorateChunk, boolean generateOnSameThread) {



        if (CHUNKS.containsKey(chunkPosition)) {

            Chunk chunk = CHUNKS.get(chunkPosition);

            if (!(chunk.isWaitingForGeneration() && !chunk.isDecorating() && shouldDecorateChunk)) {
                return null;
            }
        }

        if (LOAD_CHUNK_QUEUE.contains(chunkPosition)) {
            return null;
        }

        LOAD_CHUNK_QUEUE.add(chunkPosition);

        if (generateOnSameThread) {

            // otherwise, generate
            Chunk chunk = generateChunk(chunkPosition);

            CHUNKS.put(chunkPosition, chunk);
            LOAD_CHUNK_QUEUE.remove(chunkPosition);

            if (shouldDecorateChunk) {
                chunk.decorate();
            }

            return chunk;

        } else {

            FUTURE_LIST.add(chunkManagerExecutorService.submit(() -> {

                // otherwise, generate
                Chunk chunk = generateChunk(chunkPosition);

                CHUNKS.put(chunkPosition, chunk);
                LOAD_CHUNK_QUEUE.remove(chunkPosition);

                if (shouldDecorateChunk) {
                    chunk.decorate();
                }

            }));
        }

        return null;
    }

    protected void loadChunk(ChunkPosition chunkPosition, boolean shouldDecorateChunk) {
        loadChunk(chunkPosition, shouldDecorateChunk, false);
    }

    private void decorateChunk(ChunkPosition chunkPosition) {

        if (CHUNKS.containsKey(chunkPosition)) {
            return;
        }

        if (LOAD_CHUNK_QUEUE.contains(chunkPosition)) {
            return;
        }

        DECORATE_CHUNK_QUEUE.add(chunkPosition);

        chunkManagerExecutorService.submit(() -> {

            Chunk chunk = CHUNKS.get(chunkPosition);

            if (chunk == null) return;

            chunk.decorate();
            DECORATE_CHUNK_QUEUE.remove(chunkPosition);

        });

    }

    // happens on chunk generation thread -- NOT MAIN THREAD!
    private Chunk generateChunk(ChunkPosition chunkPosition) {

        Chunk chunk;
        try {
            chunk = new Chunk(
                    this,
                    chunkPosition.getX(),
                    chunkPosition.getY(),
                    chunkPosition.getZ()
            );
        } catch (Exception e) {
            e.printStackTrace();
            chunk = null;
        }

        return chunk;
    }

    public void queueUnloadChunk(ChunkPosition chunkPosition) {

        UNLOAD_CHUNK_QUEUE.offer(chunkPosition);

    }

    private void unloadChunk(ChunkPosition chunkPosition) {

        Chunk chunk = CHUNKS.get(chunkPosition);
        CHUNKS.remove(chunkPosition);
        chunk.clean();

    }

    public boolean isPlayerInUnloadedChunk(TestPlayer player) {

        List<Chunk> chunksTouchingPlayer = Collision.getChunksTouchingBoundingBox(player.getBoundingBox(), true);

		return chunksTouchingPlayer.contains(null);
	}

    public void updateChunksAroundPlayer(IPlayer player) {

        List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player);

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
                loadChunk(chunkPosition, true);
            }
        }


        // unload chunks
        while (!UNLOAD_CHUNK_QUEUE.isEmpty()) {
            ChunkPosition chunkPosition = UNLOAD_CHUNK_QUEUE.poll();
            unloadChunk(chunkPosition);
        }

        // set chunks as processing or not
        for (Map.Entry<ChunkPosition, Chunk> chunkEntry : CHUNKS.entrySet()) {

            chunkEntry.getValue().setShouldProcess(
                    chunkProcessDistance >= new Vector3d(chunkEntry.getKey().getVector()).distance(player.getPosition())
            );

        }
    }

    public List<ChunkPosition> getChunkPositionsBlockPosition(int x, int y, int z, int chunkRadius) {

        List<ChunkPosition> chunkPositions = new ArrayList<>();
        ChunkPosition originChunkPosition = getChunkPositionFromBlockPositionClamped(x, y, z);

        for (int xIterator = -chunkRadius; xIterator < chunkRadius; xIterator++) {
            for (int yIterator = -chunkRadius; yIterator < chunkRadius; yIterator++) {
                for (int zIterator = -chunkRadius; zIterator < chunkRadius; zIterator++) {

                    int chunkX = xIterator * Chunk.SIZE_X + originChunkPosition.getX();
                    if (chunkX < 0 || chunkX >= getSizeX()) continue;


                    int chunkY = yIterator * Chunk.SIZE_Y + originChunkPosition.getY();
                    if (chunkY < 0 || chunkY >= getSizeY()) continue;


                    int chunkZ = zIterator * Chunk.SIZE_Z + originChunkPosition.getZ();
                    if (chunkZ < 0 || chunkZ >= getSizeZ()) continue;

                    ChunkPosition chunkPosition = new ChunkPosition(chunkX, chunkY, chunkZ);

                    chunkPositions.add(chunkPosition);

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
            return BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.NULL);
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

        Iterator<Future> futureIterator = FUTURE_LIST.iterator();
        while (futureIterator.hasNext()) {
            Future future = futureIterator.next();

            if (!future.isDone() && !future.isCancelled()) {
                continue;
            }

            try {
                Object futureResult = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            futureIterator.remove();
        }

    }

    // called at unload of world
    public void shutdown() {
        chunkManagerExecutorService.shutdown();
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
        for (int x = 0; x <= xRange; x += Chunk.SIZE_X) {
            for (int y = 0; y <= yRange; y += Chunk.SIZE_Y) {
                for (int z = 0; z <= zRange; z += Chunk.SIZE_Z) {

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
}
