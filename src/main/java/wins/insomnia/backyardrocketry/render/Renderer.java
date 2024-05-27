package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.gui.GuiMesh;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer implements IUpdateListener, IFixedUpdateListener {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private ShaderProgram guiShaderProgram;
    private final TextureManager TEXTURE_MANAGER;
    private final FontMesh FONT_MESH;
    private final GuiMesh GUI_MESH;
    private final ArrayList<IRenderable> RENDER_LIST;
    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs
    private double fpsTimer = 0.0;
    private Matrix4f modelMatrix;
    private int renderMode = 0;
    private int guiScale = 1;

    public Renderer() {
        RENDER_LIST = new ArrayList<>();
        TEXTURE_MANAGER = new TextureManager();

        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");
        guiShaderProgram = new ShaderProgram("gui.vert", "gui.frag");
        setGuiScale(2);

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

        glClearColor(0.25882352941176473f, 0.6901960784313725f, 1f, 1f);

    }

    public void fixedUpdate() {
        if (KeyboardInput.get().isKeyJustReleased(GLFW_KEY_F3)) {
            renderMode++;
            if (renderMode == 3) {
                renderMode = 0;
            }
        }
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

    // master draw method used in game loop
    private void draw(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        render();
        glfwSwapBuffers(window.getWindowHandle());
    }

    // is thread-safe
    public void addRenderable(IRenderable renderable) {

        synchronized (this) {
            RENDER_LIST.add(renderable);
        }

    }

    // is thread-safe
    public void removeRenderable(IRenderable renderable) {
        synchronized (this) {
            RENDER_LIST.remove(renderable);
        }
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    private void render() {

        camera.updateProjectionMatrix();
        camera.updateViewMatrix();
        camera.updateFrustum();

        // placeholder render code
        shaderProgram.use();
        shaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        shaderProgram.setUniform("vs_viewMatrix", camera.getViewMatrix());
        shaderProgram.setUniform("vs_projectionMatrix", camera.getProjectionMatrix());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, TextureManager.get().getBlockAtlasTexture().getTextureHandle());

        activateRenderMode();

        modelMatrix.identity();

        // render renderables
        synchronized (this) {
            for (IRenderable renderable : RENDER_LIST) {
                if (!renderable.shouldRender()) continue;

                shaderProgram.setUniform("vs_modelMatrix", modelMatrix);
                renderable.render();

            }
        }

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

                shaderProgram.setUniform("vs_modelMatrix", modelMatrix);

                glDisable(GL_DEPTH_TEST);

                glBindTexture(GL_TEXTURE_2D, TextureManager.get().getBlockOutlineTexture().getTextureHandle());

                TargetBlockOutlineMesh.get(raycastResult.getFace()).render(GL_LINES);

                glEnable(GL_DEPTH_TEST);

                activateRenderMode();
            }

        }


        // draw debug information

        String debugString = "";
        debugString = debugString + DebugInfo.getMemoryUsage();
        debugString = debugString + "\n\n" + DebugInfo.getFramesPerSecond();
        debugString = debugString + "\n\n" + DebugInfo.getFixedUpdatesPerSecond();
        debugString = debugString + "\n\n" + DebugInfo.getRenderMode();

        if (BackyardRocketry.getInstance().getPlayer() instanceof TestPlayer player) {
            debugString = debugString + "\n\n" + DebugInfo.getPlayerBlockPosition(player);
            debugString = debugString + "\n\n" + DebugInfo.getPlayerPosition(player);
            debugString = debugString + "\n\n" + DebugInfo.getPlayerRotation(player);
            debugString = debugString + "\n\n" + DebugInfo.getPlayerTargetBlockInfo(player);
        }

        drawText(debugString, 0,  0);
        drawGuiTexture(TEXTURE_MANAGER.getCrosshairTexture(), getCenterAnchorX() - 8, getCenterAnchorY() - 8);
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

    public int getRightAnchor() {
        return Window.get().getWidth() / guiScale;
    }

    public int getRenderMode() {
        return renderMode;
    }


    public void drawGuiTexture(Texture texture, int guiX, int guiY) {

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

    }


    public void drawText(String text, int guiX, int guiY) {

        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);


        guiShaderProgram.use();
        glBindTexture(GL_TEXTURE_2D, TEXTURE_MANAGER.getDebugFontTexture().getTextureHandle());
        glActiveTexture(GL_TEXTURE0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        modelMatrix.identity();

        guiShaderProgram.setUniform("vs_textureSizeX", 1);
        guiShaderProgram.setUniform("vs_textureSizeY", 1);
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


        // generate and draw font mesh
        FONT_MESH.setText(text);
        glBindVertexArray(FONT_MESH.getVao());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, FONT_MESH.getIndexCount(), GL_UNSIGNED_INT, 0);


        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);


        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);
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

            //System.out.println("Renderable not unregistered when game closed: " + renderable);

        }

        FONT_MESH.clean();
        TEXTURE_MANAGER.clean();
        GUI_MESH.clean();
        glDeleteProgram(shaderProgram.getProgramHandle());

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
}
