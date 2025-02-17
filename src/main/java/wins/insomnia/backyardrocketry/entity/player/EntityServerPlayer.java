package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.network.player.PacketPlayerTransform;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ServerWorld;

import java.util.Arrays;


public class EntityServerPlayer extends EntityPlayer {
    private boolean[] movementInputs = new boolean[MOVEMENT_INPUT_SIZE];
    private final int CONNECTION_ID;
    private boolean jumping = false; // true when player queued a jump

    public EntityServerPlayer(int connectionId, ServerWorld world) {
        super(world);
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


    @Override
    public void fixedUpdate() {
        super.fixedUpdate();

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

        if (movementInputs[MOVEMENT_INPUT_CROUCH]) {
            moveSpeed = CROUCH_SPEED;
        } else if (movementInputs[MOVEMENT_INPUT_SPRINT]) {
            moveSpeed = SPRINT_SPEED;
        } else {
            moveSpeed = WALK_SPEED;
        }

        moveAmount.mul(moveSpeed);


        getVelocity().x = Math.lerp(getVelocity().x, moveAmount.x, 0.5f);
        getVelocity().z = Math.lerp(getVelocity().z, moveAmount.z, 0.5f);


        if (!hasEntityComponent(ComponentGravity.class)) {
            float verticalMoveAmount = upDirection - downDirection;
            getVelocity().y = Math.lerp(getVelocity().y, verticalMoveAmount * moveSpeed, 0.6f);
        } else {
            GRAVITY_COMPONENT.fixedUpdate();
        }

        if (jumping) {
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

        //blockInteraction();


        getWorld().updateChunksAroundPlayer(this);

        //pickupNearbyItems();

    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        sendUnreliableInfoToClients();
    }
}
