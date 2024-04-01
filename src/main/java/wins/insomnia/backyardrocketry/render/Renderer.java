package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.IUpdateListener;

import java.util.ArrayList;
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
    private final TextureManager TEXTURE_MANAGER;
    private final FontQuad FONT_QUAD;

    private int framesPerSecond = 0;

    private Texture texture;
    private Texture texture2;
    private Mesh mesh;

    private Matrix4f modelMatrix;



    public Renderer() {
        TEXTURE_MANAGER = new TextureManager();
        FONT_QUAD = new FontQuad();

        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");

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
        texture2 = new Texture("stone.png");

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
    }

    public void update(double deltaTime) {
        draw(BackyardRocketry.getInstance().getWindow());
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
            //glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0);
        }

        drawText("This is a test string!");
    }


    public void drawText(String text) {

        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);


        shaderProgram.use();
        glBindTexture(GL_TEXTURE_2D, TEXTURE_MANAGER.getFontTexture().getTextureHandle());
        glActiveTexture(GL_TEXTURE0);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        modelMatrix.identity();

        shaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        shaderProgram.setUniform("vs_viewMatrix", camera.getViewMatrix());// modelMatrix);
        shaderProgram.setUniform("vs_projectionMatrix", camera.getProjectionMatrix());
        shaderProgram.setUniform("vs_modelMatrix", modelMatrix);


        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(FONT_QUAD.getVao());
        glDrawElements(GL_TRIANGLES, FONT_QUAD.getIndexCount(), GL_UNSIGNED_INT, 0);


        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);
    }


    public void clean() {

        mesh.clean();
        FONT_QUAD.clean();
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
