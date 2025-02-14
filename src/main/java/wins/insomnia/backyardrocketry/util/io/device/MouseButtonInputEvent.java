package wins.insomnia.backyardrocketry.util.io.device;

public class MouseButtonInputEvent extends InputEvent {

    public enum ButtonState {
        justPressed,
        justReleased,
        pressed,
        released
    }
    private final int BUTTON;
    private final ButtonState BUTTON_STATE;

    public MouseButtonInputEvent(int button, boolean pressed, boolean justHappened) {

        BUTTON = button;

        if (pressed) {
            if (justHappened) {
                BUTTON_STATE = ButtonState.justPressed;
            } else {
                BUTTON_STATE = ButtonState.pressed;
            }
        } else {
            if (justHappened) {
                BUTTON_STATE = ButtonState.justReleased;
            } else {
                BUTTON_STATE = ButtonState.released;
            }
        }

    }

    public int getButton() {
        return BUTTON;
    }

    public ButtonState getState() {
        return BUTTON_STATE;
    }

    public boolean isPressed() {
        return BUTTON_STATE == ButtonState.pressed || BUTTON_STATE == ButtonState.justPressed;
    }

    public boolean isReleased() {
        return BUTTON_STATE == ButtonState.released || BUTTON_STATE == ButtonState.justReleased;
    }

    public boolean isJustPressed() {
        return BUTTON_STATE == ButtonState.justPressed;
    }

    public boolean isJustReleased() {
        return BUTTON_STATE == ButtonState.justReleased;
    }

}
