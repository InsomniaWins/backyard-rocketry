package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.render.Mesh;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ChunkMesh extends Mesh {

    private Chunk chunk;

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
    }

    public void addFrontFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 3 + indexOffset, 1 + indexOffset,
                1 + indexOffset, 3 + indexOffset, 2 + indexOffset
        ));

        vertices.addAll(List.of(
                0.5f + offX, 0.5f + offY, 0.5f + offZ, 1.0f, 1.0f, // top right front
                0.5f + offX, -0.5f + offY, 0.5f + offZ, 1.0f, 0.0f, // bottom right front
                -0.5f + offX, -0.5f + offY, 0.5f + offZ, 0.0f, 0.0f, // bottom left front
                -0.5f + offX, 0.5f + offY, 0.5f + offZ, 0.0f, 1.0f // top left front
        ));
    }


    public void addBackFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 1 + indexOffset, 3 + indexOffset,
                1 + indexOffset, 2 + indexOffset, 3 + indexOffset
        ));

        vertices.addAll(List.of(
                0.5f + offX,  0.5f + offY, -0.5f + offZ, 0.0f, 1.0f, // top right back
                0.5f + offX, -0.5f + offY, -0.5f + offZ, 0.0f, 0.0f, // bottom right back
                -0.5f + offX, -0.5f + offY, -0.5f + offZ, 1.0f, 0.0f, // bottom left back
                -0.5f + offX,  0.5f + offY, -0.5f + offZ,  1.0f, 1.0f // top left back
        ));
    }

    public void addRightFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 3 + indexOffset, 1 + indexOffset,
                1 + indexOffset, 3 + indexOffset, 2 + indexOffset
        ));

        vertices.addAll(List.of(
                0.5f + offX, 0.5f + offY, 0.5f + offZ, 0.0f, 1.0f, // top right front
                0.5f + offX, 0.5f + offY, -0.5f + offZ, 1.0f, 1.0f, // top right back
                0.5f + offX, -0.5f + offY, -0.5f + offZ, 1.0f, 0.0f, // bottom right back
                0.5f + offX, -0.5f + offY, 0.5f + offZ,  0.0f, 0.0f // bottom right front
        ));
    }

    public void addLeftFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 1 + indexOffset, 3 + indexOffset,
                1 + indexOffset, 2 + indexOffset, 3 + indexOffset
        ));

        vertices.addAll(List.of(
                -0.5f + offX, 0.5f + offY, 0.5f + offZ, 1.0f, 1.0f, // top right front
                -0.5f + offX, 0.5f + offY, -0.5f + offZ, 0.0f, 1.0f, // top right back
                -0.5f + offX, -0.5f + offY, -0.5f + offZ, 0.0f, 0.0f, // bottom right back
                -0.5f + offX, -0.5f + offY, 0.5f + offZ,  1.0f, 0.0f // bottom right front
        ));
    }

    public void addTopFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 1 + indexOffset, 3 + indexOffset,
                1 + indexOffset, 2 + indexOffset, 3 + indexOffset
        ));

        vertices.addAll(List.of(
                0.5f + offX, 0.5f + offY, 0.5f + offZ, 1.0f, 0.0f, // top right front
                0.5f + offX, 0.5f + offY, -0.5f + offZ, 1.0f, 1.0f, // top right back
                -0.5f + offX, 0.5f + offY, -0.5f + offZ, 0.0f, 1.0f, // top left back
                -0.5f + offX, 0.5f + offY, 0.5f + offZ, 0.0f, 0.0f // top left front
        ));
    }

    public void addBottomFace(ArrayList<Float> vertices, ArrayList<Integer> indices, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        indices.addAll(List.of(
                indexOffset, 3 + indexOffset, 1 + indexOffset,
                1 + indexOffset, 3 + indexOffset, 2 + indexOffset
        ));

        vertices.addAll(List.of(
                0.5f + offX, -0.5f + offY, 0.5f + offZ, 1.0f, 1.0f, // top right front
                0.5f + offX, -0.5f + offY, -0.5f + offZ, 1.0f, 0.0f, // top right back
                -0.5f + offX, -0.5f + offY, -0.5f + offZ,  0.0f, 0.0f, // top left back
                -0.5f + offX, -0.5f + offY, 0.5f + offZ,  0.0f, 1.0f // top left front
        ));
    }


    public void generateMesh() {

        if (!isClean()) clean();
        isClean = false;

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();


        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    if (chunk.getBlock(x,y,z) != Block.AIR) {

                        int topNeighbor = chunk.getBlock(x, y+1, z);
                        int bottomNeighbor = chunk.getBlock(x, y-1, z);
                        int leftNeighbor = chunk.getBlock(x-1, y, z);
                        int rightNeighbor = chunk.getBlock(x+1, y, z);
                        int backNeighbor = chunk.getBlock(x, y, z-1);
                        int frontNeighbor = chunk.getBlock(x, y, z+1);


                        if (frontNeighbor == Block.AIR || frontNeighbor == -1) {
                            addFrontFace(vertices, indices, x, y, z);
                        }

                        if (backNeighbor == Block.AIR || backNeighbor == -1) {
                            addBackFace(vertices, indices, x, y, z);
                        }

                        if (rightNeighbor == Block.AIR || rightNeighbor == -1) {
                            addRightFace(vertices, indices, x, y, z);
                        }

                        if (leftNeighbor == Block.AIR || leftNeighbor == -1) {
                            addLeftFace(vertices, indices, x, y, z);
                        }

                        if (bottomNeighbor == Block.AIR || bottomNeighbor == -1) {
                            addBottomFace(vertices, indices, x, y, z);
                        }

                        if (topNeighbor == Block.AIR || topNeighbor == -1) {
                            addTopFace(vertices, indices, x, y, z);
                        }

                    }

                }
            }
        }


        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertexArray.length; i++) {
            vertexArray[i] = vertices.get(i);
        }

        int[] indexArray = new int[indices.size()];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i] = indices.get(i);
        }

        indexCount = indexArray.length;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

    }
}