package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.blockproperty.BlockProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chunk implements IFixedUpdateListener, IUpdateListener {

    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 32;
    public static  final int SIZE_Z = 32;
    public static final ExecutorService chunkMeshGenerationExecutorService = Executors.newFixedThreadPool(10);

    private final int X;
    private final int Y;
    private final int Z;
    private final int RANDOM_TICK_AMOUNT = 9;
    private final ChunkMesh CHUNK_MESH;
    private final ChunkMesh TRANSPARENT_CHUNK_MESH;
    private final World WORLD;

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

    private void generateBlocks() {

        long seed = BackyardRocketry.getInstance().getPlayer().getWorld().getSeed();

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    int globalBlockX = x + X;
                    int globalBlockY = y + Y;
                    int globalBlockZ = z + Z;

                    int groundHeight = (int) (10 + 5 * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * 0.025, globalBlockZ * 0.025) + 1f)) + 16;


                    if (globalBlockY > groundHeight) {// || (OpenSimplex2.noise3_ImproveXZ(seed, x * 0.15, y * 0.15, z * 0.15) + 1f) < 1f) {
                        continue;
                    }

                    if (globalBlockY == groundHeight) {
                        blocks[x][y][z] = BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.GRASS);
                    } else if (globalBlockY > groundHeight - 4) {
                        blocks[x][y][z] = BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.DIRT);
                    } else {
                        if (World.RANDOM.nextInt(2) == 0) {
                            blocks[x][y][z] = BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.COBBLESTONE);
                        } else {
                            blocks[x][y][z] = BitHelper.getBlockStateWithoutPropertiesFromBlockId(Block.STONE);
                        }
                    }

                }
            }
        }


        if (Thread.currentThread() == Main.MAIN_THREAD) {
            shouldRegenerateMesh = true;

            for (Chunk chunk : getNeighborChunks()) {
                if (chunk == null) continue;
                chunk.shouldRegenerateMesh = true;
            }
        } else {
            synchronized (this) {

                shouldRegenerateMesh = true;


                for (Chunk chunk : getNeighborChunks()) {
                    if (chunk == null) continue;
                    chunk.shouldRegenerateMesh = true;
                }

            }
        }

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

            chunkMeshGenerationExecutorService.submit(this::generateMesh);
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

    @Override
    public void fixedUpdate() {

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

    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }

}
