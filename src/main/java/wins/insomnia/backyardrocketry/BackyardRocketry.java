package wins.insomnia.backyardrocketry;

import wins.insomnia.backyardrocketry.render.Window;

public class BackyardRocketry {

    private Window window;

    public BackyardRocketry() {

        init();

    }

    private void init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        window = new Window();


    }

    public Window getWindow() {
        return window;
    }
}
