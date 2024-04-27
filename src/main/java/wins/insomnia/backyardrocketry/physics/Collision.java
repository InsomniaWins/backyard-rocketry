package wins.insomnia.backyardrocketry.physics;

import org.joml.Math;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
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

    public static void registerCollisionBody(ICollisionBody body) {

        COLLISION_BODIES.add(new WeakReference<>(body));

    }

}
