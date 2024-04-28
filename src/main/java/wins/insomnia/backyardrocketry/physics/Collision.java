package wins.insomnia.backyardrocketry.physics;

import org.joml.Math;
import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.world.Block;
import wins.insomnia.backyardrocketry.world.BlockState;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;

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

    public static final List<WeakReference<ICollisionBody>> COLLISION_BODIES = new ArrayList<>();

    public static List<ICollisionBody> getBodiesTouchingBoundingBox(BoundingBox boundingBox) {

        List<ICollisionBody> bodies = new ArrayList<>();

        for (WeakReference bodyReference : COLLISION_BODIES) {

            ICollisionBody body = (ICollisionBody) bodyReference.get();

            AABBCollisionResultType result = body.getBoundingBox().collideWithBoundingBox(boundingBox);

            if (result != AABBCollisionResultType.OUTSIDE) {

                bodies.add(body);

            }

        }

        return bodies;
    }

    public static List<Chunk> getChunksTouchingBoundingBox(BoundingBox boundingBox) {

        World world = BackyardRocketry.getInstance().getPlayer().getWorld();

        List<Chunk> chunks = new ArrayList<>();
        for (Chunk chunk : world.getChunks()) {

            AABBCollisionResultType collisionResult = boundingBox.collideWithBoundingBox(chunk.getBoundingBox());

            if (collisionResult != AABBCollisionResultType.OUTSIDE) {



                chunks.add(chunk);
            }

        }

        return chunks;

    }


    // -------------------  THANK YOU { Kevin Reid } !!!!!!  ----------------------------------
    //
    //  ->   https://gamedev.stackexchange.com/users/9825/kevin-reid
    //  ->   https://gamedev.stackexchange.com/questions/47362/cast-ray-to-select-block-in-voxel-game

    private static BlockRaycastResult blockCollisionCheck(int blockX, int blockY, int blockZ, Block.Face face) {

        World world = BackyardRocketry.getInstance().getPlayer().getWorld();
        Chunk chunk = world.getChunkContainingBlock(blockX, blockY, blockZ);

        if (chunk == null) return null;

        BlockState blockState = world.getBlockState(blockX, blockY, blockZ);

        if (blockState == null) return null;

        if (Block.getBlockCollision(blockState.getBlock()) == null) return null;

        return new BlockRaycastResult(chunk, blockX, blockY, blockZ, face);
    }

    /**
     * Call the callback with (x,y,z,value,face) of all blocks along the line
     * segment from point 'origin' in vector direction 'direction' of length
     * 'radius'. 'radius' may be infinite.
     *
     * 'face' is the normal vector of the face of that block that was entered.
     * It should not be used after the callback returns.
     *
     * If the callback returns a true value, the traversal will be stopped.
     */
    public static BlockRaycastResult blockRaycast(Vector3d origin, Vector3d direction, float length) {
        // From "A Fast Voxel Traversal Algorithm for Ray Tracing"
        // by John Amanatides and Andrew Woo, 1987
        // <http://www.cse.yorku.ca/~amana/research/grid.pdf>
        // <http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.42.3443>
        // Extensions to the described algorithm:
        //   • Imposed a distance limit.
        //   • The face passed through to reach the current cube is provided to
        //     the callback.

        // The foundation of this algorithm is a parameterized representation of
        // the provided ray,
        //                    origin + t * direction,
        // except that t is not actually stored; rather, at any given point in the
        // traversal, we keep track of the *greater* t values which we would have
        // if we took a step sufficient to cross a cube boundary along that axis
        // (i.e. change the integer part of the coordinate) in the variables
        // tMaxX, tMaxY, and tMaxZ.

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

        // See description above. The initial values depend on the fractional
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

        World world = BackyardRocketry.getInstance().getPlayer().getWorld();

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





    public static void registerCollisionBody(ICollisionBody body) {

        COLLISION_BODIES.add(new WeakReference<>(body));

    }

}
