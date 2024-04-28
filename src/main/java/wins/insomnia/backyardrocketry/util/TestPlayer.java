package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.world.Block;
import wins.insomnia.backyardrocketry.world.BlockState;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class TestPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer, ICollisionBody {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
    private final float GRAVITY = -0.1f;
    private float walkSpeed = 0.22f;
    private float sprintSpeed = 0.5f;
    private float jumpSpeed = 0.5f;
    private boolean onGround = false;
    private final Vector3d VELOCITY = new Vector3d();
    private final World WORLD;

    private Transform transform;
    private Transform previousTransform;

    private float eyeHeight = 1.57f;
    private float height = 1.73f;
    private float halfWidth = 0.3f;
    private float cameraInterpolationFactor = 0f;
    private Vector3f interpolatedCameraRotation;
    private Vector3d interpolatedCameraPosition;
    private boolean lockMouseToCenterForCameraRotation = false;
    public boolean hasGravity = true;

    private final BoundingBox BOUNDING_BOX;


    public TestPlayer(World world) {

        WORLD = world;

        transform = new Transform();
        previousTransform = new Transform();

        interpolatedCameraRotation = new Vector3f(previousTransform.getRotation());
        interpolatedCameraPosition = getCameraPosition();

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

        onGround = false;
        if (VELOCITY.y != 0f) {
            for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
                double newVelocity = boundingBox.collideY(getBoundingBox(), VELOCITY.y);

                if (newVelocity != VELOCITY.y && Math.signum(VELOCITY.y) < 0d) {
                    onGround = true;
                }

                VELOCITY.y = newVelocity;
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

    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void fixedUpdate() {

        // make sure interpolation of camera transformation is complete
        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().getRotation().set(transform.getRotation());
        BackyardRocketry.getInstance().getRenderer().getCamera().getTransform().getPosition().set(getCameraPosition());


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

        if (hasGravity) {
            VELOCITY.add(0f, GRAVITY, 0f);

            if (isOnGround() && keyboardInput.isKeyPressed(GLFW_KEY_SPACE)) {
                VELOCITY.y = jumpSpeed;
            }
        } else {
            float verticalMoveAmount = (keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0) - (keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0);
            VELOCITY.y = Math.lerp(VELOCITY.y, verticalMoveAmount * moveSpeed, 0.6f);
        }

        // apply translation and rotation
        previousTransform.set(transform);
        move();
        updateBoundingBox();

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_F2)) {
            lockMouseToCenterForCameraRotation = !lockMouseToCenterForCameraRotation;
        }

        if (lockMouseToCenterForCameraRotation) {
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

        if (mouseInput.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {

            breakBlock();

        }

        if (mouseInput.isButtonJustPressed(GLFW_MOUSE_BUTTON_RIGHT)) {

            placeBlock(Block.COBBLESTONE);

        }
    }

    private void breakBlock() {

        Vector3d rayFrom = new Vector3d(getPosition()).add(0, eyeHeight, 0);
        Vector3d rayDirection = new Vector3d(0, 0, -1)
                .rotateX(-transform.getRotation().x)
                .rotateY(-transform.getRotation().y);
        int rayLength = 7;

        BlockRaycastResult raycastResult = Collision.blockRaycast(rayFrom, rayDirection, rayLength);
        if (raycastResult == null) return;


        raycastResult.getChunk().getBlockState(
                raycastResult.getChunk().toLocalX(raycastResult.getBlockX()),
                raycastResult.getChunk().toLocalY(raycastResult.getBlockY()),
                raycastResult.getChunk().toLocalZ(raycastResult.getBlockZ())
        ).setBlock(Block.AIR);

    }

    private void placeBlock(int blockToPlace) {

        Vector3d rayFrom = new Vector3d(getPosition()).add(0, eyeHeight, 0);
        Vector3d rayDirection = new Vector3d(0, 0, -1)
                .rotateX(-transform.getRotation().x)
                .rotateY(-transform.getRotation().y);
        int rayLength = 7;

        BlockRaycastResult raycastResult = Collision.blockRaycast(rayFrom, rayDirection, rayLength);
        if (raycastResult == null) return;

        Block.Face face = raycastResult.getFace();

        int placePosX = raycastResult.getBlockX();
        int placePosY = raycastResult.getBlockY();
        int placePosZ = raycastResult.getBlockZ();

        switch (face) {
            case NEG_X -> {
                placePosX -= 1;
            }
            case POS_X -> {
                placePosX += 1;
            }

            case NEG_Y -> {
                placePosY -= 1;
            }
            case POS_Y -> {
                placePosY += 1;
            }

            case NEG_Z -> {
                placePosZ -= 1;
            }
            case POS_Z -> {
                placePosZ += 1;
            }

            default -> {
                return;
            }
        }

        Chunk chunk = getWorld().getChunkContainingBlock(placePosX, placePosY, placePosZ);

        if (chunk == null) return;


        BlockState blockState = chunk.getBlockState(
                chunk.toLocalX(placePosX),
                chunk.toLocalY(placePosY),
                chunk.toLocalZ(placePosZ)
        );

        if (blockState == null) return;

        BoundingBox blockBoundingBox = Block.getBlockCollision(blockToPlace);

        if (blockBoundingBox != null) {
            blockBoundingBox.translate(placePosX, placePosY, placePosZ);

            if (getBoundingBox().collideWithBoundingBoxExclusive(blockBoundingBox) != Collision.AABBCollisionResultType.OUTSIDE) {
                return;
            }
        }

        blockState.setBlock(blockToPlace);
    }

    private void updateBoundingBox() {

        BOUNDING_BOX.getMin().x = getPosX() - halfWidth;
        BOUNDING_BOX.getMin().y = getPosY();
        BOUNDING_BOX.getMin().z = getPosZ() - halfWidth;

        BOUNDING_BOX.getMax().x = getPosX() + halfWidth;
        BOUNDING_BOX.getMax().y = getPosY() + height;
        BOUNDING_BOX.getMax().z = getPosZ() + halfWidth;
    }

    @Override
    public void update(double deltaTime) {

        interpolateCameraTransform(deltaTime);

    }

    public Vector3d getCameraPosition() {

        return getCameraPosition(getPosition());

    }

    public Vector3d getCameraPosition(Vector3d playerPosition) {

        return new Vector3d(playerPosition).add(0, eyeHeight, 0);

    }

    private void interpolateCameraTransform(double deltaTime) {

        cameraInterpolationFactor += (float) deltaTime / CAMERA_INTERPOLATION_DURATION;
        cameraInterpolationFactor = Math.min(cameraInterpolationFactor, 1f);

        Camera camera = Renderer.get().getCamera();

        // reset interpolation to t = 0 to begin interpolation
        interpolatedCameraRotation.set(previousTransform.getRotation());
        interpolatedCameraPosition.set(getCameraPosition(previousTransform.getPosition()));

        // interpolate camera rotation and position
        interpolatedCameraRotation.set(
                Transform.lerpAngle(interpolatedCameraRotation.x, transform.getRotation().x, cameraInterpolationFactor),
                Transform.lerpAngle(interpolatedCameraRotation.y, transform.getRotation().y, cameraInterpolationFactor),
                Transform.lerpAngle(interpolatedCameraRotation.z, transform.getRotation().z, cameraInterpolationFactor)
        );
        interpolatedCameraPosition.lerp(getCameraPosition(), cameraInterpolationFactor);

        // set camera rotation and position to interpolated values
        camera.getTransform().getRotation().set(interpolatedCameraRotation);
        camera.getTransform().getPosition().set(interpolatedCameraPosition);

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
