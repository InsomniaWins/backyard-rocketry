package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

public class BoundingBox {

    private Vector3d min;
    private Vector3d max;

    public BoundingBox(BoundingBox box) {
        this.min = new Vector3d(box.min);
        this.max = new Vector3d(box.max);
    }

    public BoundingBox() {

        min = new Vector3d();
        max = new Vector3d();
    }

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

        min = new Vector3d(minX, minY, minZ);
        max = new Vector3d(maxX, maxY, maxZ);

    }

    public Vector3d getMin() {
        return min;
    }

    public Vector3d getMax() {
        return max;
    }

    public Collision.AABBCollisionResultType collideWithBoundingBox(BoundingBox other) {
        // Check for non-collision conditions along each axis
        if (this.max.x < other.min.x ||
                this.min.x > other.max.x ||
                this.max.y < other.min.y ||
                this.min.y > other.max.y ||
                this.max.z < other.min.z ||
                this.min.z > other.max.z) {
            return Collision.AABBCollisionResultType.OUTSIDE; // No collision (outside)
        }
        // Check if this bounding box fully contains the other
        if (this.min.x <= other.min.x &&
                this.min.y <= other.min.y &&
                this.min.z <= other.min.z &&
                this.max.x >= other.max.x &&
                this.max.y >= other.max.y &&
                this.max.z >= other.max.z) {
            return Collision.AABBCollisionResultType.CONTAINS; // Fully contains the other
        }
        // Check if this bounding box is fully contained by the other
        if (this.min.x >= other.min.x &&
                this.min.y >= other.min.y &&
                this.min.z >= other.min.z &&
                this.max.x <= other.max.x &&
                this.max.y <= other.max.y &&
                this.max.z <= other.max.z) {
            return Collision.AABBCollisionResultType.INSIDE; // Fully contained by the other
        }
        // Otherwise, the bounding boxes are clipping each other
        return Collision.AABBCollisionResultType.CLIPPING; // Clipping
    }

    public Vector3d getSize() {
        return new Vector3d(
                max.x - min.x,
                max.y - min.y,
                max.z - min.z
        );
    }

    public BoundingBox translate(Vector3d offset) {

        min.x += offset.x;
        min.y += offset.y;
        min.z += offset.z;

        max.x += offset.x;
        max.y += offset.y;
        max.z += offset.z;

        return this;
    }

    public BoundingBox translate(double x, double y, double z) {

        min.x += x;
        min.y += y;
        min.z += z;

        max.x += x;
        max.y += y;
        max.z += z;

        return this;
    }

    public double collideY(BoundingBox other, double offsetY) {
        if (other.getMax().x > getMin().x && other.getMin().x < getMax().x && other.getMax().z > getMin().z && other.getMin().z < getMax().z) {
            if (offsetY > 0f && other.getMax().y <= getMin().y) {
                double distanceToFace = getMin().y - other.getMax().y;

                if (distanceToFace < offsetY) {
                    offsetY = distanceToFace;
                }

            } else if (offsetY < 0f && other.getMin().y >= getMax().y) {
                double distanceToFace = getMax().y - other.getMin().y;

                if (distanceToFace > offsetY) {
                    offsetY = distanceToFace;
                }
            }

        }
        return offsetY;
    }

    public double collideX(BoundingBox other, double offsetX) {
        if (other.getMax().z > getMin().z && other.getMin().z < getMax().z && other.getMax().y > getMin().y && other.getMin().y < getMax().y) {
            if (offsetX > 0f && other.getMax().x <= getMin().x) {
                double distanceToFace = getMin().x - other.getMax().x;

                if (distanceToFace < offsetX) {
                    offsetX = distanceToFace;
                }
            }
            else if (offsetX < 0f && other.getMin().x >= getMax().x) {
                double distanceToFace = getMax().x - other.getMin().x;

                if (distanceToFace > offsetX) {
                    offsetX = distanceToFace;
                }
            }

        }

        return offsetX;
    }


    public double collideZ(BoundingBox other, double offsetZ) {
        if (other.getMax().x > getMin().x && other.getMin().x < getMax().x && other.getMax().y > getMin().y && other.getMin().y < getMax().y) {
            if (offsetZ > 0f && other.getMax().z <= getMin().z) {
                double distanceToFace = getMin().z - other.getMax().z;

                if (distanceToFace < offsetZ) {
                    offsetZ = distanceToFace;
                }
            }
            else if (offsetZ < 0f && other.getMin().z >= getMax().z) {
                double distanceToFace = getMax().z - other.getMin().z;

                if (distanceToFace > offsetZ) {
                    offsetZ = distanceToFace;
                }
            }

        }
        return offsetZ;
    }


    /*
    Vector3d collideWithXPlane(double p_186671_1_, Vector3d p_186671_3_, Vector3d p_186671_4_) {
        Vector3d vec3d = p_186671_3_.getIntermediateWithXValue(p_186671_4_, p_186671_1_);
        return vec3d != null && intersectsWithYZ(vec3d) ? vec3d : null;
    }


    Vector3d collideWithYPlane(double p_186663_1_, Vector3d p_186663_3_, Vector3d p_186663_4_) {
        Vector3d vec3d = p_186663_3_.getIntermediateWithYValue(p_186663_4_, p_186663_1_);
        return vec3d != null && intersectsWithXZ(vec3d) ? vec3d : null;
    }


    Vector3d collideWithZPlane(double p_186665_1_, Vector3d p_186665_3_, Vector3d p_186665_4_) {
        Vector3d vec3d = p_186665_3_.getIntermediateWithZValue(p_186665_4_, p_186665_1_);
        return vec3d != null && intersectsWithXY(vec3d) ? vec3d : null;
    }


    public boolean intersectsWithYZ(Vector3d vec) {
        return vec.y >= min.y && vec.y <= max.y && vec.z >= min.z && vec.z <= max.z;
    }

    public boolean intersectsWithXZ(Vector3d vec) {
        return vec.x >= min.x && vec.x <= max.x && vec.z >= min.z && vec.z <= max.z;
    }

    public boolean intersectsWithXY(Vector3d vec) {
        return vec.x >= min.x && vec.x <= max.x && vec.y >= min.y && vec.y <= max.y;
    }


    public RayCastResult calculateIntercept(Vector3d vecA, Vector3d vecB) {
        Vector3d vec3d = collideWithXPlane(min.x, vecA, vecB);
        Collision.Facing facing = Collision.Facing.NEG_X;
        Vector3d vec3d1 = collideWithXPlane(max.x, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = Collision.Facing.POS_X;
        }

        vec3d1 = collideWithYPlane(min.y, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = Collision.Facing.NEG_Y;
        }

        vec3d1 = collideWithYPlane(max.y, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = Collision.Facing.POS_Y;
        }

        vec3d1 = collideWithZPlane(min.z, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vec3d, vec3d1))
        {
            vec3d = vec3d1;
            facing = Collision.Facing.NEG_Z;
        }

        vec3d1 = collideWithZPlane(max.z, vecA, vecB);

        if (vec3d1 != null && isClosest(vecA, vec3d, vec3d1)) {
            vec3d = vec3d1;
            facing = Collision.Facing.POS_Z;
        }

        return vec3d == null ? null : new RayCastResult(vec3d, facing);
    }

*/

    public BoundingBox grow(double units) {

        min.sub(units, units, units);
        max.add(units, units, units);

        return this;
    }

    public String toString() {
        return "{min: (" + min.x + ", " + min.y + ", " + min.z + "), max: (" + max.x + ", " + max.y + ", " + max.z + ")}";
    }
}
