package wins.insomnia.backyardrocketry.physics;

import wins.insomnia.backyardrocketry.world.chunk.Chunk;
import wins.insomnia.backyardrocketry.world.block.Blocks;

public class BlockRaycastResult {

    private Chunk chunk;
    private int blockX;
    private int blockY;
    private int blockZ;
    private Blocks.Face face;


    public BlockRaycastResult(Chunk chunk, int blockX, int blockY, int blockZ, Blocks.Face face) {
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
    public Blocks.Face getFace() {
        return face;
    }

    public boolean equals(BlockRaycastResult otherBlockRaycastResult, boolean includeFace) {
        if (getBlockX() != otherBlockRaycastResult.getBlockX()) return false;
        if (getBlockY() != otherBlockRaycastResult.getBlockY()) return false;
        if (getBlockZ() != otherBlockRaycastResult.getBlockZ()) return false;
        if (getChunk() != otherBlockRaycastResult.getChunk()) return false;

        if (includeFace) {
            if (getFace() != otherBlockRaycastResult.getFace()) return false;
        }

        return true;
    }

    public boolean equals(BlockRaycastResult otherBlockRaycastResult) {
        return equals(otherBlockRaycastResult, true);
    }

    public byte getBlock() {
        return chunk.getBlock(
                chunk.toLocalX(blockX),
                chunk.toLocalY(blockY),
                chunk.toLocalZ(blockZ)
        );
    }

    public short getLightValue() {
        return chunk.getLightValue(
                chunk.toLocalX(blockX),
                chunk.toLocalY(blockY),
                chunk.toLocalZ(blockZ)
        );
    }
}
