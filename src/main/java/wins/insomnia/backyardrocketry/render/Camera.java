package wins.insomnia.backyardrocketry.render;

import org.joml.Matrix4f;
import wins.insomnia.backyardrocketry.BackyardRocketry;

public class Camera {

    private final Matrix4f PROJECTION_MATRIX;
    private final Matrix4f VIEW_MATRIX;

    public Camera() {

        BackyardRocketry backyardRocketryInstance = BackyardRocketry.getInstance();
        Window gameWindow = backyardRocketryInstance.getWindow();
        int[] gameWindowSize = gameWindow.getSize();

        PROJECTION_MATRIX = new Matrix4f();
        PROJECTION_MATRIX.setPerspective(70f, (float) gameWindowSize[0] / (float) gameWindowSize[1], 0.01f, 100f);

        VIEW_MATRIX = new Matrix4f();
        VIEW_MATRIX.identity();
        VIEW_MATRIX.translate(0f, 0f, -3f);

    }


    public Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public Matrix4f getViewMatrix() {
        return VIEW_MATRIX;
    }


}
