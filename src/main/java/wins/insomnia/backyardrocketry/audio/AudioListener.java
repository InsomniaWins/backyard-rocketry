package wins.insomnia.backyardrocketry.audio;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class AudioListener {

	public AudioListener() {
		this(0f, 0f, 0f);
	}

	public AudioListener(float x, float y, float z) {

		alListener3f(AL_POSITION, x, y, z);
		alListener3f(AL_VELOCITY, 0f, 0f, 0f);

	}


	public void setSpeed(float speedX, float speedY, float speedZ) {
		alListener3f(AL_VELOCITY, speedX, speedY, speedZ);
	}

	public void setPosition(float x, float y, float z) {
		alListener3f(AL_POSITION, x, y, z);
	}

	public void setOrientation(Vector3f facing, Vector3f up) {
		float[] data = new float[6];
		data[0] = facing.x;
		data[1] = facing.y;
		data[2] = facing.z;
		data[3] = up.x;
		data[4] = up.y;
		data[5] = up.z;
		alListenerfv(AL_ORIENTATION, data);
	}

}
