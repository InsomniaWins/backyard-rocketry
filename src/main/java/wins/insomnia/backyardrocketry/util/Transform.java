package wins.insomnia.backyardrocketry.util;

import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {

    private Vector3f position;
    private Quaternionf rotation;


    public Transform() {
        position = new Vector3f();
        rotation = new Quaternionf();
    }

    public Quaternionf getHorizontalRotation() {
        return new Quaternionf().rotateY(Math.safeAsin(2.0F * (rotation.x * rotation.z + rotation.y * rotation.w)));
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public void set(Transform transform) {
        getPosition().set(transform.getPosition());
        getRotation().set(transform.getRotation());
    }
}
