package wins.insomnia.backyardrocketry.render;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.input.IInputCallback;
import wins.insomnia.backyardrocketry.util.input.InputEvent;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.KeyboardInputEvent;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    public final int DEFAULT_WIDTH;
    public final int DEFAULT_HEIGHT;
    private final long WINDOW_HANDLE;
    private int width;
    private int height;
    private boolean pixelPerfectViewport = false;
    private ResolutionFrameBuffer resolutionFrameBuffer;
    private IInputCallback keyboardInputCallback;
    private boolean fullscreen = false;


    public Window(int width, int height, String title) {

        // make the window

        this.width = width;
        this.height = height;

        DEFAULT_WIDTH = width;
        DEFAULT_HEIGHT = height;

        WINDOW_HANDLE = glfwCreateWindow(width, height, title, NULL, NULL);
        if (WINDOW_HANDLE == NULL) {

            throw new RuntimeException("Failed to make a window!");

        }


        // setup callback for window
        glfwSetWindowSizeCallback(WINDOW_HANDLE, this::windowResizeCallback);

    }

    public void postInitialize() {
        setResolution(320, 240, true);

        keyboardInputCallback = inputEvent -> {

			if (!(inputEvent instanceof KeyboardInputEvent keyboardInputEvent)) return;

			if (keyboardInputEvent.getKey() == GLFW_KEY_F11 && keyboardInputEvent.isJustPressed()) {
				keyboardInputEvent.consume();

                setFullscreen(!isFullscreen());

			}

		};

        KeyboardInput.get().registerInputCallback(keyboardInputCallback);

    }


    public void setFullscreen(boolean value) {
        fullscreen = value;

        long monitorHandle = glfwGetPrimaryMonitor();

        if (fullscreen) {

            glfwSetWindowAttrib(getWindowHandle(), GLFW_DECORATED, GLFW_FALSE);
            glfwSetWindowSize(getWindowHandle(), 1920, 1080);
            glfwSetWindowPos(getWindowHandle(), 0, 0);



        } else {

            glfwSetWindowSize(getWindowHandle(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

            GLFWVidMode videoMode = glfwGetVideoMode(monitorHandle);

            int displayWidth = 32;
            int displayHeight = 32;

            if (videoMode != null) {

                displayWidth = videoMode.width();
                displayHeight = videoMode.height();

            }

            int centerX = (int) (displayWidth * 0.5 - DEFAULT_WIDTH * 0.5);
            int centerY = (int) (displayHeight * 0.5 - DEFAULT_HEIGHT * 0.5);

            glfwSetWindowPos(getWindowHandle(), centerX, centerY);
            glfwSetWindowAttrib(getWindowHandle(), GLFW_DECORATED, GLFW_TRUE);

        }
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void clean() {
        KeyboardInput.get().unregisterInputCallback(keyboardInputCallback);
        getResolutionFrameBuffer().clean();
    }

    public void setResolution(int width, int height, boolean pixelPerfect) {

        if (resolutionFrameBuffer != null) {
            resolutionFrameBuffer.clean();
        }

        resolutionFrameBuffer = new ResolutionFrameBuffer(width, height);
        setPixelPerfectViewport(pixelPerfect);

    }

    public void setResolution(int width, int height) {

        setResolution(width, height, isPixelPerfectViewport());

    }

    public ResolutionFrameBuffer getResolutionFrameBuffer() {
        return resolutionFrameBuffer;
    }

    public boolean isPixelPerfectViewport() {
        return pixelPerfectViewport;
    }

    public void setPixelPerfectViewport(boolean isPixelPerfect) {
        pixelPerfectViewport = isPixelPerfect;
    }

    private void windowResizeCallback(long windowHandle, int width, int height) {

        Window window = BackyardRocketry.getInstance().getWindow();

        window.width = width;
        window.height = height;

        glViewport(0, 0, window.width, window.height);
    }

    public long getWindowHandle() {
        return WINDOW_HANDLE;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getSize() {
        return new int[] {width, height};
    }

    public static Window get() {
        return BackyardRocketry.getInstance().getWindow();
    }


}
