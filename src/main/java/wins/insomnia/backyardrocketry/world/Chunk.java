package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.entity.EntityItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.debug.DebugTime;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.util.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Chunk implements IFixedUpdateListener, IUpdateListener {

    public enum GenerationPhase {
        UNLOADED,
        GENERATING_LAND,
        READY_FOR_DECORATION,
        PLACING_DECORATIONS,
        GENERATED
    }
    public boolean isClean = false;
    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 20;
    public static final int SIZE_Y = 20;
    public static  final int SIZE_Z = 20;
    public static final ExecutorService chunkMeshGenerationExecutorService = Executors.newFixedThreadPool(4);
    private final int X;
    private final int Y;
    private final int Z;
    private final int RANDOM_TICK_AMOUNT = 18;
    private final ChunkMesh CHUNK_MESH;
    private final ChunkMesh TRANSPARENT_CHUNK_MESH;
    private final World WORLD;
    private AtomicInteger generationPhase = new AtomicInteger(GenerationPhase.UNLOADED.ordinal());
    private boolean shouldProcess = false;
    private byte[][][] blocks;
    protected AtomicBoolean shouldRegenerateMesh = new AtomicBoolean(false);
    protected AtomicBoolean shouldInstantlyGenerateMesh = new AtomicBoolean(false);
    protected int ticksToLive = 240;

    public Chunk(World world, ChunkPosition chunkPosition) {


        X = chunkPosition.getBlockX();
        Y = chunkPosition.getBlockY();
        Z = chunkPosition.getBlockZ();

        BOUNDING_BOX = new BoundingBox(
                X, Y, Z,
                X + SIZE_X, Y + SIZE_Y, Z + SIZE_Z
        );

        WORLD = world;
        CHUNK_MESH = new ChunkMesh(this, false);
        TRANSPARENT_CHUNK_MESH = new ChunkMesh(this, true);

        initializeBlocks();

        Updater.get().registerFixedUpdateListener(this);
        Updater.get().registerUpdateListener(this);

        //System.out.println("Loaded chunk: " + chunkPosition);
    }


    protected void generateLand() {

        generationPhase.set(GenerationPhase.GENERATING_LAND.ordinal());

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    int globalBlockX = x + X;
                    int globalBlockY = y + Y;
                    int globalBlockZ = z + Z;

                    int groundHeight = WorldGeneration.getGroundHeight(globalBlockX, globalBlockZ);

                    byte block = Block.AIR;

                    if (globalBlockY > groundHeight) {

                        if (globalBlockY <= WORLD.getSeaLevel()) {
                            block = Block.WATER;
                        } else {
                            continue;
                        }

                    } else {

                        if (globalBlockY == groundHeight) {
                            block = Block.GRASS;
                        } else if (globalBlockY > groundHeight - 4) {
                            block = Block.DIRT;
                        } else {
                            if (World.RANDOM.nextInt(2) == 0) {
                                block = Block.COBBLESTONE;
                            } else {
                                block = Block.STONE;
                            }
                        }
                    }


                    blocks[x][y][z] = block;

                }
            }
        }

        setShouldRegenerateMesh(true);
        Updater.get().queueMainThreadInstruction(this::updateNeighborChunkMeshes);

    }


    public GenerationPhase getGenerationPhase() {
        return GenerationPhase.values()[generationPhase.get()];
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

        blocks[x][y][z] = block;

        setShouldRegenerateMesh(regenerateMesh, instantly);

        if (shouldRegenerateMesh.get()) {
            updateNeighborChunkMeshesIfBlockIsOnBorder(toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }

    }

    public void setBlock(int x, int y, int z, byte block, boolean regenerateMesh) {

        setBlock(x, y, z, block, regenerateMesh, false);

    }

    private void updateNeighborChunkMeshesIfBlockIsOnBorder(int x, int y, int z) {

        if (isBlockOnChunkBorder(x, y, z)) {

            updateNeighborChunkMeshes();

        }
    }

    private void updateNeighborChunkMeshes() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            throw new ConcurrentModificationException("Tried updating neighboring chunk meshes from thread other than the main thread!");
        }


        for (Chunk chunk : getNeighborChunks()) {
            if (chunk == null) {
                continue;
            }

            chunk.shouldRegenerateMesh.set(true);
            chunk.shouldInstantlyGenerateMesh.set(false);
        }
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

        return blocks[x][y][z];
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

        return blocks[x][y][z];
    }


    public int getBlockState(int x, int y, int z) {

        if (!isBlockInBounds(x, y, z)) {
            return WORLD.getBlockState(x, y, z);
        }

        return blocks[toLocalX(x)][toLocalY(y)][toLocalZ(z)];
    }

    public int getBlockStateLocal(int x, int y, int z) {

        if (!isBlockInBoundsLocal(x, y, z)) {
            return WORLD.getBlockState(toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }

        return blocks[x][y][z];
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

    private void generateMesh() {
        generateMesh(true);
    }

    private void generateMesh(boolean isDelayed) {

        shouldRegenerateMesh.set(false);


        CHUNK_MESH.generateMesh(blocks, isDelayed);
        TRANSPARENT_CHUNK_MESH.generateMesh(blocks, isDelayed);

    }

    public void clean() {
        shouldRegenerateMesh.set(false);

        Renderer.get().removeRenderable(CHUNK_MESH);
        Renderer.get().removeRenderable(TRANSPARENT_CHUNK_MESH);

        CHUNK_MESH.destroy();
        TRANSPARENT_CHUNK_MESH.destroy();

    }

    public void setShouldRegenerateMesh(boolean value, boolean instantly) {
        shouldInstantlyGenerateMesh.set(instantly);
        shouldRegenerateMesh.set(value);
    }

    public void setShouldRegenerateMesh(boolean value) {
        shouldInstantlyGenerateMesh.set(false);
        shouldRegenerateMesh.set(value);
    }



    public ChunkPosition getChunkPosition() {
        return World.get().getChunkPositionFromBlockPosition(getX(), getY(), getZ());
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

    private void initializeBlocks() {

        blocks = new byte[SIZE_X][SIZE_Y][SIZE_Z];

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = 0; y < SIZE_Y; y++) {

                    blocks[x][y][z] = Block.AIR;

                }
            }
        }

    }

    public ChunkMesh getChunkMesh() {
        return CHUNK_MESH;
    }

    public ChunkMesh getTransparentChunkMesh() {
        return TRANSPARENT_CHUNK_MESH;
    }



    @Override
    public void update(double deltaTime) {

        if (shouldRegenerateMesh.get()) {

            if (!CHUNK_MESH.isGenerating() && !TRANSPARENT_CHUNK_MESH.isGenerating()) {

                shouldRegenerateMesh.set(false);

                CHUNK_MESH.setGenerating(true);
                TRANSPARENT_CHUNK_MESH.setGenerating(true);

                boolean instantly = shouldInstantlyGenerateMesh.get();
                shouldInstantlyGenerateMesh.set(false);

                chunkMeshGenerationExecutorService.submit(() -> {

                    generateMesh(instantly);

                });
            }
        }


        //<editor-fold desc="create chunk meshes">
        // Moving this block to fixedUpdate would stop the block outline from looking delayed when placing/breaking
        // a block; however, it also destabilizes FPS
        if (CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
            CHUNK_MESH.createOpenGLMeshData();
        }

        if (TRANSPARENT_CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
            TRANSPARENT_CHUNK_MESH.createOpenGLMeshData();
        }
        //</editor-fold>
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

    public boolean isFinishedGenerating() {
        return getGenerationPhase() == GenerationPhase.GENERATED;
    }

    @Override
    public void fixedUpdate() {

    }

    @Override
    public void registeredFixedUpdateListener() {

        Renderer.get().addRenderable(CHUNK_MESH);
        Renderer.get().addRenderable(TRANSPARENT_CHUNK_MESH);

    }

    @Override
    public void unregisteredFixedUpdateListener() {

        clean();
    }

    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }

}
