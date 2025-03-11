package wins.insomnia.backyardrocketry.world.chunk;

import org.checkerframework.checker.units.qual.A;
import org.joml.Math;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.debug.DebugTime;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class ChunkLighting {

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
	private static void floodFillLight(Chunk parentChunk, int colorChannel, int desiredValue, int localX, int localY, int localZ) {

		// color channels
		// 0 = r,
		// 1 = g,
		// 2 = b,
		// 3 = sky?

		if (desiredValue < 1) return;

		if (!parentChunk.containsLocalBlockPosition(localX, localY, localZ)) return;

		byte block = parentChunk.chunkData.getBlock(
				localX, localY, localZ
		);
		if (!Blocks.isBlockTransparent(block)) return;

		short currentLightValue = parentChunk.getLightValue(localX, localY, localZ);
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

		parentChunk.chunkData.setLightValue(localX, localY, localZ, currentLightValue);




		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX + 1, localY, localZ);
		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX - 1, localY, localZ);
		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX, localY + 1, localZ);
		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX, localY - 1, localZ);
		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX, localY, localZ + 1);
		floodFillLight(parentChunk, colorChannel, desiredValue - 1, localX, localY, localZ - 1);

	}

	// CALL WHILE HAVING OWNERSHIP!
	private static void floodFillLight(Chunk chunk, short lightColor, int x, int y, int z) {

		int r = getLightR(lightColor);
		int g = getLightG(lightColor);
		int b = getLightB(lightColor);

		floodFillLight(chunk, 0, r, x, y, z);
		floodFillLight(chunk, 1, g, x, y, z);
		floodFillLight(chunk, 2, b, x, y, z);

	}

	private static void clearLightData(Chunk chunk) {

		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int y = 0; y < Chunk.SIZE_Y; y++) {
				for (int z = 0; z < Chunk.SIZE_Z; z++) {

					chunk.chunkData.setLightValue(x, y, z, (short) 0);

				}
			}
		}

	}

	public static void updateLighting(Chunk chunk) {

		ChunkData chunkData = chunk.chunkData;


		while (!chunkData.grabThreadOwnership());


		clearLightData(chunk);

		// grab list of lights from neighboring chunks that will spill over to this chunk
		LinkedList<LightingInformation> lightingInformationQueue = new LinkedList<>();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {

					ChunkPosition currentChunkPosition = chunk.getChunkPosition().newOffsetChunkPosition(i - 1, j - 1, k - 1);
					Chunk currentChunk = chunk.getWorld().getChunkAt(currentChunkPosition);

					if (currentChunk == null || currentChunk != chunk) continue;

					// if server chunk, check if chunk is developed enough to make lighting
					if (currentChunk instanceof ServerChunk currentServerChunk) {
						if (!currentServerChunk.hasFinishedPass(ServerChunk.GenerationPass.DECORATION)) {
							continue;
						}
					}

					for (int x = 0; x < Chunk.SIZE_X; x++) {
						for (int y = 0; y < Chunk.SIZE_Y; y++) {
							for (int z = 0; z < Chunk.SIZE_Z; z++) {

								short lightLevel = Blocks.getBlockMinimumLightLevel(currentChunk.getBlock(x, y, z));

								if (lightLevel != 0b0000_0000_0000_0000) {

									lightingInformationQueue.push(new LightingInformation(
											x,
											y,
											z,
											lightLevel
									));
								}
							}
						}
					}


				}
			}
		}

		while (!lightingInformationQueue.isEmpty()) {
			LightingInformation lightingInformation = lightingInformationQueue.pop();

			floodFillLight(
					chunk,
					lightingInformation.light(),
					lightingInformation.localX,
					lightingInformation.localY,
					lightingInformation.localZ
			);

		}

		while (!chunkData.loseThreadOwnership());


	}

	private record LightingInformation(
			int localX,
			int localY,
			int localZ,
			short light

	) {

	}

}
