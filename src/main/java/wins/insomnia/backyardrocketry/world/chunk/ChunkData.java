package wins.insomnia.backyardrocketry.world.chunk;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.checkerframework.checker.units.qual.A;
import org.joml.Math;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ChunkData {

	private final long SEED;
	private final int X;
	private final int Y;
	private final int Z;
	private byte[][][] blocks;
	private byte[][][] blockStates;
	private short[][][] lightValues;
	private Thread ownerThread = null;


	public ChunkData(long seed, int x, int y, int z, boolean shouldGenerate, boolean areWorldCoordinates) {

		SEED = seed;

		if (!areWorldCoordinates) {
			x = ChunkPosition.getBlockX(x);
			y = ChunkPosition.getBlockY(y);
			z = ChunkPosition.getBlockZ(z);
		}

		X = x;
		Y = y;
		Z = z;

		initializeBlocks();

		if (shouldGenerate) {
			WorldGeneration.generateChunkData(this);
		}

	}

	public void replaceBlocks(ChunkData newChunkData) {

		while (!grabThreadOwnership());

		blocks = newChunkData.blocks;
		blockStates = newChunkData.blockStates;

		while (!loseThreadOwnership());

	}

	public static int getLightR(short light) {
		int mask = 0xF << (3 * 4);

		int maskedNum = light & mask;

		return maskedNum >>> (3 * 4);
	}

	public static int getLightG(short light) {
		int mask = 0xF << (2 * 4);

		int maskedNum = light & mask;

		return maskedNum >>> (2 * 4);
	}

	public static int getLightB(short light) {
		int mask = 0xF << 4;

		int maskedNum = light & mask;

		return maskedNum >>> 4;
	}

	public static int getLightS(short light) {
		return light << 12 >>> 12;
	}

	// R, G, B, S are values from 0 to 15
	public static short makeLightValue(int r, int g, int b, int s) {
		return (short) (s | b << 4 | g << 8 | r << 12);
	}


	private int getLightLevel(short lightValue) {
		int r = getLightR(lightValue);
		int g = getLightG(lightValue);
		int b = getLightB(lightValue);
		int s = getLightS(lightValue);

		return (r + g + b + s) / 4;
	}


	// CALL WHILE HAVING OWNERSHIP!
	private void floodFillLight(Chunk chunk, int colorChannel, int desiredValue, int x, int y, int z, ArrayList<int[]> alreadyCheckedBlocks) {

		// color channels
		// 0 = r,
		// 1 = g,
		// 2 = b,
		// 3 = sky?

		if (desiredValue < 1) return;

		if (x < 0 || x > 19 || y < 0 || y > 19 || z < 0 || z > 19) return;

		int[] blockPosition = new int[] {x, y, z};

		for (int[] checkedBlock : alreadyCheckedBlocks) {
			if (Arrays.equals(checkedBlock, blockPosition)) {
				return;
			}
		}


		byte block = chunk.getBlock(x, y, z);
		if (!Blocks.isBlockTransparent(block)) return;

		short currentLightValue = getLightValue(x, y, z);
		int currentChannelValue = 0;
		switch (colorChannel) {
			case 0 -> currentChannelValue = getLightR(currentLightValue);
			case 1 -> currentChannelValue = getLightG(currentLightValue);
			case 2 -> currentChannelValue = getLightB(currentLightValue);
		}

		if (currentChannelValue >= desiredValue) return;

		desiredValue = Math.min(15, desiredValue);

		switch (colorChannel) {
			case 0 -> {
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 15, BitHelper.getBit(desiredValue, 3));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 14, BitHelper.getBit(desiredValue, 2));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 13, BitHelper.getBit(desiredValue, 1));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 12, BitHelper.getBit(desiredValue, 0));
			}
			case 1 -> {
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 11, BitHelper.getBit(desiredValue, 3));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 10, BitHelper.getBit(desiredValue, 2));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 9, BitHelper.getBit(desiredValue, 1));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 8, BitHelper.getBit(desiredValue, 0));
			}
			case 2 -> {
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 7, BitHelper.getBit(desiredValue, 3));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 6, BitHelper.getBit(desiredValue, 2));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 5, BitHelper.getBit(desiredValue, 1));
				currentLightValue = (short) BitHelper.setBitToValue(currentLightValue, 4, BitHelper.getBit(desiredValue, 0));
			}
		}

		if (desiredValue == 0) return;

		setLightValue(x, y, z, currentLightValue);

		alreadyCheckedBlocks.add(blockPosition);
		floodFillLight(chunk, colorChannel, desiredValue - 1, x + 1, y, z, new ArrayList<>(alreadyCheckedBlocks));
		floodFillLight(chunk, colorChannel, desiredValue - 1, x - 1, y, z, new ArrayList<>(alreadyCheckedBlocks));
		floodFillLight(chunk, colorChannel, desiredValue - 1, x, y + 1, z, new ArrayList<>(alreadyCheckedBlocks));
		floodFillLight(chunk, colorChannel, desiredValue - 1, x, y - 1, z, new ArrayList<>(alreadyCheckedBlocks));
		floodFillLight(chunk, colorChannel, desiredValue - 1, x, y, z + 1, new ArrayList<>(alreadyCheckedBlocks));
		floodFillLight(chunk, colorChannel, desiredValue - 1, x, y, z - 1, new ArrayList<>(alreadyCheckedBlocks));
	}

	// CALL WHILE HAVING OWNERSHIP!
	private void floodFillLight(Chunk chunk, short lightColor, int x, int y, int z) {

		int r = getLightR(lightColor);
		int g = getLightG(lightColor);
		int b = getLightB(lightColor);


		floodFillLight(chunk, 0, r, x, y, z, new ArrayList<>());
		floodFillLight(chunk, 1, g, x, y, z, new ArrayList<>());
		floodFillLight(chunk, 2, b, x, y, z, new ArrayList<>());

	}



	public void updateLighting(Chunk chunk) {

		while (!grabThreadOwnership());

		LinkedList<Vector3i> lightPositionQueue = new LinkedList<>();
		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {
					short lightLevel = Blocks.getBlockMinimumLightLevel(getBlock(x, y, z));
					setLightValue(x, y, z, lightLevel);

					if (lightLevel != 0b0000_0000_0000_0000) {
						lightPositionQueue.push(new Vector3i(x, y, z));
					}
				}
			}
		}

		while (!lightPositionQueue.isEmpty()) {
			Vector3i lightPosition = lightPositionQueue.pop();
			short desiredLightValue = getLightValue(lightPosition.x, lightPosition.y, lightPosition.z);

			setLightValue(lightPosition.x, lightPosition.y, lightPosition.z, (byte) 0);

			floodFillLight(
					chunk,
					desiredLightValue,
					lightPosition.x,
					lightPosition.y,
					lightPosition.z
			);

		}

		while (!loseThreadOwnership());

	}


	private ChunkData(long seed, int chunkX, int chunkY, int chunkZ, boolean shouldGenerate) {

		this(seed, chunkX, chunkY, chunkZ, shouldGenerate, false);

	}

	public ChunkData(long seed, int chunkX, int chunkY, int chunkZ) {
		this(seed, chunkX, chunkY, chunkZ, true);
	}

	public boolean hasThreadOwnership() {
		return ownerThread == Thread.currentThread();
	}

	public boolean loseThreadOwnership() {
		if (ownerThread != Thread.currentThread()) return false;

		ownerThread = null;
		return true;
	}

	public synchronized boolean grabThreadOwnership() {
		if (ownerThread != null) return false;

		ownerThread = Thread.currentThread();
		return true;
	}







	public ChunkPosition getChunkPosition(World world) {

		return world.getChunkPositionFromBlockPosition(X, Y, Z);

	}

	public int getWorldX() {
		return X;
	}

	public int getWorldY() {
		return Y;
	}

	public int getWorldZ() {
		return Z;
	}

	public long getSeed() {
		return SEED;
	}

	private void initializeBlocks() {

		blocks = new byte[Chunk.SIZE_X][Chunk.SIZE_Y][Chunk.SIZE_Z];
		blockStates = new byte[Chunk.SIZE_X][Chunk.SIZE_Y][Chunk.SIZE_Z];
		lightValues = new short[Chunk.SIZE_X][Chunk.SIZE_Y][Chunk.SIZE_Z];

	}


	// make sure you have thread ownership
	public void setBlock(int x, int y, int z, byte block) {
		blocks[x][y][z] = block;
	}

	// make sure you have thread ownership
	public void setBlockState(int x, int y, int z, byte blockState) {
		blockStates[x][y][z] = blockState;
	}

	// make sure you have thread ownership
	public void setLightValue(int x, int y, int z, short lightValue) {
		lightValues[x][y][z] = lightValue;
	}

	public byte getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}

	public byte getBlockState(int x, int y, int z) {
		return blockStates[x][y][z];
	}

	public short getLightValue(int x, int y, int z) {
		return lightValues[x][y][z];
	}

	public byte[][][] getBlocks() {
		return blocks;
	}

	public byte[][][] getBlockStates() {
		return blockStates;
	}


	public static ChunkData deserialize(byte[] data) {

		int dataIndex = 0;


		byte[] buffer;

		// read seed

		buffer = new byte[Long.BYTES];
		for (int i = 0; i < Long.BYTES; i++) {
			buffer[i] = data[dataIndex++];
		}
		long seed = Longs.fromByteArray(buffer);



		// read x

		buffer = new byte[Integer.BYTES];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = data[dataIndex++];
		}
		int worldX = Ints.fromByteArray(buffer);


		// read y

		buffer = new byte[Integer.BYTES];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = data[dataIndex++];
		}
		int worldY = Ints.fromByteArray(buffer);


		// read z

		buffer = new byte[Integer.BYTES];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = data[dataIndex++];
		}
		int worldZ = Ints.fromByteArray(buffer);

		ChunkData chunkData = new ChunkData(seed, worldX, worldY, worldZ, false, true);


		// read blocks

		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {

					chunkData.blocks[x][y][z] = data[dataIndex++];

				}
			}
		}

		// read block states

		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {

					chunkData.blockStates[x][y][z] = data[dataIndex++];

				}
			}
		}

		return chunkData;

	}

	public static byte[] serialize(ChunkData chunkData) {
		byte[] data = new byte[ChunkIO.BYTES_PER_CHUNK];

		int dataIndex = 0;

		byte[] buffer;


		// store seed

		buffer = Longs.toByteArray(chunkData.SEED);
		for (int i = 0; i < Long.BYTES; i++) {
			data[dataIndex++] = buffer[i];
		}


		// store x

		buffer = Ints.toByteArray(chunkData.X);
		for (int i = 0; i < Integer.BYTES; i++) {
			data[dataIndex++] = buffer[i];
		}


		// store y

		buffer = Ints.toByteArray(chunkData.Y);
		for (int i = 0; i < Integer.BYTES; i++) {
			data[dataIndex++] = buffer[i];
		}


		// store Z

		buffer = Ints.toByteArray(chunkData.Z);
		for (int i = 0; i < Integer.BYTES; i++) {
			data[dataIndex++] = buffer[i];
		}


		// store blocks

		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {
					data[dataIndex++] = chunkData.blocks[x][y][z];
				}
			}
		}


		// store block states

		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {
					data[dataIndex++] = chunkData.blockStates[x][y][z];
				}
			}
		}


		return data;
	}
}
