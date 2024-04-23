package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.render.Mesh;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public void addFace(ArrayList<Float> vertices, ArrayList<Integer> indices, ArrayList<Double> faceVertexArray, ArrayList<Integer> faceIndexArray, int offX, int offY, int offZ) {

        int indexOffset = vertices.size() / 5;

        for (int faceIndex : faceIndexArray) {
            indices.add(faceIndex + indexOffset);
        }

        for (int i = 0; i < faceVertexArray.size(); i++) {

            int vertexDataIndex = i % 5;
            float vertexData = faceVertexArray.get(i).floatValue();

            if (vertexDataIndex == 0) {
                vertexData += offX;
            } else if (vertexDataIndex == 1) {
                vertexData += offY;
            } else if (vertexDataIndex == 2) {
                vertexData += offZ;
            }

            vertices.add(vertexData);

        }
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



                        BlockModelData blockModelData = BlockModelData.getBlockModelFromBlockState(chunk.getBlockState(x,y,z));
                        for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

                            String faceName = faceEntry.getKey();
                            HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();
                            ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
                            ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");

                            String cullface = (String) faceData.get("cullface");
                            if (cullface.equals("top")) {
                                if (topNeighbor == Block.AIR || topNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else if (cullface.equals("bottom")) {
                                if (bottomNeighbor == Block.AIR || bottomNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else if (cullface.equals("left")) {
                                if (leftNeighbor == Block.AIR || leftNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else if (cullface.equals("right")) {
                                if (rightNeighbor == Block.AIR || rightNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else if (cullface.equals("front")) {
                                if (frontNeighbor == Block.AIR || frontNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else if (cullface.equals("back")) {
                                if (backNeighbor == Block.AIR || backNeighbor == -1) {
                                    addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                                }
                            } else {
                                addFace(vertices, indices, faceVertexArray, faceIndexArray, x, y, z);
                            }

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