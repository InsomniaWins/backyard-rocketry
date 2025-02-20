package wins.insomnia.backyardrocketry.world.block.blockstate;


import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.BlockStateProperty;

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
	private static final HashMap<Byte, ArrayList<String>> BLOCK_STATES = new HashMap<>();
	private static int nextId = 0;

	public static String[] getBlockStates(byte block) {
		return BLOCK_STATES.get(block).toArray(new String[1]);
	}

	public static String getBlockState(byte block, int blockStateIndex) {
		return BLOCK_STATES.get(block).get(blockStateIndex);
	}

	public static byte getBlockStateIndex(byte block, BlockState blockState) {

		ArrayList<String> states = BLOCK_STATES.get(block);

		if (states == null || states.isEmpty()) return 0;

		String blockStateName = blockState.getStateString();

		for (int i = 0; i < states.size(); i++) {
			String stateName = states.get(i);

			if (stateName.equals(blockStateName)) {
				return (byte) i;
			}

		}

		return 0;
	}


	public static void registerBlockState(byte block, BlockState blockState) {

		if (blockState == null) {
			BLOCK_STATES.put(block, new ArrayList<>(List.of("default")));
			return;
		}

		BlockStateProperty<?>[] properties = blockState.getProperties();
		registerPropertyCombinations(block, properties);
	}


	private static void registerPropertyCombinations(byte block, BlockStateProperty[] properties) {
		registerPropertyCombinations(block, properties, 0, null, true);
	}

	private static Object[] registerPropertyCombinations(byte block, BlockStateProperty[] properties, int propertyIndex, Object[] currentCombination, boolean first) {
		if (properties.length <= propertyIndex) return currentCombination;

		if (currentCombination == null) currentCombination = new Object[properties.length];

		Object[] propertyCombinations = properties[propertyIndex].getPossibleCombinations();

		for (int i = 0; i < propertyCombinations.length; i++) {

			Object currentPropertyValue = propertyCombinations[i];
			currentCombination[propertyIndex] = currentPropertyValue;

			currentCombination = registerPropertyCombinations(block, properties, propertyIndex + 1, currentCombination, false);

			if (i != propertyCombinations.length - 1 || first) {

				addBlockState(block, properties, currentCombination);

			}

		}

		return currentCombination;
	}


	private static void addBlockState(byte block, BlockStateProperty[] properties, Object[] currentCombination) {

		if (!BLOCK_STATES.containsKey(block)) {
			BLOCK_STATES.put(block, new ArrayList<>());
		}

		ArrayList<String> states = BLOCK_STATES.get(block);

		if (states.size() == MAX_STATES) {
			throw new RuntimeException("Cannot add any more block states for block:" + Block.getBlockName(block));
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

		states.add(stateName.append('}').toString());
	}



	private static int nextIdIncrement() {
		return nextId++;
	}

}
