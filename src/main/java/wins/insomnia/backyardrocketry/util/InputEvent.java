package wins.insomnia.backyardrocketry.util;

public class InputEvent {

    private boolean consumed = false;

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

}
