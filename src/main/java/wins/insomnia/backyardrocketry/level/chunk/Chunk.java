package wins.insomnia.backyardrocketry.level.chunk;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.level.Level;
import wins.insomnia.backyardrocketry.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Chunk extends UpdateListener {

    public static final int GENERATION_STATUS_EMPTY = 0;
    public static final int GENERATION_STATUS_GENERATING_TERRAIN = 1;
    public static final int GENERATION_STATUS_WAITING_FOR_POPULATION = 2;
    public static final int GENERATION_STATUS_POPULATING = 3;
    public static final int GENERATION_STATUS_GENERATED = 4;
    public static final Vector3i SIZE = new Vector3i(32, 32, 32);

    private final ChunkPosition CHUNK_POSITION;
    private final AtomicInteger GENERATION_STATUS = new AtomicInteger(GENERATION_STATUS_EMPTY);
    private final Level LEVEL;
    private final ChunkPosition[] NEIGHBORING_CHUNK_POSITIONS;



    private int[][][] BLOCK_STATES = new int[SIZE.x][SIZE.y][SIZE.z];
    private int ticksToLive = 0;


    public Chunk(Level level, ChunkPosition chunkPosition) {
        this.LEVEL = level;
        this.CHUNK_POSITION = new ChunkPosition(chunkPosition);

        NEIGHBORING_CHUNK_POSITIONS = new ChunkPosition[] { // 3^3 cube without center (this chunk is the center)

                new ChunkPosition(CHUNK_POSITION).add(-1, -1, -1),
                new ChunkPosition(CHUNK_POSITION).add(-1, -1, 0),
                new ChunkPosition(CHUNK_POSITION).add(-1, -1, 1),

                new ChunkPosition(CHUNK_POSITION).add(0, -1, -1),
                new ChunkPosition(CHUNK_POSITION).add(0, -1, 0),
                new ChunkPosition(CHUNK_POSITION).add(0, -1, 1),

                new ChunkPosition(CHUNK_POSITION).add(1, -1, -1),
                new ChunkPosition(CHUNK_POSITION).add(1, -1, 0),
                new ChunkPosition(CHUNK_POSITION).add(1, -1, 1),





                new ChunkPosition(CHUNK_POSITION).add(-1, 0, -1),
                new ChunkPosition(CHUNK_POSITION).add(-1, 0, 0),
                new ChunkPosition(CHUNK_POSITION).add(-1, 0, 1),

                new ChunkPosition(CHUNK_POSITION).add(0, 0, -1),
                new ChunkPosition(CHUNK_POSITION).add(0, 0, 1),

                new ChunkPosition(CHUNK_POSITION).add(1, 0, -1),
                new ChunkPosition(CHUNK_POSITION).add(1, 0, 0),
                new ChunkPosition(CHUNK_POSITION).add(1, 0, 1),





                new ChunkPosition(CHUNK_POSITION).add(-1, 1, -1),
                new ChunkPosition(CHUNK_POSITION).add(-1, 1, 0),
                new ChunkPosition(CHUNK_POSITION).add(-1, 1, 1),

                new ChunkPosition(CHUNK_POSITION).add(0, 1, -1),
                new ChunkPosition(CHUNK_POSITION).add(0, 1, 0),
                new ChunkPosition(CHUNK_POSITION).add(0, 1, 1),

                new ChunkPosition(CHUNK_POSITION).add(1, 1, -1),
                new ChunkPosition(CHUNK_POSITION).add(1, 1, 0),
                new ChunkPosition(CHUNK_POSITION).add(1, 1, 1),

        };
    }


    public int getLocalBlockState(int localX, int localY, int localZ) {
        return BLOCK_STATES[localX][localY][localZ];
    }

    public int getLocalBlock(int localX, int localY, int localZ) {
        int blockState = getLocalBlockState(localX, localY, localZ);
        return BitHelper.getBlockIdFromBlockState(blockState);
    }


    public ChunkPosition getChunkPosition() {
        return CHUNK_POSITION;
    }

    public void setTicksToLive(int amount) {
        ticksToLive = amount;
    }


    @CalledFromMainThread
    private void populate() {

        ThreadSafety.assertThread(Main.MAIN_THREAD);

        BackyardRocketry.getInstance().getChunkGenerationThreadPool().submit(() -> {

            // populate


            // tell chunk, it's finished generating
            GENERATION_STATUS.set(GENERATION_STATUS_GENERATED);
        });

    }

    @CalledFromMainThread
    private void generateTerrain() {

        ThreadSafety.assertThread(Main.MAIN_THREAD);

        BackyardRocketry.getInstance().getChunkGenerationThreadPool().submit(() -> {

            // generate terrain


            // tell chunk to populate
            GENERATION_STATUS.set(GENERATION_STATUS_WAITING_FOR_POPULATION);
        });

    }

    public AtomicInteger getGenerationStatus() {
        return GENERATION_STATUS;
    }

    public Level getLevel() {
        return LEVEL;
    }

    public boolean isFinishedGenerating() {
        return getGenerationStatus().get() == GENERATION_STATUS_GENERATED;
    }

    @Override
    public void fixedUpdate() {

        if (ticksToLive > 0) {
            if (GENERATION_STATUS.compareAndSet(GENERATION_STATUS_EMPTY, GENERATION_STATUS_GENERATING_TERRAIN)) {

                generateTerrain();

            }

            if (GENERATION_STATUS.get() == GENERATION_STATUS_WAITING_FOR_POPULATION) {

                // check if bordering chunks are ready for population

                boolean shouldPopulate = getLevel().areChunksLoaded(NEIGHBORING_CHUNK_POSITIONS, GENERATION_STATUS_GENERATED, GENERATION_STATUS_WAITING_FOR_POPULATION);

                if (shouldPopulate) {
                    if (GENERATION_STATUS.compareAndSet(GENERATION_STATUS_WAITING_FOR_POPULATION, GENERATION_STATUS_POPULATING)) {

                        populate();

                    }
                }
            }
        } else {
            unload();
        }

        ticksToLive--;
    }

    private void unload() {

        LEVEL.unloadChunk(getChunkPosition());
        System.out.println("Unloaded chunk: " + getChunkPosition());

    }

    public void clean() {



    }

    @Override
    public void update(double deltaTime) {





    }

}
