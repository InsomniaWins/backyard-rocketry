package wins.insomnia.backyardrocketry.render;

import org.joml.Vector2i;
import org.joml.primitives.Rectanglei;
import org.lwjgl.glfw.GLFWVidMode;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.io.device.IInputCallback;
import wins.insomnia.backyardrocketry.util.io.device.KeyboardInput;
import wins.insomnia.backyardrocketry.util.io.device.KeyboardInputEvent;
import wins.insomnia.backyardrocketry.util.io.device.MouseInput;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    public final int DEFAULT_WIDTH;
    public final int DEFAULT_HEIGHT;

    public final int DEFAULT_RESOLUTION_WIDTH = 320;
    public final int DEFAULT_RESOLUTION_HEIGHT = 240;

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
        setResolution(DEFAULT_RESOLUTION_WIDTH, DEFAULT_RESOLUTION_HEIGHT, true);

        keyboardInputCallback = inputEvent -> {

			if (!(inputEvent instanceof KeyboardInputEvent keyboardInputEvent)) return;

			if (keyboardInputEvent.getKey() == GLFW_KEY_F11 && keyboardInputEvent.isJustPressed()) {
				keyboardInputEvent.consume();

                setFullscreen(!isFullscreen());

			}

		};

        KeyboardInput.get().registerInputCallback(keyboardInputCallback);

    }

    public Vector2i getMonitorResolution() {

        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        int displayWidth = 1920;
        int displayHeight = 1080;

        if (videoMode != null) {

            displayWidth = videoMode.width();
            displayHeight = videoMode.height();

        }


        return new Vector2i(displayWidth, displayHeight);

    }



    public void setFullscreen(boolean value) {
        fullscreen = value;

        if (fullscreen) {

            glfwSetWindowAttrib(getWindowHandle(), GLFW_DECORATED, GLFW_FALSE);

            Vector2i monitorResolution = getMonitorResolution();

            glfwSetWindowSize(getWindowHandle(), monitorResolution.x, monitorResolution.y);
            glfwSetWindowPos(getWindowHandle(), 0, 0);

        } else {

            glfwSetWindowSize(getWindowHandle(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

            Vector2i monitorResolution = getMonitorResolution();

            int centerX = (int) (monitorResolution.x * 0.5 - DEFAULT_WIDTH * 0.5);
            int centerY = (int) (monitorResolution.y * 0.5 - DEFAULT_HEIGHT * 0.5);

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


        /*


        //
        //the following code expands the viewport to the window borders
        //


        int pixelsNeededX = (int) ((window.width / getViewportScale()) - DEFAULT_RESOLUTION_WIDTH);
        int pixelsNeededY = (int) ((window.height / getViewportScale()) - DEFAULT_RESOLUTION_HEIGHT);

        setResolution(
                DEFAULT_RESOLUTION_WIDTH + pixelsNeededX,
                DEFAULT_RESOLUTION_HEIGHT + pixelsNeededY
        );*/


    }

    public int getViewportMouseX() {

        int mouseX = MouseInput.get().getMouseX();

        Rectanglei viewportDimensions = getViewportDimensions();
        mouseX -= viewportDimensions.minX;

        mouseX = (int) (((double) mouseX) / getViewportScale());

        return mouseX;
    }

    public int getViewportMouseY() {

        int mouseY = MouseInput.get().getMouseY();

        Rectanglei viewportDimensions = getViewportDimensions();
        mouseY -= viewportDimensions.minY;

        mouseY = (int) (((double) mouseY) / getViewportScale());

        return mouseY;
    }

    public Vector2i getViewportMouse() {
        return new Vector2i(
                getViewportMouseX(),
                getViewportMouseY()
        );
    }

    public double getViewportScale() {
        if (isPixelPerfectViewport()) {
            int pixelPerfectScaleX = getWidth() / getResolutionFrameBuffer().getWidth();
            int pixelPerfectScaleY = getHeight() / getResolutionFrameBuffer().getHeight();

            return Math.max(1, Math.min(pixelPerfectScaleX, pixelPerfectScaleY));
        } else {
            double viewportScaleX = getWidth() / (double) getResolutionFrameBuffer().getWidth();
            double viewportScaleY = getHeight() / (double) getResolutionFrameBuffer().getHeight();

            return Math.min(viewportScaleX, viewportScaleY);
        }
    }

    public Rectanglei getViewportDimensions() {

        int viewportX;
        int viewportY;
        int viewportWidth;
        int viewportHeight;

        double viewportScale = getViewportScale();

        if (isPixelPerfectViewport()) {
            viewportScale = (int) viewportScale;
        }

        viewportWidth = (int) (getResolutionFrameBuffer().getWidth() * viewportScale);
        viewportHeight = (int) (getResolutionFrameBuffer().getHeight() * viewportScale);

        viewportX = getWidth() / 2 - viewportWidth / 2;
        viewportY = getHeight() / 2 - viewportHeight / 2;

        return new Rectanglei(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight);
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
