package wins.insomnia.backyardrocketry.world.blockproperty;

import org.joml.Random;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.world.*;

public class BlockGrassProperties implements IBlockProperties {

    String stateString = "default";

    @Override
    public void update(Chunk chunk, int x, int y, int z) {

        BlockState blockState = chunk.getBlockState(x, y, z);

        if (blockState == null) {
            return;
        }

        int worldBlockX = chunk.toGlobalX(x);
        int worldBlockY = chunk.toGlobalY(y);
        int worldBlockZ = chunk.toGlobalZ(z);
        BlockState blockStateAbove = World.get().getBlockState(worldBlockX, worldBlockY + 1, worldBlockZ);

        if (blockStateAbove == null) {
            return;
        }

        boolean airIsAbove = blockStateAbove.getBlock() == Block.AIR;

        if (!airIsAbove && blockState.getBlock() != Block.DIRT) {



            blockState.setBlock(Block.DIRT);
        }

    }

    @Override
    public void onBreak(Chunk chunk, int x, int y, int z) {

    }

    @Override
    public void onPlace(Chunk chunk, int x, int y, int z) {
        //stateString = "deep";
    }

    @Override
    public String getStateString(Chunk chunk, int x, int y, int z) {
        return stateString;
    }
}
