package wins.insomnia.backyardrocketry.world;

import org.joml.SimplexNoise;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.stb.STBPerlin;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Chunk implements IFixedUpdateListener {

    private final BoundingBox BOUNDING_BOX;
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

        BOUNDING_BOX = new BoundingBox(
                x, y, z,
                x + 16, y + 16, z + 16
        );

        CHUNK_MESH = new ChunkMesh(this);
        Renderer.get().addRenderable(CHUNK_MESH);

        initializeBlocks();

        generateBlocks();

        generateMesh();

        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);
    }

    public List<BoundingBox> getBoundingBoxesOfBlocksPotentiallyCollidingWithBoundingBox(BoundingBox boundingBox) {

        Vector3i minPos = new Vector3i(
                (int) boundingBox.getMin().x-1,
                (int) boundingBox.getMin().y-1,
                (int) boundingBox.getMin().z-1
        );

        Vector3i maxPos = new Vector3i(
                (int) (Math.round(boundingBox.getMax().x)+1),
                (int) (Math.round(boundingBox.getMax().y)+1),
                (int) (Math.round(boundingBox.getMax().z)+1)
        );

        Vector3i localMinPos = new Vector3i(minPos).sub(X, Y, Z);
        Vector3i localMaxPos = new Vector3i(maxPos).sub(X, Y, Z);

        localMinPos.set(-1,-1,-1);
        localMaxPos.set(17, 17, 17);

        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int x = localMinPos.x; x < localMaxPos.x; x++) {
            for (int y = localMinPos.y; y < localMaxPos.y; y++) {
                for (int z = localMinPos.z; z < localMaxPos.z; z++) {

                    int block = getBlock(x, y, z);

                    if (block == -1) continue;

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

    public Vector3f getPosition() {
        return new Vector3f(X,Y,Z);
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

        long seed = World.RANDOM.nextLong();

        for (int y = 0; y < SIZE_Y; y++) {
            for (int x = 0; x < SIZE_X; x++) {
                for (int z = 0; z < SIZE_Z; z++) {

                    /*if ((OpenSimplex2.noise3_ImproveXZ(seed, x * 0.15, y * 0.15, z * 0.15) + 1f) < 1f) {
                        blocks[x][y][z].setBlock(Block.AIR);
                        continue;
                    }*/



                    if (y == 15) {

                        if (x == 3 || z == 3 || x == 7) {
                            blocks[x][y][z].setBlock(Block.GRASS);
                        } else {
                            blocks[x][y][z].setBlock(Block.AIR);
                        }


                    } else if (y > 10) {
                        blocks[x][y][z].setBlock(Block.DIRT);
                    } else {
                        if (World.RANDOM.nextInt(2) == 0) {
                            blocks[x][y][z].setBlock(Block.COBBLESTONE);
                        } else {
                            blocks[x][y][z].setBlock(Block.STONE);
                        }
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

    public BoundingBox getBoundingBox() {
        return BOUNDING_BOX;
    }
}
