package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3i;

public class BlockState {

    private final Chunk CHUNK;
    private int block = -1;
    private int x;
    private int y;
    private int z;
    private IBlockProperties blockProperties = null;

    public BlockState(Chunk chunk, int block, int x, int y, int z) {
        this.CHUNK = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        setBlock(block);
    }

    public void update() {
        if (blockProperties != null) {

            blockProperties.update(CHUNK, x, y, z);

        }
    }

    public void setBlock(int newBlock) {

        int oldBlock = block;
        if (oldBlock > -1 && blockProperties != null) {
            blockProperties.onBreak(CHUNK, x, y, z);
        }

        block = newBlock;
        blockProperties = Block.createBlockProperties(block);

        if (block > -1 && blockProperties != null) {
            blockProperties.onPlace(CHUNK, x, y, z);
        }

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vector3i getPosition() {
        return new Vector3i(x, y, z);
    }

    public int getBlock() {
        return block;
    }

}
