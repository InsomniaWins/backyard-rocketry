package wins.insomnia.backyardrocketry.entity;

import org.joml.Math;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.render.BlockModelData;
import wins.insomnia.backyardrocketry.render.IRenderable;
import wins.insomnia.backyardrocketry.render.Mesh;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.world.World;

public class EntityItem extends Entity implements IRenderable {

	private boolean uniqueMesh = true;
	private Mesh mesh;
	private ItemStack itemStack;
	private final BoundingBox BOUNDING_BOX = new BoundingBox();

	public EntityItem(ItemStack itemStack, World world) {
		super(world);
		this.itemStack = itemStack;


		if (itemStack.getItem() instanceof BlockItem blockItem) {
			mesh = BlockModelData.getMeshFromBlock(blockItem.getBlock());
			uniqueMesh = false;
		} else {
			// make mesh for dropped item
		}

	}

	// returns the item-stack previously stored by this item entity
	public ItemStack setItemStack(ItemStack itemStack) {
		ItemStack previousItemStack = this.itemStack;
		this.itemStack = itemStack;
		return previousItemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	@Override
	public void render() {

		Transform transform = getTransform();

		Renderer.get().getModelMatrix().identity()
				.translate((float) transform.getPosition().x, (float) transform.getPosition().y, (float) transform.getPosition().z)
				.rotateZ(getRotation().z)
				.rotateY(getRotation().y)
				.rotateX(getRotation().x)
				.translate(-0.5f, -0.5f, -0.5f);
		Renderer.get().getShaderProgram().setUniform("vs_modelMatrix", Renderer.get().getModelMatrix());

		mesh.render();
	}

	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public boolean isClean() {

		if (!hasUniqueMesh()) return true;

		return mesh == null || mesh.isClean();
	}

	@Override
	public void clean() {
		if (hasUniqueMesh() && mesh != null) {
			mesh.clean();
		}

		mesh = null;
	}

	@Override
	public int getRenderPriority() {
		return 0;
	}

	public boolean hasUniqueMesh() {
		return uniqueMesh;
	}


	@Override
	public void removedFromWorld() {
		Renderer.get().removeRenderable(this);
		clean();
		System.out.println("removed entity from world: " + this);
	}

	@Override
	public void addedToWorld() {
		Renderer.get().addRenderable(this);
	}

	private void updateBoundingBox() {
		BOUNDING_BOX.getMin().set(getPosition()).add(-0.25, -0.25, -0.25);
		BOUNDING_BOX.getMax().set(getPosition()).add(0.25, 0.25, 0.25);
	}

	@Override
	public void fixedUpdate() {

		updateBoundingBox();

		super.fixedUpdate();
		getRotation().y += Math.toRadians(1);





	}
}
