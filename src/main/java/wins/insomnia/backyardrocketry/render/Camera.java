package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.Transform;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;
    private final Transform TRANSFORM;
    private float fov;

    public Camera() {

        fov = (float) Math.toRadians(90f);

        TRANSFORM = new Transform();
        TRANSFORM.getPosition().set(0,0,0);

        PROJECTION_MATRIX = new Matrix4f();
        updateProjectionMatrix();

        VIEW_MATRIX = new Matrix4f();
        updateViewMatrix();

    }

    public void updateProjectionMatrix() {
        BackyardRocketry backyardRocketryInstance = BackyardRocketry.getInstance();
        Window gameWindow = backyardRocketryInstance.getWindow();
        int[] gameWindowSize = gameWindow.getSize();

        PROJECTION_MATRIX.setPerspective(fov, (float) gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 100f);
    }

    public void updateViewMatrix() {
        VIEW_MATRIX.identity();
        VIEW_MATRIX.rotateXYZ(TRANSFORM.getRotation().x, TRANSFORM.getRotation().y, TRANSFORM.getRotation().z);
        VIEW_MATRIX.translate(new Vector3f((float) -TRANSFORM.getPosX(), (float) -TRANSFORM.getPosY(), (float) -TRANSFORM.getPosZ()));
    }


    public Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public Matrix4f getViewMatrix() {
        return VIEW_MATRIX;
    }

    public Transform getTransform() {
        return TRANSFORM;
    }

}
