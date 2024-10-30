package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.entity.LivingEntity;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.render.gui.PlayerGui;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;
import wins.insomnia.backyardrocketry.util.input.MouseInput;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

public class TestPlayer extends LivingEntity implements IPlayer, ICollisionBody {

    private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
    private final float CROUCH_SPEED = 0.1f;
    private final float WALK_SPEED = 0.22f;
    private final float FLY_SPEED = 1f;
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
    private final FirstPersonHandItemRenderable FIRST_PERSON_HAND_ITEM;

    private byte[] hotbarItems = {
            Block.GRASS,
            Block.COBBLESTONE,
            Block.DIRT,
            Block.STONE,
            Block.LOG,
            Block.LEAVES,
            Block.WOODEN_PLANKS,
            Block.GLASS,
            Block.BRICKS,
            Block.WOOD
    };
    private int currentHotbarSlot = 0;
    private boolean onGround = false;
    private float cameraInterpolationFactor = 0f;
    private int blockInteractionTimer = 0;
    private boolean lockMouseToCenterForCameraRotation = false;
    public boolean hasGravity = true;
    private boolean hasCollision = true;
    private BlockRaycastResult targetBlock;
    private int breakProgress = 0;
    private final PlayerInventoryManager INVENTORY_MANAGER = new PlayerInventoryManager(this);


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
        TRANSFORM.getRotation().x = Math.toRadians(90);

        FIRST_PERSON_HAND_ITEM = new FirstPersonHandItemRenderable();
        Renderer.get().addRenderable(FIRST_PERSON_HAND_ITEM);
        FIRST_PERSON_HAND_ITEM.setBlock(Block.GRASS);
    }

    public Vector3d getPosition() {
        return getTransform().getPosition();
    }

    public Vector3d getInterpolatedPosition() {
        return INTERPOLATED_CAMERA_POSITION;
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
        if (hasCollision) {
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
        } else {
            getPosition().x += VELOCITY.x;
            getPosition().y += VELOCITY.y;
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
        if (keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            moveSpeed = CROUCH_SPEED;
        }

        if (!hasGravity) {
            moveSpeed = FLY_SPEED;
        }
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


        if (keyboardInput.isKeyJustPressed(GLFW_KEY_F4)) {
            hasGravity = !hasGravity;
            hasCollision = !hasCollision;
        }


        VELOCITY.x = Math.lerp(VELOCITY.x, moveAmount.x, 0.5f);
        VELOCITY.z = Math.lerp(VELOCITY.z, moveAmount.z, 0.5f);

        if (hasGravity) {
            VELOCITY.add(0f, World.get().getGravity(), 0f);

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

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            lockMouseToCenterForCameraRotation = !lockMouseToCenterForCameraRotation;
            updateCursorVisibility();
        }

        if (lockMouseToCenterForCameraRotation && INVENTORY_MANAGER.isClosed()) {
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

        if (keyboardInput.isKeyJustPressed(GLFW_KEY_E)) {
            INVENTORY_MANAGER.toggleInventory();
            updateCursorVisibility();
        }

        WORLD.updateChunksAroundPlayer(this);
    }

    public void updateCursorVisibility() {
        glfwSetInputMode(
                Window.get().getWindowHandle(),
                GLFW_CURSOR,
                lockMouseToCenterForCameraRotation && INVENTORY_MANAGER.isClosed() ? GLFW_CURSOR_HIDDEN : GLFW_CURSOR_NORMAL
        );
    }

    @Override
    public void registeredFixedUpdateListener() {

    }

    @Override
    public void unregisteredFixedUpdateListener() {

    }

    private void hotbarManagement() {

        if (INVENTORY_MANAGER.isOpen()) return;

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

        setCurrentHotbarSlot(currentHotbarSlot);
    }

    private void blockInteraction() {

        MouseInput mouseInput = MouseInput.get();

        Vector3d rayFrom = new Vector3d(getPosition()).add(0, EYE_HEIGHT, 0);
        Vector3d rayDirection = new Vector3d(0, 0, -1)
                .rotateX(-TRANSFORM.getRotation().x)
                .rotateY(-TRANSFORM.getRotation().y);
        int rayLength = 7;

        BlockRaycastResult previousTargetBlock = targetBlock;
        targetBlock = Collision.blockRaycast(rayFrom, rayDirection, rayLength);

        if (previousTargetBlock != null && targetBlock != null && !previousTargetBlock.equals(targetBlock, false)) {
            breakProgress = 0;
        }
        if (mouseInput.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            FIRST_PERSON_HAND_ITEM.playSwingAnimation(false);
        }

        if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_RIGHT)) {
            blockInteractionTimer = 0;
        }
        if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_LEFT)) {
            blockInteractionTimer = 0;
        }

        if (blockInteractionTimer == 0) {
            if (mouseInput.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {

                breakBlock();

            }

            if (mouseInput.isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {

                placeBlock(getHotbarSlotContents(currentHotbarSlot));
                blockInteractionTimer = 5;

            }
        } else {
            blockInteractionTimer = Math.max(0, blockInteractionTimer - 1);
        }

        if (mouseInput.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)) {
            breakProgress = 0;
        }

    }

    public BlockRaycastResult getTargetBlock() {
        return targetBlock;
    }

    private void breakBlock() {

        if (INVENTORY_MANAGER.isOpen()) return;
        if (targetBlock == null) return;

        FIRST_PERSON_HAND_ITEM.playSwingAnimation(true);


        byte block = getWorld().getBlock(
                targetBlock.getBlockX(),
                targetBlock.getBlockY(),
                targetBlock.getBlockZ()
        );

        if (Block.getBlockHealth(block) < 0) {
            breakProgress = 0;
            return;
        }

        if (shouldInstantlyBreakBlocks()) {
            breakProgress = Block.getBlockHealth(block);
        } else {
            breakProgress += 1;
        }

        if (breakProgress >= Block.getBlockHealth(block)) {
            Chunk targetBlockChunk = targetBlock.getChunk();
            targetBlockChunk.setBlock(
                    targetBlockChunk.toLocalX(targetBlock.getBlockX()),
                    targetBlockChunk.toLocalY(targetBlock.getBlockY()),
                    targetBlockChunk.toLocalZ(targetBlock.getBlockZ()),
                    Block.AIR
            );
            breakProgress = 0;

            if (shouldInstantlyBreakBlocks()) {
                blockInteractionTimer = 5;
            } else if (Block.getBlockHealth(block) > 1) {
                blockInteractionTimer = 2;
            }
        }



    }

    private boolean shouldInstantlyBreakBlocks() {
        return false;
    }

    private void placeBlock(byte blockToPlace) {

        if (INVENTORY_MANAGER.isOpen()) return;
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
                blockToPlace,
                true
        );

        FIRST_PERSON_HAND_ITEM.playSwingAnimation(false);
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

    @Override
    public void registeredUpdateListener() {

    }

    @Override
    public void unregisteredUpdateListener() {

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
        FIRST_PERSON_HAND_ITEM.setBlock(getHotbarSlotContents(currentHotbarSlot));
    }

    public byte getHotbarSlotContents(int slotIndex) {
        return hotbarItems[slotIndex];
    }

    public int getBreakProgress() {
        return breakProgress;
    }

    public PlayerInventoryManager getInventoryManager() {
        return INVENTORY_MANAGER;
    }
}
