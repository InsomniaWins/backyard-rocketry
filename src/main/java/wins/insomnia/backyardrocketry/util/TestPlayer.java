package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class TestPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer, ICollisionBody {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
    private final float GRAVITY = -0.1f;
    private float walkSpeed = 0.25f;
    private float sprintSpeed = 0.5f;
    private float jumpSpeed = 0.5f;
    private boolean grounded = false;
    private final Vector3d VELOCITY = new Vector3d();
    private final World WORLD;

    private Transform transform;
    private Transform previousTransform;

    private float eyeHeight = 1.6f;
    private float height = 1.8f;
    private float cameraInterpolationFactor = 0f;
    private Vector3f interpolatedRotation;
    private Vector3d interpolatedPosition;

    private final BoundingBox BOUNDING_BOX;


    public TestPlayer(World world) {

        WORLD = world;

        transform = new Transform();
        previousTransform = new Transform();

        interpolatedRotation = new Vector3f(previousTransform.getRotation());
        interpolatedPosition = new Vector3d(previousTransform.getPosition());

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

        BOUNDING_BOX = new BoundingBox();
        updateBoundingBox();

        Collision.registerCollisionBody(this);


    }

    public Vector3d getPosition() {
        return getTransform().getPosition();
    }

    public double getPosX() {
        return getTransform().getPosition().x;
    }

    public double getPosY() {
        return getTransform().getPosition().y;
    }

    public double getPosZ() {
        return getTransform().getPosition().z;
    }

    public void move() {

        List<Chunk> chunksNearPlayer = Collision.getChunksTouchingBoundingBox(
                new BoundingBox(
                        getPosX() - 16, getPosY() - 16, getPosZ() - 16,
                        getPosX() + 16, getPosY() + 16, getPosZ() + 16
                )
        );

        List<BoundingBox> blockBoundingBoxesNearPlayer = new ArrayList<>();

        if (!chunksNearPlayer.isEmpty()) {

            BoundingBox tempBoundingBox = new BoundingBox(getBoundingBox()).grow(VELOCITY.length() * 2);

            for (Chunk chunk : chunksNearPlayer) {
                List<BoundingBox> boundingBoxes = chunk.getBoundingBoxesOfBlocksPotentiallyCollidingWithBoundingBox(tempBoundingBox);
                blockBoundingBoxesNearPlayer.addAll(boundingBoxes);
            }

        }

        if (VELOCITY.x != 0f) {
            for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
                VELOCITY.x = boundingBox.collideX(getBoundingBox(), VELOCITY.x);
            }

            getPosition().x += VELOCITY.x;
        }

        if (VELOCITY.y != 0f) {
            for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
                VELOCITY.y = boundingBox.collideY(getBoundingBox(), VELOCITY.y);
            }

            getPosition().y += VELOCITY.y;
        }

        if (VELOCITY.z != 0f) {
            for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
                VELOCITY.z = boundingBox.collideZ(getBoundingBox(), VELOCITY.z);
            }

            getPosition().z += VELOCITY.z;
        }
    }

    @Override
    public void fixedUpdate() {

        // make sure interpolation of camera transformation is complete
        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().set(transform);


        // define vars
        float moveSpeed = BackyardRocketry.getInstance().getKeyboardInput().isKeyPressed(GLFW_KEY_LEFT_CONTROL) ? sprintSpeed : walkSpeed;
        float rotateSpeed = 0.0025f;


        // get input
        KeyboardInput keyboardInput = BackyardRocketry.getInstance().getKeyboardInput();
        MouseInput mouseInput = BackyardRocketry.getInstance().getMouseInput();

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

        VELOCITY.x = Math.lerp(VELOCITY.x, moveAmount.x, 0.5f);
        VELOCITY.z = Math.lerp(VELOCITY.z, moveAmount.z, 0.5f);
        VELOCITY.add(0f, GRAVITY, 0f);

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_SPACE)) {
            VELOCITY.y = jumpSpeed;
        }

        // apply translation and rotation
        previousTransform.set(transform);
        move();
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

        BOUNDING_BOX.getMin().x = getPosX() - 0.2f;
        BOUNDING_BOX.getMin().y = getPosY() - eyeHeight;
        BOUNDING_BOX.getMin().z = getPosZ() - 0.2f;

        BOUNDING_BOX.getMax().x = getPosX() + 0.2f;
        BOUNDING_BOX.getMax().y = getPosY() + (height - eyeHeight);
        BOUNDING_BOX.getMax().z = getPosZ() + 0.2f;
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

    public World getWorld() {
        return WORLD;
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
