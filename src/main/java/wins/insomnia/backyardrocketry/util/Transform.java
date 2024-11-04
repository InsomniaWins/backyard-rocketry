package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Transform {

    public static final double TAO = Math.PI * 2.0;

    private Vector3d position;
    private Vector3f rotation;

    
    public Transform() {
        position = new Vector3d();
        rotation = new Vector3f();
    }
    public Vector3d getPosition() {
        return position;
    }

    public double getPosX() {
        return position.x;
    }

    public double getPosY() {
        return position.y;
    }

    public double getPosZ() {
        return position.z;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void rotateX(float angle) {
        rotation.x += angle;
        wrapRotation();
    }

    public void rotateY(float angle) {
        rotation.y += angle;
        wrapRotation();
    }

    public void rotateZ(float angle) {
        rotation.z += angle;
        wrapRotation();
    }

    public Transform setPosition(Vector3d newValue) {
        position.set(newValue);
        return this;
    }

    public Transform setRotation(Vector3f newValue) {
        rotation.set(newValue);
        return this;
    }

    // makes sure angles outside the -π to π are wrapped to avoid overflow (angles outside float bounds)
    public void wrapRotation() {
        while (rotation.x > Math.PI) {
            rotation.x -= TAO;
        }

        while (rotation.y > Math.PI) {
            rotation.y -= TAO;
        }

        while (rotation.z > Math.PI) {
            rotation.z -= TAO;
        }

        while (rotation.x < -Math.PI) {
            rotation.x += TAO;
        }

        while (rotation.y < -Math.PI) {
            rotation.y += TAO;
        }

        while (rotation.z < -Math.PI) {
            rotation.z += TAO;
        }
    }

    public void set(Transform transform) {
        getPosition().set(transform.getPosition());
        getRotation().set(transform.getRotation());
    }

    public static float lerpAngle(float from, float to, float alpha) {

        double difference = (to - from) % TAO;
        double shortest = ((2.0 * difference) % TAO) - difference;
        return (float) (from + shortest * alpha);

    }
}
