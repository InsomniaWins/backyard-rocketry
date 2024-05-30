package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.gui.GuiMesh;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.world.ChunkMesh;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final ConcurrentLinkedQueue<IRenderable> RENDER_QUEUE;
    private final ConcurrentLinkedQueue<IRenderable> REMOVE_RENDER_QUEUE;
    private final ArrayList<IRenderable> RENDER_LIST;
    private final ConcurrentLinkedQueue<IGuiRenderable> GUI_RENDER_LIST;
    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs
    private double fpsTimer = 0.0;
    private Matrix4f modelMatrix;
    private int renderMode = 0;
    private int guiScale = 1;

    public Renderer() {
        RENDER_QUEUE = new ConcurrentLinkedQueue<>();
        REMOVE_RENDER_QUEUE = new ConcurrentLinkedQueue<>();
        RENDER_LIST = new ArrayList<>();
        GUI_RENDER_LIST = new ConcurrentLinkedQueue<>();
        TEXTURE_MANAGER = new TextureManager();

        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");
        guiShaderProgram = new ShaderProgram("gui.vert", "gui.frag");
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

        if (renderable instanceof IGuiRenderable) {
            GUI_RENDER_LIST.add((IGuiRenderable) renderable);
        } else {
            RENDER_QUEUE.add(renderable);
        }

    }

    private void sortRenderList() {

        RENDER_LIST.sort((renderable1, renderable2) -> {

            if (!renderable1.shouldRender() || !renderable2.shouldRender()) {
                return 0;
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

    // is thread-safe
    public void removeRenderable(IRenderable renderable) {
        if (renderable instanceof IGuiRenderable) {
            GUI_RENDER_LIST.remove((IGuiRenderable) renderable);
        } else {

			RENDER_QUEUE.remove(renderable);

            REMOVE_RENDER_QUEUE.add(renderable);
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
        while (!RENDER_QUEUE.isEmpty()) {
            RENDER_LIST.add(RENDER_QUEUE.poll());
        }

        while (!REMOVE_RENDER_QUEUE.isEmpty()) {
            RENDER_LIST.remove(REMOVE_RENDER_QUEUE.poll());
        }

        sortRenderList();
        glDisable(GL_BLEND);
        for (IRenderable renderable : RENDER_LIST) {

            if (!renderable.shouldRender()) continue;

            shaderProgram.setUniform("vs_modelMatrix", modelMatrix);

            renderable.render();
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


        // render gui
        for (IGuiRenderable renderable : GUI_RENDER_LIST) {

            if (!renderable.shouldRender()) continue;

            shaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            renderable.render();

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

        drawText(debugString, 0, 0, 2, TEXTURE_MANAGER.getDebugFontTexture());
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

        shaderProgram.use();


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

        shaderProgram.use();
    }


    public void drawText(String text, int guiX, int guiY) {
        drawText(text, guiX, guiY, guiScale, TextureManager.get().getFontTexture());
    }

    public void drawText(String text, int guiX, int guiY, Texture fontTexture) {
        drawText(text, guiX, guiY, guiScale, fontTexture);
    }

    public void drawText(String text, int guiX, int guiY, int scale, Texture fontTexture) {


        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

        guiShaderProgram.use();

        if (scale != guiScale) {
            guiShaderProgram.setUniform("vs_scale", scale);
        }

        glBindTexture(GL_TEXTURE_2D, fontTexture.getTextureHandle());
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

        if (scale != guiScale) {
            guiShaderProgram.setUniform("vs_scale", this.guiScale);
        }

        shaderProgram.use();
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
