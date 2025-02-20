package wins.insomnia.backyardrocketry.world.chunk;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;

public class ChunkData {

	private final long SEED;
	private final int X;
	private final int Y;
	private final int Z;
	private byte[][][] blocks;
	private BlockStateManager[][][] blockStatePalette;


	private ChunkData(long seed, int x, int y, int z, boolean shouldGenerate, boolean areWorldCoordinates) {

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

	private ChunkData(long seed, int chunkX, int chunkY, int chunkZ, boolean shouldGenerate) {

		this(seed, chunkX, chunkY, chunkZ, shouldGenerate, false);

	}

	public ChunkData(long seed, int chunkX, int chunkY, int chunkZ) {
		this(seed, chunkX, chunkY, chunkZ, true);
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

	}

	public void setBlock(int x, int y, int z, byte block) {
		blocks[x][y][z] = block;
	}

	public byte getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}

	public byte[][][] getBlocks() {
		return blocks;
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


		return data;
	}
}
