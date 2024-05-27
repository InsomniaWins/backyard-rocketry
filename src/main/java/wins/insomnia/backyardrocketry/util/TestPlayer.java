package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.render.gui.PlayerGui;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class TestPlayer implements IUpdateListener, IFixedUpdateListener, IPlayer, ICollisionBody {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
    private final float GRAVITY = -0.1f;
    private final float WALK_SPEED = 0.22f;
    private final float SPRINT_SPEED = 0.5f;
    private final float JUMP_SPEED = 0.5f;
    private final float EYE_HEIGHT = 1.57f;
    private final float HEIGHT = 1.73f;
    private final float HALF_WIDTH = 0.3f;
    private final Vector3d VELOCITY = new Vector3d();
    private final World WORLD;
    private final BoundingBox BOUNDING_BOX;
    private final Transform TRANSFORM;
    private final Transform PREVIOUS_TRANSFORM;
    private final PlayerGui GUI_ELEMENT;
    private final Vector3f INTERPOLATED_CAMERA_ROTATION;
    private final Vector3d INTERPOLATED_CAMERA_POSITION;


    private int[] hotbarItems = {
            Block.GRASS,
            Block.COBBLESTONE,
            Block.DIRT,
            Block.STONE,
            Block.AIR,
            Block.AIR,
            Block.AIR,
            Block.AIR,
            Block.AIR,
            Block.AIR
    };
    private int currentHotbarSlot = 0;
    private boolean onGround = false;
    private float cameraInterpolationFactor = 0f;
    private int blockInteractionTimer = 0;
    private boolean lockMouseToCenterForCameraRotation = false;
    public boolean hasGravity = true;
    private BlockRaycastResult targetBlock;


    public TestPlayer(World world) {

        WORLD = world;

        TRANSFORM = new Transform();
        PREVIOUS_TRANSFORM = new Transform();

        INTERPOLATED_CAMERA_ROTATION = new Vector3f(PREVIOUS_TRANSFORM.getRotation());
        INTERPOLATED_CAMERA_POSITION = getCameraPosition();

        BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
        BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

        BOUNDING_BOX = new BoundingBox();
        updateBoundingBox();

        GUI_ELEMENT = new PlayerGui(this);
        Renderer.get().addRenderable(GUI_ELEMENT);

    }

    public Vector3d getPosition() {
        return getTransform().getPosition();
    }

    @Override
    public Vector3i getBlockPosition() {
        return new Vector3i(
                (int) getPosition().x,
                (int) getPosition().y,
                (int) getPosition().z
        );
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


        // get bounding boxes of blocks near player

        BoundingBox broadPhaseBoundingBox = new BoundingBox(getBoundingBox()).grow(VELOCITY.length() * 2);

        List<Chunk> broadPhaseChunks = Collision.getChunksTouchingBoundingBox(broadPhaseBoundingBox);
        List<BoundingBox> blockBoundingBoxesNearPlayer = new ArrayList<>();

        if (!broadPhaseChunks.isEmpty()) {
            for (Chunk chunk : broadPhaseChunks) {
                List<BoundingBox> boundingBoxes = chunk.getBlockBoundingBoxes(broadPhaseBoundingBox);
                blockBoundingBoxesNearPlayer.addAll(boundingBoxes);
            }
        }



        // move and collide

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
        Renderer.get().getCamera().getTransform().getRotation().set(TRANSFORM.getRotation());
        Renderer.get().getCamera().getTransform().getPosition().set(getCameraPosition());

        KeyboardInput keyboardInput = KeyboardInput.get();
        float moveSpeed = KeyboardInput.get().isKeyPressed(GLFW_KEY_LEFT_CONTROL) ? SPRINT_SPEED : WALK_SPEED;
        float rotateSpeed = 0.0025f;


        // get input
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
        ).rotateY(-TRANSFORM.getRotation().y);

        if (moveAmount.length() > 0f) {
            moveAmount.normalize();
        }

        moveAmount.y = (upDirection - downDirection);
        moveAmount.mul(moveSpeed);


        if (!getWorld().isPlayerInUnloadedChunk(this)) {

            VELOCITY.x = Math.lerp(VELOCITY.x, moveAmount.x, 0.5f);
            VELOCITY.z = Math.lerp(VELOCITY.z, moveAmount.z, 0.5f);

            if (hasGravity) {
                VELOCITY.add(0f, GRAVITY, 0f);

                if (isOnGround() && keyboardInput.isKeyPressed(GLFW_KEY_SPACE)) {
                    VELOCITY.y = JUMP_SPEED;
                }
            } else {
                float verticalMoveAmount = (keyboardInput.isKeyPressed(GLFW_KEY_SPACE) ? 1 : 0) - (keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 1 : 0);
                VELOCITY.y = Math.lerp(VELOCITY.y, verticalMoveAmount * moveSpeed, 0.6f);
            }

            // apply translation and rotation
            PREVIOUS_TRANSFORM.set(TRANSFORM);
            move();
            updateBoundingBox();

            if (keyboardInput.isKeyJustPressed(GLFW_KEY_F2)) {
                lockMouseToCenterForCameraRotation = !lockMouseToCenterForCameraRotation;

                glfwSetInputMode(
                        Window.get().getWindowHandle(),
                        GLFW_CURSOR,
                        lockMouseToCenterForCameraRotation ? GLFW_CURSOR_HIDDEN : GLFW_CURSOR_NORMAL
                );
            }

            if (lockMouseToCenterForCameraRotation) {
                float verticalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().y;
                float horizontalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().x;

                mouseInput.setMousePosition(BackyardRocketry.getInstance().getWindow().getWidth() / 2, BackyardRocketry.getInstance().getWindow().getHeight() / 2, false);

                TRANSFORM.rotateX(verticalRotateAmount);
                TRANSFORM.rotateY(horizontalRotateAmount);

                // clamp vertical rotation
                TRANSFORM.getRotation().x = Math.max(TRANSFORM.getRotation().x, (float) -Math.PI * 0.5f);
                TRANSFORM.getRotation().x = Math.min(TRANSFORM.getRotation().x, (float) Math.PI * 0.5f);
            }

            cameraInterpolationFactor = 0f;

            hotbarManagement();
            blockInteraction();
        }

        WORLD.updateChunksAroundPlayer(this);
    }

    private void hotbarManagement() {

        KeyboardInput keyboardInput = KeyboardInput.get();
        MouseInput mouseInput = MouseInput.get();

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_1)) {
            setCurrentHotbarSlot(0);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_2)) {
            setCurrentHotbarSlot(1);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_3)) {
            setCurrentHotbarSlot(2);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_4)) {
            setCurrentHotbarSlot(3);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_5)) {
            setCurrentHotbarSlot(4);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_6)) {
            setCurrentHotbarSlot(5);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_7)) {
            setCurrentHotbarSlot(6);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_8)) {
            setCurrentHotbarSlot(7);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_9)) {
            setCurrentHotbarSlot(8);
        } else if (keyboardInput.isKeyJustPressed(GLFW_KEY_0)) {
            setCurrentHotbarSlot(9);
        }

        if (mouseInput.getMouseScrollY() != 0.0) {

            int scrollDirection = (int) Math.signum(mouseInput.getMouseScrollY());
            offsetCurrentHotbarSlot(-scrollDirection);

        }

    }

    public void offsetCurrentHotbarSlot(int offsetAmount) {

        currentHotbarSlot += offsetAmount;

        while (currentHotbarSlot > 9) {
            currentHotbarSlot -= 10;
        }

        while (currentHotbarSlot < 0) {
            currentHotbarSlot += 10;
        }

    }

    private void blockInteraction() {

        MouseInput mouseInput = MouseInput.get();

        Vector3d rayFrom = new Vector3d(getPosition()).add(0, EYE_HEIGHT, 0);
        Vector3d rayDirection = new Vector3d(0, 0, -1)
                .rotateX(-TRANSFORM.getRotation().x)
                .rotateY(-TRANSFORM.getRotation().y);
        int rayLength = 7;

        targetBlock = Collision.blockRaycast(rayFrom, rayDirection, rayLength);

        if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_RIGHT)) {
            blockInteractionTimer = 0;
        }
        if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_LEFT)) {
            blockInteractionTimer = 0;
        }

        if (blockInteractionTimer == 0) {
            if (mouseInput.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {

                breakBlock();
                blockInteractionTimer = 5;

            }

            if (mouseInput.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {

                placeBlock(getHotbarSlotContents(currentHotbarSlot));
                blockInteractionTimer = 5;

            }
        } else {
            blockInteractionTimer = Math.max(0, blockInteractionTimer - 1);
        }

    }

    public BlockRaycastResult getTargetBlock() {
        return targetBlock;
    }

    private void breakBlock() {

        if (targetBlock == null) return;

        Chunk targetBlockChunk = targetBlock.getChunk();
        targetBlockChunk.setBlock(
                        targetBlockChunk.toLocalX(targetBlock.getBlockX()),
                        targetBlockChunk.toLocalY(targetBlock.getBlockY()),
                        targetBlockChunk.toLocalZ(targetBlock.getBlockZ()),
                        Block.AIR
        );

    }

    private void placeBlock(int blockToPlace) {

        if (targetBlock == null) return;

        Block.Face face = targetBlock.getFace();

        int placePosX = targetBlock.getBlockX();
        int placePosY = targetBlock.getBlockY();
        int placePosZ = targetBlock.getBlockZ();

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

        BoundingBox blockBoundingBox = Block.getBlockCollision(blockToPlace);

        if (blockBoundingBox != null) {
            blockBoundingBox.translate(placePosX, placePosY, placePosZ);

            if (getBoundingBox().collideWithBoundingBoxExclusive(blockBoundingBox) != Collision.AABBCollisionResultType.OUTSIDE) {
                return;
            }
        }

        chunk.setBlock(
                chunk.toLocalX(placePosX),
                chunk.toLocalY(placePosY),
                chunk.toLocalZ(placePosZ),
                blockToPlace
        );
    }

    private void updateBoundingBox() {

        BOUNDING_BOX.getMin().x = getPosX() - HALF_WIDTH;
        BOUNDING_BOX.getMin().y = getPosY();
        BOUNDING_BOX.getMin().z = getPosZ() - HALF_WIDTH;

        BOUNDING_BOX.getMax().x = getPosX() + HALF_WIDTH;
        BOUNDING_BOX.getMax().y = getPosY() + HEIGHT;
        BOUNDING_BOX.getMax().z = getPosZ() + HALF_WIDTH;
    }

    @Override
    public void update(double deltaTime) {

        interpolateCameraTransform(deltaTime);

    }

    public Vector3d getCameraPosition() {

        return getCameraPosition(getPosition());

    }

    public Vector3d getCameraPosition(Vector3d playerPosition) {

        return new Vector3d(playerPosition).add(0, EYE_HEIGHT, 0);

    }

    private void interpolateCameraTransform(double deltaTime) {

        cameraInterpolationFactor += (float) deltaTime / CAMERA_INTERPOLATION_DURATION;
        cameraInterpolationFactor = Math.min(cameraInterpolationFactor, 1f);

        Camera camera = Renderer.get().getCamera();

        // reset interpolation to t = 0 to begin interpolation
        INTERPOLATED_CAMERA_ROTATION.set(PREVIOUS_TRANSFORM.getRotation());
        INTERPOLATED_CAMERA_POSITION.set(getCameraPosition(PREVIOUS_TRANSFORM.getPosition()));

        // interpolate camera rotation and position
        INTERPOLATED_CAMERA_ROTATION.set(
                Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.x, TRANSFORM.getRotation().x, cameraInterpolationFactor),
                Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.y, TRANSFORM.getRotation().y, cameraInterpolationFactor),
                Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.z, TRANSFORM.getRotation().z, cameraInterpolationFactor)
        );
        INTERPOLATED_CAMERA_POSITION.lerp(getCameraPosition(), cameraInterpolationFactor);

        // set camera rotation and position to interpolated values
        camera.getTransform().getRotation().set(INTERPOLATED_CAMERA_ROTATION);
        camera.getTransform().getPosition().set(INTERPOLATED_CAMERA_POSITION);

    }

    public Transform getTransform() {
        return TRANSFORM;
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

    public int getCurrentHotbarSlot() {
        return currentHotbarSlot;
    }

    public void setCurrentHotbarSlot(int currentHotbarSlot) {
        this.currentHotbarSlot = currentHotbarSlot;
    }

    public int getHotbarSlotContents(int slotIndex) {
        return hotbarItems[slotIndex];
    }
}
