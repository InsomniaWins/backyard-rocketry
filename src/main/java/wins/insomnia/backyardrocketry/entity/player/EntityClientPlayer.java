package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.entity.component.ComponentFootstepAudio;
import wins.insomnia.backyardrocketry.entity.component.ComponentGravity;
import wins.insomnia.backyardrocketry.gui.elements.PlayerGui;
import wins.insomnia.backyardrocketry.physics.BlockRaycastResult;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.io.device.KeyboardInput;
import wins.insomnia.backyardrocketry.util.io.device.MouseInput;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.block.Block;

import static org.lwjgl.glfw.GLFW.*;

public class EntityClientPlayer extends EntityPlayer {

	private final float CAMERA_INTERPOLATION_DURATION = 1.0f / Updater.getFixedUpdatesPerSecond();
	private final PlayerGui GUI_ELEMENT;
	private final Vector3f INTERPOLATED_CAMERA_ROTATION;
	private final Vector3d INTERPOLATED_CAMERA_POSITION;
	private final FirstPersonHandItemRenderable FIRST_PERSON_HAND_ITEM;
	private float cameraInterpolationFactor = 0f;
	private boolean lockMouseToCenterForCameraRotation = false;


	public EntityClientPlayer(ClientWorld world) {
		super(world);

		INTERPOLATED_CAMERA_ROTATION = new Vector3f(getPreviousTransform().getRotation());
		INTERPOLATED_CAMERA_POSITION = getCameraPosition();

		GUI_ELEMENT = new PlayerGui(this);
		Updater.get().registerUpdateListener(GUI_ELEMENT);
		Renderer.get().addRenderable(GUI_ELEMENT);

		FIRST_PERSON_HAND_ITEM = new FirstPersonHandItemRenderable();
		Renderer.get().addRenderable(FIRST_PERSON_HAND_ITEM);
		FIRST_PERSON_HAND_ITEM.setBlock(Block.GRASS);

	}

	@Override
	public Vector3d getInterpolatedPosition() {
		return INTERPOLATED_CAMERA_POSITION;
	}



	public void updateCursorVisibility() {
		glfwSetInputMode(
				Window.get().getWindowHandle(),
				GLFW_CURSOR,
				lockMouseToCenterForCameraRotation ? GLFW_CURSOR_HIDDEN : GLFW_CURSOR_NORMAL
		);
	}


	// unreliably send movement keyboard inputs to server
	private void sendInputsToServer(KeyboardInput keyboardInput, MouseInput mouseInput) {

		boolean[] inputs = new boolean[MOVEMENT_INPUT_SIZE];


	}

	// unreliably send rotation to server
	private void sendRotationToServer(Vector3f rotation) {



	}



	@Override
	protected void handleCrouchingAndSprinting() {



		/*
		KeyboardInput keyboardInput = KeyboardInput.get();

		moveSpeed = keyboardInput.isKeyPressed(GLFW_KEY_LEFT_CONTROL) ? SPRINT_SPEED : WALK_SPEED;

		if (keyboardInput.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
			moveSpeed = CROUCH_SPEED;
			crouching = true;
		} else {
			crouching = false;
		}
		*/


	}


	@Override
	public void fixedUpdate() {
		super.fixedUpdate();

		KeyboardInput keyboardInput = KeyboardInput.get();
		MouseInput mouseInput = MouseInput.get();

		sendInputsToServer(keyboardInput, mouseInput);

		Renderer.get().getCamera().getTransform().getRotation().set(getRotation());
		Renderer.get().getCamera().getTransform().getPosition().set(getCameraPosition());

		float rotateSpeed = 0.0025f;

		if (keyboardInput.isKeyJustPressed(GLFW_KEY_ESCAPE)) {
			lockMouseToCenterForCameraRotation = !lockMouseToCenterForCameraRotation;
			updateCursorVisibility();
		}

		if (lockMouseToCenterForCameraRotation) {
			float verticalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().y;
			float horizontalRotateAmount = rotateSpeed * mouseInput.getMouseMotion().x;

			mouseInput.setMousePosition(BackyardRocketry.getInstance().getWindow().getWidth() / 2, BackyardRocketry.getInstance().getWindow().getHeight() / 2, false);

			getTransform().rotateX(verticalRotateAmount);
			getTransform().rotateY(horizontalRotateAmount);

			// clamp vertical rotation
			getTransform().getRotation().x = Math.max(getTransform().getRotation().x, (float) -Math.PI * 0.5f);
			getTransform().getRotation().x = Math.min(getTransform().getRotation().x, (float) Math.PI * 0.5f);

			sendRotationToServer(getTransform().getRotation());
		}

		cameraInterpolationFactor = 0f;



	}


	private void interpolateCameraTransform(double deltaTime) {

		cameraInterpolationFactor += (float) deltaTime / CAMERA_INTERPOLATION_DURATION;
		cameraInterpolationFactor = Math.min(cameraInterpolationFactor, 1f);

		Camera camera = Renderer.get().getCamera();

		// reset interpolation to t = 0 to begin interpolation
		INTERPOLATED_CAMERA_ROTATION.set(getPreviousTransform().getRotation());
		INTERPOLATED_CAMERA_POSITION.set(getCameraPosition(getPreviousTransform().getPosition()));

		// interpolate camera rotation and position
		INTERPOLATED_CAMERA_ROTATION.set(
				Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.x, getTransform().getRotation().x, cameraInterpolationFactor),
				Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.y, getTransform().getRotation().y, cameraInterpolationFactor),
				Transform.lerpAngle(INTERPOLATED_CAMERA_ROTATION.z, getTransform().getRotation().z, cameraInterpolationFactor)
		);
		INTERPOLATED_CAMERA_POSITION.lerp(getCameraPosition(), cameraInterpolationFactor);

		// set camera rotation and position to interpolated values
		camera.getTransform().getRotation().set(INTERPOLATED_CAMERA_ROTATION);
		camera.getTransform().getPosition().set(INTERPOLATED_CAMERA_POSITION);

		float desiredBobValue = (isOnGround() && isMoving())
				? (float) Math.sin(Updater.getCurrentTime() * 70f * moveSpeed) * 0.1f
				: 0f;
		float viewBobValue = (float) Math.lerp(camera.getViewBobValue(), desiredBobValue, deltaTime * 40f * moveSpeed);

		camera.setViewBobValue(viewBobValue);

	}


	@Override
	public void setCurrentHotbarSlot(int index) {

		super.setCurrentHotbarSlot(index);

		FIRST_PERSON_HAND_ITEM.setBlock(getHotbarSlotContents(index));

	}


	@Override
	public void update(double deltaTime) {

		super.update(deltaTime);

		interpolateCameraTransform(deltaTime);

		Camera camera = Renderer.get().getCamera();
		AudioManager.updateListenerPosition(new Vector3f(camera.getTransform().getPosition()), camera);


	}

	@Override
	public void unregisteredUpdateListener() {

		Updater.get().unregisterUpdateListener(GUI_ELEMENT);

	}





	/*
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

	}*/



	/*


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
				.rotateX(-getTransform().getRotation().x)
				.rotateY(-getTransform().getRotation().y);

		BlockRaycastResult previousTargetBlock = targetBlock;
		targetBlock = Collision.blockRaycast(getWorld(), rayFrom, rayDirection, REACH_DISTANCE);

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

			} else {

				breakAudioDelay = 0;

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
			targetBlockChunk.breakBlock(
					targetBlockChunk.toLocalX(targetBlock.getBlockX()),
					targetBlockChunk.toLocalY(targetBlock.getBlockY()),
					targetBlockChunk.toLocalZ(targetBlock.getBlockZ()),
					true
			);

			BlockAudio blockAudio = Block.getBlockAudio(block);
			if (blockAudio != null) {
				AudioPlayer digAudioPlayer = AudioManager.get().playAudioSpatial(blockAudio.getBreakAudio(), false, false, true)
						.setPitch(0.7f + (float) Math.random() * 0.6f)
						.setPosition(targetBlock.getBlockX() + 0.5f, targetBlock.getBlockY() + 0.5f, targetBlock.getBlockZ() + 0.5f);
			}

			breakAudioDelay = 5;

			breakProgress = 0;

			if (shouldInstantlyBreakBlocks()) {
				blockInteractionTimer = 5;
			} else if (Block.getBlockHealth(block) > 1) {
				blockInteractionTimer = 2;
			}
		} else {

			if (breakAudioDelay <= 0) {

				BlockAudio blockAudio = Block.getBlockAudio(block);
				AudioPlayer audioPlayer = AudioManager.get().playAudioSpatial(blockAudio.getBreakAudio(), false, false, true)
						.setPitch(0.7f + (float) Math.random() * 0.6f)
						.setGain(0.25f)
						.setPosition(targetBlock.getBlockX() + 0.5f, targetBlock.getBlockY() + 0.5f, targetBlock.getBlockZ() + 0.5f);
				breakAudioDelay = 5;

			} else {

				breakAudioDelay -= 1;

			}


		}



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

		BlockAudio blockAudio = Block.getBlockAudio(blockToPlace);
		AudioManager.get().playAudioSpatial(blockAudio.getPlaceAudio(), false, false, true)
				.setPitch(0.8f + (float) Math.random() * 0.4f)
				.setPosition(placePosX + 0.5f, placePosY + 0.5f, placePosZ + 0.5f);

		chunk.setBlock(
				chunk.toLocalX(placePosX),
				chunk.toLocalY(placePosY),
				chunk.toLocalZ(placePosZ),
				blockToPlace,
				true,
				true
		);

		FIRST_PERSON_HAND_ITEM.playSwingAnimation(false);
	}












	 */




}
