package wins.insomnia.backyardrocketry.render;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long WINDOW_HANDLE;
    private int width;
    private int height;

    public Window(int width, int height, String title) {

        // make the window

        this.width = width;
        this.height = height;

        WINDOW_HANDLE = glfwCreateWindow(width, height, title, NULL, NULL);
        if (WINDOW_HANDLE == NULL) {

            throw new RuntimeException("Failed to make a window!");

        }


        // setup callback for window
        glfwSetKeyCallback(WINDOW_HANDLE, this::windowKeyInputCallback);
        glfwSetWindowSizeCallback(WINDOW_HANDLE, this::windowResizeCallback);
    }


    private void windowResizeCallback(long windowHandle, int width, int height) {

        Window window = BackyardRocketry.getInstance().getWindow();

        window.width = width;
        window.height = height;

        glViewport(0,0,window.width, window.height);
    }

    private void windowKeyInputCallback(long windowHandle, int key, int scancode, int action, int mods) {

        /*

        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        }

        */

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

}
