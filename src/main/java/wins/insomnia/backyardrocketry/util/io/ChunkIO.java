package wins.insomnia.backyardrocketry.util.io;

import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.ChunkPosition;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChunkIO {


	// cubic size of region (in chunk amount)
	public static final int REGION_SIZE = 20;
	public static final String REGION_FILE_EXTENSION = ".region";
	public static final String TEMP_REGION_FILE_EXTENSION = ".tempregion";
	public static final int BYTES_PER_CHUNK = Long.BYTES +  // seed
			(Integer.BYTES * 3) +                           // chunk position
			Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z +    // block data
			Chunk.SIZE_X * Chunk.SIZE_Y * Chunk.SIZE_Z;     // block state data
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

	public static Path getTempRegionPathFromRegionName(String regionName) {
		return Paths.get(chunksPath + FileIO.FILE_SEPARATOR + regionName + TEMP_REGION_FILE_EXTENSION);
	}

	// returns EITHER x, y, or z of region from EITHER chunk x, y, z
	public static int getRegionXYZ(int chunkXYZ) {
		return chunkXYZ / REGION_SIZE;
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


	public static byte[] readRegionFile(int regionX, int regionY, int regionZ) throws IOException {
		return readRegionFile(regionX, regionY, regionZ, -1);
	}


	public static ChunkData loadChunk(ChunkPosition chunkPosition) {

		int regionX = getRegionXYZ(chunkPosition.getX());
		int regionY = getRegionXYZ(chunkPosition.getY());
		int regionZ = getRegionXYZ(chunkPosition.getZ());

		int chunkDataOffset = getChunkOffsetInRegion(chunkPosition.getX(), chunkPosition.getY(), chunkPosition.getZ());

		byte[] data;

		try {
			data = readRegionFile(regionX, regionY, regionZ, chunkDataOffset);
		} catch (IOException e) {

			e.printStackTrace();
			return null;

		}

		return ChunkData.deserialize(data);
	}




	public static byte[] readRegionFile(int regionX, int regionY, int regionZ, int specificChunkIndex) throws IOException {


		Path regionPath = getRegionPathFromRegionName(
				getRegionNameFromRegion(
						regionX,
						regionY,
						regionZ
				)
		);


		// wait for file to not be busy, then mark it as busy
		while (FileIO.checkAndMarkFileAsBusy(regionPath)) {}



		File file = regionPath.toFile();
		final FileChannel channel = new FileInputStream(file).getChannel();
		MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

		byte[] regionData;

		if (specificChunkIndex > -1) {

			regionData = new byte[BYTES_PER_CHUNK];
			buffer.get(BYTES_PER_CHUNK * specificChunkIndex, regionData);

		} else {

			int byteAmount = BYTES_PER_CHUNK * REGION_SIZE * REGION_SIZE * REGION_SIZE;
			regionData = new byte[byteAmount];
			buffer.get(regionData);

		}


		channel.close();



		// un-mark file as busy

		FileIO.markFileAsUnbusy(regionPath);

		return regionData;
	}

	public static void writeRegionFile(int regionX, int regionY, int regionZ, int specificChunkIndex, byte[] data) throws IOException {

		String regionName = getRegionNameFromRegion(regionX, regionY, regionZ);
		Path regionPath = getRegionPathFromRegionName(regionName);



		if (specificChunkIndex == -1) {

			while (FileIO.checkAndMarkFileAsBusy(regionPath)) {}



			File file = regionPath.toFile();
			final FileChannel channel = new FileOutputStream(file).getChannel();
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());

			buffer.put(data);

			channel.close();



			// un-mark file as busy

			FileIO.markFileAsUnbusy(regionPath);

			return;
		}




		byte[] fileData;

		try {

			fileData = readRegionFile(regionX, regionY, regionZ);

		} catch (Exception e) {

			e.printStackTrace();
			return;
		}


		// wait for file to not be busy, then mark it as busy
		while (FileIO.checkAndMarkFileAsBusy(regionPath)) {}



		File file = regionPath.toFile();
		final FileChannel channel = new FileOutputStream(file).getChannel();
		MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());


		int byteIndex = specificChunkIndex * BYTES_PER_CHUNK;

		int byteAmount = BYTES_PER_CHUNK * REGION_SIZE * REGION_SIZE * REGION_SIZE;

		if (byteIndex > 0) {
			buffer.put(fileData, 0, byteIndex);
		}

		buffer.put(byteIndex, data, 0, BYTES_PER_CHUNK);
		buffer.put(byteIndex + BYTES_PER_CHUNK, fileData, byteIndex + BYTES_PER_CHUNK, fileData.length - (byteIndex + BYTES_PER_CHUNK));


		channel.close();


		FileIO.markFileAsUnbusy(regionPath);

	}



	public static void saveChunk(Chunk chunk, ChunkData chunkData) {

		ChunkPosition chunkPosition = chunk.getChunkPosition();

		try {
			writeRegionFile(
					getRegionXYZ(chunkPosition.getX()),
					getRegionXYZ(chunkPosition.getY()),
					getRegionXYZ(chunkPosition.getZ()),
					getChunkOffsetInRegion(chunkPosition.getX(), chunkPosition.getY(), chunkPosition.getZ()),
					ChunkData.serialize(chunkData)
			);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}




}
