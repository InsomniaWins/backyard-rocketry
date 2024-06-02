package wins.insomnia.backyardrocketry.render;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class TextRenderer {

	public static int getTextPixelWidth(String text) {
		return text.length() * 7;
	}

	public static void drawText(String text, int guiX, int guiY) {
		drawText(text, guiX, guiY, Renderer.get().getGuiScale(), TextureManager.get().getFontTexture());
	}

	public static void drawText(String text, int guiX, int guiY, Texture fontTexture) {
		drawText(text, guiX, guiY, Renderer.get().getGuiScale(), fontTexture);
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
		Renderer.get().getGuiShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getModelMatrix().ortho(
				0f, // left
				BackyardRocketry.getInstance().getWindow().getWidth(), // right
				0f, // bottom
				BackyardRocketry.getInstance().getWindow().getHeight(), // top
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


		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);


		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

		if (scale != Renderer.get().getGuiScale()) {
			Renderer.get().getGuiShaderProgram().setUniform("vs_scale", Renderer.get().getGuiScale());
		}

		Renderer.get().getShaderProgram().use();
	}

}
