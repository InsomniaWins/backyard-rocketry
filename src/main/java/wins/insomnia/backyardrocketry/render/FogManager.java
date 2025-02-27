package wins.insomnia.backyardrocketry.render;

import org.joml.Math;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;

public class FogManager implements IUpdateListener {

	private float fogEnd = 1f;
	private float fogStart = 0f;
	public float desiredFogEnd = 1f;
	public float desiredFogStart = 0f;

	public float fogTransitionSpeed = 10.1f;

	public void setDesiredFogEnd(float end) {
		desiredFogEnd = end;
	}
	public void setDesiredFogStart(float start) {
		desiredFogStart = start;
	}
	public float getDesiredFogStart() {
		return desiredFogStart;
	}
	public float getDesiredFogEnd() {
		return desiredFogEnd;
	}
	public float getFogEnd() {
		return fogEnd;
	}
	public float getFogStart() {
		return fogStart;
	}

	public float getFogTransitionSpeed() {
		return fogTransitionSpeed;
	}

	public void setFogTransitionSpeed(float transitionSpeed) {
		fogTransitionSpeed = transitionSpeed;
	}

	@Override
	public void update(double deltaTime) {

		fogStart = Math.lerp(fogStart, desiredFogStart, fogTransitionSpeed * (float) deltaTime);
		fogEnd = Math.lerp(fogEnd, desiredFogEnd, fogTransitionSpeed * (float) deltaTime);

	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}
}
