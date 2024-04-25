package wins.insomnia.backyardrocketry.physics;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Collision {

    public enum AABBCollisionResultType {

        OUTSIDE,
        INSIDE,
        CLIPPING

    }

    public static final List<WeakReference<ICollisionBody>> COLLISION_BODIES = new ArrayList<>();

    public static List<ICollisionBody> getBodiesInBoundingBox(BoundingBox boundingBox) {

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

    public static void registerCollisionBody(ICollisionBody body) {

        COLLISION_BODIES.add(new WeakReference<>(body));

    }

}
