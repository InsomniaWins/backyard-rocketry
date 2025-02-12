package wins.insomnia.backyardrocketry.util.input;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardInputEvent extends InputEvent {



    public enum KeyState {
        justPressed,
        justReleased,
        pressed,
        released
    }
    private final int KEY;
    private final KeyState KEY_STATE;

    public KeyboardInputEvent(int key, boolean pressed, boolean justHappened) {

        KEY = key;

        if (pressed) {
            if (justHappened) {
                KEY_STATE = KeyState.justPressed;
            } else {
                KEY_STATE = KeyState.pressed;
            }
        } else {
            if (justHappened) {
                KEY_STATE = KeyState.justReleased;
            } else {
                KEY_STATE = KeyState.released;
            }
        }

    }

    public int getKey() {
        return KEY;
    }

    public KeyState getState() {
        return KEY_STATE;
    }

    public boolean isPressed() {
        return KEY_STATE == KeyState.pressed || KEY_STATE == KeyState.justPressed;
    }

    public boolean isReleased() {
        return KEY_STATE == KeyState.released || KEY_STATE == KeyState.justReleased;
    }

    public boolean isJustPressed() {
        return KEY_STATE == KeyState.justPressed;
    }

    public boolean isJustReleased() {
        return KEY_STATE == KeyState.justReleased;
    }

}
