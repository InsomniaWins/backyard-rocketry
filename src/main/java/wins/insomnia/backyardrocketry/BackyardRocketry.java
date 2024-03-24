package wins.insomnia.backyardrocketry;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import wins.insomnia.backyardrocketry.render.Mesh;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.ShaderProgram;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.world.World;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BackyardRocketry {

    private Window window;
    private Renderer renderer;
    private boolean running = false;


    ShaderProgram shaderProgram;



    public void run() {

        if (running) {
            throw new RuntimeException("Tried to run game while game is already running!");
        }

        running = true;

        init();




        // TODO: remove placeholder code here!!!

        // create shader program
        shaderProgram = new ShaderProgram("vertex.vert", "fragment.frag");

        float[] vertexArray = {
                0.5f,  0.5f, 0.0f,  // top right
                0.5f, -0.5f, 0.0f,  // bottom right
                -0.5f, -0.5f, 0.0f,  // bottom left
                -0.5f,  0.5f, 0.0f   // top left
        };
        int[] indexArray = {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);


        // set first parameter of shader
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        loop(vao);

        glDeleteProgram(shaderProgram.getProgram());


        // TODO: remove placeholder code here!!!



        glfwFreeCallbacks(window.getWindowHandle());
        glfwDestroyWindow(window.getWindowHandle());

        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    private void update(double deltaTime, long updateIndex) {





    }

    private void loop(int vao) {

        glClearColor(1f, 0f, 0f, 1f);

        double updateLimit = 1.0 / 60.0;
        double previousTime = glfwGetTime();
        double deltaTime = 0.0;

        long totalUpdates = 0L;
        long totalFrames = 0L;

        int updatesPerSecond = 0;
        int framesPerSecond = 0;

        double timer = previousTime;


        // game loop
        while (!glfwWindowShouldClose(window.getWindowHandle())) {

            double currentTime = glfwGetTime();
            deltaTime += currentTime - previousTime;

            glfwPollEvents();


            while (deltaTime >= updateLimit) {

                update(deltaTime, totalUpdates);
                updatesPerSecond += 1;
                totalUpdates += 1;
                deltaTime -= updateLimit;

            }

            draw(shaderProgram, vao);
            framesPerSecond += 1;
            totalFrames += 1;

            while (glfwGetTime() - timer > 1.0) {

                //System.out.println("FPS: " + framesPerSecond + ",     UPS: " + updatesPerSecond + ",     Total Frames: " + totalFrames + ",     Total Updates: " + totalUpdates);

                timer += 1.0;
                updatesPerSecond = 0;
                framesPerSecond = 0;
            }

            previousTime = currentTime;

        }

    }

    private void draw(ShaderProgram shaderProgram, int vao) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram.getProgram());
        glBindVertexArray(vao);

        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glfwSwapBuffers(window.getWindowHandle());

    }


    private void init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = new Window(300, 300, "Backyard Rocketry");

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window.getWindowHandle(), pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window.getWindowHandle(),
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window.getWindowHandle());

        // Enable v-sync
        glfwSwapInterval(0); // set to 1 for vsync

        // Make the window visible
        glfwShowWindow(window.getWindowHandle());
        GL.createCapabilities();

        // create renderer
        renderer = new Renderer();

    }

    public Window getWindow() {
        return window;
    }
}
