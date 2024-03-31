package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Updater {

    public static final int FIXED_UPDATES_PER_SECOND = 20;
    private final List<WeakReference<IUpdateListener>> UPDATE_LISTENERS;
    private final List<WeakReference<IFixedUpdateListener>> FIXED_UPDATE_LISTENERS;

    public Updater() {

        UPDATE_LISTENERS = new ArrayList<>();
        FIXED_UPDATE_LISTENERS = new ArrayList<>();

    }

    public void registerUpdateListener(IUpdateListener updateListener) {
        UPDATE_LISTENERS.add(new WeakReference<>(updateListener));
    }

    public void registerFixedUpdateListener(IFixedUpdateListener updateListener) {
        FIXED_UPDATE_LISTENERS.add(new WeakReference<>(updateListener));
    }

    private void fixedUpdate() {

        for (WeakReference<IFixedUpdateListener> updateListener : FIXED_UPDATE_LISTENERS) {
            updateListener.get().fixedUpdate();
        }

        if (BackyardRocketry.getInstance().getKeyboardInput().isKeyJustPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(BackyardRocketry.getInstance().getWindow().getWindowHandle(), true);
        }

    }

    private void update(double deltaTime) {

        for (WeakReference<IUpdateListener> updateListener : UPDATE_LISTENERS) {
            updateListener.get().update(deltaTime);
        }

    }

    public void loop() {
        final double secondsPerTick = 1.0 / 20.0;

        glfwSetTime(0.0);
        double currentTime = glfwGetTime();
        double accumulator = 0.0;


        while (!glfwWindowShouldClose(BackyardRocketry.getInstance().getWindow().getWindowHandle())) {

            glfwPollEvents();

            double newTime = glfwGetTime();
            double timeSincePreviousLoopIteration = newTime - currentTime;
            currentTime = newTime;

            accumulator += timeSincePreviousLoopIteration;

            while (accumulator >= secondsPerTick) {
                BackyardRocketry.getInstance().getKeyboardInput().updateKeyStates();
                fixedUpdate();
                accumulator -= secondsPerTick;
            }

            update(timeSincePreviousLoopIteration);

            BackyardRocketry.getInstance().getRenderer().draw(BackyardRocketry.getInstance().getWindow());

        }
    }

    public static int getFixedUpdatesPerSecond() {
        return FIXED_UPDATES_PER_SECOND;
    }


}
