package wins.insomnia.backyardrocketry.util;

import org.lwjgl.opengl.GL30;

import java.util.ArrayList;

public class OpenGLWrapper {

	public static ArrayList<Integer> VAO_LIST = new ArrayList<>();
	private static boolean trackingVaos = false;

	public static void setTrackingVaos(boolean value) {
		VAO_LIST.clear();
		trackingVaos = value;
	}

	public static void trackVaos() {
		VAO_LIST.clear();
		trackingVaos = true;
	}

	public static boolean isTrackingVaos() {
		return trackingVaos;
	}

	public static int glGenVertexArrays() {
		int vao = GL30.glGenVertexArrays();
		if (trackingVaos) {
			VAO_LIST.add(vao);
		}
		return vao;
	}

	public static int getVaoCount() {
		return VAO_LIST.size();
	}

	public static void glDeleteVertexArrays(int vao) {
		GL30.glDeleteVertexArrays(vao);
		if (trackingVaos) {
			VAO_LIST.remove(Integer.valueOf(vao));
		}
	}



}
