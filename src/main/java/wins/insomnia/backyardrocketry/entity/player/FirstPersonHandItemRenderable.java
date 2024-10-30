package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.update.Updater;

public class FirstPersonHandItemRenderable implements IRenderable {

	private Mesh handMesh = null;

	private double swingAnimationStartTime = -100;
	private float swingAnimationSpeed = 4f;
	private float swingAnimationFrameValue = 1f;

	//region TODO: replace with method(s) that works with item stacks when item and item stacks are implemented
	public void setBlock(byte block) {
		resetHandMesh();
		handMesh = BlockModelData.getMeshFromBlock(block);
	}

	public void resetHandMesh() {
		handMesh = null;
	}

	//endregion




	@Override
	public void render() {
		if (handMesh == null || handMesh.isClean()) {
			return;
		}

		swingAnimationFrameValue = Math.min(1f, (float) (Updater.getCurrentTime() * swingAnimationSpeed - swingAnimationStartTime));
		float swingAnimationSineValue = Math.sin(swingAnimationFrameValue * (float) Math.PI);

		Renderer.get().getModelMatrix().identity()
				.translate(-0.5f, -0.5f, -0.5f)
				.translate(1.75f, -1.4f + Math.sin((float) Updater.getCurrentTime() * 0.75f) * 0.05f, -3f)
				.translate(0f, swingAnimationSineValue * 0.5f, -swingAnimationSineValue * 0.5f)
				.rotateY(0.45f)
				.rotateX((float) Math.sin(Updater.getCurrentTime() * 0.75) * 0.02f)
				.rotateX(-swingAnimationSineValue * 1.2f);

		int[] gameWindowSize = Window.get().getSize();
		Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", new Matrix4f().setPerspective(70f, gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 16f));
		Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", new Matrix4f().identity());
		Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		handMesh.render();
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getCamera().getProjectionMatrix());
		Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", Renderer.get().getCamera().getViewMatrix());
	}


	public void playSwingAnimation(boolean withCooldown) {


		if (swingAnimationFrameValue < 0.75f && withCooldown) {
			return;
		}

		swingAnimationStartTime = Updater.getCurrentTime() * swingAnimationSpeed;

	}


	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public boolean isClean() {
		return handMesh == null || handMesh.isClean();
	}

	@Override
	public void clean() {
		if (handMesh == null) return;
		if (!handMesh.isClean()) {
			handMesh.clean();
		}
	}

	@Override
	public int getRenderPriority() {
		return 100;
	}
}
