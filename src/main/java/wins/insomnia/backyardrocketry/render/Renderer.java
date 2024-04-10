package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.DebugNoclipPlayer;
import wins.insomnia.backyardrocketry.util.IUpdateListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer implements IUpdateListener {
    private Camera camera;
    private ShaderProgram shaderProgram;
    private ShaderProgram textShaderProgram;
    private final TextureManager TEXTURE_MANAGER;
    private final FontMesh FONT_MESH;
    private final ArrayList<WeakReference<IRenderable>> RENDER_LIST;
    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs
    private double fpsTimer = 0.0;
    private Texture texture;
    private Matrix4f modelMatrix;



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
        textShaderProgram = new ShaderProgram("text.vert", "text.frag");

        modelMatrix = new Matrix4f().identity();

        texture = new Texture("cobblestone.png");

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);

        FONT_MESH = new FontMesh();

        glClearColor(0.25882352941176473f, 0.6901960784313725f, 1f, 1f);

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

    public void addRenderable(IRenderable renderable) {

        RENDER_LIST.add(new WeakReference<>(renderable));

    }

    private void render() {

        camera.updateProjectionMatrix();
        camera.updateViewMatrix();

        // placeholder render code
        shaderProgram.use();
        shaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        shaderProgram.setUniform("vs_viewMatrix", camera.getViewMatrix());
        shaderProgram.setUniform("vs_projectionMatrix", camera.getProjectionMatrix());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());

        modelMatrix.identity();

        glEnable(GL_CULL_FACE);

        for (WeakReference<IRenderable> renderableWeakReference : RENDER_LIST) {
            IRenderable renderable = renderableWeakReference.get();

            if (renderable == null) continue;

            if (renderable.shouldRender()) {

                renderable.render();
                shaderProgram.setUniform("vs_modelMatrix", modelMatrix);

            }

        }

        if (BackyardRocketry.getInstance().getPlayer() instanceof DebugNoclipPlayer player) {

            String debugString = String.format(
                    "Memory Usage: %sMiB / %sMiB\nFPS: %d\nFixed UPS: %d\nX: %f\nY: %f\nZ: %f\nRot X: %f\nRot Y: %f\nRot Z: %f",
                    Runtime.getRuntime().freeMemory() / 1_048_576,
                    Runtime.getRuntime().totalMemory() / 1_048_576,
                    getFramesPerSecond(),
                    BackyardRocketry.getInstance().getUpdater().getUpdatesPerSecond(),
                    player.getTransform().getPosition().x,
                    player.getTransform().getPosition().y,
                    player.getTransform().getPosition().z,
                    player.getTransform().getRotation().x,
                    player.getTransform().getRotation().y,
                    player.getTransform().getRotation().z
            );
            drawText(debugString);
        }
    }


    public void drawText(String text) {

        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

        FONT_MESH.setText(text);


        textShaderProgram.use();
        glBindTexture(GL_TEXTURE_2D, TEXTURE_MANAGER.getFontTexture().getTextureHandle());
        glActiveTexture(GL_TEXTURE0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        modelMatrix.identity();

        textShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        textShaderProgram.setUniform("vs_projectionMatrix", modelMatrix.ortho(0f, BackyardRocketry.getInstance().getWindow().getWidth(), 0f, BackyardRocketry.getInstance().getWindow().getHeight(), 0.01f, 100f));

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(FONT_MESH.getVao());

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);


        // TODO: Add back-face culling to text rendering!!!

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glDrawElements(GL_TRIANGLES, FONT_MESH.getIndexCount(), GL_UNSIGNED_INT, 0);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glBindVertexArray(0);



        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);
    }


    public void clean() {

        for (WeakReference<IRenderable> renderableWeakReference : RENDER_LIST) {

            IRenderable renderable = renderableWeakReference.get();

            if (renderable == null) continue;

            if (!renderable.isClean()) {
                renderable.clean();
            }

        }

        FONT_MESH.clean();
        TEXTURE_MANAGER.clean();
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
