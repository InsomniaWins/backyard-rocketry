package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import org.joml.Math;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.util.OpenGLWrapper;
import wins.insomnia.backyardrocketry.util.debug.DebugTime;
import wins.insomnia.backyardrocketry.util.update.DelayedMainThreadInstruction;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesh extends Mesh implements IPositionOwner {

    private Chunk chunk;
    public boolean unloaded = false;

    int meshDataIndexCount = -1;
    float[] meshDataVertexArray = new float[0];
    int[] meshDataIndexArray = new int[0];
    private AtomicBoolean generating = new AtomicBoolean(false);
    private boolean meshIsReadyToRender = false;
    private AtomicBoolean readyToCreateOpenGLMeshData = new AtomicBoolean(false);
    private final boolean isTransparent;

    public boolean isReadyToCreateOpenGLMeshData() {
        return readyToCreateOpenGLMeshData.get();
    }

    @Override
    public void clean() {
        if (Thread.currentThread() != Main.MAIN_THREAD) {
            new ConcurrentModificationException("Tried deleting OpenGL mesh data on thread other than main thread!").printStackTrace();
        }

        super.clean();
    }

    protected void destroy() {
        unloaded = true;
        if (!isClean()) clean();
        chunk = null;
    }

    public void createOpenGLMeshData() {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            new ConcurrentModificationException("Tried creating OpenGL mesh data on thread other than main thread!").printStackTrace();
        }

        clean();
        
        if (unloaded) {
            return;
        }

        meshIsReadyToRender = false;

        indexCount = meshDataIndexCount;

        vao = OpenGLWrapper.glGenVertexArrays();

        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, meshDataVertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, meshDataIndexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 5 * Float.BYTES);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8 * Float.BYTES);


        readyToCreateOpenGLMeshData.set(false);
        isClean.set(false);
        meshIsReadyToRender = true;
        setGenerating(false);
    }

    public ChunkMesh(Chunk chunk, boolean isTransparent) {
        this.chunk = chunk;
        this.isTransparent = isTransparent;
    }

    public Chunk getChunk() {
        return chunk;
    }




    @Override
    public boolean shouldRender() {
        if (!super.shouldRender()) {
            return false;
        }


        if (vao < 0 || !meshIsReadyToRender || indexCount == 0) {
            return false;
        }

        Camera camera = Renderer.get().getCamera();

        /*


        // render distance culling
        if (chunk.getPosition().distance(camera.getTransform().getPosition().get(new Vector3f())) > camera.getRenderDistance()) return false;


        */

        // frustum culling
        FrustumIntersection frustum = camera.getFrustum();
        Vector3f boundingBoxMin = chunk.getBoundingBox().getMin().get(new Vector3f());
        Vector3f boundingBoxMax = chunk.getBoundingBox().getMax().get(new Vector3f());
        return frustum.testAab(boundingBoxMin, boundingBoxMax);

    }

    public void setGenerating(boolean value) {
        generating.set(value);
    }

    public boolean isGenerating() {
        return generating.get();
    }

    @Override
    public void render() {

        ShaderProgram chunkMeshShaderProgram = Renderer.get().getShaderProgram("chunk_mesh");
        chunkMeshShaderProgram.use();

        Renderer.get().getModelMatrix().identity().translate(chunk.getPosition());
        chunkMeshShaderProgram.setUniform("vs_modelMatrix",Renderer.get().getModelMatrix());
        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);


        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
    }

    public void generateMesh(byte[][][] blocks) {
        generateMesh(blocks, true);
    }

    public void generateMesh(byte[][][] blocks, boolean isDelayed) {

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        byte[][][] blockNeighbors = new byte[9][9][9];

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {



                    byte block = blocks[x][y][z];
                    if (block == Block.AIR) continue;

                    if (isTransparent != Block.isBlockTransparent(block)) continue;



                    blockNeighbors[0][0][0] = chunk.getBlock(x-1, y-1, z-1);
                    blockNeighbors[1][0][0] = chunk.getBlock(x, y-1, z-1);
                    blockNeighbors[2][0][0] = chunk.getBlock(x+1, y-1, z-1);
                    blockNeighbors[0][1][0] = chunk.getBlock(x-1, y, z-1);
                    blockNeighbors[1][1][0] = chunk.getBlock(x, y, z-1);
                    blockNeighbors[2][1][0] = chunk.getBlock(x+1, y, z-1);
                    blockNeighbors[0][2][0] = chunk.getBlock(x-1, y+1, z-1);
                    blockNeighbors[1][2][0] = chunk.getBlock(x, y+1, z-1);
                    blockNeighbors[2][2][0] = chunk.getBlock(x+1, y+1, z-1);

                    blockNeighbors[0][0][1] = chunk.getBlock(x-1, y-1, z);
                    blockNeighbors[1][0][1] = chunk.getBlock(x, y-1, z);
                    blockNeighbors[2][0][1] = chunk.getBlock(x+1, y-1, z);
                    blockNeighbors[0][1][1] = chunk.getBlock(x-1, y, z);
                    blockNeighbors[1][1][1] = block;
                    blockNeighbors[2][1][1] = chunk.getBlock(x+1, y, z);
                    blockNeighbors[0][2][1] = chunk.getBlock(x-1, y+1, z);
                    blockNeighbors[1][2][1] = chunk.getBlock(x, y+1, z);
                    blockNeighbors[2][2][1] = chunk.getBlock(x+1, y+1, z);

                    blockNeighbors[0][0][2] = chunk.getBlock(x-1, y-1, z+1);
                    blockNeighbors[1][0][2] = chunk.getBlock(x, y-1, z+1);
                    blockNeighbors[2][0][2] = chunk.getBlock(x+1, y-1, z+1);
                    blockNeighbors[0][1][2] = chunk.getBlock(x-1, y, z+1);
                    blockNeighbors[1][1][2] = chunk.getBlock(x, y, z+1);
                    blockNeighbors[2][1][2] = chunk.getBlock(x+1, y, z+1);
                    blockNeighbors[0][2][2] = chunk.getBlock(x-1, y+1, z+1);
                    blockNeighbors[1][2][2] = chunk.getBlock(x, y+1, z+1);
                    blockNeighbors[2][2][2] = chunk.getBlock(x+1, y+1, z+1);


                    BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(block, x, y, z);


                    for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

                        HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();
                        ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
                        ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");
                        ArrayList<Double> faceNormalArray = (ArrayList<Double>) faceData.get("normal");

                        String cullface = (String) faceData.get("cullface");

                        if (shouldAddFaceToMesh(cullface, block, blockNeighbors)) {

                            addFace(
                                    true,
                                    vertices, indices,
                                    faceVertexArray, faceIndexArray,
                                    faceNormalArray,
                                    x, y, z,
                                    blockNeighbors
                            );

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

        if (Main.MAIN_THREAD != Thread.currentThread()){
            if (isDelayed) {
                Updater.get().queueDelayedMainThreadInstruction(new DelayedMainThreadInstruction(() -> {
                    meshDataVertexArray = vertexArray;
                    meshDataIndexArray = indexArray;
                    meshDataIndexCount = indexArray.length;
                    readyToCreateOpenGLMeshData.set(true);
                    isClean.set(false);
                }));
            } else {
                Updater.get().queueMainThreadInstruction(() -> {
                    meshDataVertexArray = vertexArray;
                    meshDataIndexArray = indexArray;
                    meshDataIndexCount = indexArray.length;
                    readyToCreateOpenGLMeshData.set(true);
                    isClean.set(false);
                });
            }
        } else {
            meshDataVertexArray = vertexArray;
            meshDataIndexArray = indexArray;
            meshDataIndexCount = indexArray.length;
            readyToCreateOpenGLMeshData.set(true);
            isClean.set(false);
        }

    }

    private boolean shouldAddFaceToMesh(String cullface, byte block, byte[][][] blockNeighbors) {

        byte posYNeighbor = blockNeighbors[1][2][1];
        byte negYNeighbor = blockNeighbors[1][0][1];
        byte negXNeighbor = blockNeighbors[0][1][1];
        byte posXNeighbor = blockNeighbors[2][1][1];
        byte negZNeighbor = blockNeighbors[1][1][0];
        byte posZNeighbor = blockNeighbors[1][1][2];



        if (cullface == null) return true;



        switch (cullface) {
            case "top" -> {
                if (posYNeighbor == Block.WORLD_BORDER || posYNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && posYNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(posYNeighbor)) {
                    return true;
                }
            }
            case "bottom" -> {

                if (negYNeighbor == Block.WORLD_BORDER || negYNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && negYNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(negYNeighbor)) {
                    return true;
                }
            }
            case "left" -> {
                if (negXNeighbor == Block.WORLD_BORDER || negXNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && negXNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(negXNeighbor)) {
                    return true;
                }
            }
            case "right" -> {
                if (posXNeighbor == Block.WORLD_BORDER || posXNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && posXNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(posXNeighbor)) {
                    return true;
                }
            }
            case "front" -> {
                if (posZNeighbor == Block.WORLD_BORDER || posZNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && posZNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(posZNeighbor)) {
                    return true;
                }
            }
            case "back" -> {
                if (negZNeighbor == Block.WORLD_BORDER || negZNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && negZNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(negZNeighbor)) {
                    return true;
                }
            }

            default -> {
                return true;
            }
        }
        return false;
    }

    private boolean vectorArrayEqualTo(float[] normalArray, float x, float y, float z) {
        return normalArray[0] == x && normalArray[1] == y && normalArray[2] == z;
    }

    private float calculateVertexAoValue(boolean side1, boolean corner, boolean side2) {
        if (side1 && side2) return 0.2f;
        if ((side1 && corner && !side2) || (!side1 && corner && side2)) return 0.375f;
        if (!side1 && !side2 && !corner) return 1f;

        return 0.75f;
    }

    private float getVertexAo(float[] vertexPosition, byte[][][] blockNeighbors, float[] normal) {

        float returnValue = 1f;

        // negative x face
        if (vectorArrayEqualTo(normal, -1f, 0f, 0f)) {
            byte[] neighbors = new byte[] {
                    blockNeighbors[0][1][2], blockNeighbors[0][0][2], blockNeighbors[0][0][1],
                    blockNeighbors[0][0][0], blockNeighbors[0][1][0],
                    blockNeighbors[0][2][0], blockNeighbors[0][2][1], blockNeighbors[0][2][2],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };

            if (vertexPosition[1] == 0f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[1] == 0f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[1] == 1f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[1] == 1f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }
        }

        // positive x face
        else if (vectorArrayEqualTo(normal, 1f, 0f, 0f)) {
            byte[] neighbors = new byte[] {
                    blockNeighbors[2][1][0], blockNeighbors[2][0][0], blockNeighbors[2][0][1],
                    blockNeighbors[2][0][2], blockNeighbors[2][1][2],
                    blockNeighbors[2][2][2], blockNeighbors[2][2][1], blockNeighbors[2][2][0],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };

            if (vertexPosition[1] == 0f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[1] == 0f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[1] == 1f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[1] == 1f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }

        }

        // negative y face
        else if (vectorArrayEqualTo(normal, 0f, -1f, 0f)) {

            byte[] neighbors = new byte[] {
                    blockNeighbors[1][0][0], blockNeighbors[0][0][0], blockNeighbors[0][0][1],
                    blockNeighbors[0][0][2], blockNeighbors[1][0][2],
                    blockNeighbors[2][0][2], blockNeighbors[2][0][1], blockNeighbors[2][0][0],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };

            if (vertexPosition[0] == 0f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[0] == 0f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }
        }

        // positive y face
        else if (vectorArrayEqualTo(normal, 0f, 1f, 0f)) {
            byte[] neighbors = new byte[] {
                    blockNeighbors[1][2][2], blockNeighbors[0][2][2], blockNeighbors[0][2][1],
                    blockNeighbors[0][2][0], blockNeighbors[1][2][0],
                    blockNeighbors[2][2][0], blockNeighbors[2][2][1], blockNeighbors[2][2][2],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };

            if (vertexPosition[0] == 0f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[0] == 0f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[2] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[2] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }
        }

        // negative z face
        else if (vectorArrayEqualTo(normal, 0f, 0f, -1f)) {

            byte[] neighbors = new byte[] {
                    blockNeighbors[0][1][0], blockNeighbors[0][0][0], blockNeighbors[1][0][0],
                    blockNeighbors[2][0][0], blockNeighbors[2][1][0],
                    blockNeighbors[2][2][0], blockNeighbors[1][2][0], blockNeighbors[0][2][0],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };


            if (vertexPosition[0] == 0f && vertexPosition[1] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[1] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[0] == 0f && vertexPosition[1] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[1] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }

        }

        // positive z face
        else if (vectorArrayEqualTo(normal, 0f, 0f, 1f)) {
            byte[] neighbors = new byte[] {
                    blockNeighbors[2][1][2], blockNeighbors[2][0][2], blockNeighbors[1][0][2],
                    blockNeighbors[0][0][2], blockNeighbors[0][1][2],
                    blockNeighbors[0][2][2], blockNeighbors[1][2][2], blockNeighbors[2][2][2],
            };

            boolean[] neighborResults = new boolean[] {
                    !Block.isBlockTransparent(neighbors[0]),
                    !Block.isBlockTransparent(neighbors[1]),
                    !Block.isBlockTransparent(neighbors[2]),
                    !Block.isBlockTransparent(neighbors[3]),
                    !Block.isBlockTransparent(neighbors[4]),
                    !Block.isBlockTransparent(neighbors[5]),
                    !Block.isBlockTransparent(neighbors[6]),
                    !Block.isBlockTransparent(neighbors[7])
            };


            if (vertexPosition[0] == 1f && vertexPosition[1] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[0], neighborResults[1], neighborResults[2]);
            }
            else if (vertexPosition[0] == 0f && vertexPosition[1] == 0f) {
                returnValue = calculateVertexAoValue(neighborResults[2], neighborResults[3], neighborResults[4]);
            }
            else if (vertexPosition[0] == 1f && vertexPosition[1] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[6], neighborResults[7], neighborResults[0]);
            }
            else if (vertexPosition[0] == 0f && vertexPosition[1] == 1f) {
                returnValue = calculateVertexAoValue(neighborResults[4], neighborResults[5], neighborResults[6]);
            }
        }

        return returnValue;

    }

    public void addFace(boolean ambientOcclusion, ArrayList<Float> vertices, ArrayList<Integer> indices, ArrayList<Double> faceVertexArray, ArrayList<Integer> faceIndexArray, ArrayList<Double> faceNormalsArray, int offX, int offY, int offZ, byte[][][] blockNeighbors) {

        try {
            int indexOffset = vertices.size() / 9;

            for (int faceIndex : faceIndexArray) {
                indices.add(faceIndex + indexOffset);
            }

            float[] vertexPosition = new float[3];

            for (int i = 0; i < faceVertexArray.size(); i++) {

                int vertexDataIndex = i % 5;
                float vertexData = faceVertexArray.get(i).floatValue();

                if (vertexDataIndex == 0) {
                    vertexPosition[0] = vertexData;
                    vertexData += offX;
                } else if (vertexDataIndex == 1) {
                    vertexPosition[1] = vertexData;
                    vertexData += offY;
                } else if (vertexDataIndex == 2) {
                    vertexPosition[2] = vertexData;
                    vertexData += offZ;
                }

                vertices.add(vertexData);



                // if vertexDataIndex is last bit of vertex data
                if (vertexDataIndex == 4) {

                    float[] normal = new float[3];

                    // if face does not have normals
                    if (faceNormalsArray == null) {
                        // make "up" normal
                        vertices.add(0f);
                        vertices.add(1f);
                        vertices.add(0f);
                        normal[0] = 0f;
                        normal[1] = 1f;
                        normal[2] = 0f;
                    } else {
                        // normals

                        normal[0] = faceNormalsArray.get(0).floatValue();
                        normal[1] = faceNormalsArray.get(1).floatValue();
                        normal[2] = faceNormalsArray.get(2).floatValue();

                        vertices.add(normal[0]);
                        vertices.add(normal[1]);
                        vertices.add(normal[2]);
                    }

                    // ambient occlusion value
                    if (ambientOcclusion && faceNormalsArray != null) {

                        float aoValue = getVertexAo(vertexPosition, blockNeighbors, normal);
                        vertices.add(aoValue);

                    } else {
                        vertices.add(1f);
                    }

                }

            }
        } catch (Exception e) {

            // I know this try-catch is useless. it's just for debugging purposes.

            e.printStackTrace();
            throw e;
        }

    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(chunk.getPosition());
    }

    @Override
    public boolean hasTransparency() {
        return isTransparent;
    }
}