package wins.insomnia.backyardrocketry.scenes;

import wins.insomnia.backyardrocketry.render.IRenderable;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;

public class Scene implements IRenderable, IUpdateListener, IFixedUpdateListener {
	@Override
	public void render() {

	}

	@Override
	public boolean shouldRender() {
		return false;
	}

	@Override
	public boolean isClean() {
		return false;
	}

	@Override
	public void clean() {

	}

	@Override
	public int getRenderPriority() {
		return 0;
	}

	@Override
	public boolean hasTransparency() {
		return false;
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

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void registeredUpdateListener() {

	}

	@Override
	public void unregisteredUpdateListener() {

	}

	public void sceneRegistered() {

	}

	public void sceneUnregistered() {

	}
}
