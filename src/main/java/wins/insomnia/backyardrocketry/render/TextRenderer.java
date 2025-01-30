package wins.insomnia.backyardrocketry.render;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class TextRenderer {

	private static Color fontColor = Color.WHITE;

	public static int getTextPixelWidth(String text) {
		return text.length() * 7;
	}

	public static int getTextPixelHeight(int lineAmount) {
		return lineAmount * 12;
	}

	public static void drawText(String text, int guiX, int guiY) {
		drawText(text, guiX, guiY, Renderer.get().getGuiScale(), TextureManager.getTexture("font"));
	}

	public static void drawText(String text, int guiX, int guiY, Texture fontTexture) {
		drawText(text, guiX, guiY, Renderer.get().getGuiScale(), fontTexture);
	}

	public static void drawTextShadow(String text, int guiX, int guiY, int scale, Texture fontTexture) {
		Color previousColor = fontColor;
		fontColor = new Color(Color.BLACK);
		drawText(text, guiX + 1, guiY + 1, scale, fontTexture);
		fontColor = previousColor;
		drawText(text, guiX, guiY, scale, fontTexture);
	}

	public static void drawTextOutline(String text, int guiX, int guiY, int scale, Texture fontTexture) {
		Color previousColor = fontColor;
		fontColor = new Color(Color.BLACK);
		drawText(text, guiX + 1, guiY, scale, fontTexture);
		drawText(text, guiX + 1, guiY + 1, scale, fontTexture);
		drawText(text, guiX, guiY + 1, scale, fontTexture);
		drawText(text, guiX - 1, guiY + 1, scale, fontTexture);
		drawText(text, guiX - 1, guiY, scale, fontTexture);
		drawText(text, guiX - 1, guiY - 1, scale, fontTexture);
		drawText(text, guiX, guiY - 1, scale, fontTexture);
		drawText(text, guiX + 1, guiY - 1, scale, fontTexture);
		fontColor = previousColor;
		drawText(text, guiX, guiY, scale, fontTexture);
	}

	public static void drawText(String text, int guiX, int guiY, int scale, Texture fontTexture) {


		int[] previousTexture = new int[1];
		glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

		Renderer.get().getGuiShaderProgram().use();

		if (scale != Renderer.get().getGuiScale()) {
			Renderer.get().getGuiShaderProgram().setUniform("vs_scale", scale);
		}

		glBindTexture(GL_TEXTURE_2D, fontTexture.getTextureHandle());
		glActiveTexture(GL_TEXTURE0);

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		Renderer.get().getModelMatrix().identity();

		Renderer.get().getGuiShaderProgram().setUniform("vs_textureSizeX", 1);
		Renderer.get().getGuiShaderProgram().setUniform("vs_textureSizeY", 1);
		Renderer.get().getGuiShaderProgram().setUniform("vs_posX", guiX);
		Renderer.get().getGuiShaderProgram().setUniform("vs_posY", guiY);
		Renderer.get().getGuiShaderProgram().setUniform("fs_texture", GL_TEXTURE0);
		Renderer.get().getGuiShaderProgram().setUniform("fs_colorModulation", fontColor.getRGB());
		Renderer.get().getGuiShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getModelMatrix().ortho(
				0f, // left
				Window.get().getResolutionFrameBuffer().getWidth(), // right
				0f, // bottom
				Window.get().getResolutionFrameBuffer().getHeight(), // top
				0.01f, // z-near
				1f // z-far
		));



		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


		// TODO: Add back-face culling to text rendering!!!

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);


		// generate and draw font mesh
		Renderer.get().getFontMesh().setText(text);
		glBindVertexArray(Renderer.get().getFontMesh().getVao());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, Renderer.get().getFontMesh().getIndexCount(), GL_UNSIGNED_INT, 0);

		Renderer.get().getGuiShaderProgram().setUniform("fs_colorModulation", Color.WHITE.getRGB());

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);


		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

		if (scale != Renderer.get().getGuiScale()) {
			Renderer.get().getGuiShaderProgram().setUniform("vs_scale", Renderer.get().getGuiScale());
		}

		Renderer.get().getShaderProgram().use();
	}

	public static void setFontColor(Color color) {
		fontColor = color;
	}

	public static Color getFontColor() {
		return fontColor;
	}

}
