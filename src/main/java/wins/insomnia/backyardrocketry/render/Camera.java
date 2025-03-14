package wins.insomnia.backyardrocketry.render;

import org.joml.*;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.FancyToString;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.debug.DebugInfo;
import wins.insomnia.backyardrocketry.util.update.Updater;

import java.lang.Math;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;
    private final Transform TRANSFORM;
    private final Transform PREVIOUS_TRANSFORM;
    private final Transform INTERPOLATED_TRANSFORM;
    private final FrustumIntersection FRUSTUM;
    private float interpolationFactor = 0.0f;
    private float fov;
    private float renderDistance = 364f;
    private float viewBobValue = 0f;
    private float zNear = 0.1f;
    private float zFar = renderDistance * 2f;

    public Camera() {

        fov = (float) Math.toRadians(70f);

        TRANSFORM = new Transform();
        PREVIOUS_TRANSFORM = new Transform();
        INTERPOLATED_TRANSFORM = new Transform();

        PROJECTION_MATRIX = new Matrix4f();
        updateProjectionMatrix();

        VIEW_MATRIX = new Matrix4f();
        updateViewMatrix();

        FRUSTUM = new FrustumIntersection();
    }

    public float getFov() {
        return fov;
    }

    public void setRenderDistance(float renderDistance) {
        this.renderDistance = renderDistance;
        updateZNearFar();
    }

    private void updateZNearFar() {

        zNear = 0.1f;
        zFar = renderDistance * 2f;

    }

    public float getZNear() {
        return zNear;
    }

    public float getZFar() {
        return zFar;
    }

    public float getViewBobValue() {
        return viewBobValue;
    }

    public void setViewBobValue(float value) {
        viewBobValue = value;
    }

    public float getRenderDistance() {
        return renderDistance;
    }

    public void updateProjectionMatrix() {
        BackyardRocketry backyardRocketryInstance = BackyardRocketry.getInstance();
        Window gameWindow = backyardRocketryInstance.getWindow();

        float aspect = 1920f / 1080f;
        if (Window.get() != null) {
            aspect = Window.get().getResolutionFrameBuffer().getWidth() / (float) Window.get().getResolutionFrameBuffer().getHeight();
        }

        PROJECTION_MATRIX.setPerspective(fov, aspect, zNear, zFar);
    }

    public void updateViewMatrix() {
        VIEW_MATRIX.identity();
        VIEW_MATRIX.rotateXYZ(TRANSFORM.getRotation().x, TRANSFORM.getRotation().y, TRANSFORM.getRotation().z);
        VIEW_MATRIX.translate(new Vector3f((float) -TRANSFORM.getPosX(), (float) -TRANSFORM.getPosY(), (float) -TRANSFORM.getPosZ()));
        VIEW_MATRIX.translate(0f, viewBobValue, 0f);
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
