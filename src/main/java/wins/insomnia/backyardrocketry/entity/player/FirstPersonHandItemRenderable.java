package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirstPersonHandItemRenderable implements IRenderable {

	private Mesh handMesh = null;


	//region TODO: replace with method(s) that works with item stacks when item and item stacks are implemented
	public void setBlock(byte block) {
		resetHandMesh();

		BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(block, 0, 0, 0);
		if (blockModelData == null) return;

		ArrayList<Float> vertexArray = new ArrayList<>();
		ArrayList<Integer> indexArray = new ArrayList<>();

		int indexOffset = 0;

		for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

			HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();

			ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
			ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");

			for (Double vertexValue : faceVertexArray) {
				vertexArray.add(vertexValue.floatValue());
			}

			int greatestIndexValue = 0;
			for (Integer indexValue : faceIndexArray) {
				indexArray.add(indexValue + indexOffset);
				if (indexValue > greatestIndexValue) {
					greatestIndexValue = indexValue;
				}
			}
			indexOffset += greatestIndexValue + 1;
		}

		float[] primitiveVertexArray = new float[vertexArray.size()];
		for (int i = 0; i < primitiveVertexArray.length; i++) {
			primitiveVertexArray[i] = vertexArray.get(i);
		}

		int[] primitiveIndexArray = new int[indexArray.size()];
		for (int i = 0; i < primitiveIndexArray.length; i++) {
			primitiveIndexArray[i] = indexArray.get(i);
		}

		handMesh = new Mesh(primitiveVertexArray, primitiveIndexArray);
	}

	public void resetHandMesh() {
		if (handMesh != null && !handMesh.isClean()) {
			handMesh.clean();
		}
		handMesh = null;
	}

	//endregion




	@Override
	public void render() {
		Renderer.get().getModelMatrix().identity()
				.translate(-0.5f, -0.5f, -0.5f)
				.translate(1.75f, -1.4f + (float) Math.sin(Updater.getCurrentTime() * 0.75) * 0.05f, -3f)
				.rotateY(0.45f)
				.rotateX((float) Math.sin(Updater.getCurrentTime() * 0.75) * 0.02f);

		int[] gameWindowSize = Window.get().getSize();
		Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", new Matrix4f().setPerspective(70f, gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 16f));
		Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", new Matrix4f().identity());
		Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());


		if (handMesh == null || handMesh.isClean()) {
			return;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		handMesh.render();
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		Renderer.get().getShaderProgram().setUniform("vs_projectionMatrix", Renderer.get().getCamera().getProjectionMatrix());
		Renderer.get().getShaderProgram().setUniform("vs_viewMatrix", Renderer.get().getCamera().getViewMatrix());
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
