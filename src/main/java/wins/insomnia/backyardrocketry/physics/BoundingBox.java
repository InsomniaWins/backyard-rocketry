package wins.insomnia.backyardrocketry.physics;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

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

    public Vector3d getCenter() {
        return new Vector3d(min).add(new Vector3d(max).div(2f));
    }



    public Collision.AABBCollisionResultType collideWithBoundingBoxExclusive(BoundingBox other) {
        // Check for non-collision conditions along each axis
        if (this.max.x <= other.min.x ||
                this.min.x >= other.max.x ||
                this.max.y <= other.min.y ||
                this.min.y >= other.max.y ||
                this.max.z <= other.min.z ||
                this.min.z >= other.max.z) {
            return Collision.AABBCollisionResultType.OUTSIDE; // No collision (outside)
        }
        // Check if this bounding box fully contains the other
        if (this.min.x < other.min.x &&
                this.min.y < other.min.y &&
                this.min.z < other.min.z &&
                this.max.x > other.max.x &&
                this.max.y > other.max.y &&
                this.max.z > other.max.z) {
            return Collision.AABBCollisionResultType.CONTAINS; // Fully contains the other
        }
        // Check if this bounding box is fully contained by the other
        if (this.min.x > other.min.x &&
                this.min.y > other.min.y &&
                this.min.z > other.min.z &&
                this.max.x < other.max.x &&
                this.max.y < other.max.y &&
                this.max.z < other.max.z) {
            return Collision.AABBCollisionResultType.INSIDE; // Fully contained by the other
        }
        // Otherwise, the bounding boxes are clipping each other
        return Collision.AABBCollisionResultType.CLIPPING; // Clipping
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

    public BoundingBox grow(double units) {

        min.sub(units, units, units);
        max.add(units, units, units);

        return this;
    }


    // excludeBounds = true will not return true when point is on very edge of bounding box
    public boolean containsPoint(Vector3d point, boolean excludeBounds) {
        boolean xBounds = excludeBounds
                ? (point.x > min.x && point.x < max.x)
                : (point.x >= min.x && point.x <= max.x);

        boolean yBounds = excludeBounds
                ? (point.y > min.y && point.y < max.y)
                : (point.y >= min.y && point.y <= max.y);

        boolean zBounds = excludeBounds
                ? (point.z > min.z && point.z < max.z)
                : (point.z >= min.z && point.z <= max.z);

        return xBounds && yBounds && zBounds;
    }

    public boolean containsPoint(Vector3d point) {
        return containsPoint(point, false);
    }

    public boolean containsPoint(Vector3f point) {
        return containsPoint(new Vector3d(point), false);
    }

    public boolean containsPoint(Vector3f point, boolean excludeBounds) {
        return containsPoint(new Vector3d(point), excludeBounds);
    }





    //Helper function for Line/AABB test.  Tests collision on a single dimension
    //Param:    Start of line, Direction/length of line,
    //          Min value of AABB on plane, Max value of AABB on plane
    //          Enter and Exit "timestamps" of intersection (OUT)
    //Return:   True if there is overlap between Line and AABB, False otherwise
    //Note:     Enter and Exit are used for calculations and are only updated in case of intersection
    public static boolean lineAABB1d(float start, float dir, float min, float max, float[] enter, float[] exit) {
        //If the line segment is more of a point, just check if it's within the segment
        if(Math.abs(dir) < 1.0E-8)
            return (start >= min && start <= max);

        //Find if the lines overlap
        float   ooDir = 1.0f / dir;
        float   t0 = (min - start) * ooDir;
        float   t1 = (max - start) * ooDir;

        //Make sure t0 is the "first" of the intersections
        if(t0 > t1) {
            float temp = t0;
            t0 = t1;
            t1 = temp;
        }

        //Check if intervals are disjoint
        if(t0 > exit[0] || t1 < enter[0])
            return false;

        //Reduce interval based on intersection
        if(t0 > enter[0])
            enter[0] = t0;
        if(t1 < exit[0])
            exit[0] = t1;

        return true;
    }

    //Check collision between a line segment and an AABB
    //Param:    Start point of line segement, End point of line segment,
    //          One corner of AABB, opposite corner of AABB,
    //          Location where line hits the AABB (OUT)
    //Return:   True if a collision occurs, False otherwise
    //Note:     If no collision occurs, OUT param is not reassigned and is not considered useable
    public boolean lineAABB(Vector3d start, Vector3d end, double[] hitPoint) {

        if (hitPoint.length != 3) {
            throw new RuntimeException("hitPoint MUST be a double[3]!");
        }

        float[] enter = new float[] {0f};
        float[] exit = new float[] {1f};
        Vector3d dir = new Vector3d(end).sub(start);

        //Check each dimension of Line/AABB for intersection
        if(!lineAABB1d((float) start.x, (float) dir.x, (float) min.x, (float) max.x, enter, exit))
            return false;
        if(!lineAABB1d((float) start.y, (float) dir.y, (float) min.y, (float) max.y, enter, exit))
            return false;
        if(!lineAABB1d((float) start.z, (float) dir.z, (float) min.z, (float) max.z, enter, exit))
            return false;

        //If there is intersection on all dimensions, report that point

        Vector3d hPoint = new Vector3d(start).add(new Vector3d(dir).mul(enter[0]));
        hitPoint[0] = hPoint.x;
        hitPoint[1] = hPoint.y;
        hitPoint[2] = hPoint.z;

        return true;
    }











    /*
    public static boolean intersection(BoundingBox b, Ray r) {
        double tmin = -Double.MAX_VALUE, tmax = Double.MAX_VALUE;

        Vector3f origin = r.getOrigin();
        Vector3f direction = r.getDirection();
        Vector3f inverseDirection = new Vector3f(direction).negate();

        for (int i = 0; i < 3; ++i) {
            double t1 = (b.min.get(i) - origin.get(i)) * inverseDirection.get(i);
            double t2 = (b.max.get(i) - origin.get(i)) * inverseDirection.get(i);

            tmin = Math.max(tmin, Math.min(t1, t2));
            tmax = Math.min(tmax, Math.max(t1, t2));
        }

        return tmax > Math.max(tmin, 0.0);
    }*/










    public String toString() {
        return "{min: (" + min.x + ", " + min.y + ", " + min.z + "), max: (" + max.x + ", " + max.y + ", " + max.z + ")}";
    }
}
