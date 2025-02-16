package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.world.chunk.Chunk;

public interface IBlockProperties {
    void update(Chunk chunk, int x, int y, int z);
    void randomUpdate(Chunk chunk, int x, int y, int z);
    void onBreak(Chunk chunk, int x, int y, int z);
    void onPlace(Chunk chunk, int x, int y, int z);

    String getStateString(Chunk chunk, int x, int y, int z);
}
