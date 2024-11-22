package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.gui.GuiMesh;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.debug.DebugTime;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkMesh;

import java.util.*;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer implements IUpdateListener, IFixedUpdateListener {
    private Camera camera;
    private final TextureManager TEXTURE_MANAGER;
    private final FontMesh FONT_MESH;
    private final GuiMesh GUI_MESH;
    private final LinkedList<IRenderable> RENDER_LIST;
    private final LinkedList<IGuiRenderable> GUI_RENDER_LIST;
    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs
    private double fpsTimer = 0.0;
    private Matrix4f modelMatrix;
    private int renderMode = 0;
    private int guiScale = 1;
    private boolean renderDebugInformation = false;
    private ShaderProgram defaultShaderProgram = null;
    private ShaderProgram guiShaderProgram = null;
    private ShaderProgram chunkMeshShaderProgram = null;

    private final HashMap<String, ShaderProgram> SHADER_PROGRAM_MAP = new HashMap<>();

    public Renderer() {
        RENDER_LIST = new LinkedList<>();
        GUI_RENDER_LIST = new LinkedList<>();
        TEXTURE_MANAGER = new TextureManager();

        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        defaultShaderProgram = registerShaderProgram("default", "vertex.vert", "fragment.frag");
        guiShaderProgram = registerShaderProgram("gui", "gui.vert", "gui.frag");
        chunkMeshShaderProgram = registerShaderProgram("chunk_mesh", "chunk_mesh/chunk_mesh.vert", "chunk_mesh/chunk_mesh.frag");
        chunkMeshShaderProgram.use();
        chunkMeshShaderProgram.setUniform("vs_atlasBlockScale", TextureManager.BLOCK_SCALE_ON_ATLAS);

        setGuiScale(3);

        modelMatrix = new Matrix4f().identity();

        Updater.get().registerUpdateListener(this);
        Updater.get().registerFixedUpdateListener(this);

        FONT_MESH = new FontMesh();
        GUI_MESH = new GuiMesh(
                new float[] {
                        0f, 0f, 0f, 0f,
                        1f, 0f, 1f, 0f,
                        0f, 1f, 0f, 1f,
                        1f, 1f, 1f, 1f
                },
                new int[] {
                        0, 1, 2,
                        1, 2, 3
                }
        );

        glClearColor(120 / 255f, 167 / 255f, 255 / 255f, 1f);

    }

    public void fixedUpdate() {


        if (KeyboardInput.get().isKeyJustPressed(GLFW_KEY_F3)) {
            renderDebugInformation = !renderDebugInformation;
        }

    }

    @Override
    public void registeredFixedUpdateListener() {

    }

    @Override
    public void unregisteredFixedUpdateListener() {

    }

    public void update(double deltaTime) {
        draw(BackyardRocketry.getInstance().getWindow());
        framesRenderedSoFar++;

        fpsTimer += deltaTime;
        while (fpsTimer > 1.0) {

            fpsTimer -= 1.0;
            framesPerSecond = framesRenderedSoFar;
            framesRenderedSoFar = 0;

        }

    }

    @Override
    public void registeredUpdateListener() {

    }

    @Override
    public void unregisteredUpdateListener() {

    }

    // master draw method used in game loop
    private void draw(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        render();
        glfwSwapBuffers(window.getWindowHandle());
    }

    // is thread-safe
    public void addRenderable(IRenderable renderable) {

        if (renderable instanceof IGuiRenderable) {
            GUI_RENDER_LIST.add((IGuiRenderable) renderable);
        } else {
            RENDER_LIST.add(renderable);
        }

    }

    private void sortRenderList() {

        RENDER_LIST.sort((renderable1, renderable2) -> {


            if (renderable1.getRenderPriority() > renderable2.getRenderPriority()) {
                return 1;
            } else if (renderable1.getRenderPriority() < renderable2.getRenderPriority()) {
                return -1;
            }


			float distance1 = 0.0f;
			float distance2 = 0.0f;

			if (renderable1 instanceof IPositionOwner positionOwner1) {
				distance1 = (float) positionOwner1.getPosition().distance(getCamera().getTransform().getPosition());
			}

			if (renderable2 instanceof IPositionOwner positionOwner2) {
				distance2 = (float) positionOwner2.getPosition().distance(getCamera().getTransform().getPosition());
			}

            boolean hasTransparency1 = false;
            boolean hasTransparency2 = false;

            if (renderable1 instanceof Mesh mesh) {
                hasTransparency1 = mesh.hasTransparency();
            }

            if (renderable2 instanceof Mesh mesh) {
                hasTransparency2 = mesh.hasTransparency();
            }



            if (hasTransparency1 == hasTransparency2) {
                return Float.compare(distance1, distance2);
            } else if (hasTransparency1) {
                return 1;
            } else {
                return -1;
            }


		});

    }

    //  NOT THREAD SAFE
    public void removeRenderable(IRenderable renderable) {
        if (renderable instanceof IGuiRenderable) {
            GUI_RENDER_LIST.remove((IGuiRenderable) renderable);
        } else {
            RENDER_LIST.remove(renderable);
        }
    }

    public ShaderProgram getShaderProgram() {
        return SHADER_PROGRAM_MAP.get("default");
    }

    public ShaderProgram getShaderProgram(String programName) {
        return SHADER_PROGRAM_MAP.get(programName);
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    private void render() {

        camera.updateProjectionMatrix();
        camera.updateViewMatrix();
        camera.updateFrustum();

        // placeholder render code

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();


        chunkMeshShaderProgram.use();
        chunkMeshShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        chunkMeshShaderProgram.setUniform("vs_viewMatrix", viewMatrix);
        chunkMeshShaderProgram.setUniform("vs_projectionMatrix", projectionMatrix);

        defaultShaderProgram.use();
        defaultShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        defaultShaderProgram.setUniform("vs_viewMatrix", viewMatrix);
        defaultShaderProgram.setUniform("vs_projectionMatrix", projectionMatrix);


        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, TextureManager.getBlockAtlasTexture().getTextureHandle());

        activateRenderMode();

        modelMatrix.identity();

        // render renderables
        sortRenderList();
        glDisable(GL_BLEND);

        long renderTime = System.currentTimeMillis();
        for (IRenderable renderable : RENDER_LIST) {

            if (!renderable.shouldRender()) continue;

            if (renderable instanceof ChunkMesh chunkMesh) {

                if (ShaderProgram.getShaderProgramHandleInUse() != chunkMeshShaderProgram.getProgramHandle()) {
                    chunkMeshShaderProgram.use();
                }

                chunkMeshShaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            } else {

                if (ShaderProgram.getShaderProgramHandleInUse() != defaultShaderProgram.getProgramHandle()) {
                    defaultShaderProgram.use();
                }

                defaultShaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            }

            renderable.render();
        }
        renderTime = DebugTime.getElapsedTime(renderTime);


        // render target block
        if (BackyardRocketry.getInstance().getPlayer() instanceof TestPlayer player) {

            BlockRaycastResult raycastResult = player.getTargetBlock();

            if (raycastResult != null) {

                modelMatrix = modelMatrix.identity();
                modelMatrix.translate(
                        raycastResult.getBlockX(),
                        raycastResult.getBlockY(),
                        raycastResult.getBlockZ()
                );

                defaultShaderProgram.setUniform("vs_modelMatrix", modelMatrix);

                glDisable(GL_DEPTH_TEST);

                glBindTexture(GL_TEXTURE_2D, TextureManager.getTexture("block_outline").getTextureHandle());

                TargetBlockOutlineMesh.get(raycastResult.getFace()).render(GL_LINES);

                glEnable(GL_DEPTH_TEST);

                activateRenderMode();
            }

        }

        // render gui
        for (IGuiRenderable renderable : GUI_RENDER_LIST) {

            if (!renderable.shouldRender()) continue;

            renderable.render();

        }

        // draw debug information

        if (renderDebugInformation) {
            StringBuilder debugString = new StringBuilder();
            debugString.append("Render Time: ").append(renderTime).append("ms");
            debugString.append("\n\n").append(DebugInfo.getMemoryUsage());
            debugString.append("\n\n").append(DebugInfo.getFramesPerSecond());


            if (BackyardRocketry.getInstance().getPlayer() instanceof TestPlayer player) {
                debugString.append("\n\n").append(DebugInfo.getPlayerChunkPosition(player));
                debugString.append("\n\n").append(DebugInfo.getPlayerBlockPosition(player));
                debugString.append("\n\n").append(DebugInfo.getPlayerTargetBlockInfo(player));
            }

            TextRenderer.drawText(debugString.toString(), 0, 0, 2, TextureManager.getTexture("debug_font"));
        }


        StringBuilder controlsStringBuilder = new StringBuilder();
        controlsStringBuilder.append("W,A,S,D : move\n");
        controlsStringBuilder.append("Spacebar : jump\n");
        controlsStringBuilder.append("Left Shift : sneak\n");
        controlsStringBuilder.append("Left Control : sprint\n");
        controlsStringBuilder.append("Left Mouse : break block\n");
        controlsStringBuilder.append("Right Mouse : place block\n");
        controlsStringBuilder.append("F3 : toggle debug menu\n");
        controlsStringBuilder.append("F4 : toggle noclip\n");
        String controlsString = controlsStringBuilder.toString();

        TextRenderer.drawText(
                controlsString,
                0, getBottomAnchor(2) - TextRenderer.getTextPixelHeight(controlsString.length() - controlsString.replace("\n", "").length()), 2, TextureManager.getTexture("debug_font"));

    }

    public void setGuiScale(int guiScale) {
        this.guiScale = guiScale;

        int[] previousShaderHandle = new int[1];
        glGetIntegerv(GL_CURRENT_PROGRAM, previousShaderHandle);

        if (previousShaderHandle[0] != guiShaderProgram.getProgramHandle()) {
            guiShaderProgram.use();
        }

        guiShaderProgram.setUniform("vs_scale", this.guiScale);

        if (previousShaderHandle[0] != guiShaderProgram.getProgramHandle()) {
            glUseProgram(previousShaderHandle[0]);
        }
    }

    public int getCenterAnchorX() {
        return getRightAnchor() / 2;
    }

    public int getCenterAnchorY() {
        return getBottomAnchor() / 2;
    }

    public int getBottomAnchor() {
        return Window.get().getHeight() / guiScale;
    }

    public int getBottomAnchor(int customGuiScale) {
        return Window.get().getHeight() / customGuiScale;
    }

    public int getRightAnchor() {
        return Window.get().getWidth() / guiScale;
    }

    public int getRightAnchor(int customGuiScale) {
        return Window.get().getWidth() / customGuiScale;
    }

    public int getRenderMode() {
        return renderMode;
    }


    public void drawGuiTextureClipped(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int textureX, int textureY, int textureWidth, int textureHeight) {


        // get clipped texture Uv's
        float[] texturePixelScale = {
                1.0f / texture.getWidth(),
                1.0f / texture.getHeight()
        };

        float[] textureCoordinates = new float[4];
        textureCoordinates[0] = textureX * texturePixelScale[0];
        textureCoordinates[1] = -textureY * texturePixelScale[1];
        textureCoordinates[2] = (textureX + textureWidth) * texturePixelScale[0];
        textureCoordinates[3] = -(textureY + textureHeight) * texturePixelScale[1];

        glBindBuffer(GL_ARRAY_BUFFER, GUI_MESH.getVbo());
        glBufferData(GL_ARRAY_BUFFER, new float[] {
                0f, 0f, textureCoordinates[0], -textureCoordinates[1],
                1f, 0f, textureCoordinates[2], -textureCoordinates[1],
                0f, 1f, textureCoordinates[0], -textureCoordinates[3],
                1f, 1f, textureCoordinates[2], -textureCoordinates[3]
        }, GL_DYNAMIC_DRAW);


        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

        guiShaderProgram.use();
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
        glActiveTexture(GL_TEXTURE0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        modelMatrix.identity();

        guiShaderProgram.setUniform("vs_textureSizeX", guiWidth);
        guiShaderProgram.setUniform("vs_textureSizeY", guiHeight);
        guiShaderProgram.setUniform("vs_posX", guiX);
        guiShaderProgram.setUniform("vs_posY", guiY);
        guiShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        guiShaderProgram.setUniform("vs_projectionMatrix", modelMatrix.ortho(
                0f, // left
                BackyardRocketry.getInstance().getWindow().getWidth(), // right
                0f, // bottom
                BackyardRocketry.getInstance().getWindow().getHeight(), // top
                0.01f, // z-near
                1f // z-far
        ));



        glEnable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        // TODO: Add back-face culling to text rendering!!!

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);


        // draw mesh
        glBindVertexArray(GUI_MESH.getVao());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, GUI_MESH.getIndexCount(), GL_UNSIGNED_INT, 0);


        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

        defaultShaderProgram.use();


    }

    public void drawGuiTexture(Texture texture, int guiX, int guiY) {

        glBindBuffer(GL_ARRAY_BUFFER, GUI_MESH.getVbo());
        glBufferData(GL_ARRAY_BUFFER, new float[] {
                0f, 0f, 0f, 0f,
                1f, 0f, 1f, 0f,
                0f, 1f, 0f, 1f,
                1f, 1f, 1f, 1f
        }, GL_DYNAMIC_DRAW);

        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

        guiShaderProgram.use();
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
        glActiveTexture(GL_TEXTURE0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        modelMatrix.identity();

        guiShaderProgram.setUniform("vs_textureSizeX", texture.getWidth());
        guiShaderProgram.setUniform("vs_textureSizeY", texture.getHeight());
        guiShaderProgram.setUniform("vs_posX", guiX);
        guiShaderProgram.setUniform("vs_posY", guiY);
        guiShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        guiShaderProgram.setUniform("vs_projectionMatrix", modelMatrix.ortho(
                0f, // left
                BackyardRocketry.getInstance().getWindow().getWidth(), // right
                0f, // bottom
                BackyardRocketry.getInstance().getWindow().getHeight(), // top
                0.01f, // z-near
                1f // z-far
        ));



        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        // TODO: Add back-face culling to text rendering!!!

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);


        // draw mesh
        glBindVertexArray(GUI_MESH.getVao());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, GUI_MESH.getIndexCount(), GL_UNSIGNED_INT, 0);


        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

        defaultShaderProgram.use();
    }


    private void activateRenderMode() {
        switch (renderMode) {
            case 0 -> {
                glEnable(GL_CULL_FACE);
                glPolygonMode(GL_FRONT, GL_FILL);
            }
            case 1 -> {
                glEnable(GL_CULL_FACE);
                glPolygonMode(GL_FRONT, GL_LINE);
            }
            case 2 -> {
                glDisable(GL_CULL_FACE);
                glPolygonMode(GL_FRONT, GL_FILL);
            }
        }
    }

    public void clean() {

        for (IRenderable renderable : RENDER_LIST) {

            if (renderable == null) continue;

            if (!renderable.isClean()) {
                renderable.clean();
            }

        }

        for (IGuiRenderable renderable : GUI_RENDER_LIST) {

            if (renderable == null) continue;

            if (!renderable.isClean()) {
                renderable.clean();
            }

        }

        FONT_MESH.clean();
        TextureManager.clean();
        GUI_MESH.clean();

        Iterator<Map.Entry<String, ShaderProgram>> shaderIterator = SHADER_PROGRAM_MAP.entrySet().iterator();
        while (shaderIterator.hasNext()) {
            Map.Entry<String, ShaderProgram> shaderProgramEntry = shaderIterator.next();

            ShaderProgram shaderProgram = shaderProgramEntry.getValue();

            if (shaderProgram != null) {

                glDeleteProgram(shaderProgram.getProgramHandle());
                shaderIterator.remove();

            }
        }


    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public Camera getCamera() {
        return camera;
    }

    public TextureManager getTextureManager() {
        return TEXTURE_MANAGER;
    }

    public static Renderer get() {
        return BackyardRocketry.getInstance().getRenderer();
    }

    public FontMesh getFontMesh() {
        return FONT_MESH;
    }

    public int getGuiScale() {
        return guiScale;
    }

    public ShaderProgram getGuiShaderProgram() {
        return guiShaderProgram;
    }

    public ShaderProgram registerShaderProgram(String programName, String vertexName, String fragmentName) {

        ShaderProgram program = new ShaderProgram(vertexName, fragmentName);
        SHADER_PROGRAM_MAP.put(programName, program);
        return program;

    }

}
