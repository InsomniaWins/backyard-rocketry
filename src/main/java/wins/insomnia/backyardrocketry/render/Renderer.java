package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.util.IUpdateListener;

import java.util.ArrayList;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    public final ArrayList<IRenderable> renderables = new ArrayList<>();

    private Camera camera;
    private ShaderProgram shaderProgram;

    private Texture texture;
    private Texture texture2;
    private Mesh mesh;
    private Mesh mesh2;

    private Matrix4f modelMatrix;



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
        modelMatrix = new Matrix4f().identity();

        texture = new Texture("cobblestone.png");
        texture2 = new Texture("stone.png");
    }


    // master draw method used in game loop
    public void draw(Window window) {
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


        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());

        modelMatrix.identity();

        if (mesh.getVao() > -1) {
            shaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            glBindVertexArray(mesh.getVao());
            glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0);
        }

        glBindTexture(GL_TEXTURE_2D, texture2.getTextureHandle());



        modelMatrix.identity().translate(0,1f,0);

        if (mesh2.getVao() > -1) {
            shaderProgram.setUniform("vs_modelMatrix", modelMatrix);
            glBindVertexArray(mesh2.getVao());
            glDrawElements(GL_TRIANGLES, mesh2.getIndexCount(), GL_UNSIGNED_INT, 0);
        }
    }


    public void clean() {

        mesh.clean();
        glDeleteProgram(shaderProgram.getProgramHandle());

    }


    public Camera getCamera() {
        return camera;
    }
}
