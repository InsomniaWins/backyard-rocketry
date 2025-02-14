package wins.insomnia.backyardrocketry.util.io;

import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.ChunkData;
import wins.insomnia.backyardrocketry.world.ChunkPosition;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChunkIO {


	// cubic size of region (in chunk amount)
	public static final int REGION_SIZE = 20;
	public static final int REGION_SIZE_BLOCKS_X = REGION_SIZE * Chunk.SIZE_X;
	public static final int REGION_SIZE_BLOCKS_Y = REGION_SIZE * Chunk.SIZE_Y;
	public static final int REGION_SIZE_BLOCKS_Z = REGION_SIZE * Chunk.SIZE_Z;
	public static final String REGION_FILE_EXTENSION = ".region";
	public static final int BYTES_PER_CHUNK = Long.BYTES + // seed
			(Integer.BYTES * 3) +                          // chunk position
			Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z;    // block data
	private static Path chunksPath = null;

	public static int limitChunkAmount(int chunkAmount) {
		return Math.max(REGION_SIZE, chunkAmount - (chunkAmount % REGION_SIZE));
	}

	public static void setChunksPath(Path chunksPath) {
		ChunkIO.chunksPath = chunksPath;
	}

	public static Path getChunksPath() {
		return chunksPath;
	}

	public static Path getRegionPathFromRegionName(String regionName) {
		return Paths.get(chunksPath + FileIO.FILE_SEPARATOR + regionName + REGION_FILE_EXTENSION);
	}

	public static String getRegionNameFromRegion(int regionX, int regionY, int regionZ) {

		return "region_" + regionX + '_' + regionY + '_' + regionZ;

	}

	public static String getRegionNameFromChunk(int chunkX, int chunkY, int chunkZ) {


		int regionX = chunkX / REGION_SIZE;
		int regionY = chunkY / REGION_SIZE;
		int regionZ = chunkZ / REGION_SIZE;

		return getRegionNameFromRegion(regionX, regionY, regionZ);

	}

	public static int getChunkOffsetInRegion(int chunkX, int chunkY, int chunkZ) {

		int firstChunkX = (chunkX / REGION_SIZE) * REGION_SIZE;
		int firstChunkY = (chunkY / REGION_SIZE) * REGION_SIZE;
		int firstChunkZ = (chunkZ / REGION_SIZE) * REGION_SIZE;

		int localChunkX = chunkX - firstChunkX;
		int localChunkY = chunkY - firstChunkY;
		int localChunkZ = chunkZ - firstChunkZ;

		return localChunkZ + (localChunkY * REGION_SIZE) + (localChunkX * REGION_SIZE * REGION_SIZE);
	}

	public static ChunkData loadChunk(ChunkPosition chunkPosition) {

		Path regionPath = getRegionPathFromRegionName(
				getRegionNameFromChunk(
					chunkPosition.getX(),
					chunkPosition.getY(),
					chunkPosition.getZ()
				)
		);



		// try opening input stream

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(regionPath.toString());
		} catch (FileNotFoundException e) {
			return null;
		}

		BufferedInputStream bis = new BufferedInputStream(fis);



		// read data

		byte[] chunkData = new byte[BYTES_PER_CHUNK];

		int chunkDataOffset = getChunkOffsetInRegion(chunkPosition.getX(), chunkPosition.getY(), chunkPosition.getZ());

		try {

			if (chunkDataOffset > 0) {
				bis.skipNBytes((long) BYTES_PER_CHUNK * chunkDataOffset);
			}
			int bytesRead = bis.read(chunkData, 0, BYTES_PER_CHUNK);


		} catch (IOException e) {
			throw new RuntimeException(e);
		}



		// close input stream

		try {
			bis.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return ChunkData.deserialize(chunkData);
	}

	private static void createRegionFile(Path regionPath) throws IOException {

		Files.createFile(regionPath);

	}



	public static void saveChunk(ChunkData chunkData) {

		int chunkX = chunkData.getWorldX();
		int chunkY = chunkData.getWorldY();
		int chunkZ = chunkData.getWorldZ();


		String regionName = getRegionNameFromChunk(chunkX, chunkY, chunkZ);
		Path regionPath = Paths.get(chunksPath + FileIO.FILE_SEPARATOR + regionName + REGION_FILE_EXTENSION);

		if (Files.notExists(regionPath)) {
			try {
				createRegionFile(regionPath);
			} catch (IOException e) {
				return;
			}
		}

	}

}
