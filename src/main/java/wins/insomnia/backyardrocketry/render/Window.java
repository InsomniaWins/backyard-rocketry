package wins.insomnia.backyardrocketry.render;

import org.lwjgl.glfw.GLFW;
import wins.insomnia.backyardrocketry.BackyardRocketry;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final long WINDOW_HANDLE;
    private int width;
    private int height;
    private boolean pixelPerfectViewport = false;

    private ResolutionFrameBuffer resolutionFrameBuffer;

    public Window(int width, int height, String title) {

        // make the window

        this.width = width;
        this.height = height;

        WINDOW_HANDLE = glfwCreateWindow(width, height, title, NULL, NULL);
        if (WINDOW_HANDLE == NULL) {

            throw new RuntimeException("Failed to make a window!");

        }


        // setup callback for window
        glfwSetWindowSizeCallback(WINDOW_HANDLE, this::windowResizeCallback);

    }

    public void postInitialize() {
        setResolution(320, 240, true);
    }

    public void clean() {
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
