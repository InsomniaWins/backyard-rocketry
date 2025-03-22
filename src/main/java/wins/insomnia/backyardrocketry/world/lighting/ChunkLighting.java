package wins.insomnia.backyardrocketry.world.lighting;

import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ClientChunk;
import java.util.LinkedList;

public class ChunkLighting {

	private static final LinkedList<LightNode> LIGHT_QUEUE = new LinkedList<>();
	private static final LinkedList<LightRemovalNode> LIGHT_REMOVAL_QUEUE = new LinkedList<>();
	private static final LinkedList<LightNode> SUNLIGHT_QUEUE = new LinkedList<>();

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
		return light & 0b0000_0000_0000_0000_0000_0000_0000_1111;
	}

	// R, G, B, S are values from 0 to 15
	public static short makeLightValue(int r, int g, int b, int s) {
		return (short) (s | b << 4 | g << 8 | r << 12);
	}


	private static void propagateSunlight(Chunk currentChunk, int currentX, int currentY, int currentZ, int dirX, int dirY, int dirZ) {

		short currentLight = currentChunk.getLightValue(currentX, currentY, currentZ);

		// vector holding neighbor blocks local x,y,z relative to neighborChunk
		int[] neighborXYZ = new int[] {currentX + dirX, currentY + dirY, currentZ + dirZ};
		Chunk neighborChunk = currentChunk.getChunkOrNeighborFromLocalBlock(neighborXYZ);



		if (neighborChunk == null) return;



		short neighborLight = neighborChunk.getLightValue(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]);


		int neighborColor = getLightS(neighborLight);
		int currentColor = getLightS(currentLight);

		byte neighborBlock = neighborChunk.getBlock(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]);




		if (Blocks.isBlockTransparent(neighborBlock) && neighborColor + 2 <= currentColor) {


			int nextColor = currentColor - 1;
			if (dirY < 0) {
				if (currentColor == 15) {
					nextColor = 15;
				}
			}

			nextColor = 14;

			neighborLight = makeLightValue(getLightR(neighborLight), getLightG(neighborLight), getLightB(neighborLight), 15);
			System.out.println(getLightS(neighborLight));

			neighborChunk.setLightValue(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2], neighborLight);

			SUNLIGHT_QUEUE.push(new LightNode((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborChunk));
		}

	}




	private static void propagateLight(int colorChannel, Chunk currentChunk, int currentX, int currentY, int currentZ, int dirX, int dirY, int dirZ) {

		short currentLight = currentChunk.getLightValue(currentX, currentY, currentZ);

		// vector holding neighbor blocks local x,y,z relative to neighborChunk
		int[] neighborXYZ = new int[] {currentX + dirX, currentY + dirY, currentZ + dirZ};
		Chunk neighborChunk = currentChunk.getChunkOrNeighborFromLocalBlock(neighborXYZ);



		if (neighborChunk == null) return;



		short neighborLight = neighborChunk.getLightValue(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]);


		int neighborColor = 0;
		int currentColor = 0;
		switch (colorChannel) {
			case 0 -> {
				currentColor = getLightR(currentLight);
				neighborColor = getLightR(neighborLight);
			}
			case 1 -> {
				currentColor = getLightG(currentLight);
				neighborColor = getLightG(neighborLight);
			}
			default -> {
				currentColor = getLightB(currentLight);
				neighborColor = getLightB(neighborLight);
			}
		}

		byte neighborBlock = neighborChunk.getBlock(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]);




		if (Blocks.isBlockTransparent(neighborBlock) && neighborColor + 2 <= currentColor) {

			switch (colorChannel) {
				case 0 -> {
					neighborLight = makeLightValue(currentColor - 1, getLightG(neighborLight), getLightB(neighborLight), getLightS(neighborLight));
				}
				case 1 -> {
					neighborLight = makeLightValue(getLightR(neighborLight), currentColor - 1, getLightB(neighborLight), getLightS(neighborLight));
				}
				default -> {
					neighborLight = makeLightValue(getLightR(neighborLight), getLightG(neighborLight), currentColor - 1, getLightS(neighborLight));
				}
			}
			neighborChunk.setLightValue(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2], neighborLight);

			LIGHT_QUEUE.push(new LightNode((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborChunk));
		}

	}



	private static void propagateLightRemoval(int colorChannel, Chunk currentChunk, short currentLight, int currentX, int currentY, int currentZ, int dirX, int dirY, int dirZ) {


		// vector holding neighbor blocks local x,y,z relative to neighborChunk
		int[] neighborXYZ = new int[] {currentX + dirX, currentY + dirY, currentZ + dirZ};
		Chunk neighborChunk = currentChunk.getChunkOrNeighborFromLocalBlock(neighborXYZ);

		if (neighborChunk == null) return;



		short neighborLight = neighborChunk.getLightValue(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]);

		int neighborColor;
		int currentColor;
		switch (colorChannel) {
			case 0 -> {
				currentColor = getLightR(currentLight);
				neighborColor = getLightR(neighborLight);
			}
			case 1 -> {
				currentColor = getLightG(currentLight);
				neighborColor = getLightG(neighborLight);
			}
			default -> {
				currentColor = getLightB(currentLight);
				neighborColor = getLightB(neighborLight);
			}
		}

		if (neighborColor != 0 && neighborColor < currentColor) {

			neighborChunk.setLightValue(
					neighborXYZ[0], neighborXYZ[1], neighborXYZ[2],
					makeLightValue(
							0,
							0,
							0,
							getLightS(neighborLight)
					)
			);

			LIGHT_REMOVAL_QUEUE.push(new LightRemovalNode((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborLight, neighborChunk));

		} else if (neighborColor >= currentColor) {

			LIGHT_QUEUE.push(new LightNode((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborChunk));

		} else {

			short neighborMinLight = Blocks.getBlockMinimumLightLevel(
					neighborChunk.getBlock(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]),
					neighborChunk.getBlockStateGlobal(neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]),
					neighborXYZ[0], neighborXYZ[1], neighborXYZ[2]
			);

			neighborMinLight = makeLightValue(getLightR(neighborMinLight), getLightG(neighborMinLight), getLightB(neighborMinLight), getLightS(neighborLight));

			if (neighborMinLight != 0) {
				neighborChunk.setLightValue((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborMinLight);
				LIGHT_QUEUE.push(new LightNode((byte) neighborXYZ[0], (byte) neighborXYZ[1], (byte) neighborXYZ[2], neighborChunk));
			}

		}
	}


	private static void flushLightRemovalQueue() {

		while (!LIGHT_REMOVAL_QUEUE.isEmpty()) {

			LightRemovalNode lightNode = LIGHT_REMOVAL_QUEUE.pop();
			Chunk chunk = lightNode.chunk;

			short light = lightNode.lightValue;

			int x = lightNode.x;
			int y = lightNode.y;
			int z = lightNode.z;

			for (int colorChannel = 0; colorChannel < 3; colorChannel++) {
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, -1, 0, 0);
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, 1, 0, 0);
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, 0, -1, 0);
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, 0, 1, 0);
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, 0, 0, -1);
				propagateLightRemoval(colorChannel, chunk, light, x, y, z, 0, 0, 1);
			}



		}

	}

	private static void flushLightQueue() {


		while (!LIGHT_QUEUE.isEmpty()) {

			LightNode lightNode = LIGHT_QUEUE.pop();
			Chunk chunk = lightNode.chunk;

			int x = lightNode.x;
			int y = lightNode.y;
			int z = lightNode.z;

			for (int colorChannel = 0; colorChannel < 3; colorChannel++) {
				propagateLight(colorChannel, chunk, x, y, z, -1, 0, 0);
				propagateLight(colorChannel, chunk, x, y, z, 1, 0, 0);
				propagateLight(colorChannel, chunk, x, y, z, 0, -1, 0);
				propagateLight(colorChannel, chunk, x, y, z, 0, 1, 0);
				propagateLight(colorChannel, chunk, x, y, z, 0, 0, -1);
				propagateLight(colorChannel, chunk, x, y, z, 0, 0, 1);
			}

		}


	}


	private static void flushSunlightQueue() {


		while (!SUNLIGHT_QUEUE.isEmpty()) {

			LightNode lightNode = SUNLIGHT_QUEUE.pop();
			Chunk chunk = lightNode.chunk;

			int x = lightNode.x;
			int y = lightNode.y;
			int z = lightNode.z;

			propagateSunlight(chunk, x, y, z, -1, 0, 0);
			propagateSunlight(chunk, x, y, z, 1, 0, 0);
			propagateSunlight(chunk, x, y, z, 0, -1, 0);
			propagateSunlight(chunk, x, y, z, 0, 1, 0);
			propagateSunlight(chunk, x, y, z, 0, 0, -1);
			propagateSunlight(chunk, x, y, z, 0, 0, 1);

		}


	}






	public static void removeLight(Chunk chunk, int localX, int localY, int localZ) {

		short currentLight = chunk.getLightValue(localX, localY, localZ);

		LIGHT_REMOVAL_QUEUE.push(new LightRemovalNode((byte) localX, (byte) localY, (byte) localZ, currentLight, chunk));

		chunk.setLightValue(localX, localY, localZ, makeLightValue(0, 0, 0, 0));

		flushLightRemovalQueue();
		flushLightQueue();

		if (chunk instanceof ClientChunk clientChunk) {
			clientChunk.updateNeighborChunkMeshes(false, true);
		}

	}


	public static void setLight(Chunk chunk, int localX, int localY, int localZ, short light) {

		if (light == 0) {
			removeLight(chunk, localX, localY, localZ);
			return;
		}

		chunk.setLightValue(localX, localY, localZ, light);

		LIGHT_QUEUE.push(new LightNode((byte) localX, (byte) localY, (byte) localZ, chunk));

		flushLightQueue();

		if (chunk instanceof ClientChunk clientChunk) {
			clientChunk.updateNeighborChunkMeshes(false, true);
		}

	}


	public static void generateSunlight(Chunk chunk) {

		ChunkPosition topChunkPosition = chunk.getChunkPosition().newOffsetChunkPosition(0, -1, 0);
		Chunk topChunk = chunk.getWorld().getChunk(topChunkPosition);

		if (topChunk != null) {
			for (int x = 0; x < Chunk.SIZE_X; x++) {
				for (int y = 0; y < Chunk.SIZE_Y; y++) {
					for (int z = 0; z < Chunk.SIZE_Z; z++) {

						short light = topChunk.getLightValue(x, y, z);
						int sun = getLightS(light);

						if (sun > 0) SUNLIGHT_QUEUE.push(new LightNode((byte) x, (byte) y, (byte) z, topChunk));

					}
				}
			}
		} else {

			ChunkPosition chunkPosition = chunk.getChunkPosition();

			int groundHeight = WorldGeneration.getGroundHeight(chunkPosition.getBlockX(), chunkPosition.getBlockZ());

			boolean aboveGround = groundHeight <= chunkPosition.getBlockY();

			if (aboveGround) {

				for (int x = 0; x < Chunk.SIZE_X; x++) {
					for (int z = 0; z < Chunk.SIZE_Z; z++) {

						byte block = chunk.getBlock(x, Chunk.SIZE_Y, z);

						if (Blocks.isBlockTransparent(block)) {

							short lightValue = chunk.getLightValue(x, Chunk.SIZE_Y, z);

							lightValue = makeLightValue(
									getLightR(lightValue),
									getLightG(lightValue),
									getLightB(lightValue),
									15
							);

							chunk.setLightValue(x, Chunk.SIZE_Y, z, lightValue);

							SUNLIGHT_QUEUE.push(new LightNode((byte) x, (byte) Chunk.SIZE_Y, (byte) z, chunk));

						}

					}
				}

			}

		}

		flushSunlightQueue();

	}



	private record LightingInformation(
			int localX,
			int localY,
			int localZ,
			short light

	) {

	}

}
