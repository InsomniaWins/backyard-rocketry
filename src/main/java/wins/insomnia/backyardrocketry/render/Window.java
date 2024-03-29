package wins.insomnia.backyardrocketry.render;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
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
        glfwSetKeyCallback(WINDOW_HANDLE, Window::windowCallback);

    }

    private static void windowCallback(long windowHandle, int key, int scancode, int action, int mods) {

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
