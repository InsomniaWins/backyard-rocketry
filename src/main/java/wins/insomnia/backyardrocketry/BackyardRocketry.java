package wins.insomnia.backyardrocketry;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import wins.insomnia.backyardrocketry.render.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BackyardRocketry {

    private Window window;
    private Renderer renderer;
    private boolean running = false;

    private int framesPerSecond = 0;
    private int updatesPerSecond = 0;

    public void run() {

        if (running) {
            throw new RuntimeException("Tried to run game while game is already running!");
        }

        running = true;

        init();
        loop();

        renderer.clean();

        glfwFreeCallbacks(window.getWindowHandle());
        glfwDestroyWindow(window.getWindowHandle());

        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    // this is the game loop tick/process/update hook/listener
    private void update(double deltaTime) {
        renderer.update(deltaTime);
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

        window = new Window(900, 900, "Backyard Rocketry");

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window.getWindowHandle(), pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidmode == null) {
                throw new RuntimeException("Failed to create vidmode!");
            }

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

    private void loop() {
        double secondTimer = 0.0;
        final double deltaTime = 1.0 / 60.0;

        glfwSetTime(0.0);
        double currentTime = glfwGetTime();
        double accumulator = 0.0;

        while (!glfwWindowShouldClose(window.getWindowHandle())) {

            glfwPollEvents();

            double newTime = glfwGetTime();
            double timeSincePreviousLoopIteration = newTime - currentTime;
            secondTimer += timeSincePreviousLoopIteration;
            currentTime = newTime;

            accumulator += timeSincePreviousLoopIteration;

            while (accumulator >= deltaTime) {
                update(deltaTime);
                accumulator -= deltaTime;
                updatesPerSecond += 1;
            }

            renderer.draw(window);
            framesPerSecond += 1;

            if (secondTimer >= 1.0) {

                framesPerSecond = 0;
                updatesPerSecond = 0;
                secondTimer = 0.0;
            }
        }
    }

    public Window getWindow() {
        return window;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public int getUpdatesPerSecond() {
        return updatesPerSecond;
    }
}
