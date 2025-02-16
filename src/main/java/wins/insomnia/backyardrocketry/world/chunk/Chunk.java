package wins.insomnia.backyardrocketry.world.chunk;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.EntityItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Chunk implements IFixedUpdateListener, IUpdateListener {

    public boolean isClean = false;
    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 20;
    public static final int SIZE_Y = 20;
    public static  final int SIZE_Z = 20;
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

    public int getTicksToLive() {
        return ticksToLive;
    }

    public boolean isLoaded() {
        return LOADED.get();
    }

    public void setBlock(int x, int y, int z, byte block) {
        setBlock(x, y, z, block, true);

    }

    public void breakBlock(int x, int y, int z, boolean regenerateMeshInstantly) {

        byte block = getBlock(x, y, z);
        setBlock(x, y, z, Block.AIR, true, regenerateMeshInstantly);

        BlockLoot blockLoot = BlockLoot.getBlockLoot(block);

        if (blockLoot == null) {
            return;
        }

        ArrayList<ArrayList<Object>> defaultLoot = blockLoot.getLootOfType("default");
        for (ArrayList<Object> lootEntry : defaultLoot) {

            String itemSynonym = (String) lootEntry.get(0);
            int volume = (Integer) lootEntry.get(1);

            Item item = Item.getItem(itemSynonym);
            ItemStack itemStack = new ItemStack(item, volume);

            EntityItem itemEntity = new EntityItem(itemStack, WORLD);
            WORLD.addEntity(itemEntity, X + x + 0.5f, Y + y + 0.5f, Z + z + 0.5f);
        }
    }

    public void setBlock(int x, int y, int z, byte block, boolean regenerateMesh, boolean instantly) {

        if (Thread.currentThread() != Main.MAIN_THREAD) {

            throw new ConcurrentModificationException("Tried to \"setBlock\" on thread other than main thread!");

        }

        chunkData.setBlock(x,y,z, block);

        /*

        setShouldRegenerateMesh(regenerateMesh, instantly);

        if (shouldRegenerateMesh.get()) {
            updateNeighborChunkMeshesIfBlockIsOnBorder(toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }


         */
    }

    public void setBlock(int x, int y, int z, byte block, boolean regenerateMesh) {

        setBlock(x, y, z, block, regenerateMesh, false);

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

                    if (block == Block.NULL) continue;

                    BoundingBox blockBoundingBox = Block.getBlockCollision(block);

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
            return Block.NULL;
        }

        return chunkData.getBlock(x, y, z);
    }

    public byte getBlock(int x, int y, int z) {

        // if block is out of chunk bounds
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {

            int globalX = toGlobalX(x);
            int globalY = toGlobalY(y);
            int globalZ = toGlobalZ(z);

            // if block is out of world border
            if (globalX > WORLD.getSizeX()-1 || globalX < 0 || globalY > WORLD.getSizeY()-1 || globalY < 0 || globalZ > WORLD.getSizeZ()-1 || globalZ < 0 ) {
                return Block.WORLD_BORDER;
            }

            // get neighbor chunk with block
            int chunkPosX = globalX / (Chunk.SIZE_X);
            int chunkPosY = globalY / (Chunk.SIZE_Y);
            int chunkPosZ = globalZ / (Chunk.SIZE_Z);

            ChunkPosition chunkPosition = new ChunkPosition(chunkPosX, chunkPosY, chunkPosZ);
            Chunk chunk = WORLD.getChunk(chunkPosition);

            if (chunk == null) {
                return Block.NULL;
            }

            return chunk.getBlock(chunk.toLocalX(globalX), chunk.toLocalY(globalY), chunk.toLocalZ(globalZ));
        }

        return chunkData.getBlock(x,y,z);
    }


    public int getBlockState(int x, int y, int z) {

        if (!isBlockInBounds(x, y, z)) {
            return WORLD.getBlockState(x, y, z);
        }
        return chunkData.getBlock(toLocalX(x), toLocalY(y), toLocalZ(z));
    }

    public int getBlockStateLocal(int x, int y, int z) {

        if (!isBlockInBoundsLocal(x, y, z)) {
            return WORLD.getBlockState(toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }

        return chunkData.getBlock(x, y, z);
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

    public Chunk[] getNeighborChunks() {

        return new Chunk[] {
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(-1, 0, 0)),
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(1, 0, 0)),
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(0, -1, 0)),
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(0, 1, 0)),
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(0, 0, -1)),
                WORLD.getChunkAt(new ChunkPosition(getChunkPosition()).add(0, 0, 1))
        };
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

}
