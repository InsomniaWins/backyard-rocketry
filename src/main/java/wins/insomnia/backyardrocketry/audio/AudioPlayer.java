package wins.insomnia.backyardrocketry.audio;

import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;
import wins.insomnia.backyardrocketry.util.update.Updater;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SEC_OFFSET;

public class AudioPlayer implements IUpdateListener  {

	private final int SOURCE_ID;
	private boolean shouldCleanWhenFinished = false;
	private double startPlayingTime = 0.0;
	private AudioBuffer audioBuffer;

	public AudioPlayer(boolean looping, boolean relative) {

		SOURCE_ID = alGenSources();

		setLooping(looping);
		setRelative(relative);

		Updater.get().registerUpdateListener(this);
	}

	public AudioPlayer(boolean looping, boolean relative, boolean isSpatial) {

		this(looping, relative);

		if (isSpatial) {
			alSourcef(SOURCE_ID, AL_ROLLOFF_FACTOR, 0.5f);
		} else {
			alSourcef(SOURCE_ID, AL_ROLLOFF_FACTOR, 1f);
		}

	}

	public AudioPlayer setCleanWhenFinished(boolean shouldClean) {
		shouldCleanWhenFinished = shouldClean;
		return this;
	}

	public AudioPlayer setLooping(boolean looping) {
		alSourcei(SOURCE_ID, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
		return this;
	}

	public AudioPlayer setRelative(boolean relative) {
		alSourcei(SOURCE_ID, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
		return this;
	}

	public AudioPlayer setBuffer(AudioBuffer buffer) {
		stop();
		audioBuffer = buffer;
		alSourcei(SOURCE_ID, AL_BUFFER, audioBuffer.getBufferId());
		return this;
	}

	public AudioBuffer getAudioBuffer() {
		return audioBuffer;
	}

	public AudioPlayer setPosition(float x, float y, float z) {
		alSource3f(SOURCE_ID, AL_POSITION, x, y, z);
		return this;
	}

	public AudioPlayer setPosition(Vector3f position) {
		return setPosition(position.x, position.y, position.z);
	}

	public AudioPlayer setSpeed(Vector3f speed) {
		alSource3f(SOURCE_ID, AL_VELOCITY, speed.x, speed.y, speed.z);
		return this;
	}

	public AudioPlayer setGain(float gain) {
		alSourcef(SOURCE_ID, AL_GAIN, gain);
		return this;
	}

	public float getGain() {
		return alGetSourcef(SOURCE_ID, AL_GAIN);
	}

	public AudioPlayer setPitch(float pitch) {
		alSourcef(SOURCE_ID, AL_PITCH, pitch);
		return this;
	}

	public AudioPlayer setProperty(int param, float value) {
		alSourcef(SOURCE_ID, param, value);
		return this;
	}

	public AudioPlayer play() {
		alSourcePlay(SOURCE_ID);
		startPlayingTime = Updater.getCurrentTime();
		return this;
	}

	public boolean isPlaying() {
		return alGetSourcei(SOURCE_ID, AL_SOURCE_STATE) == AL_PLAYING;
	}

	public AudioPlayer pause() {
		alSourcePause(SOURCE_ID);
		return this;
	}

	public float getPlaybackPosition() {

		return alGetSourcef(SOURCE_ID, AL_SEC_OFFSET);

	}


	public void stop() {
		alSourceStop(SOURCE_ID);
		startPlayingTime = Updater.getCurrentTime();
	}

	public void clean() {
		stop();
		alDeleteSources(SOURCE_ID);
		Updater.get().unregisterUpdateListener(this);
	}


	@Override
	public void update(double deltaTime) {

		if (shouldCleanWhenFinished) {

			if (Updater.getCurrentTime() - startPlayingTime > getAudioBuffer().getDuration()) {
				clean();
			}

		}


	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}
}
