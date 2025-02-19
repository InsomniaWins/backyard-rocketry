package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3f;

public class Ray {

	private Vector3f origin;
	private Vector3f direction;

	public Ray(Vector3f origin, Vector3f direction) {

		setOrigin(origin);
		setDirection(direction);

	}

	public void setOrigin(Vector3f origin) {
		this.origin = origin;
	}

	public void setDirection(Vector3f direction) {
		this.direction = direction;
	}

	public Vector3f getOrigin() {
		return new Vector3f(origin);
	}

	public Vector3f getDirection() {
		return new Vector3f(direction);
	}

}
