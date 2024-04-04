package wins.insomnia.backyardrocketry.util;

import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Camera;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class DebugNoclipPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();


    private Transform transform;
    private Transform previousTransform;


    private float cameraInterpolationFactor = 0f;
    private Quaternionf interpolatedRotation;
    private Vector3f interpolatedPosition;


    public DebugNoclipPlayer() {

        transform = new Transform();
        previousTransform = new Transform();

        interpolatedRotation = new Quaternionf(previousTransform.getRotation());
        interpolatedPosition = new Vector3f(previousTransform.getPosition());

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

    }

    @Override
    public void fixedUpdate() {


        // make sure interpolation of camera transformation is complete

        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);


        // define vars

        float moveSpeed = 0.15f;
        float rotateSpeed = 0.05f;


        // get input

        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();

        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        float rotateRightDirection = keyboardInput.isKeyPressed(GLFW_KEY_E) ? 1 : 0;
        float rotateLeftDirection = keyboardInput.isKeyPressed(GLFW_KEY_Q) ? 1 : 0;

        float rotateDownDirection = keyboardInput.isKeyPressed(GLFW_KEY_X) ? 1 : 0;
        float rotateUpDirection = keyboardInput.isKeyPressed(GLFW_KEY_Z) ? 1 : 0;



        // get move amount for x, y, and z

        Vector3f eulerRotation = new Vector3f();
        getTransform().getRotation().getEulerAnglesXYZ(eulerRotation);

        Vector3f forwardBackwardMovement = transform.getRotation().positiveZ(new Vector3f());
        forwardBackwardMovement.mul(forwardDirection - backwardDirection);

        Vector3f leftRightMovement = transform.getRotation().positiveX(new Vector3f());
        leftRightMovement.mul(leftDirection - rightDirection);

        float upDownMovement = (downDirection - upDirection);

        Vector3f moveAmount = new Vector3f(forwardBackwardMovement).add(leftRightMovement);
        if (moveAmount.length() > 0f) {
            moveAmount.normalize();
            moveAmount.mul(moveSpeed);
        }

        moveAmount.y = upDownMovement * moveSpeed;


        // apply translation and rotation

        previousTransform.set(transform);
        transform.getPosition().add(moveAmount);
        transform.getRotation().rotateLocalX(rotateSpeed * (rotateDownDirection - rotateUpDirection));
        transform.getRotation().rotateY(rotateSpeed * (rotateRightDirection - rotateLeftDirection));

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

        interpolatedRotation.set(previousTransform.getRotation());
        interpolatedPosition.set(previousTransform.getPosition());

        interpolatedRotation.slerp(transform.getRotation(), cameraInterpolationFactor);
        interpolatedPosition.lerp(transform.getPosition(), cameraInterpolationFactor);

        camera.getTransform().getRotation().set(interpolatedRotation);
        camera.getTransform().getPosition().set(interpolatedPosition);

    }

    public Transform getTransform() {
        return transform;
    }
}
