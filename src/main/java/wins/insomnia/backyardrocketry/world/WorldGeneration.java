package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;
import wins.insomnia.backyardrocketry.world.chunk.ServerChunk;

import java.nio.ByteBuffer;

public class WorldGeneration {

    public static int SEA_LEVEL = 480;

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




    public static void plantTree(int blockX, int blockY, int blockZ) {




    }



    private static void generateLand(ChunkData chunkData) {

        while (!chunkData.grabThreadOwnership());

        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    int globalBlockX = x + chunkData.getWorldX();
                    int globalBlockY = y + chunkData.getWorldY();
                    int globalBlockZ = z + chunkData.getWorldZ();

                    int groundHeight = WorldGeneration.getGroundHeight(chunkData.getSeed(), globalBlockX, globalBlockZ);
                    int groundHeightDistance = groundHeight - globalBlockY;

                    byte block;
                    byte blockState = 0;



                    if (isBlockWorldBorder(globalBlockX, globalBlockY, globalBlockZ)) {

                        if (globalBlockY == 0) {
                            block = Blocks.BORDERSTONE;
                        } else {
                            block = Blocks.WORLD_BORDER;
                        }


                    } else if (globalBlockY > groundHeight) {

                        if (globalBlockY <= WorldGeneration.SEA_LEVEL) {
                            block = Blocks.WATER;
                        } else {
                            block = Blocks.AIR;
                        }

                    } else {


                        if (carveCaveBlock(chunkData.getSeed(), groundHeightDistance,  globalBlockX, globalBlockY, globalBlockZ)) {
                            block = Blocks.AIR;
                        } else {


                            if (globalBlockY == groundHeight) {
                                block = Blocks.GRASS;
                            } else if (globalBlockY > groundHeight - 6) {



                                if (World.RANDOM.nextFloat(groundHeightDistance) < 2) {
                                    block = Blocks.DIRT;
                                } else {
                                    block = Blocks.LIMESTONE;
                                }

                            } else {

                                if (isOre(chunkData.getSeed(), globalBlockX, globalBlockY, globalBlockZ)) {
                                    block = Blocks.HEMATITE;
                                } else {
                                    block = Blocks.LIMESTONE;
                                }

                            }

                        }
                    }

                    chunkData.setBlock(x, y, z, block);

                    blockState = Blocks.getBlock(block).onPlace(x, y, z, null);

                    chunkData.setBlockState(x, y, z, blockState);


                }
            }
        }

        while (!chunkData.loseThreadOwnership());

    }






    public static float getBlockTint(long seed, int blockX, int blockY, int blockZ) {
        float noiseScale = 0.25f;
        float tint = 0.5f * (OpenSimplex2.noise3_ImproveXZ(seed, blockX * noiseScale, blockY * noiseScale, blockZ * noiseScale) + 1f);
        return 0.9f + tint * 0.2f;
    }

    public static void getWorldPreview(long seed, ByteBuffer textureData, final int SEED_WIDTH, final int SEED_HEIGHT) {

        final Color LAND_COLOR = new Color(90, 197, 79);
        final Color WATER_COLOR = new Color(0, 152, 220);

        for (int iz = 0; iz < SEED_WIDTH; iz++) {
            for (int ix = 0; ix < SEED_HEIGHT; ix++) {

                double[] center = World.getCenterXZ();
                int blockX = (int) center[0] - (SEED_WIDTH / 2) + ix;
                int blockZ = (int) center[1] - (SEED_HEIGHT / 2) + iz;

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
                    color.setRgb(rgb);

                }


                textureData.put(color.getRByte());
                textureData.put(color.getGByte());
                textureData.put(color.getBByte());

                textureData.put((byte) 255);

            }
        }
        textureData.flip();

    }


    public static boolean isOre(long seed, int x, int y, int z) {

        float noiseScale = 0.09f; // smaller number = larger veins

        return (OpenSimplex2.noise3_ImproveXZ(seed,
                x * noiseScale,
                y * noiseScale,
                z * noiseScale
        )) > 0.9;

    }


    public static boolean carveCaveBlock(long seed, int groundHeightDistance, int x, int y, int z) {

        float groundDistanceScale = Math.min(1f, groundHeightDistance / 40f);

        float noiseScale = 0.035f; // smaller number = vaster caves
        float checkValue = 1f - groundDistanceScale * 0.5f; // bigger number, shorter caves



        return (OpenSimplex2.noise3_ImproveXZ(seed,
                x * noiseScale,
                y * noiseScale,
                z * noiseScale
        )) > checkValue;

    }

    public static int getGroundHeight(long seed, int globalBlockX, int globalBlockZ) {
        int noiseAmplitude = 25;
        float noiseScale = 0.0025f;
        return (int) (SEA_LEVEL + noiseAmplitude * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * noiseScale, globalBlockZ * noiseScale) + 1f)) + 16;
    }

    public static int getGroundHeight(int globalBlockX, int globalBlockZ) {
        long seed = GameplayScene.getWorld().getSeed();

        return getGroundHeight(seed, globalBlockX, globalBlockZ);
    }





    // WARNING: called on thread other than main thread
    public static void runChunkGenerationPass(ServerChunk chunk, ChunkData chunkData, ServerChunk.GenerationPass pass, Runnable finishedPassRunnable) {

        switch (pass) {
            case TERRAIN -> generateTerrain(chunk, chunkData, finishedPassRunnable);
            case DECORATION -> {
                try {
                    decorateChunk(chunk, chunkData, finishedPassRunnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // WARNING: called on thread other than main thread!
    private static void generateTerrain(ServerChunk chunk, ChunkData chunkData, Runnable finishedPassRunnable) {

        generateLand(chunkData);
        finishedPassRunnable.run();

    }

    // WARNING: called on thread other than main thread
    private static void decorateChunk(ServerChunk chunk, ChunkData chunkData, Runnable finishedPassRunnable) {


        boolean generateTrees = false;


        ServerWorld serverWorld = ServerWorld.getServerWorld();

        for (int x = 0; x < Chunk.SIZE_X; x++) {

            int blockX = chunk.getX() + x;

            for (int y = 0; y < Chunk.SIZE_Y; y++) {

                int blockY = chunk.getY() + y;

                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    int blockZ = chunk.getZ() + z;

                    int groundHeight = WorldGeneration.getGroundHeight(chunkData.getSeed(), blockX, blockZ);

                    // if block is just above ground
                    if (blockY == groundHeight + 1) {

                        // tree generation
                        if (generateTrees) {
                            byte rootBlock = serverWorld.getBlock(blockX, blockY, blockZ);
                            byte groundBlock = serverWorld.getBlock(blockX, blockY - 1, blockZ);

                            if (rootBlock == Blocks.AIR && (groundBlock == Blocks.DIRT || groundBlock == Blocks.GRASS)) {
                                if (World.RANDOM.nextInt(0, 140) == 0) {
                                    StructureManager.placeDecoration(
                                            StructureManager.DECO_PINE_TREE,
                                            blockX,
                                            blockY,
                                            blockZ,
                                            true
                                    );
                                }
                            }
                        }

                    }



                }
            }
        }


        finishedPassRunnable.run();

    }

}
