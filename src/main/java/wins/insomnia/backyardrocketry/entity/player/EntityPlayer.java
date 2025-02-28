package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;
import wins.insomnia.backyardrocketry.entity.LivingEntity;
import wins.insomnia.backyardrocketry.entity.component.ComponentFootstepAudio;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.entity.component.ComponentStandardPlayer;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.physics.ICollisionBody;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class EntityPlayer extends LivingEntity implements IPlayer, ICollisionBody, IBoundingBoxEntity {

	public static final int MOVEMENT_INPUT_SIZE = 7; // amount of inputs to send
	protected final int MOVEMENT_INPUT_FORWARD = 0;
	protected final int MOVEMENT_INPUT_BACKWARD = 1;
	protected final int MOVEMENT_INPUT_RIGHT = 2;
	protected final int MOVEMENT_INPUT_LEFT = 3;
	protected final int MOVEMENT_INPUT_JUMP = 4;
	protected final int MOVEMENT_INPUT_CROUCH = 5;
	protected final int MOVEMENT_INPUT_SPRINT = 6;

	private final float REACH_DISTANCE = 4f;

	// movement speeds (meters per tick)
	public final float CROUCH_SPEED = 1f / Updater.getFixedUpdatesPerSecond();
	public final float WALK_SPEED = 3f / Updater.getFixedUpdatesPerSecond();
	public final float SPRINT_SPEED = 6f / Updater.getFixedUpdatesPerSecond();
	public final float FLY_SPEED = WALK_SPEED * 2.5f;
	public final float JUMP_SPEED = 0.45f;
	protected float moveSpeed;
	protected boolean onGround = false;
	protected boolean crouching = false;
	protected boolean moving = false;
	protected final ComponentFootstepAudio FOOTSTEP_AUDIO;
	protected final ComponentGravity GRAVITY_COMPONENT;
	protected BlockRaycastResult targetBlock;

	public final float HEIGHT = 1.73f;
	public final float EYE_HEIGHT = HEIGHT - 0.1778f;
	public final float HALF_WIDTH = 0.4f;
	private final BoundingBox BOUNDING_BOX;
	private final Transform PREVIOUS_TRANSFORM;
	private int breakAudioDelay = 0;
	private byte[] hotbarItems = {
			Blocks.GRASS,
			Blocks.COBBLESTONE,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.LOG,
			Blocks.LEAVES,
			Blocks.WOODEN_PLANKS,
			Blocks.GLASS,
			Blocks.BRICKS,
			Blocks.LIMESTONE
	};
	private int currentHotbarSlot = 0;
	private int blockInteractionTimer = 0;
	private int breakProgress = 0;
	private boolean hasCollision = true;
	private boolean flying = false;
	private final PlayerInventoryManager INVENTORY_MANAGER = new PlayerInventoryManager(this);

	public EntityPlayer(World world, java.util.UUID uuid) {
		super(world, uuid);

		PREVIOUS_TRANSFORM = new Transform();

		BackyardRocketry.getInstance().getUpdater().registerUpdateListener(this);
		BackyardRocketry.getInstance().getUpdater().registerFixedUpdateListener(this);

		BOUNDING_BOX = new BoundingBox();
		updateBoundingBox();


		getRotation().x = Math.toRadians(90);

		addEntityComponent(new ComponentStandardPlayer(this));
		GRAVITY_COMPONENT = new ComponentGravity(this, 1f);
		addEntityComponent(GRAVITY_COMPONENT);
		FOOTSTEP_AUDIO = new ComponentFootstepAudio(this);
		addEntityComponent(FOOTSTEP_AUDIO);
	}

	public void setFlying(boolean value) {
		flying = value;
	}

	public float getReachDistance() {
		return REACH_DISTANCE;
	}

	public Transform getPreviousTransform() {
		return PREVIOUS_TRANSFORM;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public boolean isMoving() {
		return moving;
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

	@Override
	public Vector3d getInterpolatedPosition() {
		return new Vector3d(getPosition());
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

	public boolean isFlying() {
		return flying;
	}

	public void move() {


		// get bounding boxes of blocks near player

		BoundingBox broadPhaseBoundingBox = new BoundingBox(getBoundingBox()).grow(getVelocity().length() * 2);

		List<Chunk> broadPhaseChunks = Collision.getChunksTouchingBoundingBox(getWorld(), broadPhaseBoundingBox);
		List<BoundingBox> blockBoundingBoxesNearPlayer = new ArrayList<>();

		if (!broadPhaseChunks.isEmpty()) {
			for (Chunk chunk : broadPhaseChunks) {
				List<BoundingBox> boundingBoxes = chunk.getBlockBoundingBoxes(broadPhaseBoundingBox);
				blockBoundingBoxesNearPlayer.addAll(boundingBoxes);
			}
		}



		// move and collide
		if (hasCollision) {
			if (getVelocity().x != 0f) {
				for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
					getVelocity().x = boundingBox.collideX(getBoundingBox(), getVelocity().x);
				}

				getPosition().x += getVelocity().x;
			}

			onGround = false;
			if (getVelocity().y != 0f) {
				for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
					double newVelocity = boundingBox.collideY(getBoundingBox(), getVelocity().y);

					if (newVelocity != getVelocity().y && Math.signum(getVelocity().y) < 0d) {
						onGround = true;
					}

					getVelocity().y = newVelocity;
				}

				getPosition().y += getVelocity().y;
			}

			if (getVelocity().z != 0f) {
				for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
					getVelocity().z = boundingBox.collideZ(getBoundingBox(), getVelocity().z);
				}

				getPosition().z += getVelocity().z;
			}
		} else {
			getPosition().x += getVelocity().x;
			getPosition().y += getVelocity().y;
			getPosition().z += getVelocity().z;
		}
	}

	public boolean isOnGround() {
		return onGround;
	}

	protected void handleCrouchingAndSprinting() {



	}


	@Override
	public void fixedUpdate() {

	}

	@Override
	public void registeredFixedUpdateListener() {

	}

	@Override
	public void unregisteredFixedUpdateListener() {

	}

	private boolean shouldInstantlyBreakBlocks() {
		return false;
	}

	protected void updateBoundingBox() {

		BOUNDING_BOX.getMin().x = getPosX() - HALF_WIDTH;
		BOUNDING_BOX.getMin().y = getPosY();
		BOUNDING_BOX.getMin().z = getPosZ() - HALF_WIDTH;

		BOUNDING_BOX.getMax().x = getPosX() + HALF_WIDTH;
		BOUNDING_BOX.getMax().y = getPosY() + HEIGHT;
		BOUNDING_BOX.getMax().z = getPosZ() + HALF_WIDTH;
	}

	@Override
	public void update(double deltaTime) {
		super.update(deltaTime);
	}

	public Vector3d getCameraPosition() {

		return getCameraPosition(getPosition());

	}

	public Vector3d getCameraPosition(Vector3d playerPosition) {

		return new Vector3d(playerPosition).add(0, EYE_HEIGHT, 0);

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

	public byte getHotbarSlotContents(int slotIndex) {
		return hotbarItems[slotIndex];
	}

	public int getBreakProgress() {
		return breakProgress;
	}

	public PlayerInventoryManager getInventoryManager() {
		return INVENTORY_MANAGER;
	}

	public BlockRaycastResult getTargetBlock() {
		return targetBlock;
	}


	public static class PlayerMovementResult {

		public Vector3d position;
		public Vector3d velocity;
		public boolean onGround;

		public PlayerMovementResult(EntityPlayer player) {
			onGround = player.isOnGround();
		}

	}

}
