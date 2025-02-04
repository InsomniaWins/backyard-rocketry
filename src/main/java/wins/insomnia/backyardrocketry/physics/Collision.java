package wins.insomnia.backyardrocketry.physics;

import org.joml.Math;
import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Collision {

    public enum AABBCollisionResultType {

        OUTSIDE,
        INSIDE,
        CLIPPING,
        CONTAINS

    }

    public static boolean isBlockInWorldBorder(int x, int y, int z) {

        return World.getServerWorld().isBlockInWorldBorder(x, y, z);
    }

    public static final List<WeakReference<ICollisionBody>> COLLISION_BODIES = new ArrayList<>();

    public static List<Chunk> getChunksTouchingBoundingBox(World world, BoundingBox boundingBox, boolean includeUnloadedChunks) {

        List<Chunk> chunks = new ArrayList<>();


        // get min chunk position, and get range for chunk loops

        ChunkPosition currentChunkPosition = world.getChunkPositionFromBlockPositionClamped(
                (int) boundingBox.getMax().x,
                (int) boundingBox.getMax().y,
                (int) boundingBox.getMax().z
        );

        int xRange = currentChunkPosition.getX();
        int yRange = currentChunkPosition.getY();
        int zRange = currentChunkPosition.getZ();

        currentChunkPosition = world.getChunkPositionFromBlockPositionClamped(
                (int) boundingBox.getMin().x,
                (int) boundingBox.getMin().y,
                (int) boundingBox.getMin().z
        );

        int minChunkX = currentChunkPosition.getX();
        int minChunkY = currentChunkPosition.getY();
        int minChunkZ = currentChunkPosition.getZ();

        xRange -= currentChunkPosition.getX();
        yRange -= currentChunkPosition.getY();
        zRange -= currentChunkPosition.getZ();


        // loop through chunks to find loaded chunks colliding
        for (int x = 0; x <= xRange; x++) {
            for (int y = 0; y <= yRange; y++) {
                for (int z = 0; z <= zRange; z++) {

                    currentChunkPosition.set(minChunkX + x, minChunkY + y, minChunkZ + z);

                    Chunk chunk = world.getChunkAt(currentChunkPosition);

                    if (!includeUnloadedChunks) {

                        if (chunk == null) {
                            continue;
                        }

                    } else {

                        // if we are including null chunks,
                        // check to see if chunk is in world border
                        // if it's not, then it will never exist, so continue and dont add null to list
                        if (!World.getServerWorld().isChunkPositionInWorldBorder(currentChunkPosition)) {

                            continue;
                        }

                    }



                    chunks.add(chunk);

                }
            }
        }


        return chunks;
    }

    public static List<Chunk> getChunksTouchingBoundingBox(World world, BoundingBox boundingBox) {
        return getChunksTouchingBoundingBox(world, boundingBox, false);
    }


    private static BlockRaycastResult blockCollisionCheck(int blockX, int blockY, int blockZ, Block.Face face) {

        World world = World.getServerWorld();
        Chunk chunk = world.getChunkContainingBlock(blockX, blockY, blockZ);

        if (chunk == null) return null;

        byte blockId = world.getBlock(blockX, blockY, blockZ);

        if (Block.getBlockCollision(blockId) == null) return null;

        return new BlockRaycastResult(chunk, blockX, blockY, blockZ, face);
    }



    // TODO: block raycast is inaccurate when origin position is integer (0.00, 12.00, 1.00, etc.)
    // -------------------  THANK YOU { Kevin Reid } !!!!!!  ----------------------------------
    //
    //  ->   https://gamedev.stackexchange.com/users/9825/kevin-reid
    //  ->   https://gamedev.stackexchange.com/questions/47362/cast-ray-to-select-block-in-voxel-game

    public static BlockRaycastResult blockRaycast(Vector3d origin, Vector3d direction, double length) {

        // Cube containing origin point.
        int x = (int) Math.floor(origin.x);
        int y = (int) Math.floor(origin.y);
        int z = (int) Math.floor(origin.z);

        // Break out direction vector.
        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;

        // Direction to increment x,y,z when stepping.
        int stepX = (int) Math.signum(dx);
        int stepY = (int) Math.signum(dy);
        int stepZ = (int) Math.signum(dz);

        // part of the origin.
        double tMaxX = intbound(origin.x, dx);
        double tMaxY = intbound(origin.y, dy);
        double tMaxZ = intbound(origin.z, dz);

        // The change in t when taking a step (always positive).
        double tDeltaX = stepX/dx;
        double tDeltaY = stepY/dy;
        double tDeltaZ = stepZ/dz;

        // Buffer for reporting faces to the callback.
        Block.Face face = Block.Face.NULL;

        // Avoids an infinite loop.
        if (dx == 0 && dy == 0 && dz == 0)
            throw new RuntimeException("Raycast in zero direction!");

        // Rescale from units of 1 cube-edge to units of 'direction' so we can
        // compare with 't'.
        length /= Math.sqrt(dx*dx+dy*dy+dz*dz);

        World world = World.getServerWorld();

        while (/* ray has not gone past bounds of world */
                (stepX > 0 ? x < world.getSizeX() : x >= 0) &&
                        (stepY > 0 ? y < world.getSizeY() : y >= 0) &&
                        (stepZ > 0 ? z < world.getSizeZ() : z >= 0)) {

            // Invoke the callback, unless we are not *yet* within the bounds of the
            // world.
            if (!(x < 0 || y < 0 || z < 0 || x >= world.getSizeX() || y >= world.getSizeY() || z >= world.getSizeZ())) {

                BlockRaycastResult result = blockCollisionCheck(x, y, z, face);

                if (result != null) {
                    return result;
                }
            }

            // tMaxX stores the t-value at which we cross a cube boundary along the
            // X axis, and similarly for Y and Z. Therefore, choosing the least tMax
            // chooses the closest cube boundary. Only the first case of the four
            // has been commented in detail.
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    if (tMaxX > length) break;
                    // Update which cube we are now in.
                    x += stepX;
                    // Adjust tMaxX to the next X-oriented boundary crossing.
                    tMaxX += tDeltaX;
                    // Record the normal vector of the cube face we entered.
                    face = stepX > 0 ? Block.Face.NEG_X : Block.Face.POS_X;
                } else {
                    if (tMaxZ > length) break;
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    face = stepZ > 0 ? Block.Face.NEG_Z : Block.Face.POS_Z;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    if (tMaxY > length) break;
                    y += stepY;
                    tMaxY += tDeltaY;
                    face = stepY > 0 ? Block.Face.NEG_Y : Block.Face.POS_Y;
                } else {
                    // Identical to the second case, repeated for simplicity in
                    // the conditionals.
                    if (tMaxZ > length) break;
                    z += stepZ;
                    tMaxZ += tDeltaZ;
                    face = stepZ > 0 ? Block.Face.NEG_Z : Block.Face.POS_Z;
                }
            }
        }

        return null;
    }

    static double intbound(double s, double ds) {
        // Find the smallest positive t such that s+t*ds is an integer.
        if (ds < 0) {
            return intbound(-s, -ds);
        } else {
            s = mod(s, 1);
            // problem is now s+t*ds = 1
            return (1-s)/ds;
        }
    }

    static double mod(double value, double modulus) {
        return (value % modulus + modulus) % modulus;
    }


    // -----------------------------------------------------

}
