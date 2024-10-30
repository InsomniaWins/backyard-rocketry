package wins.insomnia.backyardrocketry.render;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Color {
	private boolean constant = false;
	private float r;
	private float g;
	private float b;
	private float a;


	public static Color WHITE = new Color(1f, 1f, 1f).setConstant();
	public static Color TRANSPARENT = new Color(1f, 1f, 1f, 0f).setConstant();
	public static Color RED = new Color(1f, 0f, 0f).setConstant();
	public static Color GREEN = new Color(0f, 1f, 0f).setConstant();
	public static Color BLUE = new Color(0f, 0f, 1f).setConstant();
	public static Color BLACK = new Color(0f, 0f, 0f).setConstant();

	public Color(Color fromColor) {
		constant = false;
		setRGBA(fromColor.getRGBA());
	}

	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}

	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public Vector3f getRGB() {
		return new Vector3f(r,g,b);
	}

	public Vector4f getRGBA() {
		return new Vector4f(r,g,b,a);
	}

	public Color setR(float value) {

		if (constant) return this;

		r = value;
		return this;
	}

	public Color setG(float value) {

		if (constant) return this;

		g = value;
		return this;
	}

	public Color setB(float value) {

		if (constant) return this;

		b = value;
		return this;
	}

	public Color setA(float value) {

		if (constant) return this;

		a = value;
		return this;
	}

	public Color setRGB(Vector3f value) {

		if (constant) return this;

		r = value.x;
		g = value.y;
		b = value.z;
		return this;
	}

	public Color setRGBA(Vector4f value) {

		if (constant) return this;

		r = value.x;
		g = value.y;
		b = value.z;
		a = value.w;
		return this;
	}

	private Color setConstant() {
		constant = true;
		return this;
	}
}
