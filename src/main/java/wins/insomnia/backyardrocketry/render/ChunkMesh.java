package wins.insomnia.backyardrocketry.render;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL30;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.texture.BlockAtlasTexture;
import wins.insomnia.backyardrocketry.util.HelpfulMath;
import wins.insomnia.backyardrocketry.util.update.DelayedMainThreadInstruction;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class ChunkMesh extends Mesh implements IPositionOwner {

    public static final int VERTEX_ATTRIBUTE_FLOAT_AMOUNT = 16;
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

    public void destroy() {
        unloaded = true;
        if (!isClean()) clean();
        chunk = null;
    }

    public void createOpenGLMeshData() throws ConcurrentModificationException {

        if (Thread.currentThread() != Main.MAIN_THREAD) {
            ConcurrentModificationException exception = new ConcurrentModificationException(
                    "Tried creating OpenGL mesh data on thread other than main thread!"
            );
            getChunk().getWorld().logInfo(Arrays.toString(exception.getStackTrace()));
            throw exception;

        }

        clean();
        
        if (unloaded) {
            return;
        }

        meshIsReadyToRender = false;

        indexCount = meshDataIndexCount;

        vao = GL30.glGenVertexArrays();

        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, meshDataVertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, meshDataIndexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 3 * Float.BYTES);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 5 * Float.BYTES);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 8 * Float.BYTES);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 9 * Float.BYTES);
        glVertexAttribPointer(5, 1, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 10 * Float.BYTES);
        glVertexAttribPointer(6, 1, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 11 * Float.BYTES);
        glVertexAttribPointer(7, 4, GL_FLOAT, false, VERTEX_ATTRIBUTE_FLOAT_AMOUNT * Float.BYTES, 12 * Float.BYTES);


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

        Renderer.get().getModelMatrix()
                .identity()
                .translate(chunk.getPosition());
        chunkMeshShaderProgram.setUniform("vs_modelMatrix",Renderer.get().getModelMatrix());
        glBindVertexArray(vao);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glEnableVertexAttribArray(7);

        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glDisableVertexAttribArray(6);
        glDisableVertexAttribArray(7);
    }

    public void generateMesh(byte[][][] blocks, byte[][][] blockStates) {
        generateMesh(blocks, blockStates, true);
    }

    public void generateMesh(byte[][][] blocks, byte[][][] blockStates, boolean isDelayed) {

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        byte[][][] blockNeighbors = new byte[9][9][9];

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    int globalX = getChunk().toGlobalX(x);
                    int globalY = getChunk().toGlobalY(y);
                    int globalZ = getChunk().toGlobalZ(z);

                    byte block = blocks[x][y][z];
                    if (block == Blocks.AIR) continue;

                    if (isTransparent != Blocks.isBlockTransparent(block)) continue;

                    byte blockState = blockStates[x][y][z];

                    BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(block, blockState, x, y, z);

                    if (blockModelData == null) continue;

                    HashMap<String, String> textures = blockModelData.getTextures();


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



                    for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

                        HashMap<String, Object> faceData = (HashMap<String, Object>) faceEntry.getValue();
                        ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
                        ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");

                        String cullface = (String) faceData.get("cullface");
                        float blockWaveStrength = ((Double) faceData.getOrDefault("wave_strength", 0.0)).floatValue();

                        double rotationValue = 0.0;
                        Object possibleRotations = faceData.get("possible_rotations");
                        if (possibleRotations instanceof ArrayList<?> rotationList) {

                            Object rotation = rotationList.get(BlockModelData.getRandomBlockNumberBasedOnBlockPosition(globalX, globalY, globalZ) % rotationList.size());

                            if (rotation instanceof Double) {
                                rotationValue = (Double) rotation;
                            }

                        }


                        String faceTextureName = textures.get((String) faceData.get("texture"));

                        if (shouldAddFaceToMesh(cullface, block, blockNeighbors)) {

                            addFace(
                                    false,
                                    vertices, indices,
                                    faceVertexArray, faceIndexArray,
                                    x, y, z,
                                    blockNeighbors,
                                    BlockAtlasTexture.get().getBlockTexture(faceTextureName),
                                    rotationValue,
                                    blockWaveStrength
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
                if (posYNeighbor == Blocks.WORLD_BORDER || posYNeighbor == Blocks.NULL) {
                    return false;
                }

                if (block == Blocks.WATER) {
                    return (posYNeighbor != Blocks.WATER);
                }

                if (Blocks.shouldHideNeighboringFaces(block) && posYNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(posYNeighbor)) {
                    return true;
                }
            }
            case "bottom" -> {

                if (negYNeighbor == Blocks.WORLD_BORDER || negYNeighbor == Blocks.NULL) {
                    return false;
                }

                if (Blocks.shouldHideNeighboringFaces(block) && negYNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(negYNeighbor)) {
                    return true;
                }
            }
            case "left" -> {
                if (negXNeighbor == Blocks.WORLD_BORDER || negXNeighbor == Blocks.NULL) {
                    return false;
                }

                if (Blocks.shouldHideNeighboringFaces(block) && negXNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(negXNeighbor)) {
                    return true;
                }
            }
            case "right" -> {
                if (posXNeighbor == Blocks.WORLD_BORDER || posXNeighbor == Blocks.NULL) {
                    return false;
                }

                if (Blocks.shouldHideNeighboringFaces(block) && posXNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(posXNeighbor)) {
                    return true;
                }
            }
            case "front" -> {
                if (posZNeighbor == Blocks.WORLD_BORDER || posZNeighbor == Blocks.NULL) {
                    return false;
                }

                if (Blocks.shouldHideNeighboringFaces(block) && posZNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(posZNeighbor)) {
                    return true;
                }
            }
            case "back" -> {
                if (negZNeighbor == Blocks.WORLD_BORDER || negZNeighbor == Blocks.NULL) {
                    return false;
                }

                if (Blocks.shouldHideNeighboringFaces(block) && negZNeighbor == block) {
                    return false;
                }

                if (Blocks.isBlockTransparent(negZNeighbor)) {
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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
                    !Blocks.isBlockTransparent(neighbors[0]),
                    !Blocks.isBlockTransparent(neighbors[1]),
                    !Blocks.isBlockTransparent(neighbors[2]),
                    !Blocks.isBlockTransparent(neighbors[3]),
                    !Blocks.isBlockTransparent(neighbors[4]),
                    !Blocks.isBlockTransparent(neighbors[5]),
                    !Blocks.isBlockTransparent(neighbors[6]),
                    !Blocks.isBlockTransparent(neighbors[7])
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



    public void addFace(
            boolean ambientOcclusion,
            ArrayList<Float> vertices,
            ArrayList<Integer> indices,
            ArrayList<Double> faceVertexArray,
            ArrayList<Integer> faceIndexArray,
            int offX, int offY, int offZ,
            byte[][][] blockNeighbors,
            BlockAtlasTexture.BlockTexture blockTexture,
            double textureRotationValue,
            float waveStrength
    ) {

        textureRotationValue = Math.toRadians(textureRotationValue);

        float[] textureUV = blockTexture.getFrames().getFrameUV(blockTexture.getFrames().getCurrentFrameIndex());

        int lightValue = 0x0000;

        ChunkPosition chunkPosition = chunk.getChunkPosition();

        try {
            int vertexAmount = vertices.size() / VERTEX_ATTRIBUTE_FLOAT_AMOUNT;

            for (int faceIndex : faceIndexArray) {
                indices.add(faceIndex + vertexAmount);
            }

            float[] vertexPosition = new float[3];
            float[] vertexNormal = new float[3];


            // temp variable to store uv for uv rotation
            float[] uv = new float[] {0f, 0f};


            for (int i = 0; i < faceVertexArray.size(); i++) {

                int vertexDataIndex = i % 8;
                float vertexData = faceVertexArray.get(i).floatValue();

                if (vertexDataIndex == 0) {
                    vertexPosition[0] = vertexData;
                    // offset x position of vertex
                    vertexData += offX;
                } else if (vertexDataIndex == 1) {
                    vertexPosition[1] = vertexData;
                    // offset y position of vertex
                    vertexData += offY;
                } else if (vertexDataIndex == 2) {
                    vertexPosition[2] = vertexData;
                    // offset z position of vertex
                    vertexData += offZ;
                } else if (vertexDataIndex == 3) {
                    // store u for later
                    uv[0] = vertexData;
                    continue;

                } else if (vertexDataIndex == 4) {

                    // rotate u and v
                    uv[1] = vertexData;

                    float pivotX = textureUV[0] + BlockAtlasTexture.HALF_BLOCK_SCALE_ON_ATLAS;
                    float pivotY = textureUV[1] + BlockAtlasTexture.HALF_BLOCK_SCALE_ON_ATLAS;

                    HelpfulMath.rotatePoint(uv, pivotX, pivotY, (float) textureRotationValue);

                    vertices.add(uv[0]);
                    vertices.add(uv[1]);

                    continue;

                } else if (vertexDataIndex == 5 || vertexDataIndex == 6 || vertexDataIndex == 7) {
                    // set normal of vertex
                    vertexNormal[vertexDataIndex - 5] = vertexData;

                    // if the normal of the vertex is ready
                    if (vertexDataIndex == 7) {



                        // add lighting
                        short lightValueShort;

                        byte block = blockNeighbors[1][1][1];
                        if (Blocks.isBlockTransparent(block)) {

                             lightValueShort = chunk.getLightValue(offX, offY, offZ);

                        } else {

                            float absX = Math.abs(vertexNormal[0]);
                            float absY = Math.abs(vertexNormal[1]);
                            float absZ = Math.abs(vertexNormal[2]);

                            if (absX > absY && absX > absZ) {

                                if (vertexNormal[0] > 0f) {
                                    lightValueShort = chunk.getLightValue(offX + 1, offY, offZ);
                                } else {
                                    lightValueShort = chunk.getLightValue(offX - 1, offY, offZ);
                                }

                            } else if (absY > absX && absY > absZ) {

                                if (vertexNormal[1] > 0f) {
                                    lightValueShort = chunk.getLightValue(offX, offY + 1, offZ);
                                } else {
                                    lightValueShort = chunk.getLightValue(offX, offY - 1, offZ);
                                }

                            } else {

                                if (vertexNormal[2] > 0f) {
                                    lightValueShort = chunk.getLightValue(offX, offY, offZ + 1);
                                } else {
                                    lightValueShort = chunk.getLightValue(offX, offY, offZ - 1);
                                }

                            }

                        }

                        lightValue = (lightValueShort << 16);

                    }

                }

                // add bit of data
                vertices.add(vertexData);

                // if vertexDataIndex is last bit of vertex data
                if (vertexDataIndex == 7) {

                    // add ambient occlusion value
                    if (ambientOcclusion) {

                        float aoValue = getVertexAo(vertexPosition, blockNeighbors, vertexNormal);
                        vertices.add(aoValue);

                    } else {
                        vertices.add(
                                WorldGeneration.getBlockTint(
                                        getChunk().getWorld().getSeed(),
                                        chunkPosition.getBlockX() + offX,
                                        chunkPosition.getBlockY() + offY,
                                        chunkPosition.getBlockZ() + offZ
                                )
                        );
                    }

                    vertices.add((float) blockTexture.getFrames().getFramesPerSecond());
                    vertices.add((float) blockTexture.getFrames().getFrameAmount());
                    vertices.add(waveStrength);

                    float lightRed = (float) ((lightValue) >>> 28);
                    float lightGreen = (float) ((lightValue << 4) >>> 28);
                    float lightBlue = (float) ((lightValue << 8) >>> 28);
                    float lightSun = (float) ((lightValue << 12) >>> 28);

                    vertices.add(lightRed);
                    vertices.add(lightGreen);
                    vertices.add(lightBlue);
                    vertices.add(lightSun);

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
        return new Vector3d(chunk.getPosition()).add(Chunk.SIZE_X / 2d, Chunk.SIZE_Y / 2d, Chunk.SIZE_Z / 2d);
    }

    @Override
    public boolean hasTransparency() {
        return isTransparent;
    }
}