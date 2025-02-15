package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.world.ServerWorld;


public class EntityServerPlayer extends EntityPlayer {
    private boolean[] movementInputs = new boolean[6];

    public EntityServerPlayer(ServerWorld world) {
        super(world);
    }



    public void setMovementInputs(boolean[] movementInputs) {

        if (movementInputs.length < MOVEMENT_INPUT_SIZE) return;

        this.movementInputs = movementInputs;

    }



    @Override
    public void fixedUpdate() {
        super.fixedUpdate();

        FOOTSTEP_AUDIO.fixedUpdate();


        handleCrouchingAndSprinting();

        float rotateSpeed = 0.0025f;


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
        moveAmount.mul(moveSpeed);


        getVelocity().x = Math.lerp(getVelocity().x, moveAmount.x, 0.5f);
        getVelocity().z = Math.lerp(getVelocity().z, moveAmount.z, 0.5f);

        if (!hasEntityComponent(ComponentGravity.class)) {
            float verticalMoveAmount = upDirection - downDirection;
            getVelocity().y = Math.lerp(getVelocity().y, verticalMoveAmount * moveSpeed, 0.6f);
        }

        if (isOnGround() && movementInputs[MOVEMENT_INPUT_JUMP]) {
            getVelocity().y = JUMP_SPEED;
            FOOTSTEP_AUDIO.playAudio();
        }

        // apply translation and rotation
        getPreviousTransform().set(getTransform());
        move();
        updateBoundingBox();

        double moveDistance = getPreviousTransform().getPosition().distance(getTransform().getPosition());
        FOOTSTEP_AUDIO.setMoveDistance((float) moveDistance);
        FOOTSTEP_AUDIO.setOnGround(isOnGround());

        moving = moveDistance > 0.01f;

        //blockInteraction();


        getWorld().updateChunksAroundPlayer(this);

        //pickupNearbyItems();

    }
}
