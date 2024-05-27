package wins.insomnia.backyardrocketry.util.input;

import org.joml.Vector2i;
import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.lang.ref.WeakReference;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    private final long WINDOW_HANDLE;

    private double mouseScrollY = 0.0;
    private double mouseScrollX = 0.0;
    private int previousMouseX = 0;
    private int previousMouseY = 0;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private int previousFixedMouseX = 0;
    private int previousFixedMouseY = 0;

    private final Queue<InputEvent> INPUT_EVENT_QUEUE; // input events for the current update() call
    private final Queue<InputEvent> QUEUED_INPUTS; // input events to be processed in the next update() call
    private final HashMap<Integer, MouseButtonInputEvent.ButtonState> BUTTON_STATES;
    private final Vector2i MOUSE_MOTION;
    private final List<WeakReference<IInputCallback>> INPUT_CALLBACK_LIST;

    public MouseInput(long windowHandle) {

        WINDOW_HANDLE = windowHandle;

        INPUT_EVENT_QUEUE = new LinkedList<>();
        QUEUED_INPUTS = new LinkedList<>();
        BUTTON_STATES = new HashMap<>();
        MOUSE_MOTION = new Vector2i();
        INPUT_CALLBACK_LIST = new ArrayList<>();


        double[] mouseX = new double[1]; double[] mouseY = new double[1];
        glfwGetCursorPos(WINDOW_HANDLE, mouseX, mouseY);
        previousMouseX = (int) mouseX[0];
        previousMouseY = (int) mouseY[0];
        currentMouseX = previousMouseX;
        currentMouseY = previousMouseY;
        previousFixedMouseX = previousMouseX;
        previousFixedMouseY = previousMouseY;



        glfwSetMouseButtonCallback(WINDOW_HANDLE, this::mouseButtonCallback);
        glfwSetScrollCallback(WINDOW_HANDLE, this::mouseWheelCallback);
        glfwSetCursorPosCallback(WINDOW_HANDLE, this::mousePositionCallback);

    }

    public void clearConsumedInputs() {
        INPUT_EVENT_QUEUE.removeIf(InputEvent::isConsumed);
    }

    public void addInputCallback(IInputCallback callbackObject) {
        INPUT_CALLBACK_LIST.add(new WeakReference<>(callbackObject));
    }

    public boolean isButtonPressed(int key) {
        return BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.pressed || BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.justPressed;
    }

    public boolean isButtonJustPressed(int key) {
        return BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.justPressed;
    }

    public boolean isButtonReleased(int key) {
        return BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.released || BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.justReleased;
    }

    public boolean isButtonJustReleased(int key) {
        return BUTTON_STATES.get(key) == MouseButtonInputEvent.ButtonState.justReleased;
    }

    public Vector2i getMouseMotion() {
        return MOUSE_MOTION;
    }

    public void updateButtonStates() {

        clearConsumedInputs();


        for (Map.Entry<Integer, MouseButtonInputEvent.ButtonState> buttonStateEntry : BUTTON_STATES.entrySet()) {
            if (buttonStateEntry.getValue() == MouseButtonInputEvent.ButtonState.justReleased) {
                buttonStateEntry.setValue(MouseButtonInputEvent.ButtonState.released);
            }
            else if (buttonStateEntry.getValue() == MouseButtonInputEvent.ButtonState.justPressed) {
                buttonStateEntry.setValue(MouseButtonInputEvent.ButtonState.pressed);
            }
        }


        MOUSE_MOTION.x = currentMouseX - previousMouseX;
        MOUSE_MOTION.y = currentMouseY - previousMouseY;

        previousMouseX = currentMouseX;
        previousMouseY = currentMouseY;

        mouseScrollY = 0.0;
        mouseScrollX = 0.0;

        while (!QUEUED_INPUTS.isEmpty()) {
            InputEvent inputEvent = QUEUED_INPUTS.remove();

            if (inputEvent instanceof MouseButtonInputEvent mouseButtonInputEvent) {

                if (mouseButtonInputEvent.isJustPressed()) {
                    BUTTON_STATES.put(mouseButtonInputEvent.getButton(), MouseButtonInputEvent.ButtonState.justPressed);
                }
                else if (mouseButtonInputEvent.isJustReleased()) {
                    BUTTON_STATES.put(mouseButtonInputEvent.getButton(), MouseButtonInputEvent.ButtonState.justReleased);
                }

                INPUT_EVENT_QUEUE.add(mouseButtonInputEvent);

            }
            else if (inputEvent instanceof  MouseMovementInputEvent mouseMovementInputEvent) {

                INPUT_EVENT_QUEUE.add(mouseMovementInputEvent);

            } else if (inputEvent instanceof MouseScrollInputEvent mouseScrollInputEvent) {

                mouseScrollY += mouseScrollInputEvent.getOffsetY();
                mouseScrollX += mouseScrollInputEvent.getOffsetX();

                INPUT_EVENT_QUEUE.add(mouseScrollInputEvent);

            }
        }


        sendInputsToCallbacks();

    }

    public double getMouseScrollY() {
        return mouseScrollY;
    }

    public double getMouseScrollX() {
        return mouseScrollX;
    }

    private void sendInputsToCallbacks() {
        while (!INPUT_EVENT_QUEUE.isEmpty()) {

            InputEvent inputEvent = INPUT_EVENT_QUEUE.remove();

            for (WeakReference<IInputCallback> weakReference: INPUT_CALLBACK_LIST) {
                IInputCallback inputCallback = weakReference.get();

                if (inputEvent.isConsumed()) {
                    break;
                }

                if (inputCallback == null) throw new RuntimeException("inputCallback is null!");

                inputCallback.inputEvent(inputEvent);
            }
        }
    }

    public void mouseButtonCallback(long windowHandle, int button, int action, int mods) {

        InputEvent inputEvent = null;

        if (action == GLFW_PRESS) {
            inputEvent = new MouseButtonInputEvent(button, true, true);
        }
        else if (action == GLFW_RELEASE) {
            inputEvent = new MouseButtonInputEvent(button, false, true);
        }

        if (inputEvent == null) {
            return;
        }

        QUEUED_INPUTS.add(inputEvent);

    }

    private void mouseWheelCallback(long windowHandle, double xOffset, double yOffset) {

        InputEvent inputEvent = new MouseScrollInputEvent(xOffset, yOffset);
        QUEUED_INPUTS.add(inputEvent);

    }

    private void mousePositionCallback(long windowHandle, double x, double y) {
        currentMouseX = (int) x;
        currentMouseY = (int) y;

        InputEvent inputEvent = new MouseMovementInputEvent(previousMouseX, previousMouseY, currentMouseX, currentMouseY);
        QUEUED_INPUTS.add(inputEvent);

    }

    public void setMousePosition(int x, int y) {
        setMousePosition(x, y, true);
    }

    public void setMousePosition(int x, int y, boolean isMovement) {
        if (isMovement) {

            mousePositionCallback(WINDOW_HANDLE, x, y);

        }
        else {

            glfwSetCursorPos(WINDOW_HANDLE, x, y);

            previousMouseX = x;
            previousMouseY = y;

            currentMouseX = x;
            currentMouseY = y;

            MOUSE_MOTION.x = 0;
            MOUSE_MOTION.y = 0;

        }
    }

    public static MouseInput get() {
        return BackyardRocketry.getInstance().getMouseInput();
    }
}
