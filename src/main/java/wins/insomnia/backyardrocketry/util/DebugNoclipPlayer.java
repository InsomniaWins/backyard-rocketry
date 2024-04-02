package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Camera;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class DebugNoclipPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer {

    private Transform transform;

    public DebugNoclipPlayer() {

        transform = new Transform();

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

    }

    @Override
    public void fixedUpdate() {

        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);

        float moveSpeed = 0.15f;
        float rotateSpeed = 0.05f;
        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();


        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        float rotateRightDirection = keyboardInput.isKeyPressed(GLFW_KEY_E) ? 1 : 0;
        float rotateLeftDirection = keyboardInput.isKeyPressed(GLFW_KEY_Q) ? 1 : 0;

        transform.getPosition().add(
                moveSpeed * (leftDirection - rightDirection),
                moveSpeed * (downDirection - upDirection),
                moveSpeed * (forwardDirection - backwardDirection)
        );

        transform.getRotation().rotateAxis(rotateSpeed * (rotateRightDirection - rotateLeftDirection), 0, 1, 0);
    }

    @Override
    public void update(double deltaTime) {

        interpolateCameraTransform(deltaTime);

    }

    private void interpolateCameraTransform(double deltaTime) {

        Camera camera = BackyardRocketry.getInstance().getRenderer().getCamera();

        camera.getTransform().getPosition().lerp(transform.getPosition(), (float) deltaTime * Updater.getFixedUpdatesPerSecond());
        camera.getTransform().getRotation().slerp(transform.getRotation(), (float) deltaTime * Updater.getFixedUpdatesPerSecond());

    }

    public Transform getTransform() {
        return transform;
    }
}
