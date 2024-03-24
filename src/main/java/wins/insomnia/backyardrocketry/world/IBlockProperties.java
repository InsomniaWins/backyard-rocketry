package wins.insomnia.backyardrocketry.world;

public interface IBlockProperties {
    void update(Chunk chunk, int x, int y, int z);
    void onBreak(Chunk chunk, int x, int y, int z);
    void onPlace(Chunk chunk, int x, int y, int z);
}
