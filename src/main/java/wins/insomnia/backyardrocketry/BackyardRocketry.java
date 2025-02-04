package wins.insomnia.backyardrocketry;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.scenes.GameplayScene;
import wins.insomnia.backyardrocketry.scenes.SceneManager;
import wins.insomnia.backyardrocketry.scenes.screens.LoadingScene;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.util.update.Updater;
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
    private final SceneManager SCENE_MANAGER;
    private final AudioManager AUDIO_MANAGER;


    public enum VersionPhase {
        ALPHA,
        BETA,
        RELEASE
    }


    private static final VersionPhase VERSION_PHASE = VersionPhase.ALPHA;
    private static final int VERSION_MAJOR = 1;
    private static final int VERSION_MINOR = 2;



    public BackyardRocketry() {
        UPDATER = new Updater();
        SCENE_MANAGER = new SceneManager();
        AUDIO_MANAGER = new AudioManager();
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
        BlockModelData.clean();
        getAudioManager().clean();
        window.clean();

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

        window = new Window(320 * 3, 240 * 3, "Backyard Rocketry");

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

        // window post init
        window.postInitialize();

        // create renderer
        renderer = new Renderer();

        // load assets
        SCENE_MANAGER.changeScene(new LoadingScene());

    }


    public IPlayer getClientPlayer() {
        return GameplayScene.getClientPlayer();
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

    public SceneManager getSceneManager() {
        return SCENE_MANAGER;
    }

    public static VersionPhase getVersionPhase() {
        return VERSION_PHASE;
    }

    public static int getVersionMajor() {
        return VERSION_MAJOR;
    }

    public static int getVersionMinor() {
        return VERSION_MINOR;
    }

    public static String getVersionString() {

        StringBuilder outString = new StringBuilder();
        switch (VERSION_PHASE) {
            case ALPHA -> outString.append("Alpha 0");
            case BETA -> outString.append("Beta 0");
            case RELEASE -> outString.append("1");
        }

        outString.append(".").append(VERSION_MAJOR);
        outString.append(".").append(VERSION_MINOR);

        return outString.toString();
    }

    public AudioManager getAudioManager() {
        return AUDIO_MANAGER;
    }
}
