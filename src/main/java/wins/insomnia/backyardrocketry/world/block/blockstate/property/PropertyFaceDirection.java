package wins.insomnia.backyardrocketry.world.block.blockstate.property;

public class PropertyFaceDirection extends PropertyInteger {

	public enum FaceDirection {
		POS_X,
		POS_Y,
		POS_Z,
		NEG_X,
		NEG_Y,
		NEG_Z
	}
	public static final FaceDirection[] FACE_DIRECTION_VALUES = FaceDirection.values();

	public PropertyFaceDirection(String propertyName) {
		super(propertyName, 0, 5);
	}

	public void setDirection(FaceDirection faceDirection) {
		setValue(faceDirection.ordinal());
	}

	public FaceDirection getDirection() {
		return FACE_DIRECTION_VALUES[getValue()];
	}

}
