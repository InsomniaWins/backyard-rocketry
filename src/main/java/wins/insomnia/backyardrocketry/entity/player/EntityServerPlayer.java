package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.network.entity.player.PacketPlayerTransform;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.util.Arrays;
import java.util.UUID;


public class EntityServerPlayer extends EntityPlayer {
    private boolean loadingTerrain = true; // true when player is just now connecting and loading terrain
    private boolean[] movementInputs = new boolean[MOVEMENT_INPUT_SIZE];
    private final int CONNECTION_ID;
    private boolean jumping = false; // true when player queued a jump

    public EntityServerPlayer(int connectionId, ServerWorld world, java.util.UUID uuid) {
        super(world, uuid);
        CONNECTION_ID = connectionId;

        moveSpeed = WALK_SPEED;
    }

    public int getConnectionId() {
        return CONNECTION_ID;
    }

    public void setMovementInputs(boolean[] movementInputs) {

        if (movementInputs.length != MOVEMENT_INPUT_SIZE) return;

        this.movementInputs = movementInputs;

    }

    public void sendUnreliableInfoToClients() {

        ServerController.sendUnreliable(
                new PacketPlayerTransform().setTransform(getTransform())
        );

    }


    public void jump() {

        jumping = true;

    }

    private void handleMovement() {

        handleCrouchingAndSprinting();


        // get input
        float forwardDirection = movementInputs[MOVEMENT_INPUT_FORWARD] ? 1 : 0;
        float backwardDirection = movementInputs[MOVEMENT_INPUT_BACKWARD] ? 1 : 0;

        float leftDirection = movementInputs[MOVEMENT_INPUT_LEFT] ? 1 : 0;
        float rightDirection = movementInputs[MOVEMENT_INPUT_RIGHT] ? 1 : 0;

        float upDirection = movementInputs[MOVEMENT_INPUT_JUMP] ? 1 : 0;
        float downDirection = movementInputs[MOVEMENT_INPUT_CROUCH] ? 1 : 0;

        Vector3f moveAmount = new Vector3f(
                (rightDirection - leftDirection),
                0f,
                (backwardDirection - forwardDirection)
        ).rotateY(-getRotation().y);

        if (moveAmount.length() > 0f) {
            moveAmount.normalize();
        }

        moveAmount.y = (upDirection - downDirection);

        if (isFlying()) {
            moveSpeed = FLY_SPEED;
        } else {
            if (movementInputs[MOVEMENT_INPUT_CROUCH]) {
                moveSpeed = CROUCH_SPEED;
            } else if (movementInputs[MOVEMENT_INPUT_SPRINT]) {
                moveSpeed = SPRINT_SPEED;
            } else {
                moveSpeed = WALK_SPEED;
            }
        }

        moveAmount.mul(moveSpeed);


        getVelocity().x = Math.lerp(getVelocity().x, moveAmount.x, 0.5f);
        getVelocity().z = Math.lerp(getVelocity().z, moveAmount.z, 0.5f);


        if (isFlying()) {

            getVelocity().y = Math.lerp(getVelocity().y, moveAmount.y, 0.5f);

        } else {
            if (!hasEntityComponent(ComponentGravity.class)) {
                float verticalMoveAmount = upDirection - downDirection;
                getVelocity().y = Math.lerp(getVelocity().y, verticalMoveAmount * moveSpeed, 0.6f);
            } else {
                GRAVITY_COMPONENT.fixedUpdate();
            }
        }

        if (!isFlying() && jumping) {
            if (isOnGround()) {
                getVelocity().y = JUMP_SPEED;
                FOOTSTEP_AUDIO.playAudio();
            }

            jumping = false;
        }


        // apply translation and rotation
        getPreviousTransform().set(getTransform());
        move();
        updateBoundingBox();

        double moveDistance = getPreviousTransform().getPosition().distance(getTransform().getPosition());

        moving = moveDistance > 0.01f;

    }

    @Override
    public void fixedUpdate() {
        super.fixedUpdate();


        if (!isLoadingTerrain()) {
            handleMovement();
            //blockInteraction();
            //pickupNearbyItems();
        }

        getWorld().updateChunksAroundPlayer(this);

    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        sendUnreliableInfoToClients();
    }

    public void setFinishedLoadingTerrain() {
        loadingTerrain = false;
    }

    public boolean isLoadingTerrain() {

        ChunkPosition chunkPosition = getWorld().getPlayersChunkPosition(this);
        ServerWorld serverWorld = (ServerWorld) getWorld();

        return loadingTerrain || !serverWorld.isChunkLoaded(chunkPosition, ServerChunk.GenerationPass.DECORATION);
    }
}
