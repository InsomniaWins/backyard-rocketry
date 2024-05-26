package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;
import wins.insomnia.backyardrocketry.util.Updater;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Chunk implements IFixedUpdateListener {

    private final BoundingBox BOUNDING_BOX;
    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 16;
    public static  final int SIZE_Z = 16;
    private static final ExecutorService chunkMeshGenerationExecutorService = Executors.newFixedThreadPool(10);

    private final int X;
    private final int Y;
    private final int Z;

    private final ChunkMesh CHUNK_MESH;
    private final World WORLD;

    private BlockState[][][] blocks;
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
        CHUNK_MESH = new ChunkMesh(this);

        initializeBlocks();


        Renderer.get().addRenderable(CHUNK_MESH);


        generateBlocks();

        Updater.get().registerFixedUpdateListener(this);
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

        return blocks[x][y][z].getBlock();
    }

    public BlockState getBlockState(int x, int y, int z) {

        if (!isBlockInBounds(x, y, z)) {
            return null;
        }

        return blocks[x][y][z];
    }

    public boolean isBlockInBounds(int x, int y, int z) {
        return !((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1));
    }

    public boolean isBlockOnChunkBorder(int x, int y, int z) {

        return (x == 0 || x == SIZE_X -1 || y == 0 || y == SIZE_Y - 1 || z == 0 || z == SIZE_Z - 1);

    }


    // MAKE SURE TO clean() BEFORE RUNNING!
    private void generateMesh() {

        CHUNK_MESH.generateMesh();

        synchronized (this) {
            shouldRegenerateMesh = false;
        }

    }

    public void clean() {
        shouldRegenerateMesh = false;
        CHUNK_MESH.unloaded = true;

        if (!CHUNK_MESH.isClean()) {
            CHUNK_MESH.clean();
        }

        Renderer.get().removeRenderable(CHUNK_MESH);
    }

    public void setShouldRegenerateMesh(boolean value) {

        shouldRegenerateMesh = value;

    }

    private void generateBlocks() {

        long seed = BackyardRocketry.getInstance().getPlayer().getWorld().getSeed();

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    int globalBlockX = x + X;
                    int globalBlockY = y + Y;
                    int globalBlockZ = z + Z;

                    int groundHeight = (int) (10 + 2 * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * 0.025, globalBlockZ * 0.025) + 1f)) + 16;


                    if (globalBlockY > groundHeight) {// || (OpenSimplex2.noise3_ImproveXZ(seed, x * 0.15, y * 0.15, z * 0.15) + 1f) < 1f) {
                        continue;
                    }

                    if (globalBlockY == groundHeight) {
                        blocks[x][y][z].setBlock(Block.GRASS, false);
                    } else if (globalBlockY > groundHeight - 4) {
                        blocks[x][y][z].setBlock(Block.DIRT, false);
                    } else {
                        if (World.RANDOM.nextInt(2) == 0) {
                            blocks[x][y][z].setBlock(Block.COBBLESTONE, false);
                        } else {
                            blocks[x][y][z].setBlock(Block.STONE, false);
                        }
                    }

                }
            }
        }

        shouldRegenerateMesh = true;


        for (Chunk chunk : getNeighborChunks()) {
            if (chunk == null) continue;
            chunk.shouldRegenerateMesh = true;
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

        blocks = new BlockState[SIZE_X][SIZE_Y][SIZE_Z];

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = 0; y < SIZE_Y; y++) {

                    blocks[x][y][z] = new BlockState(this, Block.AIR, x, y, z);

                }
            }
        }

    }

    public ChunkMesh getChunkMesh() {
        return CHUNK_MESH;
    }


    @Override
    public void fixedUpdate() {

        synchronized (this) {
            if (shouldRegenerateMesh) {

                shouldRegenerateMesh = false;

                if (!CHUNK_MESH.isClean()) {
                    CHUNK_MESH.clean();
                }

                chunkMeshGenerationExecutorService.execute(() -> {
                    generateMesh();
                });
            }

            if (CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
                CHUNK_MESH.createOpenGLMeshData();
            }
        }
    }

    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }

}
