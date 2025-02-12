package wins.insomnia.backyardrocketry.render.texture;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.ShaderProgram;
import wins.insomnia.backyardrocketry.render.Window;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class TextureRenderer {

	public static void drawGuiTextureNineSlice(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int sliceSize) {
		drawGuiTextureNineSlice(texture, guiX, guiY, guiWidth, guiHeight, sliceSize, false);
	}

	public static void drawGuiTextureTiled(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int textureX, int textureY, int textureWidth, int textureHeight) {

		int tileAmountX = (int) (guiWidth / (float) textureWidth);
		int tileAmountY = (int) (guiHeight / (float) textureHeight);

		int tileRemainderX = guiWidth % textureWidth;
		int tileRemainderY = guiHeight % textureHeight;

		for (int x = 0; x < tileAmountX; x++) {
			for (int y = 0; y < tileAmountY; y++) {

				drawGuiTextureClipped(
						texture,
						guiX + x * textureWidth,
						guiY + y * textureHeight,
						textureWidth,
						textureHeight,
						textureX,
						textureY,
						textureWidth,
						textureHeight
				);

				if (x == tileAmountX - 1 && tileRemainderX > 0) {

					drawGuiTextureClipped(
							texture,
							guiX + x * textureWidth + textureWidth,
							guiY + y * textureHeight,
							tileRemainderX,
							textureHeight,
							textureX,
							textureY,
							tileRemainderX,
							textureHeight
					);

				}

				if (y == tileAmountY - 1 && tileRemainderY > 0) {

					drawGuiTextureClipped(
							texture,
							guiX + x * textureWidth,
							guiY + y * textureHeight + textureHeight,
							textureWidth,
							tileRemainderY,
							textureX,
							textureY,
							textureWidth,
							tileRemainderY
					);

				}

				if (y == tileAmountY - 1 && x == tileAmountX - 1 && tileRemainderX > 0 && tileRemainderY > 0) {

					drawGuiTextureClipped(
							texture,
							guiX + x * textureWidth + textureWidth,
							guiY + y * textureHeight + textureHeight,
							tileRemainderX,
							tileRemainderY,
							textureX,
							textureY,
							tileRemainderX,
							tileRemainderY
					);

				}

			}
		}

	}

	public static void drawGuiTextureTiled(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight) {
		drawGuiTextureTiled(texture, guiX, guiY, guiWidth, guiHeight, 0, 0, texture.getWidth(), texture.getHeight());
	}

	public static void drawGuiTextureNineSlice(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int sliceSize, boolean drawCenter) {

		Renderer renderer = Renderer.get();

		// center
		if (drawCenter) {

			drawGuiTextureTiled(
					texture,
					guiX + sliceSize,
					guiY + sliceSize,
					guiWidth - 2 * sliceSize,
					guiHeight - 2 * sliceSize,
					sliceSize,
					sliceSize,
					sliceSize,
					sliceSize
			);

		}


		// vertical sides
		int verticalTileAmount = guiHeight / sliceSize - 2;
		int verticalTileRemainder = guiHeight % sliceSize;
		for (int i = 0; i < verticalTileAmount; i++) {

			// left
			drawGuiTextureClipped(texture, guiX, guiY + i * sliceSize + sliceSize, sliceSize, sliceSize, 0, sliceSize, sliceSize, sliceSize);

			// right
			drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + i * sliceSize + sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize, sliceSize, sliceSize);


			if (verticalTileRemainder > 0 && i == guiHeight / sliceSize - 3) {

				// left
				drawGuiTextureClipped(texture, guiX, guiY + i * sliceSize + sliceSize + sliceSize, sliceSize, verticalTileRemainder, 0, sliceSize, sliceSize, verticalTileRemainder);

				// right
				drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + i * sliceSize + sliceSize + sliceSize, sliceSize, verticalTileRemainder, sliceSize * 2, sliceSize, sliceSize, verticalTileRemainder);

			}

		}


		// horizontal sides
		int horizontalTileAmount = guiWidth / sliceSize - 2;
		int horizontalTileRemainder = guiWidth % sliceSize;
		for (int i = 0; i < horizontalTileAmount; i++) {
			// top
			drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize, guiY, sliceSize, sliceSize, sliceSize, 0, sliceSize, sliceSize);

			// bottom
			drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize, guiY + guiHeight - sliceSize, sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize, sliceSize);

			if (horizontalTileRemainder > 0 && i == guiWidth / sliceSize - 3) {

				// top
				drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize + sliceSize, guiY, horizontalTileRemainder, sliceSize, sliceSize, 0, horizontalTileRemainder, sliceSize);

				// bottom
				drawGuiTextureClipped(texture, guiX + i * sliceSize + sliceSize + sliceSize, guiY + guiHeight - sliceSize, horizontalTileRemainder, sliceSize, sliceSize, sliceSize * 2, horizontalTileRemainder, sliceSize);

			}

		}


		// top left
		drawGuiTextureClipped(texture, guiX, guiY, sliceSize, sliceSize, 0, 0, sliceSize, sliceSize);

		// top right
		drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY, sliceSize, sliceSize, sliceSize * 2, 0, sliceSize, sliceSize);

		// bottom left
		drawGuiTextureClipped(texture, guiX, guiY + guiHeight - sliceSize, sliceSize, sliceSize, 0, sliceSize * 2, sliceSize, sliceSize);

		// bottom right
		drawGuiTextureClipped(texture, guiX + guiWidth - sliceSize, guiY + guiHeight - sliceSize, sliceSize, sliceSize, sliceSize * 2, sliceSize * 2, sliceSize, sliceSize);
	}


	public static void drawGuiTextureClipped(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight, int textureX, int textureY, int textureWidth, int textureHeight) {

		Renderer renderer = Renderer.get();

		// get clipped texture Uv's
		float[] texturePixelScale = {
				1.0f / texture.getWidth(),
				1.0f / texture.getHeight()
		};

		float[] textureCoordinates = new float[4];
		textureCoordinates[0] = textureX * texturePixelScale[0];
		textureCoordinates[1] = -textureY * texturePixelScale[1];
		textureCoordinates[2] = (textureX + textureWidth) * texturePixelScale[0];
		textureCoordinates[3] = -(textureY + textureHeight) * texturePixelScale[1];

		glBindBuffer(GL_ARRAY_BUFFER, renderer.getGuiMesh().getVbo());
		glBufferData(GL_ARRAY_BUFFER, new float[] {
				0f, 0f, textureCoordinates[0], -textureCoordinates[1],
				1f, 0f, textureCoordinates[2], -textureCoordinates[1],
				0f, 1f, textureCoordinates[0], -textureCoordinates[3],
				1f, 1f, textureCoordinates[2], -textureCoordinates[3]
		}, GL_DYNAMIC_DRAW);


		int[] previousTexture = new int[1];
		glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

		ShaderProgram guiShaderProgram = renderer.getShaderProgram("gui");

		guiShaderProgram.use();
		glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
		glActiveTexture(GL_TEXTURE0);

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		renderer.getModelMatrix().identity();

		guiShaderProgram.setUniform("vs_textureSizeX", guiWidth);
		guiShaderProgram.setUniform("vs_textureSizeY", guiHeight);
		guiShaderProgram.setUniform("vs_posX", guiX);
		guiShaderProgram.setUniform("vs_posY", guiY);
		guiShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
		guiShaderProgram.setUniform("vs_projectionMatrix", renderer.getModelMatrix().ortho(
				0f, // left
				Window.get().getResolutionFrameBuffer().getWidth(), // right
				0f, // bottom
				Window.get().getResolutionFrameBuffer().getHeight(), // top
				0.01f, // z-near
				1f // z-far
		));



		glEnable(GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


		// TODO: Add back-face culling to text rendering!!!

		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);


		// draw mesh
		glBindVertexArray(renderer.getGuiMesh().getVao());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, renderer.getGuiMesh().getIndexCount(), GL_UNSIGNED_INT, 0);


		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);


		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

		renderer.getShaderProgram().use();


	}

	public static void drawGuiTextureFit(Texture texture, int guiX, int guiY, int guiWidth, int guiHeight) {

		Renderer renderer = Renderer.get();
		ShaderProgram guiShaderProgram = renderer.getShaderProgram("gui");

		glBindBuffer(GL_ARRAY_BUFFER, renderer.getGuiMesh().getVbo());
		glBufferData(GL_ARRAY_BUFFER, new float[] {
				0f, 0f, 0f, 0f,
				1f, 0f, 1f, 0f,
				0f, 1f, 0f, 1f,
				1f, 1f, 1f, 1f
		}, GL_DYNAMIC_DRAW);

		int[] previousTexture = new int[1];
		glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

		guiShaderProgram.use();
		glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
		glActiveTexture(GL_TEXTURE0);

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		Matrix4f modelMatrix = renderer.getModelMatrix().identity();

		guiShaderProgram.setUniform("vs_textureSizeX", guiWidth);
		guiShaderProgram.setUniform("vs_textureSizeY", guiHeight);
		guiShaderProgram.setUniform("vs_posX", guiX);
		guiShaderProgram.setUniform("vs_posY", guiY);
		guiShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
		guiShaderProgram.setUniform("vs_projectionMatrix", modelMatrix.ortho(
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


		// draw mesh
		glBindVertexArray(renderer.getGuiMesh().getVao());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, renderer.getGuiMesh().getIndexCount(), GL_UNSIGNED_INT, 0);


		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);


		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

		renderer.getShaderProgram().use();

	}

	public static void drawGuiTexture(Texture texture, int guiX, int guiY) {

		Renderer renderer = Renderer.get();
		ShaderProgram guiShaderProgram = renderer.getShaderProgram("gui");

		glBindBuffer(GL_ARRAY_BUFFER, renderer.getGuiMesh().getVbo());
		glBufferData(GL_ARRAY_BUFFER, new float[] {
				0f, 0f, 0f, 0f,
				1f, 0f, 1f, 0f,
				0f, 1f, 0f, 1f,
				1f, 1f, 1f, 1f
		}, GL_DYNAMIC_DRAW);

		int[] previousTexture = new int[1];
		glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture);

		guiShaderProgram.use();
		glBindTexture(GL_TEXTURE_2D, texture.getTextureHandle());
		glActiveTexture(GL_TEXTURE0);

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		Matrix4f modelMatrix = renderer.getModelMatrix().identity();

		guiShaderProgram.setUniform("vs_textureSizeX", texture.getWidth());
		guiShaderProgram.setUniform("vs_textureSizeY", texture.getHeight());
		guiShaderProgram.setUniform("vs_posX", guiX);
		guiShaderProgram.setUniform("vs_posY", guiY);
		guiShaderProgram.setUniform("fs_texture", GL_TEXTURE0);
		guiShaderProgram.setUniform("vs_projectionMatrix", modelMatrix.ortho(
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


		// draw mesh
		glBindVertexArray(renderer.getGuiMesh().getVao());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, renderer.getGuiMesh().getIndexCount(), GL_UNSIGNED_INT, 0);


		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);


		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, previousTexture[0]);

		renderer.getShaderProgram().use();
	}
}
