package wins.insomnia.backyardrocketry.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {

    private Vector3f position;
    private Quaternionf rotation;


    public Transform() {

        position = new Vector3f();
        rotation = new Quaternionf();

    }


    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

}
