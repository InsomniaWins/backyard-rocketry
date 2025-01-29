package wins.insomnia.backyardrocketry.render;


import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

public class ResolutionFrameBuffer {

	private int handle = -1;
	private int textureHandle = -1;
	private int depthTextureHandle = -1;
	private int width;
	private int height;

	public ResolutionFrameBuffer(int width, int height) {

		this.width = width;
		this.height = height;

		handle = GL30.glGenFramebuffers();

		// bind this frame buffer
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, getHandle());


		// make color texture

		textureHandle = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, textureHandle);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureHandle, 0);


		// make depth texture

		depthTextureHandle = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, depthTextureHandle);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTextureHandle, 0);

	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean hasHandle() {
		return getHandle() > -1;
	}

	public boolean hasTextureHandle() {
		return getTextureHandle() > -1;
	}

	public boolean hasDepthTextureHandle() {
		return getDepthTextureHandle() > -1;
	}

	public int getHandle() {
		return handle;
	}

	public int getTextureHandle() {
		return textureHandle;
	}

	public int getDepthTextureHandle() {
		return depthTextureHandle;
	}

	public boolean isClean() {
		return hasHandle() && !hasTextureHandle() && !hasDepthTextureHandle();
	}

	public void clean() {
		if (isClean()) return;

		if (hasHandle()) {
			GL30.glDeleteFramebuffers(getHandle());
			handle = -1;
		}

		if (hasTextureHandle()) {
			GL30.glDeleteTextures(getTextureHandle());
			textureHandle = -1;
		}

		if (hasDepthTextureHandle()) {
			GL30.glDeleteTextures(getDepthTextureHandle());
			depthTextureHandle = -1;
		}


	}

}
