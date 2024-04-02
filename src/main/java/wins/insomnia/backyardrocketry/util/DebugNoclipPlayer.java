package wins.insomnia.backyardrocketry.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Camera;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class DebugNoclipPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer {

    private Transform transform;
    private Transform previousTransform;
    private float cameraInterpolationFactor = 0f;
    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();

    public DebugNoclipPlayer() {

        transform = new Transform();
        previousTransform = new Transform();

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

    }

    @Override
    public void fixedUpdate() {

        //BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);

        float moveSpeed = 0.45f;
        float rotateSpeed = 0.15f;
        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();


        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        float rotateRightDirection = keyboardInput.isKeyPressed(GLFW_KEY_E) ? 1 : 0;
        float rotateLeftDirection = keyboardInput.isKeyPressed(GLFW_KEY_Q) ? 1 : 0;

        previousTransform.set(transform);
        transform.getPosition().add(
                moveSpeed * (leftDirection - rightDirection),
                moveSpeed * (downDirection - upDirection),
                moveSpeed * (forwardDirection - backwardDirection)
        );
        transform.getRotation().rotateAxis(rotateSpeed * (rotateRightDirection - rotateLeftDirection), 0, 1, 0);
        cameraInterpolationFactor = 0f;
    }

    @Override
    public void update(double deltaTime) {

        interpolateCameraTransform(deltaTime);

    }

    private void interpolateCameraTransform(double deltaTime) {

        cameraInterpolationFactor += (float) deltaTime / CAMERA_INTERPOLATION_DURATION;
        cameraInterpolationFactor = Math.min(cameraInterpolationFactor, 1f);

        Camera camera = BackyardRocketry.getInstance().getRenderer().getCamera();

        Quaternionf interpolatedRotation = new Quaternionf(previousTransform.getRotation());
        interpolatedRotation.slerp(transform.getRotation(), cameraInterpolationFactor);

        Vector3f interpolatedPosition = new Vector3f(previousTransform.getPosition());
        interpolatedPosition.lerp(transform.getPosition(), cameraInterpolationFactor);

        camera.getTransform().getRotation().set(interpolatedRotation);
        camera.getTransform().getPosition().set(interpolatedPosition);

    }

    public Transform getTransform() {
        return transform;
    }
}
