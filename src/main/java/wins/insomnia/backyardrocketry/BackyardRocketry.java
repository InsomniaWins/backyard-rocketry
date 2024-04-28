package wins.insomnia.backyardrocketry;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.*;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.world.World;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class BackyardRocketry {

    private static BackyardRocketry instance;
    private Window window;
    private Renderer renderer;
    private KeyboardInput keyboardInput;
    private MouseInput mouseInput;
    private boolean running = false;
    private final Updater UPDATER;



    // TODO: REMOVE PLACEHOLDER CODE!

    private IPlayer player;
    private World world;





    public BackyardRocketry() {
        UPDATER = new Updater();
    }

    public void run() {

        if (running) {
            throw new RuntimeException("Tried to run game while game is already running!");
        }

        running = true;
        instance = this;

        init();
        UPDATER.loop();

        renderer.clean();

        glfwFreeCallbacks(window.getWindowHandle());
        glfwDestroyWindow(window.getWindowHandle());

        glfwTerminate();
        glfwSetErrorCallback(null).free();

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

        window = new Window(900, 600, "Backyard Rocketry");

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
        mouseInput = new MouseInput(window.getWindowHandle());

        // create renderer
        renderer = new Renderer();

        // load block models
        BlockModelData.loadBlockModels();
        BlockModelData.loadBlockStates();


        // TODO: REPLACE PLACEHOLDER CODE!
        world = new World();

        player = new TestPlayer(world);
        ((TestPlayer) player).getTransform().getPosition().set(7.5,20,7.5);

        world.generate();

        BoundingBox bb1 = new BoundingBox(-3,-3,-3,5,5,5);
        BoundingBox bb2 = new BoundingBox(-5,-5,-5,5,5,5);

        Collision.AABBCollisionResultType result = bb2.collideWithBoundingBox(bb1);
        if (result == Collision.AABBCollisionResultType.INSIDE) {
            System.out.println("Collision inside!");
        } else if (result == Collision.AABBCollisionResultType.CLIPPING) {
            System.out.println("Collision clipping!");
        } else if (result == Collision.AABBCollisionResultType.CONTAINS) {
            System.out.println("Collision contains!");
        } else {
            System.out.println("Collision outside!");
        }

    }

    public IPlayer getPlayer() {
        return player;
    }

    public Window getWindow() {
        return window;
    }
    public KeyboardInput getKeyboardInput() {
        return keyboardInput;
    }
    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public static BackyardRocketry getInstance() {
        return instance;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public Updater getUpdater() {
        return UPDATER;
    }
}
