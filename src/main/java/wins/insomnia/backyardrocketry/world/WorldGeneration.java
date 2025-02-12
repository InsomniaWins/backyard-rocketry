package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.scene.GameplayScene;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;

import java.nio.ByteBuffer;

public class WorldGeneration {


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
                if (groundHeight <= 80) {
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
        float noiseScale = 0.0125f;

        return (int) (60 + noiseAmplitude * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * noiseScale, globalBlockZ * noiseScale) + 1f)) + 16;
    }

    public static int getGroundHeight(int globalBlockX, int globalBlockZ) {
        long seed = GameplayScene.getWorld().getSeed();

        return getGroundHeight(seed, globalBlockX, globalBlockZ);
    }


}
