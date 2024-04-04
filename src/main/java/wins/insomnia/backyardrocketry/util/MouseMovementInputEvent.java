package wins.insomnia.backyardrocketry.util;

import org.joml.Vector2i;

public class MouseMovementInputEvent extends InputEvent {

    private int previousX;
    private int previousY;
    private int x;
    private int y;

    private boolean consumed = false;

    public MouseMovementInputEvent(int oldX, int oldY, int newX, int newY) {

        previousX = oldX;
        previousY = oldY;

        x = newX;
        y = newY;

    }

    public Vector2i getPreviousPosition() {
        return new Vector2i(previousX, previousY);
    }

    public Vector2i getPosition() {
        return new Vector2i(x, y);
    }

    public int getPreviousX() {
        return previousX;
    }

    public int getPreviousY() {
        return previousY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }

}
