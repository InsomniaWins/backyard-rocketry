package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class TestPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer, ICollisionBody {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
    private final float GRAVITY = 0.05f;
    private final Vector3f VELOCITY = new Vector3f();

    private Transform transform;
    private Transform previousTransform;


    private float eyeHeight = 1.6f;
    private float cameraInterpolationFactor = 0f;
    private Vector3f interpolatedRotation;
    private Vector3f interpolatedPosition;

    private final BoundingBox BOUNDING_BOX;

    public TestPlayer() {

        transform = new Transform();
        previousTransform = new Transform();

        interpolatedRotation = new Vector3f(previousTransform.getRotation());
        interpolatedPosition = new Vector3f(previousTransform.getPosition());

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

        BOUNDING_BOX = new BoundingBox();
        updateBoundingBox();

        Collision.registerCollisionBody(this);

    }

    @Override
    public void fixedUpdate() {


        List<ICollisionBody> bodiesInChunk = Collision.getBodiesInBoundingBox(new BoundingBox(
                0,-200,0,
                16,200,16
        ));
        if (bodiesInChunk.contains(this)) {
            System.out.println("Test Player is in center!");
        }

        System.out.println(
                "Player: " + getTransform().getPosition().x + ", "
                        + getTransform().getPosition().y + ", "
                        + getTransform().getPosition().z
        );


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
                (rightDirection - leftDirection),
                0f,
                (backwardDirection - forwardDirection)
        ).rotateY(-transform.getRotation().y);

        if (moveAmount.length() > 0f) {
            moveAmount.normalize();
        }

        moveAmount.y = (upDirection - downDirection);
        moveAmount.mul(moveSpeed);

        VELOCITY.set(moveAmount);

        // apply translation and rotation

        previousTransform.set(transform);
        transform.getPosition().add(VELOCITY);

        updateBoundingBox();

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

    private void updateBoundingBox() {

        BOUNDING_BOX.getMin().x = -0.4f + getTransform().getPosition().x;
        BOUNDING_BOX.getMin().y = -eyeHeight + getTransform().getPosition().y;
        BOUNDING_BOX.getMin().z = -0.4f + getTransform().getPosition().z;

        BOUNDING_BOX.getMax().x = 0.4f + getTransform().getPosition().x;
        BOUNDING_BOX.getMax().y = 0.2f + getTransform().getPosition().y;
        BOUNDING_BOX.getMax().z = 0.4f + getTransform().getPosition().z;

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
    public BoundingBox getBoundingBox() {

        updateBoundingBox();

        return BOUNDING_BOX;
    }

    @Override
    public boolean isBodyStatic() {
        return false;
    }
}
