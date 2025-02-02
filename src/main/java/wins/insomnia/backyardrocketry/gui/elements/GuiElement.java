package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.Vector2f;
import org.joml.primitives.Rectangled;
import wins.insomnia.backyardrocketry.render.IRenderable;
import wins.insomnia.backyardrocketry.util.update.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.update.IUpdateListener;

public class GuiElement implements IFixedUpdateListener, IUpdateListener, IRenderable {

	private final Vector2f POSITION = new Vector2f();
	private Vector2f SIZE = new Vector2f();

	public boolean containsPoint(double x, double y) {

		if (x < POSITION.x || x > POSITION.x + SIZE.x) return false;

		if (y < POSITION.y || y > POSITION.y + SIZE.y) return false;

		return true;
	}

	public void setXPosition(float x) {
		POSITION.x = x;
	}

	public void setYPosition(float y) {
		POSITION.y = y;
	}

	public void setPosition(float x, float y) {
		setXPosition(x);
		setYPosition(y);
	}

	public void setWidth(float width) {
		SIZE.x = width;
	}

	public void setHeight(float height) {
		SIZE.y = height;
	}

	public float getWidth() {
		return SIZE.x;
	}

	public float getHeight() {
		return SIZE.y;
	}

	public void setSize(float width, float height) {
		setWidth(width);
		setHeight(height);
	}

	public float getXPosition() {
		return POSITION.x;
	}

	public float getYPosition() {
		return POSITION.y;
	}

	public Vector2f getPosition() {
		return new Vector2f(POSITION);
	}

	public Vector2f getSize() {
		return new Vector2f(SIZE);
	}


	@Override
	public void render() {

	}

	@Override
	public boolean shouldRender() {
		return true;
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
		return true;
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
}
