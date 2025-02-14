package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.nio.ByteBuffer;

public class WorldGeneration {

    public static int SEA_LEVEL = 80;

    public static ChunkData generateChunkData(ChunkData chunkData) {

        generateLand(chunkData);

        return chunkData;
    }


    public static boolean isBlockWorldBorder(int blockX, int blockY, int blockZ) {

        if (blockX == 0 || blockX == World.CHUNK_AMOUNT_X * Chunk.SIZE_X - 1) {
            return true;
        }

        if (blockY == 0 || blockY == World.CHUNK_AMOUNT_Y * Chunk.SIZE_Y - 1) {
            return true;
        }

		return blockZ == 0 || blockZ == World.CHUNK_AMOUNT_Z * Chunk.SIZE_Z - 1;
	}

    private static void generateLand(ChunkData chunkData) {

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    int globalBlockX = x + chunkData.getWorldX();
                    int globalBlockY = y + chunkData.getWorldY();
                    int globalBlockZ = z + chunkData.getWorldZ();

                    int groundHeight = WorldGeneration.getGroundHeight(chunkData.getSeed(), globalBlockX, globalBlockZ);

                    byte block;


                    if (isBlockWorldBorder(globalBlockX, globalBlockY, globalBlockZ)) {

                        block = Block.WORLD_BORDER;

                    } else if (globalBlockY > groundHeight) {

                        if (globalBlockY <= WorldGeneration.SEA_LEVEL) {
                            block = Block.WATER;
                        } else {
                            block = Block.AIR;
                        }

                    } else {

                        if (globalBlockY == groundHeight) {
                            block = Block.GRASS;
                        } else if (globalBlockY > groundHeight - 4) {
                            block = Block.DIRT;
                        } else {
                            if (World.RANDOM.nextInt(2) == 0) {
                                block = Block.COBBLESTONE;
                            } else {
                                block = Block.STONE;
                            }
                        }
                    }


                    chunkData.setBlock(x, y, z, block);

                }
            }
        }

    }






    public static float getBlockTint(long seed, int blockX, int blockY, int blockZ) {
        float noiseScale = 0.25f;
        float tint = 0.5f * (OpenSimplex2.noise3_ImproveXZ(seed, blockX * noiseScale, blockY * noiseScale, blockZ * noiseScale) + 1f);
        return 0.9f + tint * 0.2f;
    }

    public static void getWorldPreview(long seed, ByteBuffer textureData) {

        final int SEED_WIDTH = World.CHUNK_AMOUNT_X * Chunk.SIZE_X;
        final int SEED_HEIGHT = World.CHUNK_AMOUNT_Z * Chunk.SIZE_Z;

        if (textureData.capacity() / 4 != SEED_WIDTH * SEED_HEIGHT) {
            return;
        }

        final Color LAND_COLOR = new Color(90, 197, 79);
        final Color WATER_COLOR = new Color(0, 152, 220);

        for (int blockZ = 0; blockZ < SEED_WIDTH; blockZ++) {
            for (int blockX = 0; blockX < SEED_HEIGHT; blockX++) {

                int groundHeight = WorldGeneration.getGroundHeight(seed, blockX, blockZ);

                Color color = LAND_COLOR;
                if (groundHeight <= SEA_LEVEL) {
                    color = WATER_COLOR;
                }

                if (blockX % Chunk.SIZE_X == 0 || blockZ % Chunk.SIZE_Z == 0) {

                    color = new Color(color);
                    Vector3f rgb = color.getRGB();
                    rgb.x *= 0.9f;
                    rgb.y *= 0.9f;
                    rgb.z *= 0.9f;
                    color.setRGB(rgb);

                }

                textureData.put(color.getRByte());
                textureData.put(color.getGByte());
                textureData.put(color.getBByte());

                textureData.put((byte) 255);

            }
        }
        textureData.flip();

    }

    public static int getGroundHeight(long seed, int globalBlockX, int globalBlockZ) {
        int noiseAmplitude = 6;
        float noiseScale = 0.0025f;
        return (int) (60 + noiseAmplitude * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * noiseScale, globalBlockZ * noiseScale) + 1f)) + 16;
    }

    public static int getGroundHeight(int globalBlockX, int globalBlockZ) {
        long seed = GameplayScene.getWorld().getSeed();

        return getGroundHeight(seed, globalBlockX, globalBlockZ);
    }


}
