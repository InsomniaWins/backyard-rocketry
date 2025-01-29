package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
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

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer implements IUpdateListener, IFixedUpdateListener {
    private Camera camera;
    private final Vector3f CLEAR_COLOR = new Vector3f();
    private final TextureManager TEXTURE_MANAGER;
    private ResolutionFrameBuffer resolutionFrameBuffer;
    private final FontMesh FONT_MESH;
    private final GuiMesh GUI_MESH;
    private final LinkedList<IRenderable> RENDER_LIST;
    private final LinkedList<IGuiRenderable> GUI_RENDER_LIST;
    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs

    private int drawCallsPerSecond = 0;
    private int drawCallsSoFar = 0; // draw calls before fps-polling occurs

    private double fpsTimer = 0.0;
    private int fpsLimit = -1; // limits the fps, -1 for unlimited
    // the time each frame should render based on fpsLimit
    private double fixedFrameRenderTime = 0.0; // DO NOT SET MANUALLY
    private Matrix4f modelMatrix;
    private int renderMode = 0;
    private int guiScale = 1;
    private double timeOfPreviousFrame = glfwGetTime(); // game time of previous frame's rendering/drawing (not total time it took to render)
    private boolean renderDebugInformation = false;
    private ShaderProgram defaultShaderProgram = null, guiShaderProgram = null, chunkMeshShaderProgram = null;
    private boolean pixelPerfectViewport = false;
    private final HashMap<String, ShaderProgram> SHADER_PROGRAM_MAP = new HashMap<>();

    public Renderer() {

        setResolution(320, 240, true);

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

        setGuiScale(1);

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

        setClearColor(120.0f / 255.0f, 167.0f / 255.0f, 1.0f);
        setFpsLimit(120);
    }

    public void setResolution(int width, int height, boolean pixelPerfect) {

        if (resolutionFrameBuffer != null) {
            resolutionFrameBuffer.clean();
        }

        resolutionFrameBuffer = new ResolutionFrameBuffer(width, height);
        setPixelPerfectViewport(pixelPerfect);

    }

    public void setResolution(int width, int height) {

        setResolution(width, height, isPixelPerfectViewport());

    }

    public void setClearColor(float r, float g, float b) {
        CLEAR_COLOR.x = r;
        CLEAR_COLOR.y = g;
        CLEAR_COLOR.z = b;
        glClearColor(CLEAR_COLOR.x, CLEAR_COLOR.y, CLEAR_COLOR.z, 1f);
        chunkMeshShaderProgram.use();
        chunkMeshShaderProgram.setUniform("fs_fogColor", CLEAR_COLOR);
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
        drawCallsSoFar++;

        fpsTimer += deltaTime;
        while (fpsTimer > 1.0) {

            fpsTimer -= 1.0;

            framesPerSecond = framesRenderedSoFar;
            drawCallsPerSecond = drawCallsSoFar;

            framesRenderedSoFar = 0;
            drawCallsSoFar = 0;

        }

    }

    @Override
    public void registeredUpdateListener() {

    }

    @Override
    public void unregisteredUpdateListener() {

    }

    public boolean isPixelPerfectViewport() {
        return pixelPerfectViewport;
    }

    public void setPixelPerfectViewport(boolean isPixelPerfect) {
        pixelPerfectViewport = isPixelPerfect;
    }

    // master draw method used in game loop
    // returns true if successfully drew to screen
    private void draw(Window window) {

        if (getFpsLimit() == -1 || glfwGetTime() - timeOfPreviousFrame > getFixedFrameRenderTime()) {
            setClearColor(0f, 0f, 0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glViewport(0, 0, resolutionFrameBuffer.getWidth(), resolutionFrameBuffer.getHeight());
            glBindFramebuffer(GL_FRAMEBUFFER, resolutionFrameBuffer.getHandle());
            setClearColor(120.0f / 255.0f, 167.0f / 255.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            timeOfPreviousFrame = glfwGetTime();
            render();
            framesRenderedSoFar++;


            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

            if (isPixelPerfectViewport()) {
                int pixelPerfectScaleX = window.getWidth() / resolutionFrameBuffer.getWidth();
                int pixelPerfectScaleY = window.getHeight() / resolutionFrameBuffer.getHeight();

                int pixelPerfectScale = Math.max(1, Math.min(pixelPerfectScaleX, pixelPerfectScaleY));
                int pixelPerfectSizeX = pixelPerfectScale * resolutionFrameBuffer.getWidth();
                int pixelPerfectSizeY = pixelPerfectScale * resolutionFrameBuffer.getHeight();
                int pixelPerfectPosX = window.getWidth() / 2 - pixelPerfectSizeX / 2;
                int pixelPerfectPosY = window.getHeight() / 2 - pixelPerfectSizeY / 2;


                glBlitFramebuffer(
                        0, 0, resolutionFrameBuffer.getWidth(), resolutionFrameBuffer.getHeight(),
                        pixelPerfectPosX, pixelPerfectPosY, pixelPerfectSizeX + pixelPerfectPosX, pixelPerfectSizeY + pixelPerfectPosY,
                        GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
                );

            } else {

                glBlitFramebuffer(
                        0, 0, resolutionFrameBuffer.getWidth(), resolutionFrameBuffer.getHeight(),
                        0, 0, window.getWidth(), window.getHeight(),
                        GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
                );

            }
            glfwSwapBuffers(window.getWindowHandle());
        }

    }

    public ResolutionFrameBuffer getResolutionFrameBuffer() {
        return resolutionFrameBuffer;
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

        Vector3d cameraPosition = getCamera().getTransform().getPosition();

        RENDER_LIST.sort((renderable1, renderable2) -> {

            // check render priority
            if (renderable1.getRenderPriority() > renderable2.getRenderPriority()) {
                return 1;
            } else if (renderable1.getRenderPriority() < renderable2.getRenderPriority()) {
                return -1;
            }


            // get distance from camera for the two renderables
			float distance1 = 0.0f;
			float distance2 = 0.0f;


			if (renderable1 instanceof IPositionOwner positionOwner1) {
				distance1 = (float) positionOwner1.getPosition().distance(cameraPosition);
			}

			if (renderable2 instanceof IPositionOwner positionOwner2) {
				distance2 = (float) positionOwner2.getPosition().distance(cameraPosition);
			}

            // check which of the renderables are opaque or not
            boolean hasTransparency1 = renderable1.hasTransparency();
            boolean hasTransparency2 = renderable2.hasTransparency();

            // sort
            // could be faster, but I'm too tired to deal with "Comparison method violates its general contract!"
            if (hasTransparency1 == hasTransparency2) {
                return -Float.compare(distance1, distance2);
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

        // TODO: implement dynamic way for shaders to listen for default uniforms like camera uniforms
        chunkMeshShaderProgram.use();

        chunkMeshShaderProgram.setUniform("vs_viewMatrix",Renderer.get().getCamera().getViewMatrix());
        chunkMeshShaderProgram.setUniform("vs_projectionMatrix",Renderer.get().getCamera().getProjectionMatrix());

        defaultShaderProgram.use();

        defaultShaderProgram.setUniform("vs_viewMatrix", viewMatrix);
        defaultShaderProgram.setUniform("vs_projectionMatrix", projectionMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, TextureManager.getBlockAtlasTexture().getTextureHandle());

        activateRenderMode();

        modelMatrix.identity();

        // render renderables
        sortRenderList();

        //glDisable(GL_BLEND);

        long renderTime = System.currentTimeMillis();
        for (IRenderable renderable : RENDER_LIST) {

            //if (renderable.hasTransparency()) continue;

            if (!renderable.shouldRender()) continue;

            if (ShaderProgram.getShaderProgramHandleInUse() != defaultShaderProgram.getProgramHandle()) {
                defaultShaderProgram.use();
            }

            defaultShaderProgram.setUniform("vs_modelMatrix", modelMatrix);

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
            debugString.append("\n\n").append(DebugInfo.getDrawCallsPerSecond());


            if (BackyardRocketry.getInstance().getPlayer() instanceof TestPlayer player) {
                debugString.append("\n\n").append(DebugInfo.getPlayerChunkPosition(player));
                debugString.append("\n\n").append(DebugInfo.getPlayerBlockPosition(player));
                debugString.append("\n\n").append(DebugInfo.getPlayerTargetBlockInfo(player));
            }

            TextRenderer.drawText(debugString.toString(), 0, 0, getGuiScale(), TextureManager.getTexture("debug_font"));
        }


        /*
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
                0, getBottomAnchor() - TextRenderer.getTextPixelHeight(controlsString.length() - controlsString.replace("\n", "").length()), getGuiScale(), TextureManager.getTexture("debug_font"));
        */
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
        return resolutionFrameBuffer.getHeight() / guiScale; // Window.get().getHeight() / guiScale;
    }

    public int getBottomAnchor(int customGuiScale) {
        return resolutionFrameBuffer.getHeight() / customGuiScale; // Window.get().getHeight() / customGuiScale;
    }

    public int getRightAnchor() {
        return resolutionFrameBuffer.getWidth() / guiScale;   // Window.get().getWidth() / guiScale;
    }

    public int getRightAnchor(int customGuiScale) {
        return resolutionFrameBuffer.getWidth() / customGuiScale; // Window.get().getWidth() / customGuiScale;
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
                resolutionFrameBuffer.getWidth(), // right
                0f, // bottom
                resolutionFrameBuffer.getHeight(), // top
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
                resolutionFrameBuffer.getWidth(), // right
                0f, // bottom
                resolutionFrameBuffer.getHeight(), // top
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

        resolutionFrameBuffer.clean();
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public int getDrawCallsPerSecond() {
        return drawCallsPerSecond;
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

    public double getFixedFrameRenderTime() {
        return fixedFrameRenderTime;
    }

    public void setFpsLimit(int limit) {

        if (limit < 0) {
            limit = -1;
        } else {
            limit = Math.max(1, limit);
        }

        fpsLimit = limit;
        fixedFrameRenderTime = 1.0 / fpsLimit;
    }

    public int getFpsLimit() {
        return fpsLimit;
    }

}
