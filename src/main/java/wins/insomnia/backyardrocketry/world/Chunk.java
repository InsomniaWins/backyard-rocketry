package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Chunk implements IFixedUpdateListener, IUpdateListener {

    public enum GenerationPhase {
        UNLOADED,
        FILLING,
        LAZY_WAITING_FOR_DECORATION,
        DECORATING,
        GENERATED
    }

    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 32;
    public static  final int SIZE_Z = 32;
    public static final ExecutorService chunkMeshGenerationExecutorService = Executors.newFixedThreadPool(10);
    private final int X;
    private final int Y;
    private final int Z;
    private final int RANDOM_TICK_AMOUNT = 18;
    private final ChunkMesh CHUNK_MESH;
    private final ChunkMesh TRANSPARENT_CHUNK_MESH;
    private final World WORLD;

    private GenerationPhase generationPhase = GenerationPhase.UNLOADED;
    private boolean shouldProcess = false;
    private int[][][] blocks;
    protected boolean shouldRegenerateMesh = false;



    public Chunk(World world, int x, int y, int z) {

        X = x;
        Y = y;
        Z = z;

        BOUNDING_BOX = new BoundingBox(
                x, y, z,
                x + SIZE_X, y + SIZE_Y, z + SIZE_Z
        );

        WORLD = world;
        CHUNK_MESH = new ChunkMesh(this, false);
        TRANSPARENT_CHUNK_MESH = new ChunkMesh(this, true);


        initializeBlocks();


        Renderer.get().addRenderable(CHUNK_MESH);
        Renderer.get().addRenderable(TRANSPARENT_CHUNK_MESH);

        generateBlocks();

        Updater.get().registerFixedUpdateListener(this);
        Updater.get().registerUpdateListener(this);
    }



    public GenerationPhase getGenerationPhase() {
        return generationPhase;
    }



    public void setBlock(int x, int y, int z, int block) {
        setBlock(x, y, z, block, true);

    }



    public void setBlock(int x, int y, int z, int block, boolean regenerateMesh) {

        int blockState = BitHelper.getBlockStateWithoutPropertiesFromBlockId(block);
        BlockProperties blockProperties = Block.getBlockPropertiesFromBlockState(blockState);

        if (blockProperties != null) {
            blockState = blockProperties.onPlace(blockState, this, toGlobalX(x), toGlobalY(y), toGlobalZ(z));
        }

        blocks[x][y][z] = blockState;

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            synchronized (this) {
                shouldRegenerateMesh = regenerateMesh;
                if (shouldRegenerateMesh) {
                    updateNeighborChunkMeshesIfBlockIsOnBorder(x, y, z);
                }
            }
        } else {
            shouldRegenerateMesh = regenerateMesh;
            if (shouldRegenerateMesh) {
                updateNeighborChunkMeshesIfBlockIsOnBorder(x, y, z);
            }
        }

    }

    private void updateNeighborChunkMeshesIfBlockIsOnBorder(int x, int y, int z) {

        if (isBlockOnChunkBorder(x, y, z)) {
            for (Chunk chunk : getNeighborChunks()) {
                if (chunk == null) continue;

                chunk.shouldRegenerateMesh = true;
            }
        }
    }

    public void setBlockState(int x, int y, int z, int blockState) {
        setBlockState(x, y, z, blockState, true);
    }

    public void setBlockState(int x, int y, int z, int blockState, boolean regenerateMesh) {
        blocks[x][y][z] = blockState;
        if (Thread.currentThread() != Main.MAIN_THREAD) {
            synchronized (this) {
                shouldRegenerateMesh = regenerateMesh;
                if (shouldRegenerateMesh) {
                    updateNeighborChunkMeshesIfBlockIsOnBorder(x, y, z);
                }
            }
        } else {
            shouldRegenerateMesh = regenerateMesh;
            if (shouldRegenerateMesh) {
                updateNeighborChunkMeshesIfBlockIsOnBorder(x, y, z);
            }
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

                    int block = getBlock(x, y, z);

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

    public int getBlock(int x, int y, int z) {

        // if out of chunk boundaries
        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {
            return WORLD.getBlock(x + X, y + Y, z + Z);
        }

        return BitHelper.getBlockIdFromBlockState(blocks[x][y][z]);
    }

    public int getBlockState(int x, int y, int z) {

        if (!isBlockInBounds(x, y, z)) {
            throw new RuntimeException("Block is not in chunk boundaries!");
        }

        return blocks[x][y][z];
    }

    // IN LOCAL SPACE
    public boolean isBlockInBounds(int x, int y, int z) {
        return !((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1));
    }

    public boolean isBlockOnChunkBorder(int x, int y, int z) {

        return (x == 0 || x == SIZE_X -1 || y == 0 || y == SIZE_Y - 1 || z == 0 || z == SIZE_Z - 1);

    }

    private void generateMesh() {

        int[][][] blockData;

        if (Thread.currentThread() == Main.MAIN_THREAD) {
            shouldRegenerateMesh = false;
            blockData = blocks.clone();
        } else {
            synchronized (this) {
                shouldRegenerateMesh = false;
                blockData = blocks.clone();
            }
        }

        CHUNK_MESH.generateMesh(blockData);
        TRANSPARENT_CHUNK_MESH.generateMesh(blockData);

    }

    public void clean() {
        shouldRegenerateMesh = false;
        CHUNK_MESH.unloaded = true;
        TRANSPARENT_CHUNK_MESH.unloaded = true;

        if (!CHUNK_MESH.isClean()) {
            CHUNK_MESH.clean();
        }

        if (!TRANSPARENT_CHUNK_MESH.isClean()) {
            TRANSPARENT_CHUNK_MESH.clean();;
        }

        Renderer.get().removeRenderable(CHUNK_MESH);
        Renderer.get().removeRenderable(TRANSPARENT_CHUNK_MESH);
    }

    public void setShouldRegenerateMesh(boolean value) {
        if (Thread.currentThread() == Main.MAIN_THREAD) {
            shouldRegenerateMesh = value;
            return;
        }

        synchronized (this) {
            shouldRegenerateMesh = value;
        }

    }

    public int getGroundHeight(int globalBlockX, int globalBlockZ) {
        long seed = BackyardRocketry.getInstance().getPlayer().getWorld().getSeed();

        int noiseAmplitude = 3;
        float noiseScale = 0.025f;

        return (int) (10 + noiseAmplitude * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * noiseScale, globalBlockZ * noiseScale) + 1f)) + 16;
    }

    protected void generateBlocks() {

        synchronized (this) {
            generationPhase = GenerationPhase.FILLING;
        }

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    int globalBlockX = x + X;
                    int globalBlockY = y + Y;
                    int globalBlockZ = z + Z;

                    int groundHeight = getGroundHeight(globalBlockX, globalBlockZ);


                    if (globalBlockY > groundHeight) {// || (OpenSimplex2.noise3_ImproveXZ(seed, x * 0.15, y * 0.15, z * 0.15) + 1f) < 1f) {
                        continue;
                    }

                    int block;

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

                    blocks[x][y][z] = BitHelper.getBlockStateWithoutPropertiesFromBlockId(block);
                    BlockProperties blockProperties = Block.getBlockProperties(block);
                    if (blockProperties != null) {
                        blocks[x][y][z] = blockProperties.onPlace(blocks[x][y][z], this, x, y, z);
                    }

                }
            }
        }


        if (Thread.currentThread() == Main.MAIN_THREAD) {
            generationPhase = GenerationPhase.LAZY_WAITING_FOR_DECORATION;
        } else {
            synchronized (this) {
                generationPhase = GenerationPhase.LAZY_WAITING_FOR_DECORATION;
            }
        }

    }

    protected void decorate() {
        performDecorationPhase();
    }

    private void performDecorationPhase() {

        synchronized (this) {
            if (generationPhase == GenerationPhase.GENERATED || generationPhase == GenerationPhase.DECORATING) {
                return;
            }

            generationPhase = GenerationPhase.DECORATING;
        }

        // generate trees

        int treeCount = 10;
        for (int i = 0; i < treeCount; i++) {

            int treeX = X + World.RANDOM.nextInt(16);
            int treeZ = Z + World.RANDOM.nextInt(16);
            int treeTrunkY = getGroundHeight(treeX, treeZ) + 1;

            if (isBlockInBounds(toLocalX(treeX), toLocalY(treeTrunkY), toLocalZ(treeZ))) {

                placeDecoration(treeX, treeTrunkY, treeZ, WorldDecorations.TREE);

            }

        }

        synchronized (this) {
            shouldRegenerateMesh = true;
        }
    }

    public ChunkPosition getChunkPosition() {
        return World.get().getChunkPositionFromBlockPosition(getX(), getY(), getZ());
    }

    private void placeDecoration(int globalX, int globalY, int globalZ, int[][] decoration) {

        // prepare neighboring chunks for decoration placing
        BoundingBox decorationBoundingBox = new BoundingBox();

        List<ChunkPosition> chunkPositionsTouchingDecoration = World.getChunkPositionsTouchingBoundingBox(decorationBoundingBox, true);


        for (ChunkPosition chunkPosition : chunkPositionsTouchingDecoration) {

            if (chunkPosition != getChunkPosition()) {

                Chunk chunk = WORLD.getChunkAt(chunkPosition);

                synchronized (this) {
                    if (chunk == null && !WORLD.LOAD_CHUNK_QUEUE.contains(chunkPosition)) {
                        chunk = WORLD.loadChunk(chunkPosition, false, true);
                    }
                }

                // if chunk is generating in another thread, wait until finished
                // also wait until chunk can be decorated / added to

                while (chunk == null) {

                    chunk = WORLD.getChunkAt(chunkPosition);



                }

                while (WORLD.LOAD_CHUNK_QUEUE.contains(chunkPosition) &&
                        (chunk.getGenerationPhase() != GenerationPhase.LAZY_WAITING_FOR_DECORATION ||
                                chunk.getGenerationPhase() != GenerationPhase.GENERATED)) {

                    // wait for chunk to be loaded and to be either ready for decorating or finished generating
                }
            }
        }

        // safe to place blocks now

        ArrayList<Integer[]> blocksToPlaceOutOfChunkBoundaries = new ArrayList<>();

		for (int[] blockData : decoration) {

			int x = blockData[0];
			int y = blockData[1];
			int z = blockData[2];
			int blockId = blockData[3];

			int globalBlockX = globalX + x;
			int globalBlockY = globalY + y;
			int globalBlockZ = globalZ + z;

			int localBlockX = toLocalX(globalBlockX);
			int localBlockY = toLocalY(globalBlockY);
			int localBlockZ = toLocalZ(globalBlockZ);

			if (isBlockInBounds(localBlockX, localBlockY, localBlockZ)) {
				setBlock(localBlockX, localBlockY, localBlockZ, blockId, false);
			} else {
				blocksToPlaceOutOfChunkBoundaries.add(new Integer[]{
                        globalBlockX, globalBlockY, globalBlockZ, blockId
				});
			}

		}

        synchronized (this) {
            WORLD.setBlocks(blocksToPlaceOutOfChunkBoundaries);
        }
    }

    public boolean isDecorating() {
        return getGenerationPhase() == GenerationPhase.DECORATING;
    }

    public boolean isWaitingForDecoration() {
        return getGenerationPhase() == GenerationPhase.LAZY_WAITING_FOR_DECORATION;
    }

    public boolean isWaitingForGeneration() {
        return getGenerationPhase() == GenerationPhase.UNLOADED;
    }

    public boolean isFilling() {
        return getGenerationPhase() == GenerationPhase.FILLING;
    }

    public boolean isGenerated() {
        return getGenerationPhase() == GenerationPhase.GENERATED;
    }



    public Chunk[] getNeighborChunks() {
        return new Chunk[] {
                WORLD.getChunkAt(X-SIZE_X, Y, Z),
                WORLD.getChunkAt(X+SIZE_X, Y, Z),
                WORLD.getChunkAt(X, Y-SIZE_Y, Z),
                WORLD.getChunkAt(X, Y+SIZE_Y, Z),
                WORLD.getChunkAt(X, Y, Z-SIZE_Z),
                WORLD.getChunkAt(X, Y, Z+SIZE_Z)
        };
    }

    private void initializeBlocks() {

        blocks = new int[SIZE_X][SIZE_Y][SIZE_Z];

        int airBlockState = BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.AIR);

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = 0; y < SIZE_Y; y++) {

                    blocks[x][y][z] = airBlockState;

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

        if (shouldRegenerateMesh && !CHUNK_MESH.isGenerating() && !TRANSPARENT_CHUNK_MESH.isGenerating()) {

            shouldRegenerateMesh = false;

            CHUNK_MESH.setGenerating(true);
            TRANSPARENT_CHUNK_MESH.setGenerating(true);

            Future futureTask = chunkMeshGenerationExecutorService.submit(this::generateMesh);
        }


        // Moving this block to fixedUpdate would stop the block outline from looking delayed when placing/breaking
        // a block; however, it also destabilizes FPS
        if (CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
            CHUNK_MESH.createOpenGLMeshData();
        }

        if (TRANSPARENT_CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
            TRANSPARENT_CHUNK_MESH.createOpenGLMeshData();
        }
        // END OF BLOCK
    }

    public boolean isProcessing() {
        return shouldProcess;
    }

    public void setShouldProcess(boolean value) {
        shouldProcess = value;
    }

    @Override
    public void fixedUpdate() {

        if (isProcessing()) {
            // random block ticks
            for (int updateIndex = 0; updateIndex < RANDOM_TICK_AMOUNT; updateIndex++) {
                int x = World.RANDOM.nextInt(SIZE_X);
                int y = World.RANDOM.nextInt(SIZE_Y);
                int z = World.RANDOM.nextInt(SIZE_Z);

                int blockState = blocks[x][y][z];

                BlockProperties blockProperties = Block.getBlockPropertiesFromBlockState(blockState);
                blockProperties.onRandomTick(blockState, this, x, y, z);
            }
        }

    }

    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }

}
