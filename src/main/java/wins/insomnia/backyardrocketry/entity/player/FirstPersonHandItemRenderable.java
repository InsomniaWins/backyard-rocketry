package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.Transform;
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

		IPlayer player = BackyardRocketry.getInstance().getPlayer();

		Transform transform = new Transform();
		transform.getRotation().set(Renderer.get().getCamera().getTransform().getRotation());
		transform.getPosition().set(player.getInterpolatedPosition());

		Vector3d cameraDirectionVector = new Vector3d(0, 0, -1)
				.rotateX(-transform.getRotation().x)
				.rotateY(-transform.getRotation().y);

		Vector3d uiRightVector = new Vector3d(1, 0, 0)
				.rotateX(-transform.getRotation().x)
				.rotateY(-transform.getRotation().y);
		Vector3d uiUpVector = new Vector3d(0, 1, 0)
				.rotateX(-transform.getRotation().x)
				.rotateY(-transform.getRotation().y);

		transform.getPosition().add(new Vector3d(uiRightVector).mul(0.5));
		transform.getPosition().add(new Vector3d(uiUpVector).negate().mul(0.6));//.mul(Window.get().getHeight() * 0.002));



		Renderer.get().getModelMatrix().identity()
				.translate(
						(float) transform.getPosition().x,
						(float) transform.getPosition().y,
						(float) transform.getPosition().z
				).translate(
						(float) cameraDirectionVector.x * 0.5f,
						(float) cameraDirectionVector.y * 0.5f,
						(float) cameraDirectionVector.z * 0.5f
				)
				.scale(0.5f)
				.rotateY(-transform.getRotation().y)
				.rotateX(-transform.getRotation().x)
				.translate(-0.5f, -0.5f, -0.5f)
				.rotateY(0.45f);

		Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

		if (handMesh == null || handMesh.isClean()) {
			return;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		handMesh.render();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
