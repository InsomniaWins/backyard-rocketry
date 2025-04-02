package wins.insomnia.backyardrocketry.world.block.blockstate;


import org.checkerframework.checker.optional.qual.OptionalPropagator;
import wins.insomnia.backyardrocketry.util.io.LoadTask;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateLog;
import wins.insomnia.backyardrocketry.world.block.blockstate.types.BlockStateStone;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockStateManager {

	/*

	---- BLOCK_STATES HashMap ----

	- byte // blockId
		- | 0 | String // block state properties for state: 0 on block: blockId
		- | 1 | String // block state properties for state: 1 on block: blockId
		- | 2 | String // block state properties for state: 2 on block: blockId



	// EXAMPLE

	- Block.COBBLESTONE // id of cobblestone
		- | 0 | "property1=false, property2=false" // block state for COBBLESTONE where property1 is false and property2 is false
		- | 1 | "property1=false, property2=true" // block state for COBBLESTONE where property1 is false and property2 is true
		- | 2 | "property1=true, property2=false" // block state for COBBLESTONE where property1 is true and property2 is false
		- | 3 | "property1=true, property2=true" // block state for COBBLESTONE where property1 is true and property2 is true

	 */
	private static final int MAX_STATES = Byte.MAX_VALUE;
	private static final HashMap<Byte, ArrayList<BlockStateContainer>> BLOCK_STATES = new HashMap<>();
	private static int nextId = 0;

	public static String[] getBlockStates(byte block) {
		return BLOCK_STATES.get(block).toArray(new String[1]);
	}

	public static String getBlockStateName(byte block, int blockStateIndex) {
		return BLOCK_STATES.get(block).get(blockStateIndex).BLOCK_STATE_NAME;
	}

	public static BlockState parseBlockState(byte block, byte blockState) {
		return BLOCK_STATES.get(block).get(blockState).BLOCK_STATE;
	}

	public static byte getBlockStateIndex(byte block, BlockState blockState) {

		ArrayList<BlockStateContainer> states = BLOCK_STATES.get(block);

		if (states == null || states.isEmpty()) return 0;

		String blockStateName = blockState.getStateString();

		for (int i = 0; i < states.size(); i++) {
			String stateName = states.get(i).BLOCK_STATE_NAME;

			if (stateName.equals(blockStateName)) {
				return (byte) i;
			}
		}

		return 0;
	}

	public static List<LoadTask> makeBlockStateRegisterTaskList() {

		List<LoadTask> list = new ArrayList<>();

		for (byte blockId : Blocks.getBlocks()) {

			String blockName = Blocks.getBlockName(blockId);

			list.add(new LoadTask("Registering " + blockName + " Block States . . .", () -> {

				registerBlockState(blockId);

			}));

		}

		return list;

	}

	public static void registerBlockState(byte block) {
		Block blockObject = Blocks.getBlock(block);

		if (blockObject == null || blockObject.getBlockState() == null || !registerBlockState(block, blockObject)) {
			BLOCK_STATES.put(block, new ArrayList<>(List.of(new BlockStateContainer("default", null, (byte) 0))));
		}
	}

	public static boolean registerBlockState(byte blockId, Block block) {
		Constructor<?>[] constructors = block.getBlockState().getDeclaredConstructors();

		if (constructors.length == 0) return false;

		try {
			BlockState blockState = (BlockState) constructors[0].newInstance();

			BlockStateProperty<?>[] properties = blockState.getProperties();
			registerPropertyCombinations(blockId, properties, block.getBlockState());

			return true;

		} catch (Exception e) {
			return false;
		}
	}


	private static void registerPropertyCombinations(byte block, BlockStateProperty[] properties, Class<? extends BlockState> blockStateClass) {
		registerPropertyCombinations(block, properties, blockStateClass, 0, null, true);
	}

	private static Object[] registerPropertyCombinations(byte block, BlockStateProperty[] properties, Class<? extends BlockState> blockStateClass, int propertyIndex, Object[] currentCombination, boolean first) {
		if (properties.length <= propertyIndex) return currentCombination;

		if (currentCombination == null) currentCombination = new Object[properties.length];

		Object[] propertyCombinations = properties[propertyIndex].getPossibleCombinations();

		for (int i = 0; i < propertyCombinations.length; i++) {

			Object currentPropertyValue = propertyCombinations[i];
			currentCombination[propertyIndex] = currentPropertyValue;

			currentCombination = registerPropertyCombinations(block, properties, blockStateClass, propertyIndex + 1, currentCombination, false);

			if (i != propertyCombinations.length - 1 || first) {

				addBlockState(block, properties, blockStateClass, currentCombination);

			}

		}

		return currentCombination;
	}


	private static void addBlockState(byte block, BlockStateProperty<Object>[] properties, Class<? extends BlockState> blockStateClass, Object[] currentCombination) {

		if (!BLOCK_STATES.containsKey(block)) {
			BLOCK_STATES.put(block, new ArrayList<>());
		}

		ArrayList<BlockStateContainer> states = BLOCK_STATES.get(block);

		if (states.size() == MAX_STATES) {
			throw new RuntimeException("Cannot add any more block states for block:" + Blocks.getBlockName(block));
		}



		StringBuilder stateName = new StringBuilder("{");

		for (int i = 0; i < properties.length; i++) {

			stateName.append(properties[i].getName())
					.append('=')
					.append(currentCombination[i].toString());

			if (i < properties.length - 1) {
				stateName.append(", ");
			}

		}
		stateName.append('}');








		BlockState blockState = createBlockState(blockStateClass);
		BlockStateProperty<Object>[] blockStateProperties = blockState.getProperties();
		for (int i = 0; i < properties.length; i++) {
			blockStateProperties[i].setValue(currentCombination[i]);
		}
		BlockStateContainer container = new BlockStateContainer(stateName.toString(), blockState, (byte) states.size());

		states.add(container);

	}


	public static BlockState createBlockState(Class<? extends BlockState> blockStateClass) {

		Constructor<?>[] constructors = blockStateClass.getDeclaredConstructors();

		if (constructors.length == 0) return null;

		try {
			BlockState blockState = (BlockState) constructors[0].newInstance();
			return blockState;

		} catch (Exception e) {
			return null;
		}

	}


	private static int nextIdIncrement() {
		return nextId++;
	}


	public static class BlockStateContainer {

		public final byte BLOCK_STATE_BYTE;
		public final String BLOCK_STATE_NAME;
		public final BlockState BLOCK_STATE;

		public BlockStateContainer(String name, BlockState object, byte blockStateId) {
			this.BLOCK_STATE_NAME = name;
			this.BLOCK_STATE = object;
			this.BLOCK_STATE_BYTE = blockStateId;


		}

		@Override
		public String toString() {
			return BLOCK_STATE_NAME;
		}

	}

}
