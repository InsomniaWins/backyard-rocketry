package wins.insomnia.backyardrocketry.world.chunk;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Chunk implements IFixedUpdateListener, IUpdateListener {

    public boolean isClean = false;
    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 32;
    public static  final int SIZE_Z = 32;
    private final int X;
    private final int Y;
    private final int Z;
    private final World WORLD;
    private final AtomicBoolean LOADED = new AtomicBoolean(false);
    private boolean shouldProcess = false;
    protected int ticksToLive = 240;
    protected ChunkData chunkData;

    public Chunk(World world, ChunkPosition chunkPosition) {

        X = chunkPosition.getBlockX();
        Y = chunkPosition.getBlockY();
        Z = chunkPosition.getBlockZ();

        BOUNDING_BOX = new BoundingBox(
                X, Y, Z,
                X + SIZE_X, Y + SIZE_Y, Z + SIZE_Z
        );

        WORLD = world;

        Updater.get().registerFixedUpdateListener(this);
        Updater.get().registerUpdateListener(this);

    }

    public void decrementTicksToLive() {
        ticksToLive = Math.max(0, --ticksToLive);
    }

    public int getTicksToLive() {
        return ticksToLive;
    }

    public void setTicksToLive(int ticks) {
        ticksToLive = Math.max(ticksToLive, ticks);
    }

    public boolean isLoaded() {
        return LOADED.get();
    }

    public void setBlock(int x, int y, int z, byte block, byte blockState) {

        while (!chunkData.grabThreadOwnership());

        chunkData.setBlock(x, y, z, block);
        chunkData.setBlockState(x, y, z, blockState);

        while (!chunkData.loseThreadOwnership());

    }

    public boolean containsLocalBlockPosition(int localX, int localY, int localZ) {

        if (localX < 0 || localX > SIZE_X - 1) return false;
        if (localY < 0 || localY > SIZE_Y - 1) return false;
        if (localZ < 0 || localZ > SIZE_Z - 1) return false;

        return true;

    }

    public boolean containsGlobalBlockPosition(int blockX, int blockY, int blockZ) {

        int localX = toLocalX(blockX);
        int localY = toLocalY(blockY);
        int localZ = toLocalZ(blockZ);

        return containsLocalBlockPosition(localX, localY, localZ);

    }

    public World getWorld() {
        return WORLD;
    }


    public List<BoundingBox> getBlockBoundingBoxes(BoundingBox boundingBox) {

		int[] minPos = new int[] {
				(int) boundingBox.getMin().x-1 - X,
				(int) boundingBox.getMin().y-1 - Y,
				(int) boundingBox.getMin().z-1 - Z
		};

		int[] maxPos = new int[] {
				(int) (Math.round(boundingBox.getMax().x)+1) - X,
				(int) (Math.round(boundingBox.getMax().y)+1) - Y,
				(int) (Math.round(boundingBox.getMax().z)+1) - Z
		};

        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int x = minPos[0]; x < maxPos[0]; x++) {
            for (int y = minPos[1]; y < maxPos[1]; y++) {
                for (int z = minPos[2]; z < maxPos[2]; z++) {

                    if (!isBlockInBoundsLocal(x, y, z)) {
                        continue;
                    }

                    byte block = getBlock(x, y, z);

                    if (block == Blocks.NULL) continue;

                    BoundingBox blockBoundingBox = Blocks.getBlockCollision(block);

                    if (blockBoundingBox == null) continue;

                    blockBoundingBox.getMin().add(X + x, Y + y, Z + z);
                    blockBoundingBox.getMax().add(X + x, Y + y, Z + z);

                    boundingBoxes.add(blockBoundingBox);
                }
            }
        }

        return boundingBoxes;
    }

    public int toLocalX(int x) {
        return x - X;
    }

    public int toLocalY(int y) {
        return y - Y;
    }
    public int toLocalZ(int z) {
        return z - Z;
    }

    public int toGlobalX(int x) {
        return x + X;
    }

    public int toGlobalY(int y) {
        return y + Y;
    }
    public int toGlobalZ(int z) {
        return z + Z;
    }


    public Vector3f getPosition() {
        return new Vector3f(X,Y,Z);
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }

    public int getBlock(Vector3i blockPos) {
        return getBlock(blockPos.x, blockPos.y, blockPos.z);
    }

    public byte getBlockOrNull(int x, int y, int z) {
        // if out of chunk boundaries
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {
            return Blocks.NULL;
        }

        return chunkData.getBlock(x, y, z);
    }


    public void setLightValue(int x, int y, int z, short lightValue) {

        // if block is out of chunk bounds
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {

            int globalX = toGlobalX(x);
            int globalY = toGlobalY(y);
            int globalZ = toGlobalZ(z);

            // if block is out of world border
            if (globalX > World.getSizeX()-1 || globalX < 0 || globalY > World.getSizeY()-1 || globalY < 0 || globalZ > World.getSizeZ()-1 || globalZ < 0 ) {
                return;
            }

            Chunk chunk = WORLD.getChunkContainingBlock(globalX, globalY, globalZ);

            if (chunk == null) {
                return;
            }

            chunk.setLightValue(chunk.toLocalX(globalX), chunk.toLocalY(globalY), chunk.toLocalZ(globalZ), lightValue);
            return;
        }

        chunkData.setLightValue(x, y, z, lightValue);

    }

    // gets the chunk containing the block at x,y,z local to this chunk,
    // then the xyz array will be given new local x,y,z values based on returned chunk
    // the new x,y,z values will be the same block position inputted, but local to the returned chunk
    // for example: <-1, 0, 0> will change to <Chunk.SIZE_X - 1, 0, 0> and the chunk to the -x of this one will be returned
    // they will not change if chunk is NULL or the block resides within this chunk
    public Chunk getChunkOrNeighborFromLocalBlock(int[] xyz) {

        if (xyz.length < 3) return null;

        // if block is out of chunk bounds
        if ((xyz[0] < 0 || xyz[0] > SIZE_X - 1) || (xyz[1] < 0 || xyz[1] > SIZE_Y - 1) || (xyz[2] < 0 || xyz[2] > SIZE_Z - 1)) {

            int globalX = toGlobalX(xyz[0]);
            int globalY = toGlobalY(xyz[1]);
            int globalZ = toGlobalZ(xyz[2]);

            // if block is out of world border
            if (globalX > World.getSizeX()-1 || globalX < 0 || globalY > World.getSizeY()-1 || globalY < 0 || globalZ > World.getSizeZ()-1 || globalZ < 0 ) {
                return null;
            }

            Chunk chunk = WORLD.getChunkContainingBlock(globalX, globalY, globalZ);

            if (chunk != null) {

                xyz[0] = chunk.toLocalX(globalX);
                xyz[1] = chunk.toLocalY(globalY);
                xyz[2] = chunk.toLocalZ(globalZ);

            }

            return chunk;
        }

        return this;

    }

    public Chunk getChunkOrNeighborFromLocalBlock(int x, int y, int z) {

        // if block is out of chunk bounds
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {

            int globalX = toGlobalX(x);
            int globalY = toGlobalY(y);
            int globalZ = toGlobalZ(z);

            // if block is out of world border
            if (globalX > World.getSizeX()-1 || globalX < 0 || globalY > World.getSizeY()-1 || globalY < 0 || globalZ > World.getSizeZ()-1 || globalZ < 0 ) {
                return null;
            }

            return WORLD.getChunkContainingBlock(globalX, globalY, globalZ);
        }

        return this;

    }

    public short getLightValue(int x, int y, int z) {

        // if block is out of chunk bounds
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {

            int globalX = toGlobalX(x);
            int globalY = toGlobalY(y);
            int globalZ = toGlobalZ(z);

            // if block is out of world border
            if (globalX > World.getSizeX()-1 || globalX < 0 || globalY > World.getSizeY()-1 || globalY < 0 || globalZ > World.getSizeZ()-1 || globalZ < 0 ) {
                return 0b0000_0000_0000_0000;
            }

            Chunk chunk = WORLD.getChunkContainingBlock(globalX, globalY, globalZ);

            if (chunk == null) {
                return 0b0000_0000_0000_0000;
            }

            return chunk.getLightValue(chunk.toLocalX(globalX), chunk.toLocalY(globalY), chunk.toLocalZ(globalZ));
        }

        return chunkData.getLightValue(x, y, z);

    }

    public byte getBlock(int x, int y, int z) {

        // if block is out of chunk bounds
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {

            int globalX = toGlobalX(x);
            int globalY = toGlobalY(y);
            int globalZ = toGlobalZ(z);

            // if block is out of world border
            if (globalX > WORLD.getSizeX()-1 || globalX < 0 || globalY > WORLD.getSizeY()-1 || globalY < 0 || globalZ > WORLD.getSizeZ()-1 || globalZ < 0 ) {
                return Blocks.WORLD_BORDER;
            }

            // get neighbor chunk with block
            int chunkPosX = globalX / (Chunk.SIZE_X);
            int chunkPosY = globalY / (Chunk.SIZE_Y);
            int chunkPosZ = globalZ / (Chunk.SIZE_Z);

            ChunkPosition chunkPosition = new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);
            Chunk chunk = WORLD.getChunkSafe(chunkPosition);

            if (chunk == null) {
                return Blocks.NULL;
            }

            return chunk.getBlock(chunk.toLocalX(globalX), chunk.toLocalY(globalY), chunk.toLocalZ(globalZ));
        }

        return chunkData.getBlock(x,y,z);
    }


    public byte getBlockStateGlobal(int x, int y, int z) {

        if (!isBlockInBounds(x, y, z)) {
            return WORLD.getBlockState(x, y, z);
        }

        return chunkData.getBlockState(toLocalX(x), toLocalY(y), toLocalZ(z));
    }

    public byte getBlockState(int x, int y, int z) {

        if (!isBlockInBoundsLocal(x, y, z)) {
            return WORLD.getBlockState(toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }

        return chunkData.getBlockState(x, y, z);
    }

    // IN GLOBAL SPACE
    public boolean isBlockInBounds(int x, int y, int z) {

        x = toLocalX(x);
        y = toLocalY(y);
        z = toLocalZ(z);

        return !((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1));
    }

    public boolean isBlockInBoundsLocal(int localX, int localY, int localZ) {
        return isBlockInBounds(toGlobalX(localX), toGlobalY(localY), toGlobalZ(localZ));
    }

    public boolean isBlockOnChunkBorder(int x, int y, int z) {

        x = toLocalX(x);
        y = toLocalY(y);
        z = toLocalZ(z);

        return (x == 0 || x == SIZE_X - 1 || y == 0 || y == SIZE_Y - 1 || z == 0 || z == SIZE_Z - 1);

    }


    public ChunkPosition getChunkPosition() {
        return World.getServerWorld().getChunkPositionFromBlockPosition(getX(), getY(), getZ());
    }

    public Chunk[] getNeighborChunks(boolean includeCorners) {

        if (!includeCorners) {
            return new Chunk[] {
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 0, -1)),
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 0, 1)),
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 0, 0)),
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 0, 0)),
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, -1, 0)),
                    WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 1, 0))
            };
        }

        return new Chunk[] {
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, -1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, -1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, -1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 0, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 0, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 0, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 1, -1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, -1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, -1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, -1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 0, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 0, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 1, 0)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, -1, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, -1, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, -1, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 0, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 0, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 0, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(-1, 1, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(0, 1, 1)),
                WORLD.getChunkAtSafe(getChunkPosition().newOffsetChunkPosition(1, 1, 1)),

        };
    }

    public Chunk[] getNeighborChunks() {
        return getNeighborChunks(false);
    }



    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void registeredUpdateListener() {

    }

    @Override
    public void unregisteredUpdateListener() {

    }

    public boolean isProcessing() {
        return shouldProcess;
    }

    public void setShouldProcess(boolean value) {
        shouldProcess = value;
    }

    @Override
    public void fixedUpdate() {
        if (--ticksToLive <= 0) {

            getWorld().queueChunkForUnloading(getChunkPosition());

        }
    }

    @Override
    public void registeredFixedUpdateListener() {

    }

    @Override
    public void unregisteredFixedUpdateListener() {

    }


    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }

    public Chunk getTopChunk() {

        ChunkPosition topChunkPosition = getChunkPosition().newOffsetChunkPosition(0, -1, 0);
        return getWorld().getChunkSafe(topChunkPosition);

    }


    public boolean hasFinishedDesiredGenerationPass() {
        return false;
    }

}
