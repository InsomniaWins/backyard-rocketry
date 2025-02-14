package wins.insomnia.backyardrocketry.world;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;

public class ChunkData {

	private final long SEED;
	private final int X;
	private final int Y;
	private final int Z;

	private byte[][][] blocks;


	private ChunkData(long seed, int chunkX, int chunkY, int chunkZ, boolean shouldGenerate) {

		SEED = seed;

		X = ChunkPosition.getBlockX(chunkX);
		Y = ChunkPosition.getBlockY(chunkY);
		Z = ChunkPosition.getBlockZ(chunkZ);

		initializeBlocks();

		if (shouldGenerate) {
			WorldGeneration.generateChunkData(this);
		}

	}

	public ChunkData(long seed, int chunkX, int chunkY, int chunkZ) {
		this(seed, chunkX, chunkY, chunkZ, true);
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

		// read x y and z

		int chunkX, chunkY, chunkZ;
		buffer = new byte[Ints.BYTES];

		for (int i = 0; i < Integer.BYTES; i++) {
			buffer[i] = data[dataIndex++];
		}
		chunkX = Ints.fromByteArray(buffer);

		for (int i = 0; i < Integer.BYTES; i++) {
			buffer[i] = data[dataIndex++];
		}
		chunkY = Ints.fromByteArray(buffer);

		for (int i = 0; i < Integer.BYTES; i++) {
			buffer[i] = data[dataIndex++];
		}
		chunkZ = Ints.fromByteArray(buffer);


		ChunkData chunkData = new ChunkData(seed, chunkX, chunkY, chunkZ, false);


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
