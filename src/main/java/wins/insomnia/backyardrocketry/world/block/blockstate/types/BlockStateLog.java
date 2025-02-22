package wins.insomnia.backyardrocketry.world.block.blockstate.types;

import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockState;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyFaceDirection;

public class BlockStateLog extends BlockState {

	public static final int PROPERTY_TOP_DIRECTION = 0;

	private final BlockStateProperty<?>[] PROPERTIES = new BlockStateProperty[] {
		new PropertyFaceDirection("top_direction")
	};


	public void setTopDirection(PropertyFaceDirection.FaceDirection faceDirection) {

		((PropertyFaceDirection) PROPERTIES[PROPERTY_TOP_DIRECTION]).setDirection(faceDirection);

	}

	public int getFaceDirection() {
		return ((PropertyFaceDirection) PROPERTIES[PROPERTY_TOP_DIRECTION]).getValue();
	}


	@Override
	public BlockStateProperty<?>[] getProperties() {
		return PROPERTIES;
	}

	public static BlockStateLog getFromFace(Blocks.Face face) {

		BlockStateLog blockStateObject = new BlockStateLog();

		if (face == Blocks.Face.POS_Y) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.POS_Y);
		} else if (face == Blocks.Face.NEG_Y) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.NEG_Y);
		} else if (face == Blocks.Face.POS_X) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.POS_X);
		} else if (face == Blocks.Face.NEG_X) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.NEG_X);
		} else if (face == Blocks.Face.POS_Z) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.POS_Z);
		} else if (face == Blocks.Face.NEG_Z) {
			blockStateObject.setTopDirection(PropertyFaceDirection.FaceDirection.NEG_Z);
		} else {
			return null;
		}

		return blockStateObject;
	}
}
