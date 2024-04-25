package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3f;

public class BoundingBox {

    private Vector3f min;
    private Vector3f max;

    public BoundingBox() {

        min = new Vector3f();
        max = new Vector3f();
    }

    public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

        min = new Vector3f(minX, minY, minZ);
        max = new Vector3f(maxX, maxY, maxZ);

    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public Collision.AABBCollisionResultType collideWithBoundingBox(BoundingBox otherBox) {

        boolean minInBounds = false;
        boolean maxInBounds = false;

        if (min.x >= otherBox.min.x && min.x <= otherBox.max.x) {
            if (min.y >= otherBox.min.y && min.y <= otherBox.max.y) {
                if (min.z >= otherBox.min.z && min.z <= otherBox.max.z) {
                    minInBounds = true;
                }
            }
        }

        if (max.x >= otherBox.min.x && max.x <= otherBox.max.x) {
            if (max.y >= otherBox.min.y && max.y <= otherBox.max.y) {
                if (max.z >= otherBox.min.z && max.z <= otherBox.max.z) {
                    maxInBounds = true;
                }
            }
        }

        if (minInBounds && maxInBounds) {
            return Collision.AABBCollisionResultType.INSIDE;
        }

        if (minInBounds || maxInBounds) {
            return Collision.AABBCollisionResultType.CLIPPING;
        }

        return Collision.AABBCollisionResultType.OUTSIDE;
    }

}
