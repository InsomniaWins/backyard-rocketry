package wins.insomnia.backyardrocketry.world;

import org.joml.*;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.util.OpenGLWrapper;
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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);


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


        Renderer.get().getModelMatrix().identity().translate(chunk.getPosition());
        Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    public void generateMesh(byte[][][] blocks) {


        ArrayList<Float> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    byte block = blocks[x][y][z];

                    if (block != Block.AIR) {

                        if (isTransparent != Block.isBlockTransparent(block)) {
                            continue;
                        }

                        BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(blocks[x][y][z], x, y, z);


                        for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

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



        if (Main.MAIN_THREAD != Thread.currentThread()){
            Updater.get().queueMainThreadInstruction(() -> {
                meshDataVertexArray = vertexArray;
                meshDataIndexArray = indexArray;
                meshDataIndexCount = indexArray.length;
                readyToCreateOpenGLMeshData.set(true);
                isClean.set(false);
            });
        } else {
            meshDataVertexArray = vertexArray;
            meshDataIndexArray = indexArray;
            meshDataIndexCount = indexArray.length;
            readyToCreateOpenGLMeshData.set(true);
            isClean.set(false);
        }

    }

    private boolean shouldAddFaceToMesh(String cullface, int x, int y, int z) {

        if (cullface == null) return true;

        byte block = chunk.getBlock(x,y,z);
        byte topNeighbor = chunk.getBlock(x, y+1, z);
        byte bottomNeighbor = chunk.getBlock(x, y-1, z);
        byte leftNeighbor = chunk.getBlock(x-1, y, z);
        byte rightNeighbor = chunk.getBlock(x+1, y, z);
        byte backNeighbor = chunk.getBlock(x, y, z-1);
        byte frontNeighbor = chunk.getBlock(x, y, z+1);



        switch (cullface) {
            case "top" -> {
                if (topNeighbor == Block.WORLD_BORDER || topNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && topNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(topNeighbor)) {
                    return true;
                }
            }
            case "bottom" -> {

                if (bottomNeighbor == Block.WORLD_BORDER || bottomNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && bottomNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(bottomNeighbor)) {
                    return true;
                }
            }
            case "left" -> {
                if (leftNeighbor == Block.WORLD_BORDER || leftNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && leftNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(leftNeighbor)) {
                    return true;
                }
            }
            case "right" -> {
                if (rightNeighbor == Block.WORLD_BORDER || rightNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && rightNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(rightNeighbor)) {
                    return true;
                }
            }
            case "front" -> {
                if (frontNeighbor == Block.WORLD_BORDER || frontNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && frontNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(frontNeighbor)) {
                    return true;
                }
            }
            case "back" -> {
                if (backNeighbor == Block.WORLD_BORDER || backNeighbor == Block.NULL) {
                    return false;
                }

                if (Block.shouldHideNeighboringFaces(block) && backNeighbor == block) {
                    return false;
                }

                if (Block.isBlockTransparent(backNeighbor)) {
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