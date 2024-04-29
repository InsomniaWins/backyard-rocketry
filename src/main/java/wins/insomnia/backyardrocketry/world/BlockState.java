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
        setBlock(newBlock, true);
    }


    /*
    public RayCastResult collisionRayCast(BlockState blockState, World world, int blockX, int blockY, int blockZ, Vector3d start, Vector3d end) {
        return rayCast(blockX, blockY, blockZ, start, end, Block.getBlockCollision(blockState.getBlock()));
    }

    public RayCastResult rayCast(int blockX, int blockY, int blockZ, Vector3d start, Vector3d end, BoundingBox boundingBox) {

        if (boundingBox == null) {
            return null;
        }

        Vector3d vec3d = start.sub(blockX, blockY, blockZ);
        Vector3d vec3d1 = end.sub(blockX, blockY, blockZ);

        RayCastResult rayCastResult = boundingBox.calculateIntercept(vec3d, vec3d1);
        return rayCastResult == null ? null : new RayCastResult(raytraceresult.hitVec.add(blockX, blockY, blockZ), rayCastResult.sideHit, blockX, blockY, blockZ);
    }
     */


    public void setBlock(int newBlock, boolean regenerateChunkMesh) {

        int oldBlock = block;
        if (oldBlock > -1 && blockProperties != null) {
            blockProperties.onBreak(CHUNK, x, y, z);
        }

        block = newBlock;
        blockProperties = Block.createBlockProperties(block);

        if (block > -1 && blockProperties != null) {
            blockProperties.onPlace(CHUNK, x, y, z);
        }

        if (regenerateChunkMesh) {


            CHUNK.setShouldRegenerateMesh(true);

            if (CHUNK.isBlockOnChunkBorder(x, y, z)) {

                for (Chunk chunk : CHUNK.getNeighborChunks()) {
                    if (chunk == null) continue;
                    chunk.shouldRegenerateMesh = true;
                }

            }

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

    public String getStateString() {

        if (blockProperties != null) {
            return blockProperties.getStateString(CHUNK, x, y, z);
        }

        return "default";
    }

}
