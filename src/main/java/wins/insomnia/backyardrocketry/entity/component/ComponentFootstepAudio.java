package wins.insomnia.backyardrocketry.entity.component;

import org.joml.Vector3f;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.audio.AudioBuffer;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;

public class ComponentFootstepAudio extends Component {

	private final Entity ENTITY;
	private float moveDistance = 0f;
	private int audioTickTimer = 0;
	private int audioTickInterval = 1;
	private boolean onGround = false;

	// horizontal distance between feet
	private float footDistance = 0.15f;
	private int footIndex = 0;

	private final Vector3f FOOT_POSITION_OFFSET = new Vector3f(0f, -0.5f, 0f);

	public ComponentFootstepAudio(Entity entity) {
		ENTITY = entity;
	}


	public void setFootDistance(float footDistance) {
		this.footDistance = footDistance;
	}

	public void setFootPositionOffset(float x, float y, float z) {
		FOOT_POSITION_OFFSET.set(x, y, z);
	}

	public Entity getEntity() {
		return ENTITY;
	}

	public boolean isOnGround() {
		return onGround;
	}

	public void setOnGround(boolean onGround) {

		if (!isOnGround() && onGround) {

			playAudio();
			playAudio();

		}


		this.onGround = onGround;
	}

	public int getAudioTickInterval() {
		return audioTickInterval;
	}

	@Override
	public void fixedUpdate() {

		if (moveDistance > 0.1f) {
			audioTickInterval = Math.max(1, (int) (1.5f / moveDistance));

			if (audioTickInterval > 100) {
				audioTickInterval = 10000000;
				audioTickTimer = audioTickInterval;
			}

		}

		if (audioTickTimer > audioTickInterval) {
			audioTickTimer = audioTickInterval;
		}

		if (audioTickTimer >= 0 && isOnGround()) {

			if (moveDistance > 0.1f) {
				audioTickTimer -= 1;

				if (audioTickTimer <= 0) {
					playAudio();
				}
			}
		}

	}

	public void setMoveDistance(float moveDistance) {
		this.moveDistance = moveDistance;
	}

	public void playAudio() {

		audioTickTimer = audioTickInterval;

		Vector3f position = new Vector3f(getEntity().getPosition()).add(FOOT_POSITION_OFFSET);

		if (footIndex == 0) {
			position.add(new Vector3f(-footDistance, 0, 0).rotateY(getEntity().getRotation().y));
		} else {
			position.add(new Vector3f(footDistance, 0, 0).rotateY(getEntity().getRotation().y));
		}


		Vector3i blockPosition = getEntity().getWorld().getBlockPosition(position);
		byte block = getEntity().getWorld().getBlock(blockPosition.x, blockPosition.y, blockPosition.z);

		BlockAudio blockAudio = Block.getBlockAudio(block);
		if (blockAudio == null) return;

		AudioBuffer stepAudio = blockAudio.getStepAudio();
		if (stepAudio == null) return;

		if (GameplayScene.get() == null || GameplayScene.hasClient()) {

			AudioManager.get().playAudioSpatial(stepAudio, false, false, true)
					.setPosition(position)
					.setPitch(0.8f + (float) (Math.random() * 0.6))
					.setGain(moveDistance * 2f);

		}

		footIndex = (footIndex + 1) % 2;

	}

}
