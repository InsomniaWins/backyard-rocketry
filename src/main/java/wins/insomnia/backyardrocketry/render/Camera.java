package wins.insomnia.backyardrocketry.render;

import org.joml.*;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.Transform;

import java.lang.Math;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;
    private final Transform TRANSFORM;
    private final FrustumIntersection FRUSTUM;

    private float fov;
    private float renderDistance = 264f;

    public Camera() {

        fov = (float) Math.toRadians(70f);

        TRANSFORM = new Transform();
        TRANSFORM.getPosition().set(0,0,0);

        PROJECTION_MATRIX = new Matrix4f();
        updateProjectionMatrix();

        VIEW_MATRIX = new Matrix4f();
        updateViewMatrix();

        FRUSTUM = new FrustumIntersection();
    }

    public float getRenderDistance() {
        return renderDistance;
    }

    public void updateProjectionMatrix() {
        BackyardRocketry backyardRocketryInstance = BackyardRocketry.getInstance();
        Window gameWindow = backyardRocketryInstance.getWindow();
        int[] gameWindowSize = gameWindow.getSize();

        PROJECTION_MATRIX.setPerspective(fov, (float) gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, renderDistance * 2);
    }

    public void updateViewMatrix() {
        VIEW_MATRIX.identity();
        VIEW_MATRIX.rotateXYZ(TRANSFORM.getRotation().x, TRANSFORM.getRotation().y, TRANSFORM.getRotation().z);
        VIEW_MATRIX.translate(new Vector3f((float) -TRANSFORM.getPosX(), (float) -TRANSFORM.getPosY(), (float) -TRANSFORM.getPosZ()));
    }

    public void updateFrustum() {
        FRUSTUM.set(
                new Matrix4f(getProjectionMatrix()).mul(getViewMatrix())
        );
    }

    public FrustumIntersection getFrustum() {
        return FRUSTUM;
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
