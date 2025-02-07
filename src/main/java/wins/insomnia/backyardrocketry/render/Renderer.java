package wins.insomnia.backyardrocketry.render;

import org.joml.*;
import org.joml.Math;
import org.joml.primitives.Rectanglei;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.gui.GuiMesh;
import wins.insomnia.backyardrocketry.render.gui.IGuiRenderable;
import wins.insomnia.backyardrocketry.render.text.FontMesh;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
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

        TargetBlockOutlineMesh.init();
    }


    public Vector3i worldPositionToGuiPosition(Camera camera, float x, float y, float z) {

        Matrix4f modelMatrix = new Matrix4f().identity();
        modelMatrix.translate(x, y, z);

        Matrix4f viewMatrix = new Matrix4f(camera.getViewMatrix());

        Matrix4f projectionMatrix = new Matrix4f(camera.getProjectionMatrix());

        Matrix4f screenMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);
        screenMatrix.mul(modelMatrix);


        float cubeW = screenMatrix.m33();
        float cubeX = screenMatrix.m30() / cubeW;
        float cubeY = screenMatrix.m31() / cubeW;
        float cubeZ = screenMatrix.m32() / cubeW;

        int viewportWidth = Window.get().getResolutionFrameBuffer().getWidth();
        int viewportHeight = Window.get().getResolutionFrameBuffer().getHeight();

        float outX = viewportWidth * (cubeX + 1) / 2;
        float outY = viewportHeight - viewportHeight * (cubeY + 1) / 2;

        Vector3i returnVector = new Vector3i((int) outX, (int) outY, 0);

        if (cubeZ < -1f || cubeZ > 1f) {

            returnVector.x = -1000000;
            returnVector.y = -1000000;
            returnVector.z = 1;

        }

        return returnVector;

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



    // master draw method used in game loop
    // returns true if successfully drew to screen
    private void draw(Window window) {

        if (getFpsLimit() == -1 || glfwGetTime() - timeOfPreviousFrame > getFixedFrameRenderTime()) {
            setClearColor(0f, 0f, 0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glViewport(0, 0, window.getResolutionFrameBuffer().getWidth(), window.getResolutionFrameBuffer().getHeight());
            glBindFramebuffer(GL_FRAMEBUFFER, window.getResolutionFrameBuffer().getHandle());
            setClearColor(120.0f / 255.0f, 167.0f / 255.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            timeOfPreviousFrame = glfwGetTime();
            render();
            framesRenderedSoFar++;


            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);

            Rectanglei viewportDimensions = window.getViewportDimensions();

            glBlitFramebuffer(
                    0, 0, window.getResolutionFrameBuffer().getWidth(), window.getResolutionFrameBuffer().getHeight(),
                    viewportDimensions.minX, viewportDimensions.minY, viewportDimensions.maxX, viewportDimensions.maxY,
                    GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST
            );

            glfwSwapBuffers(window.getWindowHandle());
        }

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


        GUI_RENDER_LIST.sort((renderable1, renderable2) -> {

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

        if (BackyardRocketry.getInstance().getClientPlayer() instanceof TestPlayer player) {

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

                getShaderProgram().setUniform("fs_lightingEnabled", false);
                getShaderProgram().setUniform("fs_color", new Vector4f(1f, 1f, 1f, TargetBlockOutlineMesh.getOutlineAlpha()));

                TargetBlockOutlineMesh.get(raycastResult.getFace()).render(GL_LINES);

                getShaderProgram().setUniform("fs_color", new Vector4f(1f, 1f, 1f, 1f));

                getShaderProgram().setUniform("fs_lightingEnabled", true);

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
            debugString.append("Render: ").append(renderTime).append("ms");
            debugString.append("\n").append(DebugInfo.getMemoryUsage());
            debugString.append("\n").append(DebugInfo.getFramesPerSecond());
            debugString.append("\n").append(DebugInfo.getDrawCallsPerSecond());


            if (BackyardRocketry.getInstance().getClientPlayer() instanceof TestPlayer player) {
                debugString.append("\n").append(DebugInfo.getPlayerChunkPosition(player));
                debugString.append("\n").append(DebugInfo.getPlayerBlockPosition(player));
                debugString.append("\n").append(DebugInfo.getPlayerTargetBlockInfo(player));
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
        return Window.get().getResolutionFrameBuffer().getHeight() / guiScale; // Window.get().getHeight() / guiScale;
    }

    public int getBottomAnchor(int customGuiScale) {
        return Window.get().getResolutionFrameBuffer().getHeight() / customGuiScale; // Window.get().getHeight() / customGuiScale;
    }

    public int getRightAnchor() {
        return Window.get().getResolutionFrameBuffer().getWidth() / guiScale;   // Window.get().getWidth() / guiScale;
    }

    public int getRightAnchor(int customGuiScale) {
        return Window.get().getResolutionFrameBuffer().getWidth() / customGuiScale; // Window.get().getWidth() / customGuiScale;
    }

    public int getRenderMode() {
        return renderMode;
    }


    public void drawGuiMesh(Mesh mesh, Texture texture, int guiX, int guiY) {
        drawGuiMesh(mesh, texture, guiX, guiY, 0f, 0f, 0f, 1f, 0f, 0f, 0f);
    }

    public void drawGuiMesh(Mesh mesh, Texture texture, int guiX, int guiY, float rotationX, float rotationY, float rotationZ, float scale, float originX, float originY, float originZ) {

        int resolutionWidth = Window.get().getResolutionFrameBuffer().getWidth();
        int resolutionHeight = Window.get().getResolutionFrameBuffer().getHeight();

        float gameWindowAspect = resolutionWidth / (float) resolutionHeight;
        float modelAspectScale = (750f / resolutionHeight);

        scale = scale * modelAspectScale * getGuiScale();

        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());

        for (int i = 0; i < 10; i++) {


            if (mesh == null || mesh.isClean()) {
                continue;
            }

            Renderer.get().getModelMatrix().identity()
                    .translate(0, 0, -1f)
                    .scale(1f, 1f, 0f)
                    .rotateX(rotationX)
                    .rotateY(rotationY)
                    .rotateY(rotationZ)
                    .scale(
                            scale,
                            scale,
                            scale
                    )
                    .translate(originX, originY, originZ);

            Vector2f viewportOffset = new Vector2f(
                    2f * (((getGuiScale() * guiX) / (float) resolutionWidth) - 0.5f),
                    2f * (((getGuiScale() * guiY) / (float) resolutionHeight) - 0.5f)
            );

            Matrix4f projectionMatrix = new Matrix4f().setPerspective(70f, gameWindowAspect, 0.01f, 1f);
            projectionMatrix.m20(-viewportOffset.x);
            projectionMatrix.m21(viewportOffset.y);

            Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", projectionMatrix);
            Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", new Matrix4f().identity());
            Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            glDisable(GL_DEPTH);
            mesh.render();
            glEnable(GL_DEPTH);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getCamera().getProjectionMatrix());
            Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", Renderer.get().getCamera().getViewMatrix());

        }

    }


    public void drawGuiTextureNineSlice(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int sliceSize) {
        drawGuiTextureNineSlice(texture, guiX, guiY, guiWidth, guiHeight, sliceSize, false);
    }

    public void drawGuiTextureTiled(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int textureX, int textureY, int textureWidth, int textureHeight) {

        int tileAmountX = (int) (guiWidth / (float) textureWidth);
        int tileAmountY = (int) (guiHeight / (float) textureHeight);

        int tileRemainderX = guiWidth % textureWidth;
        int tileRemainderY = guiHeight % textureHeight;

        for (int x = 0; x < tileAmountX; x++) {
            for (int y = 0; y < tileAmountY; y++) {

                drawGuiTextureClipped(
                        texture,
                        guiX + x * textureWidth,
                        guiY + y * textureHeight,
                        textureWidth,
                        textureHeight,
                        textureX,
                        textureY,
                        textureWidth,
                        textureHeight
                );

                if (x == tileAmountX - 1 && tileRemainderX > 0) {

                    drawGuiTextureClipped(
                            texture,
                            guiX + x * textureWidth + textureWidth,
                            guiY + y * textureHeight,
                            tileRemainderX,
                            textureHeight,
                            textureX,
                            textureY,
                            tileRemainderX,
                            textureHeight
                    );

                }

                if (y == tileAmountY - 1 && tileRemainderY > 0) {

                    drawGuiTextureClipped(
                            texture,
                            guiX + x * textureWidth,
                            guiY + y * textureHeight + textureHeight,
                            textureWidth,
                            tileRemainderY,
                            textureX,
                            textureY,
                            textureWidth,
                            tileRemainderY
                    );

                }

                if (y == tileAmountY - 1 && x == tileAmountX - 1 && tileRemainderX > 0 && tileRemainderY > 0) {

                    drawGuiTextureClipped(
                            texture,
                            guiX + x * textureWidth + textureWidth,
                            guiY + y * textureHeight + textureHeight,
                            tileRemainderX,
                            tileRemainderY,
                            textureX,
                            textureY,
                            tileRemainderX,
                            tileRemainderY
                    );

                }

            }
        }

    }

    public void drawGuiTextureTiled(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight) {
        drawGuiTextureTiled(texture, guiX, guiY, guiWidth, guiHeight, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public void drawGuiTextureNineSlice(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int sliceSize, boolean drawCenter) {

        // center
        if (drawCenter) {

            drawGuiTextureTiled(
                    texture,
                    guiX + sliceSize,
                    guiY + sliceSize,
                    guiWidth - 2 * sliceSize,
                    guiHeight - 2 * sliceSize,
                    sliceSize,
                    sliceSize,
                    sliceSize,
                    sliceSize
            );

        }


        // vertical sides
        int verticalTileAmount = guiHeight / sliceSize - 2;
        int verticalTileRemainder = guiHeight % sliceSize;
        for (int i = 0; i < verticalTileAmount; i++) {

            // left
            drawGuiTextureClipped(texture, guiX, guiY + i * sliceSize + sliceSize, sliceSize, sliceSize, 0, sliceSize, sliceSize, sliceSize);

            // right
            drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + i * sliceSize + sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize, sliceSize, sliceSize);


            if (verticalTileRemainder > 0 && i == guiHeight / sliceSize - 3) {

                // left
                drawGuiTextureClipped(texture, guiX, guiY + i * sliceSize + sliceSize + sliceSize, sliceSize, verticalTileRemainder, 0, sliceSize, sliceSize, verticalTileRemainder);

                // right
                drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + i * sliceSize + sliceSize + sliceSize, sliceSize, verticalTileRemainder, sliceSize * 2, sliceSize, sliceSize, verticalTileRemainder);

            }

        }


        // horizontal sides
        int horizontalTileAmount = guiWidth / sliceSize - 2;
        int horizontalTileRemainder = guiWidth % sliceSize;
        for (int i = 0; i < horizontalTileAmount; i++) {
            // top
            drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize, guiY, sliceSize, sliceSize, sliceSize, 0, sliceSize, sliceSize);

            // bottom
            drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize, guiY + guiHeight - sliceSize, sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize, sliceSize);

            if (horizontalTileRemainder > 0 && i == guiWidth / sliceSize - 3) {

                // top
                drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize + sliceSize, guiY, horizontalTileRemainder, sliceSize, sliceSize, 0, horizontalTileRemainder, sliceSize);

                // bottom
                drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize + sliceSize, guiY + guiHeight - sliceSize, horizontalTileRemainder, sliceSize, sliceSize, sliceSize * 2, horizontalTileRemainder, sliceSize);

            }

        }


        // top left
        drawGuiTextureClipped(texture, guiX, guiY, sliceSize, sliceSize, 0, 0, sliceSize, sliceSize);

        // top right
        drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY, sliceSize, sliceSize, sliceSize * 2, 0, sliceSize, sliceSize);

        // bottom left
        drawGuiTextureClipped(texture, guiX, guiY + guiHeight - sliceSize, sliceSize, sliceSize, 0, sliceSize * 2, sliceSize, sliceSize);

        // bottom right
        drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + guiHeight - sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize * 2, sliceSize, sliceSize);
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
                Window.get().getResolutionFrameBuffer().getWidth(), // right
                0f, // bottom
                Window.get().getResolutionFrameBuffer().getHeight(), // top
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
                Window.get().getResolutionFrameBuffer().getWidth(), // right
                0f, // bottom
                Window.get().getResolutionFrameBuffer().getHeight(), // top
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
        TargetBlockOutlineMesh.clean();

        Iterator<Map.Entry<String, ShaderProgram>> shaderIterator = SHADER_PROGRAM_MAP.entrySet().iterator();
        while (shaderIterator.hasNext()) {
            Map.Entry<String, ShaderProgram> shaderProgramEntry = shaderIterator.next();

            ShaderProgram shaderProgram = shaderProgramEntry.getValue();

            if (shaderProgram != null) {

                glDeleteProgram(shaderProgram.getProgramHandle());
                shaderIterator.remove();

            }
        }

        Window.get().getResolutionFrameBuffer().clean();
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
