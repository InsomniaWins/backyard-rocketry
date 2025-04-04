package wins.insomnia.backyardrocketry.util.io.device;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInput {

    private final long WINDOW_HANDLE;
    private final Queue<InputEvent> INPUT_EVENT_QUEUE; // input events for the current update() call
    private final Queue<InputEvent> QUEUED_INPUTS; // input events to be processed in the next update() call
    private final HashMap<Integer, KeyboardInputEvent.KeyState> KEY_STATES;
    private final List<IInputCallback> INPUT_CALLBACK_LIST;

    public KeyboardInput(long windowHandle) {

        WINDOW_HANDLE = windowHandle;

        INPUT_EVENT_QUEUE = new LinkedList<>();
        QUEUED_INPUTS = new LinkedList<>();
        KEY_STATES = new HashMap<>();
        INPUT_CALLBACK_LIST = new ArrayList<>();

        glfwSetKeyCallback(WINDOW_HANDLE, this::keyboardInputCallback);
        glfwSetCharCallback(WINDOW_HANDLE, this::characterInputCallback);
    }


    public void registerInputCallback(IInputCallback callback) {

        INPUT_CALLBACK_LIST.add(callback);

    }

    public void unregisterInputCallback(IInputCallback callback) {

        INPUT_CALLBACK_LIST.remove(callback);

    }

    public void clearConsumedInputs() {
        INPUT_EVENT_QUEUE.removeIf(InputEvent::isConsumed);
    }


    public boolean isKeyPressed(int key) {
        return KEY_STATES.get(key) == KeyboardInputEvent.KeyState.pressed || KEY_STATES.get(key) == KeyboardInputEvent.KeyState.justPressed;
    }

    public boolean isKeyJustPressed(int key) {
        return KEY_STATES.get(key) == KeyboardInputEvent.KeyState.justPressed;
    }

    public boolean isKeyReleased(int key) {
        return KEY_STATES.get(key) == KeyboardInputEvent.KeyState.released || KEY_STATES.get(key) == KeyboardInputEvent.KeyState.justReleased;
    }

    public boolean isKeyJustReleased(int key) {
        return KEY_STATES.get(key) == KeyboardInputEvent.KeyState.justReleased;
    }

    public void updateKeyStates() {

        clearConsumedInputs();


        for (Map.Entry<Integer, KeyboardInputEvent.KeyState> keyStateEntry : KEY_STATES.entrySet()) {
            if (keyStateEntry.getValue() == KeyboardInputEvent.KeyState.justReleased) {
                keyStateEntry.setValue(KeyboardInputEvent.KeyState.released);
            }
            else if (keyStateEntry.getValue() == KeyboardInputEvent.KeyState.justPressed) {
                keyStateEntry.setValue(KeyboardInputEvent.KeyState.pressed);
            }
        }


        while (!QUEUED_INPUTS.isEmpty()) {
            InputEvent inputEvent = QUEUED_INPUTS.remove();

            if (inputEvent instanceof KeyboardInputEvent keyboardInputEvent) {

                if (keyboardInputEvent.isJustPressed()) {
                    KEY_STATES.put(keyboardInputEvent.getKey(), KeyboardInputEvent.KeyState.justPressed);
                }
                else if (keyboardInputEvent.isJustReleased()) {
                    KEY_STATES.put(keyboardInputEvent.getKey(), KeyboardInputEvent.KeyState.justReleased);
                }

            } else if (inputEvent instanceof TypeInputEvent typeInputEvent) {



            }

            INPUT_EVENT_QUEUE.add(inputEvent);
        }


        sendInputsToCallbacks();

    }

    private void sendInputsToCallbacks() {
        while (!INPUT_EVENT_QUEUE.isEmpty()) {

            InputEvent inputEvent = INPUT_EVENT_QUEUE.remove();

			for (IInputCallback inputCallback : INPUT_CALLBACK_LIST) {

				if (inputEvent.isConsumed()) {
					break;
				}

				inputCallback.inputEvent(inputEvent);
			}

        }
    }

    public void keyboardInputCallback(long windowHandle, int key, int scancode, int action, int mods) {

        KeyboardInputEvent inputEvent = null;

        if (action == GLFW_PRESS) {
            inputEvent = new KeyboardInputEvent(key, true, true);
        }
        else if (action == GLFW_RELEASE) {
            inputEvent = new KeyboardInputEvent(key, false, true);
        }

        if (inputEvent == null) {
            return;
        }

        QUEUED_INPUTS.add(inputEvent);

    }

    public void characterInputCallback(long windowHandle, int character) {

        TypeInputEvent inputEvent = new TypeInputEvent((char) character);
        QUEUED_INPUTS.add(inputEvent);
    }

    public static KeyboardInput get() {
        return BackyardRocketry.getInstance().getKeyboardInput();
    }

}
