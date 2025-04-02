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

	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public Color(float r, float g, float b) {
		this(r,g,b,1f);
	}

	public Color(int r, int g, int b, int a) {
		this(r/255f, g/255f, b/255f, a/255f);
	}

	public Color(int r, int g, int b) {
		this(r, g, b, 255);
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

	public Color setRgb(float r, float g, float b) {

		if (constant) return this;

		this.r = r;
		this.g = g;
		this.b = b;

		return this;
	}

	public Color setRgb(Vector3f value) {
		return setRgb(value.x, value.y, value.z);
	}

	public Color setRGBA(Vector4f value) {

		if (constant) return this;

		r = value.x;
		g = value.y;
		b = value.z;
		a = value.w;
		return this;
	}



	public void setHsv(float hue, float saturation, float value) {

		int red = 0, green = 0, blue = 0;
		if (saturation == 0) {
			red = green = blue = (int) (value * 255.0f + 0.5f);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0f;
			float f = h - (float)java.lang.Math.floor(h);
			float p = value * (1.0f - saturation);
			float q = value * (1.0f - saturation * f);
			float t = value * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
				case 0:
					red = (int) (value * 255.0f + 0.5f);
					green = (int) (t * 255.0f + 0.5f);
					blue = (int) (p * 255.0f + 0.5f);
					break;
				case 1:
					red = (int) (q * 255.0f + 0.5f);
					green = (int) (value * 255.0f + 0.5f);
					blue = (int) (p * 255.0f + 0.5f);
					break;
				case 2:
					red = (int) (p * 255.0f + 0.5f);
					green = (int) (value * 255.0f + 0.5f);
					blue = (int) (t * 255.0f + 0.5f);
					break;
				case 3:
					red = (int) (p * 255.0f + 0.5f);
					green = (int) (q * 255.0f + 0.5f);
					blue = (int) (value * 255.0f + 0.5f);
					break;
				case 4:
					red = (int) (t * 255.0f + 0.5f);
					green = (int) (p * 255.0f + 0.5f);
					blue = (int) (value * 255.0f + 0.5f);
					break;
				case 5:
					red = (int) (value * 255.0f + 0.5f);
					green = (int) (p * 255.0f + 0.5f);
					blue = (int) (q * 255.0f + 0.5f);
					break;
			}
		}

		r = red / 255f;
		g = green / 255f;
		b = blue / 255f;

	}




	public float[] getHsv() {

		float hue, saturation, brightness;

		float cmax = Math.max(r, g);
		if (b > cmax) cmax = b;
		float cmin = Math.min(r, g);
		if (b < cmin) cmin = b;

		brightness = cmax;

		if (cmax != 0)
			saturation = (cmax - cmin) / cmax;
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = (cmax - r) / (cmax - cmin);
			float greenc = (cmax - g) / (cmax - cmin);
			float bluec = (cmax - b) / (cmax - cmin);

			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0f + redc - bluec;
			else
				hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}


		return new float[] {
			hue, saturation, brightness
		};
	}




	private Color setConstant() {
		constant = true;
		return this;
	}

	public byte getRByte() {
		return (byte) ((int) (r * 255f));
	}

	public byte getGByte() {
		return (byte) ((int) (g * 255f));
	}

	public byte getBByte() {
		return (byte) ((int) (b * 255f));
	}
}
