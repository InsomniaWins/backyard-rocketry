package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.world.Block;
import wins.insomnia.backyardrocketry.world.Chunk;

public class BlockRaycastResult {

    private Chunk chunk;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Block.Face face;


    public BlockRaycastResult(Chunk chunk, int blockX, int blockY, int blockZ, Block.Face face) {
        this.chunk = chunk;

        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;

        this.face = face;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }
    public Block.Face getFace() {
        return face;
    }

}