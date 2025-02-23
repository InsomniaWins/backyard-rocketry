package wins.insomnia.backyardrocketry.world;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import org.joml.Math;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.util.io.FileIO;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static wins.insomnia.backyardrocketry.util.io.FileIO.FILE_SEPARATOR;

public class StructureManager {

	private static final HashMap<Integer, Decoration> DECORATION_MAP = new HashMap<>();

	public static final int DECO_PINE_TREE = registerDecoration("pine_tree");

	private static int nextAvailableDecorationId = 0;


	public static int registerDecoration(String name) {
		int id = nextAvailableDecorationId++;

		int sizeX;
		int sizeY;
		int sizeZ;
		short[][][] blocks;
		int originX;
		int originY;
		int originZ;

		Path path = Paths.get(FileIO.getRootPath() + FILE_SEPARATOR + name + ".deco");
		try {
			FileInputStream fis = new FileInputStream(new File(path.toUri()));

			byte[] buffer = new byte[Ints.BYTES];

			fis.read(buffer);
			int x1 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			int y1 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			int z1 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			int x2 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			int y2 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			int z2 = Ints.fromByteArray(buffer);

			fis.read(buffer);
			originX = Ints.fromByteArray(buffer);

			fis.read(buffer);
			originY = Ints.fromByteArray(buffer);

			fis.read(buffer);
			originZ = Ints.fromByteArray(buffer);

			sizeX = x2 - x1 + 1;
			sizeY = y2 - y1 + 1;
			sizeZ = z2 - z1 + 1;

			blocks = new short[sizeX][sizeY][sizeZ];

			for (int x = x1, xi = 0;     x < x2 + 1;     x++, xi++) {
				for (int y = y1, yi = 0;     y < y2 + 1;     y++, yi++) {
					for (int z = z1, zi = 0;     z < z2 + 1;     z++, zi++) {

						byte[] block = new byte[2];
						fis.read(block);

						blocks[xi][yi][zi] = Shorts.fromByteArray(block);

					}
				}
			}

			fis.close();


		} catch (IOException e) {
			throw new RuntimeException(e);
		}


		Decoration decoration = new Decoration(sizeX, sizeY, sizeZ, blocks, originX, originY, originZ);
		DECORATION_MAP.put(id, decoration);


		return id;
	}


	public static Decoration getDecoration(int decorationId) {
		return DECORATION_MAP.get(decorationId);
	}


	public static void placeDecoration(int decorationId, int x, int y, int z, boolean ignoreAir) {

		Decoration decoration = getDecoration(decorationId);

		if (decoration == null) {
			System.err.println("Could not load decoration: " + decorationId + "!");
			return;
		}

		for (int xi = 0; xi < decoration.getSizeX(); xi++) {
			for (int yi = 0; yi < decoration.getSizeY(); yi++) {
				for (int zi = 0; zi < decoration.getSizeZ(); zi++) {

					byte[] blockData = Shorts.toByteArray(decoration.getBlocks()[xi][yi][zi]);

					if (blockData[0] == Blocks.AIR && ignoreAir) continue;

					ServerWorld.getServerWorld().setBlock(
							x + xi - decoration.getOriginX(),
							y + yi - decoration.getOriginY(),
							z + zi - decoration.getOriginZ(),
							blockData[0],
							blockData[1]
					);

				}
			}
		}

	}

	public static void placeDecoration(int decorationId, int x, int y, int z) {
		placeDecoration(decorationId, x, y, z, true);
	}

	public static void saveDecoration(String name, Vector3i point1, Vector3i point2, Vector3i origin) {

		int x1 = Math.min(point1.x, point2.x);
		int x2 = x1 == point1.x ? point2.x : point1.x;

		int y1 = Math.min(point1.y, point2.y);
		int y2 = y1 == point1.y ? point2.y : point1.y;

		int z1 = Math.min(point1.z, point2.z);
		int z2 = z1 == point1.z ? point2.z : point1.z;


		ServerWorld serverWorld = ServerWorld.getServerWorld();

		if (serverWorld == null) return;

		Path path = Paths.get(FileIO.getRootPath() + FILE_SEPARATOR + name + ".deco");

		try {
			Files.createFile(path);

			FileOutputStream fos = new FileOutputStream(new File(path.toUri()));

			fos.write(Ints.toByteArray(x1));
			fos.write(Ints.toByteArray(y1));
			fos.write(Ints.toByteArray(z1));

			fos.write(Ints.toByteArray(x2));
			fos.write(Ints.toByteArray(y2));
			fos.write(Ints.toByteArray(z2));

			fos.write(Ints.toByteArray(x2 - origin.x));
			fos.write(Ints.toByteArray(y2 - origin.y));
			fos.write(Ints.toByteArray(z2 - origin.z));

			for (int x = x1; x < x2 + 1; x++) {
				for (int y = y1; y < y2 + 1; y++) {
					for (int z = z1; z < z2 + 1; z++) {

						byte block = ServerWorld.getServerWorld().getBlock(x, y, z);
						byte blockState = ServerWorld.getServerWorld().getBlockState(x, y, z);

						fos.write(block);
						fos.write(blockState);

					}
				}
			}

			fos.close();


		} catch (IOException e) {
			throw new RuntimeException(e);
		}










	}


	public static class Decoration {

		private final int SIZE_X;
		private final int SIZE_Y;
		private final int SIZE_Z;
		private final int ORIGIN_X;
		private final int ORIGIN_Y;
		private final int ORIGIN_Z;
		private final short[][][] BLOCKS;

		public Decoration(int sizeX, int sizeY, int sizeZ, short[][][] blocks, int originX, int originY, int originZ) {
			SIZE_X = sizeX;
			SIZE_Y = sizeY;
			SIZE_Z = sizeZ;
			BLOCKS = blocks;
			ORIGIN_X = originX;
			ORIGIN_Y = originY;
			ORIGIN_Z = originZ;
		}

		public int getSizeX() {
			return SIZE_X;
		}

		public int getSizeY() {
			return SIZE_Y;
		}

		public int getSizeZ() {
			return SIZE_Z;
		}

		public int getOriginX() {
			return ORIGIN_X;
		}
		public int getOriginY() {
			return ORIGIN_Y;
		}
		public int getOriginZ() {
			return ORIGIN_Z;
		}

		public short[][][] getBlocks() {
			return BLOCKS;
		}

	}
}
