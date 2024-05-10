package wins.insomnia.backyardrocketry.util;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class FancyToString {


    public static String toString(Vector3d vector) {
        return "<" + vector.x + ", " + vector.y + ", " + vector.z + ">";
    }


    public static String toString(Vector3i vector) {
        return "<" + vector.x + ", " + vector.y + ", " + vector.z + ">";
    }


    public static String toString(Vector3f vector) {
        return "<" + vector.x + ", " + vector.y + ", " + vector.z + ">";
    }


}
