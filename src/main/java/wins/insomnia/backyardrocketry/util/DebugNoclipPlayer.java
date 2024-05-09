package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.world.World;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class DebugNoclipPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();


    private Transform transform;
    private Transform previousTransform;


    private float cameraInterpolationFactor = 0f;
    private Vector3f interpolatedRotation;
    private Vector3d interpolatedPosition;


    public DebugNoclipPlayer() {

        transform = new Transform();
        previousTransform = new Transform();

        interpolatedRotation = new Vector3f(previousTransform.getRotation());
        interpolatedPosition = new Vector3d(previousTransform.getPosition());

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

    }

    @Override
    public void fixedUpdate() {


        // make sure interpolation of camera transformation is complete

        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);


        // define vars

        float moveSpeed = 0.15f;
        float rotateSpeed = 0.0025f;


        // get input

        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();
        MouseInput mouseInput = BackyardRocketry.getInstance().getMouseInput();

        if (keyboardInput.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) moveSpeed *= 3f;

        float forwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_W) ? 1 : 0;
        float backwardDirection = keyboardInput.isKeyPressed(GLFW_KEY_S) ? 1 : 0;

        float leftDirection = keyboardInput.isKeyPressed(GLFW_KEY_A) ? 1 : 0;
        float rightDirection = keyboardInput.isKeyPressed(GLFW_KEY_D) ? 1 : 0;

        float upDirection = keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0;
        float downDirection = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0;

        Vector3f moveAmount = new Vector3f(
                (leftDirection - rightDirection),
                0f,
                (forwardDirection - backwardDirection)
        ).rotateY(-transform.getRotation().y);

        if (moveAmount.length() > 0f) {
            moveAmount.normalize();
        }

        moveAmount.y = (downDirection - upDirection);
        moveAmount.mul(moveSpeed);

        // apply translation and rotation

        previousTransform.set(transform);
        transform.getPosition().add(moveAmount);

        if (mouseInput.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            float verticalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().y;
            float horizontalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().x;

            mouseInput.setMousePosition(BackyardRocketry.getInstance().getWindow().getWidth() / 2, BackyardRocketry.getInstance().getWindow().getHeight() / 2, false);

            transform.rotateX(verticalRotateAmount);
            transform.rotateY(horizontalRotateAmount);

            // clamp vertical rotation
            transform.getRotation().x = Math.max(transform.getRotation().x, (float) -Math.PI * 0.5f);
            transform.getRotation().x = Math.min(transform.getRotation().x, (float) Math.PI * 0.5f);
        }

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

        // interpolate camera rotation

        interpolatedRotation.set(
                Transform.lerpAngle(interpolatedRotation.x, transform.getRotation().x, cameraInterpolationFactor),
                Transform.lerpAngle(interpolatedRotation.y, transform.getRotation().y, cameraInterpolationFactor),
                Transform.lerpAngle(interpolatedRotation.z, transform.getRotation().z, cameraInterpolationFactor)
        );

        // interpolate camera position

        interpolatedPosition.lerp(transform.getPosition(), cameraInterpolationFactor);

        camera.getTransform().getRotation().set(interpolatedRotation);
        camera.getTransform().getPosition().set(interpolatedPosition);

    }

    public Transform getTransform() {
        return transform;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Vector3d getPosition() {
        return transform.getPosition();
    }

    @Override
    public Vector3i getBlockPosition() {
        return new Vector3i(
                (int) transform.getPosition().x,
                (int) transform.getPosition().y,
                (int) transform.getPosition().z
        );
    }
}
