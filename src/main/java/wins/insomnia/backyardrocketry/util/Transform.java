package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Vector3f;

public class Transform {

    public static final double TAO = Math.PI * 2.0;

    private Vector3f position;
    private Vector3f rotation;

    
    public Transform() {
        position = new Vector3f();
        rotation = new Vector3f();
    }
    public Vector3f getPosition() {
        return position;
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
