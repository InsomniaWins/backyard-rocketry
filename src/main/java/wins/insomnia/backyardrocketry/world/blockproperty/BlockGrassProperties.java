package wins.insomnia.backyardrocketry.world.blockproperty;

import org.joml.Random;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.IBlockProperties;
import wins.insomnia.backyardrocketry.world.World;

public class BlockGrassProperties implements IBlockProperties {

    String stateString = "default";

    @Override
    public void update(Chunk chunk, int x, int y, int z) {

    }

    @Override
    public void onBreak(Chunk chunk, int x, int y, int z) {

    }

    @Override
    public void onPlace(Chunk chunk, int x, int y, int z) {
        if (World.RANDOM.nextDouble() < 0.5) {
            stateString = "deep";
        }
    }

    @Override
    public String getStateString(Chunk chunk, int x, int y, int z) {
        return stateString;
    }
}
