package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.KeyboardInput;
import wins.insomnia.backyardrocketry.util.KeyboardInputEvent;
import wins.insomnia.backyardrocketry.util.Transform;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;
    private final Transform TRANSFORM;

    public Camera() {

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

        PROJECTION_MATRIX.setPerspective(70f, (float) gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 100f);
    }

    public void updateViewMatrix() {
        VIEW_MATRIX.identity();
        VIEW_MATRIX.rotateXYZ(TRANSFORM.getRotation().x, TRANSFORM.getRotation().y, TRANSFORM.getRotation().z);
        VIEW_MATRIX.translate(TRANSFORM.getPosition());
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
