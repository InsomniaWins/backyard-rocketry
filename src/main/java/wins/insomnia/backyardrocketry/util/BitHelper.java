package wins.insomnia.backyardrocketry.util;

public class BitHelper {


	public static int getBit(int num, int position) {
		return (num >> position) & 1;
	}

	public static int setBitToOne(int num, int position) {
		return num | 1 << position;
	}

	public static int setBitToZero(int num, int position) {
		return num & ~(1 << position);
	}

	public static int setBitToValue(int num, int position, int value) {

		if (value == 0) {
			return setBitToZero(num, position);
		} else if (value == 1) {
			return setBitToOne(num, position);
		} else {
			throw new RuntimeException();
		}
	}


	public static int getBlockIdFromBlockState(int blockState) {
		int block = 0;
		for (int i = 0; i < 8; i++) {
			block = setBitToValue(block, i, getBit(blockState, 24 + i));
		}
		return block;
	}

	// the left-most 8 bits are used for block id's (256 possible blocks, -127 - 128)
	// the right-most 24 bits are used for block properties (16,777,216 possible hashes)
	public static int getBlockStateWithoutPropertiesFromBlockId(int block) {

		int state = 0;
		for (int i = 0; i < 8; i++) {
			state = setBitToValue(state, 24 + i, getBit(block, i));
		}

		return state;

	}

	public static int getPropertiesFromBlockState(int state) {

		int properties = 0;
		for (int i = 0; i < 24; i++) {
			properties = setBitToValue(properties, i, getBit(state, i));
		}

		return properties;

	}

}
