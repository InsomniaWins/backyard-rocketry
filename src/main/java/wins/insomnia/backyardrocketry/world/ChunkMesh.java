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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);


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

    public void addFace(ArrayList<Float> vertices, ArrayList<Integer> indices, ArrayList<Double> faceVertexArray, ArrayList<Integer> faceIndexArray, ArrayList<Double> faceNormalsArray, int offX, int offY, int offZ) {

        try {
        int indexOffset = vertices.size() / 8;

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



            // if vertexDataIndex is last bit of vertex data
            if (vertexDataIndex == 4) {

                // if face does not have normals
                if (faceNormalsArray == null) {
                    // make "up" normal
                    vertices.add(0f);
                    vertices.add(1f);
                    vertices.add(0f);
                } else {
                    vertices.add(faceNormalsArray.get(0).floatValue());
                    vertices.add(faceNormalsArray.get(1).floatValue());
                    vertices.add(faceNormalsArray.get(2).floatValue());
                }

            }

        }}
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

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

        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);


        glDisableVertexAttribArray(2);
    }

    public void generateMesh(byte[][][] blocks) {
        generateMesh(blocks, true);
    }

    public void generateMesh(byte[][][] blocks, boolean isDelayed) {

        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    byte block = blocks[x][y][z];
                    if (block == Block.AIR) continue;

                    if (isTransparent != Block.isBlockTransparent(block)) continue;

                    // use Chunk.getBlock because blocks could be in neighboring chunk(s)
                    byte posYNeighbor = chunk.getBlock(x, y+1, z);
                    byte negYNeighbor = chunk.getBlock(x, y-1, z);
                    byte negXNeighbor = chunk.getBlock(x-1, y, z);
                    byte posXNeighbor = chunk.getBlock(x+1, y, z);
                    byte negZNeighbor = chunk.getBlock(x, y, z-1);
                    byte posZNeighbor = chunk.getBlock(x, y, z+1);



                    BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(block, x, y, z);


                    for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

                        HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();
                        ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
                        ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");
                        ArrayList<Double> faceNormalArray = (ArrayList<Double>) faceData.get("normal");

                        if (shouldAddFaceToMesh((String) faceData.get("cullface"), block, posYNeighbor, negYNeighbor, negXNeighbor, posXNeighbor, negZNeighbor, posZNeighbor)) {

                            addFace(vertices, indices, faceVertexArray, faceIndexArray, faceNormalArray, x, y, z);

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

    private boolean shouldAddFaceToMesh(String cullface, byte block, byte posYNeighbor, byte negYNeighbor, byte negXNeighbor, byte posXNeighbor, byte negZNeighbor, byte posZNeighbor) {

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

    @Override
    public Vector3d getPosition() {
        return new Vector3d(chunk.getPosition());
    }

    @Override
    public boolean hasTransparency() {
        return isTransparent;
    }
}