package wins.insomnia.backyardrocketry.render;

import org.joml.*;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.Updater;

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
    private float renderDistance = 264f;

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

    public void interpolate(double deltaTime) {
        interpolationFactor += (float) deltaTime / (1.0f / Updater.getFixedUpdatesPerSecond());
        interpolationFactor = org.joml.Math.min(interpolationFactor, 1f);

        Camera camera = Renderer.get().getCamera();

        // reset interpolation to t = 0 to begin interpolation=
        INTERPOLATED_TRANSFORM.getRotation().set(PREVIOUS_TRANSFORM.getRotation());
        INTERPOLATED_TRANSFORM.getPosition().set(PREVIOUS_TRANSFORM.getPosition());

        // interpolate camera rotation and position
        INTERPOLATED_TRANSFORM.getRotation().set(
                Transform.lerpAngle(INTERPOLATED_TRANSFORM.getRotation().x, TRANSFORM.getRotation().x, interpolationFactor),
                Transform.lerpAngle(INTERPOLATED_TRANSFORM.getRotation().y, TRANSFORM.getRotation().y, interpolationFactor),
                Transform.lerpAngle(INTERPOLATED_TRANSFORM.getRotation().z, TRANSFORM.getRotation().z, interpolationFactor)
        );
        INTERPOLATED_TRANSFORM.getPosition().lerp(TRANSFORM.getPosition(), interpolationFactor);

        // set camera rotation and position to interpolated values
        camera.getTransform().getRotation().set(INTERPOLATED_TRANSFORM.getRotation());
        camera.getTransform().getPosition().set(INTERPOLATED_TRANSFORM.getPosition());
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
