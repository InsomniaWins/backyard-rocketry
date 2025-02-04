package wins.insomnia.backyardrocketry.audio;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.Camera;
import wins.insomnia.backyardrocketry.util.loading.LoadTask;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

public class AudioManager {


	private long device;
	private long context;
	private AudioListener listener;
	private final List<AudioPlayer> AUDIO_PLAYER_LIST;
	private final Matrix4f cameraMatrix;
	private ALCCapabilities deviceCapabilities;
	private static final HashMap<String, String> DEFAULT_AUDIO_REGISTRATION_MAP = new HashMap<>() {{
		put("test_sound", "test_sound.ogg");
	}};

	private final HashMap<String, AudioBuffer> AUDIO_BUFFER_MAP;

	public AudioManager() {
		AUDIO_BUFFER_MAP = new HashMap<>();
		AUDIO_PLAYER_LIST = new ArrayList<>();
		cameraMatrix = new Matrix4f();
	}

	public void init() throws Exception {
		device = alcOpenDevice((ByteBuffer) null);

		if (device == NULL) {
			throw new IllegalStateException("Failed to open the default OpenAL device.");
		}

		deviceCapabilities = ALC.createCapabilities(device);
		this.context = alcCreateContext(device, (IntBuffer) null);
		if (context == NULL) {
			throw new IllegalStateException("Failed to create OpenAL context.");
		}
		alcMakeContextCurrent(context);
		AL.createCapabilities(deviceCapabilities);
	}


	public static List<LoadTask> makeAudioBufferLoadingTaskList() {
		ArrayList<LoadTask> tasks = new ArrayList<>();

		for (String audioName : DEFAULT_AUDIO_REGISTRATION_MAP.keySet()) {

			tasks.add(new LoadTask(
					"Registering Audio: " + audioName,
					() -> AudioManager.get().registerAudio(audioName, DEFAULT_AUDIO_REGISTRATION_MAP.get(audioName))
			));

		}

		return tasks;
	}

	public AudioBuffer registerAudio(String bufferName, String audioFileName) {

		AudioBuffer audioBuffer = null;

		try {

			audioBuffer = new AudioBuffer(audioFileName);
			AUDIO_BUFFER_MAP.put(bufferName, audioBuffer);

		} catch (Exception e) {

			System.err.println("Failed to register audio: " + audioFileName);

		}

		return audioBuffer;
	}

	public void unregisterAudio(String bufferName) {

		AudioBuffer audioBuffer = AUDIO_BUFFER_MAP.get(bufferName);

		if (audioBuffer != null) {
			if (!audioBuffer.isClean()) {
				audioBuffer.clean();
			}
		}

		AUDIO_BUFFER_MAP.remove(bufferName);

	}

	public AudioBuffer getAudioBuffer(String bufferName) {
		return AUDIO_BUFFER_MAP.get(bufferName);
	}

	public AudioPlayer playAudio(AudioBuffer audioBuffer, boolean looping, boolean relative) {
		return playAudio(audioBuffer, looping, relative, true);
	}

	// the param 'shouldFree' determines whether the AudioPlayer should free itself from memory when the audio is finished playing
	public AudioPlayer playAudio(AudioBuffer audioBuffer, boolean looping, boolean relative, boolean shouldFree) {
		AudioPlayer player = new AudioPlayer(looping, relative);
		player.setBuffer(audioBuffer);
		AUDIO_PLAYER_LIST.add(player);

		player.setCleanWhenFinished(shouldFree);

		player.play();

		return player;
	}


	public AudioListener getListener() {
		return listener;
	}

	public void clean() {

		for (AudioPlayer player : AUDIO_PLAYER_LIST) {

			player.clean();

		}

		AUDIO_PLAYER_LIST.clear();

		for (AudioBuffer buffer : AUDIO_BUFFER_MAP.values()) {

			buffer.clean();

		}

		AUDIO_BUFFER_MAP.clear();



		alcMakeContextCurrent(NULL);
		AL.setCurrentProcess(null);
		memFree(deviceCapabilities.getAddressBuffer());
		alcDestroyContext(context);
		alcCloseDevice(device);

	}

	public static AudioManager get() {
		return BackyardRocketry.getInstance().getAudioManager();
	}

	public void updateListenerPosition(Vector3f position, Camera camera) {
		Matrix4f viewMatrix = camera.getViewMatrix();
		listener.setPosition(position.x, position.y, position.z);
		Vector3f at = new Vector3f();
		viewMatrix.positiveZ(at).negate();
		Vector3f up = new Vector3f();
		viewMatrix.positiveY(up);
		listener.setOrientation(at, up);
	}
}
