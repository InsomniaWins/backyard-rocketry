package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.KeyboardInput;
import wins.insomnia.backyardrocketry.util.KeyboardInputEvent;
import wins.insomnia.backyardrocketry.util.Transform;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;
    private final Transform TRANSFORM;

    public Camera() {

        TRANSFORM = new Transform();
        TRANSFORM.getPosition().set(0,0,0);

        PROJECTION_MATRIX = new Matrix4f();
        updateProjectionMatrix();

        VIEW_MATRIX = new Matrix4f();
        updateViewMatrix();

    }

    public void update(double deltaTime) {

        float moveSpeed = 2.5f;

        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();

        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        TRANSFORM.getPosition().add(0f, 0f, (float) deltaTime * (forwardDirection - backwardDirection) * moveSpeed);


        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        TRANSFORM.getPosition().add((float) deltaTime * (leftDirection - rightDirection) * moveSpeed, 0f, 0f);


        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        TRANSFORM.getPosition().add(0f, (float) deltaTime * (downDirection - upDirection) * moveSpeed, 0f);

    }

    public void updateProjectionMatrix() {
        BackyardRocketry backyardRocketryInstance = BackyardRocketry.getInstance();
        Window gameWindow = backyardRocketryInstance.getWindow();
        int[] gameWindowSize = gameWindow.getSize();

        PROJECTION_MATRIX.setPerspective(70f, (float) gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 100f);
    }

    public void updateViewMatrix() {
        VIEW_MATRIX.identity();
        VIEW_MATRIX.translate(TRANSFORM.getPosition());
    }


    public Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public Matrix4f getViewMatrix() {
        return VIEW_MATRIX;
    }

    public Transform getTransform() {
        return TRANSFORM;
    }

}
