package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.render.IRenderable;
import wins.insomnia.backyardrocketry.render.Mesh;

public class Chunk implements IRenderable {

    private final int SIZE_X = 16;
    private final int SIZE_Y = 16;
    private final int SIZE_Z = 16;

    private final int X;
    private final int Y;
    private final int Z;


    private final Mesh CHUNK_MESH;


    private BlockState[][][] blocks;

    public Chunk(int x, int y, int z) {

        X = x;
        Y = y;
        Z = z;

        CHUNK_MESH = new Mesh();

        initializeBlocks();

        generateBlocks();

        generateMesh();

    }

    private void generateMesh() {



    }

    private void generateBlocks() {



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


    @Override
    public void render() {

    }
}
