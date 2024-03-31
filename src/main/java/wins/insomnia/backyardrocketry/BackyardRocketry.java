package wins.insomnia.backyardrocketry;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.IUpdateListener;
import wins.insomnia.backyardrocketry.util.KeyboardInput;

import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BackyardRocketry {

    private static BackyardRocketry instance;
    private Window window;
    private Renderer renderer;
    private KeyboardInput keyboardInput;
    private boolean running = false;
    private final List<WeakReference<IUpdateListener>> UPDATE_LISTENERS;

    private int framesPerSecond = 0;
    private int updatesPerSecond = 0;

    public BackyardRocketry() {

        UPDATE_LISTENERS = new ArrayList<>();

    }

    public void registerUpdateListener(IUpdateListener updateListener) {
        UPDATE_LISTENERS.add(new WeakReference<>(updateListener));
    }

    public void run() {

        if (running) {
            throw new RuntimeException("Tried to run game while game is already running!");
        }

        running = true;
        instance = this;

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

        for (WeakReference<IUpdateListener> updateListener : UPDATE_LISTENERS) {
            updateListener.get().update(deltaTime);
        }

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
        }

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

        window = new Window(600, 600, "Backyard Rocketry");

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

        // create keyboard and mouse input
        keyboardInput = new KeyboardInput(window.getWindowHandle());

        // create renderer
        renderer = new Renderer();
        registerUpdateListener(renderer);

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
                keyboardInput.updateKeyStates();
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
    public KeyboardInput getKeyboardInput() {
        return keyboardInput;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public int getUpdatesPerSecond() {
        return updatesPerSecond;
    }

    public static BackyardRocketry getInstance() {
        return instance;
    }
}
