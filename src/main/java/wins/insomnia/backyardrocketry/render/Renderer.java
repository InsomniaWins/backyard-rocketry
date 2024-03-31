package wins.insomnia.backyardrocketry.render;

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

    private Texture texture;
    private Texture texture2;
    private Mesh mesh;
    private Mesh mesh2;




    public Renderer() {
        camera = new Camera();
        camera.getTransform().getPosition().set(0f, 0f, -3f);

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");

        mesh = new Mesh(
                new float[] {
                    0.5f,  0.5f, 0.0f, 1.0f, 1.0f, // top right
                    0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
                    -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
                    -0.5f,  0.5f, 0.0f,  0.0f, 1.0f // top left
                },

                new int[] {
                        0, 1, 3,
                        1, 2, 3
                }
        );

        mesh2 = new Mesh(
                new float[] {
                        0.5f,  0.5f, 0.0f, 1.0f, 1.0f, // top right
                        0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
                        -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
                        -0.5f,  0.5f, 0.0f,  0.0f, 1.0f // top left
                },

                new int[] {
                        0, 1, 3,
                        1, 2, 3
                }
        );
        mesh2.getModelMatrix().translate(0, 1f, 0);

        texture = new Texture("cobblestone.png");
        texture2 = new Texture("stone.png");
    }


    // master draw method used in game loop
    public void draw(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        render();

        glfwSwapBuffers(window.getWindowHandle());
    }


    public void update(double deltaTime) {

        //mesh.getModelMatrix().rotate((float) deltaTime, 1f, 1f, 1f);
        camera.update(deltaTime);

    }


    private void render() {

        camera.updateProjectionMatrix();
        camera.updateViewMatrix();

        // placeholder render code
        shaderProgram.use();
        shaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        shaderProgram.setUniform("vs_viewMatrix", camera.getViewMatrix());
        shaderProgram.setUniform("vs_projectionMatrix", camera.getProjectionMatrix());


        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());


        if (mesh.getVao() > -1) {
            shaderProgram.setUniform("vs_modelMatrix", mesh.getModelMatrix());
            glBindVertexArray(mesh.getVao());
            glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0);
        }

        glBindTexture(GL_TEXTURE_2D, texture2.getTextureHandle());

        if (mesh2.getVao() > -1) {
            shaderProgram.setUniform("vs_modelMatrix", mesh2.getModelMatrix());
            glBindVertexArray(mesh2.getVao());
            glDrawElements(GL_TRIANGLES, mesh2.getIndexCount(), GL_UNSIGNED_INT, 0);
        }
    }


    public void clean() {

        mesh.clean();
        glDeleteProgram(shaderProgram.getProgramHandle());

    }



}
