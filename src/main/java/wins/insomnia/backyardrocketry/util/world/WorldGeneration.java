package wins.insomnia.backyardrocketry.util.world;

import wins.insomnia.backyardrocketry.scenes.GameplayScene;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;

public class WorldGeneration {


    public static int getGroundHeight(int globalBlockX, int globalBlockZ) {
        long seed = GameplayScene.getWorld().getSeed();

        int noiseAmplitude = 6;
        float noiseScale = 0.0125f;

        return (int) (60 + noiseAmplitude * (OpenSimplex2.noise2_ImproveX(seed, globalBlockX * noiseScale, globalBlockZ * noiseScale) + 1f)) + 16;
    }


}
