package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import org.joml.Math;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.scenes.GameplayScene;
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
    public static final int CHUNK_AMOUNT_Y = 45;
    public static final int CHUNK_AMOUNT_Z = 45;
    public static int chunkLoadDistance = 8; // chunk loading RADIUS
    public static int chunkUnloadDistance = 10; // chunk unloading RADIUS
    public static int chunkProcessDistance = 3;
    private final HashMap<ChunkPosition, Chunk> CHUNKS;
    private final HashMap<ChunkPosition, ArrayList<Entity>> ENTITIES;
    private final ArrayList<Entity> ENTITY_LIST;
    public static final Random RANDOM = new Random();
    private final ExecutorService CHUNK_MANAGEMENT_EXECUTOR_SERVICE;
    public final ArrayList<ChunkPosition> CHUNKS_CURRENTLY_LOADING;
    private final Queue<ChunkManagementData> CHUNK_MANAGEMENT_QUEUE;

    private final float GRAVITY = -0.1f;
    private int seaLevel = 80;

    private long seed;

    public World() {

        seed = RANDOM.nextLong();
        CHUNKS = new HashMap<>();
        ENTITIES = new HashMap<>();
        ENTITY_LIST = new ArrayList<>();
        CHUNK_MANAGEMENT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(10, r -> new Thread(r, "chunk-management-thread"));
        CHUNK_MANAGEMENT_QUEUE = new LinkedList<>();
        CHUNKS_CURRENTLY_LOADING = new ArrayList<>();

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
        if (x > World.getServerWorld().getSizeX()-1) return false;

        if (y < 0) return false;
        if (y > World.getServerWorld().getSizeY()-1) return false;

        if (z < 0) return false;
        if (z > World.getServerWorld().getSizeZ()-1) return false;

        return true;
    }

    public boolean isPlayerInUnloadedChunk(TestPlayer player) {

        List<Chunk> chunksTouchingPlayer = Collision.getChunksTouchingBoundingBox(this, player.getBoundingBox(), true);

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

    public List<ChunkPosition> getChunkPositionsAroundPlayer(IPlayer player, int radius) {
        Vector3i playerBlockPos = player.getBlockPosition();
        return getChunkPositionsAroundBlockPosition(playerBlockPos.x, playerBlockPos.y, playerBlockPos.z, radius);
    }

    // MUST RUN IN MAIN THREAD
    public Chunk getChunkAt(ChunkPosition chunkPosition) {
        return CHUNKS.get(chunkPosition);
    }

    public byte getBlock(int x, int y, int z) {

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



    public static ServerWorld getServerWorld() {

        GameplayScene gameplayScene = GameplayScene.get();

        if (gameplayScene == null) return null;

        ServerController serverController = gameplayScene.getServer();

        if (serverController == null) return null;

        return serverController.getWorld();

    }

    public static ClientWorld getClientWorld() {

        GameplayScene gameplayScene = GameplayScene.get();

        if (gameplayScene == null) return null;

        ClientController clientController = gameplayScene.getClient();

        if (clientController == null) return null;

        return clientController.getWorld();

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


    // must run in main thread
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

    // MUST RUN IN MAIN THREAD
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

    public ChunkPosition getChunkPositionFromBlockPositionClamped(Vector3i blockPosition) {
        return getChunkPositionFromBlockPositionClamped(blockPosition.x, blockPosition.y, blockPosition.z);
    }

    public ChunkPosition getChunkPositionFromBlockPosition(Vector3i blockPosition) {
        return getChunkPositionFromBlockPosition(blockPosition.x, blockPosition.y, blockPosition.z);
    }

    public ChunkPosition getPlayersChunkPosition(IPlayer player) {
        return getChunkPositionFromBlockPositionClamped(player.getBlockPosition());
    }

    public double getChunkDistanceToPlayer(ChunkPosition chunkPosition, IPlayer player) {
        return new Vector3d(chunkPosition.getVector()).distance(new Vector3d(getPlayersChunkPosition(player).getVector()));
    }

    public void updateChunksAroundPlayer(IPlayer player) {

        List<ChunkPosition> chunkPositionsAroundPlayer = getChunkPositionsAroundPlayer(player, chunkLoadDistance);
        for (ChunkPosition chunkPosition : chunkPositionsAroundPlayer) {

            Chunk chunk = CHUNKS.get(chunkPosition);
            double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);

            if (chunk == null) {

                if (chunkDistance <= chunkLoadDistance) {
                    queueChunkForLoading(chunkPosition);
                }
            }
        }

        for (Map.Entry<ChunkPosition, Chunk> chunkEntry : CHUNKS.entrySet()) {

            ChunkPosition chunkPosition = chunkEntry.getKey();
            Chunk chunk = chunkEntry.getValue();
            double chunkDistance = getChunkDistanceToPlayer(chunkPosition, player);

            chunk.setShouldProcess(chunkDistance <= chunkProcessDistance);

            if (chunkDistance >= chunkUnloadDistance) {
                chunk.ticksToLive -= 1;
            }

            if (chunk.isProcessing()) {
                // if chunk is processing, make it stay alive
                chunk.ticksToLive = Math.max(1, chunk.ticksToLive);
            } else {
                // check for chunk unloading
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

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            Updater.get().queueMainThreadInstruction(() -> {
                Chunk chunk = new Chunk(this, chunkPosition);
                chunk.generateLand();
                CHUNKS.put(chunkPosition, chunk);
                CHUNKS_CURRENTLY_LOADING.remove(chunkPosition);
                ENTITIES.put(chunkPosition, new ArrayList<>());
            });
        } else {
            Chunk chunk = new Chunk(this, chunkPosition);
            chunk.generateLand();
            CHUNKS.put(chunkPosition, chunk);
            CHUNKS_CURRENTLY_LOADING.remove(chunkPosition);
            ENTITIES.put(chunkPosition, new ArrayList<>());
        }
    }

	public boolean isChunkLoaded(ChunkPosition chunkPosition) {
		return CHUNKS.get(chunkPosition) != null;
	}

	public Chunk getChunk(ChunkPosition chunkPosition) {
		return CHUNKS.get(chunkPosition);
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
            ArrayList<Entity> entitiesInChunk = ENTITIES.get(chunkPosition);
            ENTITIES.remove(chunkPosition);

            Chunk chunk = CHUNKS.get(chunkPosition);
            CHUNKS.remove(chunkPosition);

            Iterator<Entity> entityIterator = entitiesInChunk.iterator();
            while (entityIterator.hasNext()) {
                Entity entity = entityIterator.next();
                entity.removedFromWorld();

                ENTITY_LIST.remove(entity);
                entityIterator.remove();
            }

            Updater.get().unregisterUpdateListener(chunk);
            Updater.get().unregisterFixedUpdateListener(chunk);


        }

    }

    @Override
    public void fixedUpdate() {

        for (ArrayList<Entity> entityList : ENTITIES.values()) {
            for (Entity entity : entityList) {
                entity.fixedUpdate();
            }
        }

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

    public void logInfo(String info) {

    }

    // called at unload of world
    public void shutdown() {

        logInfo("Shutting down world . . .");

        CHUNK_MANAGEMENT_EXECUTOR_SERVICE.shutdown();
        Chunk.chunkMeshGenerationExecutorService.shutdown();

        logInfo("World shut down successfully!");
    }

    private record ChunkManagementData(ChunkManagementType managementType, ChunkPosition chunkPosition) {}

    public float getGravity() {
        return GRAVITY;
    }

    public void removeEntity(Entity entity) {

        ChunkPosition chunkPosition = getChunkPositionFromBlockPositionClamped((int) entity.getPosition().x, (int) entity.getPosition().y, (int) entity.getPosition().z);

        ArrayList<Entity> entityList = ENTITIES.get(chunkPosition);

        if (entityList == null) {
            throw new RuntimeException("Tried removing entity in chunk which is not loaded: " + entity + " : " + chunkPosition);
        }

        entity.removedFromWorld();

        entityList.remove(entity);
        ENTITY_LIST.remove(entity);

    }

    public void addEntity(Entity entity, double x, double y, double z) {

        ChunkPosition chunkPosition = getChunkPositionFromBlockPositionClamped((int) x, (int) y, (int) z);

		// if chunk is not loaded
		if (!isChunkLoaded(chunkPosition)) {
			// force chunk to load on main thread (very slow, but idc rn)
			loadChunk(chunkPosition);
		}

        ArrayList<Entity> entityList = ENTITIES.get(chunkPosition);

        if (entityList == null) {
            throw new RuntimeException("Could not get list of entities when adding entity to chunk at: " + chunkPosition);
        }

        if (entityList.contains(entity)) {
            System.out.println("Tried adding already added entity to the world: " + entity);
        }

        entityList.add(entity);
        ENTITY_LIST.add(entity);
        entity.teleport(x, y, z, 0, 0, 0);
        entity.addedToWorld();

    }

    public ArrayList<Entity> getEntityList() {
        return new ArrayList<>(ENTITY_LIST);
    }

    public Random getRandom() {
        return RANDOM;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public Vector3i getBlockPosition(Vector3f position) {
        return new Vector3i((int) position.x, (int) position.y, (int) position.z);
    }

    public Vector3i getBlockPosition(Vector3d position) {
        return new Vector3i((int) position.x, (int) position.y, (int) position.z);
    }
}
