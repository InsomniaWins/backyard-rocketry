package wins.insomnia.backyardrocketry.level;

import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.player.Player;
import wins.insomnia.backyardrocketry.level.chunk.Chunk;
import wins.insomnia.backyardrocketry.level.chunk.ChunkPosition;
import wins.insomnia.backyardrocketry.util.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Level implements IUpdateListener, IFixedUpdateListener {

    private final HashMap<ChunkPosition, Chunk> CHUNK_MAP = new HashMap<>();

    private int chunkLoadRadius = 1;


    public Level() {

        Updater.get().registerUpdateListener(this);
        Updater.get().registerFixedUpdateListener(this);

    }


    public void updateChunksAroundPlayer(Player player) {

        ThreadSafety.assertThread(Main.MAIN_THREAD);

        int chunkDiameter = chunkLoadRadius * 2 + 1;

        for (int x = 0; x < chunkDiameter; x++) {
            for (int z = 0; z < chunkDiameter; z++) {
                for (int y = 0; y < chunkDiameter; y++) {

                    ChunkPosition chunkPosition = new ChunkPosition(
                            x - chunkLoadRadius + (int) player.getPosition().x,
                            y - chunkLoadRadius + (int) player.getPosition().y,
                            z - chunkLoadRadius + (int) player.getPosition().z
                    );

                    Chunk loadedChunk = loadChunk(chunkPosition, 4);

                }
            }
        }

    }

    public void unloadChunk(ChunkPosition chunkPosition) {

        Chunk removedChunk = CHUNK_MAP.remove(chunkPosition);

        if (removedChunk != null) {
            removedChunk.clean();
            Updater.get().unregisterManualUpdateListener(removedChunk);
            System.out.println("removed chunk: " + removedChunk);
        } else {
            System.out.println(CHUNK_MAP.keySet());
        }

    }

    private Chunk loadChunk(ChunkPosition chunkPosition, int ticksToLive) {

        // if chunk already exists, return it
        Chunk returnChunk = CHUNK_MAP.get(chunkPosition);
        if (returnChunk != null) {
            returnChunk.setTicksToLive(ticksToLive);
            return returnChunk;
        }


        // else create the chunk, store it, and return it
        returnChunk = new Chunk(this, chunkPosition);
        returnChunk.setTicksToLive(ticksToLive);
        Updater.get().registerManualUpdateListener(returnChunk);
        CHUNK_MAP.put(chunkPosition, returnChunk);


        return returnChunk;
    }


    public Chunk getChunkAt(ChunkPosition chunkPosition) {
        return CHUNK_MAP.get(chunkPosition);
    }

    @CalledFromMainThread
    public boolean isChunkLoaded(ChunkPosition chunkPosition, int... acceptableGenerationStatuses) {

        Chunk chunk = getChunkAt(chunkPosition);

        if (chunk == null) return false;

        AtomicInteger chunkGenerationStatus = chunk.getGenerationStatus();

        for (int acceptableGenerationStatus : acceptableGenerationStatuses) {
            if (chunkGenerationStatus.get() == acceptableGenerationStatus) {
                return true;
            }
        }


        return false;
    }

    @CalledFromMainThread
    public boolean areChunksLoaded(ChunkPosition[] chunkPositions, int... acceptableGenerationStatuses) {

        for (ChunkPosition chunkPosition : chunkPositions) {

            if (!isChunkLoaded(chunkPosition, acceptableGenerationStatuses)) {
                return false;
            }

        }

        return true;
    }

    public ChunkPosition getChunkPositionFromPosition(Vector3d position) {

        int x = (int) position.x / Chunk.SIZE.x;
        int y = (int) position.y / Chunk.SIZE.y;
        int z = (int) position.z / Chunk.SIZE.z;

        return new ChunkPosition(x, y, z);

    }

    @Override
    public void update(double deltaTime) {


    }

    @Override
    public void fixedUpdate() {



    }

}
