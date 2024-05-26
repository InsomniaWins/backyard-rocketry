package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.Mesh;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.FancyToString;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ChunkMesh extends Mesh {

    private Chunk chunk;
    public boolean unloaded = false;


    int meshDataIndexCount = -1;
    float[] meshDataVertexArray = new float[0];
    int[] meshDataIndexArray = new int[0];


    private boolean readyToCreateOpenGLMeshData = false;

    public boolean isReadyToCreateOpenGLMeshData() {
        return readyToCreateOpenGLMeshData;
    }

    public void createOpenGLMeshData() {

        synchronized (this) {
            indexCount = meshDataIndexCount;

            vao = glGenVertexArrays();
            vbo = glGenBuffers();
            ebo = glGenBuffers();

            glBindVertexArray(vao);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, meshDataVertexArray, GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, meshDataIndexArray, GL_STATIC_DRAW);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);


            readyToCreateOpenGLMeshData = false;
        }
    }

    public ChunkMesh(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
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

    @Override
    public void render() {
        if (vao < 0) {
            return;
        }

        Camera camera = Renderer.get().getCamera();

        // render distance culling
        //if (chunk.getPosition().distance(camera.getTransform().getPosition().get(new Vector3f())) > camera.getRenderDistance()) return;

        // frustum culling
        FrustumIntersection frustum = camera.getFrustum();
        Vector3f boundingBoxMin = chunk.getBoundingBox().getMin().get(new Vector3f());
        Vector3f boundingBoxMax = chunk.getBoundingBox().getMax().get(new Vector3f());
        if (!frustum.testAab(boundingBoxMin, boundingBoxMax)) return;


        Renderer.get().getModelMatrix().identity().translate(chunk.getPosition());
        Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    // MAKE SURE TO CLEAN BEFORE RUNNING!
    public void generateMesh() {

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();


        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    if (chunk.getBlock(x,y,z) != Block.AIR) {

                        BlockModelData blockModelData = BlockModelData.getBlockModelFromBlockState(chunk.getBlockState(x,y,z));

                        for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

                            String faceName = faceEntry.getKey();
                            HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();
                            ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
                            ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");

                            if (shouldAddFaceToMesh((String) faceData.get("cullface"), x, y, z)) {
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

        synchronized (this) {
            meshDataVertexArray = vertexArray;
            meshDataIndexArray = indexArray;
            meshDataIndexCount = indexArray.length;
            readyToCreateOpenGLMeshData = true;
        }
    }

    private boolean shouldAddFaceToMesh(String cullface, int x, int y, int z) {

        int topNeighbor = chunk.getBlock(x, y+1, z);
        int bottomNeighbor = chunk.getBlock(x, y-1, z);
        int leftNeighbor = chunk.getBlock(x-1, y, z);
        int rightNeighbor = chunk.getBlock(x+1, y, z);
        int backNeighbor = chunk.getBlock(x, y, z-1);
        int frontNeighbor = chunk.getBlock(x, y, z+1);

        switch (cullface) {
            case "top" -> {
                if (topNeighbor == Block.AIR) {
                    return true;
                }
            }
            case "bottom" -> {
                if (bottomNeighbor == Block.AIR) {
                    return true;
                }
            }
            case "left" -> {
                if (leftNeighbor == Block.AIR) {
                    return true;
                }
            }
            case "right" -> {
                if (rightNeighbor == Block.AIR) {
                    return true;
                }
            }
            case "front" -> {
                if (frontNeighbor == Block.AIR) {
                    return true;
                }
            }
            case "back" -> {
                if (backNeighbor == Block.AIR) {
                    return true;
                }
            }

            default -> {
                return true;
            }
        }
        return false;
    }
}