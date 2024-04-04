package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.DebugNoclipPlayer;
import wins.insomnia.backyardrocketry.util.IPlayer;
import wins.insomnia.backyardrocketry.util.IUpdateListener;

import java.util.ArrayList;

import static org.joml.Math.*;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer implements IUpdateListener {

    public final ArrayList<IRenderable> renderables = new ArrayList<>();

    private Camera camera;
    private ShaderProgram shaderProgram;
    private ShaderProgram textShaderProgram;
    private final TextureManager TEXTURE_MANAGER;
    private final FontMesh FONT_MESH;

    private int framesPerSecond = 0;
    private int framesRenderedSoFar = 0; // frames rendered before fps-polling occurs
    private double fpsTimer = 0.0;

    private Texture texture;
    private Mesh mesh;

    private Matrix4f modelMatrix;



    public Renderer() {
        TEXTURE_MANAGER = new TextureManager();

        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");
        textShaderProgram = new ShaderProgram("text.vert", "text.frag");

        mesh = new Mesh(
                new float[] {
                        0.5f,  0.5f, 0.5f, 1.0f, 1.0f, // top right front
                        0.5f, -0.5f, 0.5f, 1.0f, 0.0f, // bottom right front
                        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, // bottom left front
                        -0.5f,  0.5f, 0.5f,  0.0f, 1.0f, // top left front
                        0.5f,  0.5f, -0.5f, 1.0f, 1.0f, // top right back
                        0.5f, -0.5f, -0.5f, 1.0f, 0.0f, // bottom right back
                        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, // bottom left back
                        -0.5f,  0.5f, -0.5f,  0.0f, 1.0f, // top left back
                },

                new int[] {
                        0, 1, 3,
                        1, 2, 3,
                        4, 5, 7,
                        5, 6, 7
                }
        );
        modelMatrix = new Matrix4f().identity();

        texture = new Texture("cobblestone.png");

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);

        FONT_MESH = new FontMesh();
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

        if (mesh.getVao() > -1) {
            shaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            glBindVertexArray(mesh.getVao());

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0);
        }

        if (BackyardRocketry.getInstance().getPlayer() instanceof DebugNoclipPlayer player) {

            Vector3f eulerRotation = new Vector3f();
            player.getTransform().getRotation().getEulerAnglesXYZ(eulerRotation);

            float cosineYawValue = signum(-player.getTransform().getRotation().y) * (1f - abs(sin(player.getTransform().getRotation().y)));
            float sineYawValue = sin(player.getTransform().getRotation().y);

            String debugString = String.format(
                    "Memory Usage: %sMiB / %sMiB\nFPS: %d\nFixed UPS: %d\nX: %f\nY: %f\nZ: %f\nRot X: %f\nRot Y: %f\nRot Z: %f\nRot W: %f",
                    Runtime.getRuntime().freeMemory() / 1_048_576,
                    Runtime.getRuntime().totalMemory() / 1_048_576,
                    getFramesPerSecond(),
                    BackyardRocketry.getInstance().getUpdater().getUpdatesPerSecond(),
                    player.getTransform().getPosition().x,
                    player.getTransform().getPosition().y,
                    player.getTransform().getPosition().z,
                    player.getTransform().getRotation().x,
                    player.getTransform().getRotation().y,
                    player.getTransform().getRotation().z,
                    player.getTransform().getRotation().w
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

        glDisable(GL_DEPTH_TEST);

        glDrawElements(GL_TRIANGLES, FONT_MESH.getIndexCount(), GL_UNSIGNED_INT, 0);

        glEnable(GL_DEPTH_TEST);
        glBindVertexArray(0);



        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);
    }


    public void clean() {

        mesh.clean();
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
}
