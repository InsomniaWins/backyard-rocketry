package wins.insomnia.backyardrocketry.util;

import org.joml.Math;

public class HelpfulMath {

	public static void rotatePoint(float[] point, float pivotX, float pivotY, float radians) {
		float outX = Math.cos(radians) * (point[0] - pivotX) - Math.sin(radians) * (point[1] - pivotY) + pivotX;
		float outY = Math.sin(radians) * (point[0] - pivotX) + Math.cos(radians) * (point[1] - pivotY) + pivotY;

		point[0] = outX;
		point[1] = outY;
	}


}
