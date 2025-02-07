package wins.insomnia.backyardrocketry.world.block;

import wins.insomnia.backyardrocketry.audio.AudioBuffer;
import wins.insomnia.backyardrocketry.audio.AudioManager;

import java.util.HashMap;

public class BlockAudio {


	public static final BlockAudio GENERIC_STONE = new BlockAudio("stone_step", "stone_place", "stone_break");
	public static final BlockAudio GENERIC_DIRT = new BlockAudio("dirt_step", "dirt_place", "dirt_break");
	public static final BlockAudio GENERIC_WOOD = new BlockAudio("wood_step", "wood_place", "wood_break");
	public static final BlockAudio GENERIC_GLASS = new BlockAudio("glass_step", "glass_place", "glass_break");
	public static final BlockAudio GENERIC_LEAVES = new BlockAudio("leaves_step", "leaves_place", "leaves_break");


	public enum BlockActionType {

		STEP,
		PLACE,
		BREAK

	}

	private final HashMap<BlockActionType, String> AUDIO_NAMES_MAP;

	public BlockAudio(String stepAudioName, String placeAudioName, String breakAudioName) {
		AUDIO_NAMES_MAP = new HashMap<>();
		setActionAudio(BlockActionType.STEP, stepAudioName);
		setActionAudio(BlockActionType.PLACE, placeAudioName);
		setActionAudio(BlockActionType.BREAK, breakAudioName);
	}

	public void setActionAudio(BlockActionType actionType, String audioName) {
		AUDIO_NAMES_MAP.put(actionType, audioName);
	}

	public AudioBuffer getStepAudio() {
		return getActionAudio(BlockActionType.STEP);
	}

	public AudioBuffer getPlaceAudio() {
		return getActionAudio(BlockActionType.PLACE);
	}

	public AudioBuffer getBreakAudio() {
		return getActionAudio(BlockActionType.BREAK);
	}


	public AudioBuffer getActionAudio(BlockActionType actionType) {

		String audioName = AUDIO_NAMES_MAP.get(actionType);

		if (audioName == null || audioName.isEmpty()) return null;

		AudioBuffer audioBuffer = AudioManager.get().getAudioBuffer(audioName);
		return audioBuffer;

	}


}
