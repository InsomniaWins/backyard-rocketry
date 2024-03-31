package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class DebugNoclipPlayer implements IUpdateListener {

    private Transform transform;

    public DebugNoclipPlayer() {

        transform = new Transform();
        BackyardRocketry.getInstance().registerUpdateListener(this);

    }

    @Override
    public void update(double deltaTime) {

        float moveSpeed = 2.5f;
        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();


        float horizontalAngle = transform.getRotation().y;
        float xFactor = cos(horizontalAngle);
        float zFactor = cos(horizontalAngle);

        System.out.println(toDegrees(transform.getRotation().y));

        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        transform.getPosition().add(
                0f,
                0f,
                zFactor * (float) deltaTime * (forwardDirection - backwardDirection) * moveSpeed
        );



        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        transform.getPosition().add(
                xFactor * (float) deltaTime * (leftDirection - rightDirection) * moveSpeed,
                0f,
                0f
        );


        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        transform.getPosition().add(0f, (float) deltaTime * (downDirection - upDirection) * moveSpeed, 0f);




        float rotateRightDirection = keyboardInput.isKeyPressed(GLFW_KEY_E) ? 1 : 0;
        float rotateLeftDirection = keyboardInput.isKeyPressed(GLFW_KEY_Q) ? 1 : 0;

        transform.getRotation().rotateAxis((float) deltaTime * (rotateRightDirection - rotateLeftDirection), 0, 1, 0);

        updateCameraTransform();

    }

    public void updateCameraTransform() {
        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);
    }
}
