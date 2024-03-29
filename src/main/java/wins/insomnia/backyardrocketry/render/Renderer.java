package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import java.util.ArrayList;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    public final ArrayList<IRenderable> renderables = new ArrayList<>();

    private Camera camera;
    private ShaderProgram shaderProgram;

    private Texture texture;
    private int vao = -1;
    private int vao2 = -1;
    private Matrix4f modelMatrix1;
    private Matrix4f modelMatrix2;




    public Renderer() {

        camera = new Camera();

        glClearColor(1f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);

        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");

        float[] vertexArray = {
                0.5f,  0.5f, 0.0f, 1.0f, 1.0f, // top right
                0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
                -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
                -0.5f,  0.5f, 0.0f,  0.0f, 1.0f // top left
        };
        int[] indexArray = {
                0, 1, 3   // first triangle
        };

        int[] indexArray2 = {
                1, 2, 3    // second triangle
        };


        vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);



        vao2 = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao2);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray2, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        texture = new Texture("cobblestone.png");

        modelMatrix1 = new Matrix4f().identity();
        modelMatrix2 = new Matrix4f().identity();


    }

    // master draw method used in game loop
    public void draw(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        render();

        glfwSwapBuffers(window.getWindowHandle());
    }

    public void update(double deltaTime) {

        modelMatrix1.rotate((float) deltaTime, 0f, 0f, 1f);
        modelMatrix2.rotate((float) deltaTime, 0f, 0f, 1f);


        modelMatrix1.rotate((float) deltaTime, 1f, 0f, 0f);
        modelMatrix2.rotate((float) deltaTime, 1f, 0f, 0f);

    }

    private void render() {

        // placeholder render code
        shaderProgram.use();
        shaderProgram.setUniform("fs_texture", GL_TEXTURE0);
        shaderProgram.setUniform("vs_viewMatrix", camera.getViewMatrix());
        shaderProgram.setUniform("vs_projectionMatrix", camera.getProjectionMatrix());


        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());


        if (vao > -1) {
            shaderProgram.setUniform("vs_modelMatrix", modelMatrix1);
            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
        }


        if (vao2 > -1) {
            shaderProgram.setUniform("vs_modelMatrix", modelMatrix2);
            glBindVertexArray(vao2);
            glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);
        }

    }

    public void clean() {

        glDeleteProgram(shaderProgram.getProgramHandle());

    }



}
