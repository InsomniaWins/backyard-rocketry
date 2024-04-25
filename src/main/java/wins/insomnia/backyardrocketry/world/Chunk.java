package wins.insomnia.backyardrocketry.world;

import org.joml.Random;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.IFixedUpdateListener;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Chunk implements IFixedUpdateListener {

    private final int SIZE_X = 16;
    private final int SIZE_Y = 16;
    private final int SIZE_Z = 16;

    private final int X;
    private final int Y;
    private final int Z;

    private final ChunkMesh CHUNK_MESH;


    private BlockState[][][] blocks;

    public Chunk(int x, int y, int z) {

        X = x;
        Y = y;
        Z = z;

        CHUNK_MESH = new ChunkMesh(this);
        BackyardRocketry.getInstance().getRenderer().addRenderable(CHUNK_MESH);

        initializeBlocks();

        generateBlocks();

        generateMesh();

        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);
    }

    public int getBlock(int x, int y, int z) {

        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {
            return -1;
        }

        return blocks[x][y][z].getBlock();
    }

    public BlockState getBlockState(int x, int y, int z) {

        if ((x < 0 || x > SIZE_X - 1) || (y < 0 || y > SIZE_Y - 1) || (z < 0 || z > SIZE_Z - 1)) {
            return null;
        }

        return blocks[x][y][z];
    }


    private void generateMesh() {

        CHUNK_MESH.generateMesh();

    }

    private void generateBlocks() {

        Random random = new Random();

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    if (y == 15) {
                        blocks[x][y][z].setBlock(Block.GRASS);
                    } else if (y > 10) {
                        blocks[x][y][z].setBlock(Block.DIRT);
                    } else {
                        blocks[x][y][z].setBlock(Block.COBBLESTONE);
                    }

                }
            }
        }

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

        if (BackyardRocketry.getInstance().getKeyboardInput().isKeyJustPressed(GLFW_KEY_R)) {

            if (!CHUNK_MESH.isClean()) {
                CHUNK_MESH.clean();
            }

            generateBlocks();
            generateMesh();

        }

    }
}
