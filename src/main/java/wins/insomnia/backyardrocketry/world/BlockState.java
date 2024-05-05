package wins.insomnia.backyardrocketry.world;

import org.joml.Math;
import org.joml.Vector3i;

import java.util.Objects;

public class BlockState {

    private final Chunk CHUNK;
    private int block = -1;
    private final int X;
    private final int Y;
    private final int Z;
    private final int POSITION_HASH;
    private float health = 1f;
    private IBlockProperties blockProperties = null;

    public BlockState(Chunk chunk, int block, int x, int y, int z) {
        CHUNK = chunk;
        X = x;
        Y = y;
        Z = z;
        POSITION_HASH = Objects.hash(getWorldX(), getWorldY(), getWorldZ());
        setBlock(block);
    }

    public boolean hasBlockProperties() {
        return blockProperties != null;
    }

    public IBlockProperties getBlockProperties() {
        return blockProperties;
    }

    public void randomUpdate() {

        if (blockProperties == null) return;

        blockProperties.randomUpdate(CHUNK, X, Y, Z);
    }

    public void update() {

        if (blockProperties == null) return;

        blockProperties.update(CHUNK, X, Y, Z);

    }

    public void damage(float damageAmount) {
        if (damageAmount <= 0) {
            throw new RuntimeException("Tried to damage a block with a negative break amount!");
        }

        health -= damageAmount;
        health = Math.max(health, 0f);

        if (health == 0f) {
            breakBlock();
            health = 1f;
        }
    }

    public void breakBlock() {
        setBlock(Block.AIR);
    }

    public float getHealth() {
        return health;
    }

    public float getBreakProgress() {
        return 1f - health;
    }

    public void setBlock(int newBlock) {
        setBlock(newBlock, true);
    }

    public void setBlock(int newBlock, boolean regenerateChunkMesh) {

        int oldBlock = block;
        if (oldBlock > -1 && blockProperties != null) {
            blockProperties.onBreak(CHUNK, X, Y, Z);
        }

        block = newBlock;
        blockProperties = Block.createBlockProperties(block);

        if (block > -1 && blockProperties != null) {
            blockProperties.onPlace(CHUNK, X, Y, Z);
        }

        if (regenerateChunkMesh) {


            CHUNK.setShouldRegenerateMesh(true);

            if (CHUNK.isBlockOnChunkBorder(X, Y, Z)) {

                for (Chunk chunk : CHUNK.getNeighborChunks()) {
                    if (chunk == null) continue;
                    chunk.shouldRegenerateMesh = true;
                }

            }

        }
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }

    public int getPositionHash() {
        return POSITION_HASH;
    }

    public int getWorldX() {
        return CHUNK.getX() + X;
    }

    public int getWorldY() {
        return CHUNK.getY() + Y;
    }

    public int getWorldZ() {
        return CHUNK.getZ() + Z;
    }

    public Vector3i getPosition() {
        return new Vector3i(X, Y, Z);
    }

    public int getBlock() {
        return block;
    }

    public String getStateString() {

        if (blockProperties != null) {
            return blockProperties.getStateString(CHUNK, X, Y, Z);
        }

        return "default";
    }

}
