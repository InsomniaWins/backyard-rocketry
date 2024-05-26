package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Updater {

    public static final int FIXED_UPDATES_PER_SECOND = 20;
    private final List<WeakReference<IUpdateListener>> UPDATE_LISTENERS;
    private final List<WeakReference<IFixedUpdateListener>> FIXED_UPDATE_LISTENERS;
    private final List<WeakReference<IUpdateListener>> QUEUED_UPDATE_LISTENERS;
    private final List<WeakReference<IFixedUpdateListener>> QUEUED_FIXED_UPDATE_LISTENERS;


    private int updatesPerSecond = 0;
    private int updatesProcessedSoFar = 0; // updates processed before ups-polling occurs
    private double upsTimer = 0.0;


    public Updater() {

        UPDATE_LISTENERS = new ArrayList<>();
        FIXED_UPDATE_LISTENERS = new ArrayList<>();
        QUEUED_UPDATE_LISTENERS = new ArrayList<>();
        QUEUED_FIXED_UPDATE_LISTENERS = new ArrayList<>();
    }

    // is thread-safe
    public void registerUpdateListener(IUpdateListener updateListener) {
        synchronized (this) {
            QUEUED_UPDATE_LISTENERS.add(new WeakReference<>(updateListener));
        }
    }

    // is thread-safe
    // TODO: For some reason, this method causes thread pool threads to remain open after the program closes
    // need to figure out why??????
    public void registerFixedUpdateListener(IFixedUpdateListener updateListener) {
        synchronized (this) {
            QUEUED_FIXED_UPDATE_LISTENERS.add(new WeakReference<>(updateListener));
        }
    }

    private void fixedUpdate() {


        synchronized (this) {
            Iterator<WeakReference<IFixedUpdateListener>> queueIterator = QUEUED_FIXED_UPDATE_LISTENERS.iterator();
            while (queueIterator.hasNext()) {
                WeakReference<IFixedUpdateListener> reference = queueIterator.next();

                queueIterator.remove();

                if (reference.get() == null) {
                    continue;
                }

                FIXED_UPDATE_LISTENERS.add(reference);
            }


            Iterator<WeakReference<IFixedUpdateListener>> listenerIterator = FIXED_UPDATE_LISTENERS.iterator();
            while (listenerIterator.hasNext()) {
                WeakReference<IFixedUpdateListener> updateListenerReference = listenerIterator.next();

                IFixedUpdateListener updateListener = updateListenerReference.get();


                if (updateListener == null) {
                    listenerIterator.remove();
                    continue;
                }

                updateListener.fixedUpdate();
            }

            updatesProcessedSoFar++;
        }
    }

    private void update(double deltaTime) {


        synchronized (this) {
            Iterator<WeakReference<IUpdateListener>> queueIterator = QUEUED_UPDATE_LISTENERS.iterator();
            while (queueIterator.hasNext()) {
                WeakReference<IUpdateListener> reference = queueIterator.next();

                queueIterator.remove();

                if (reference.get() == null) {
                    continue;
                }

                UPDATE_LISTENERS.add(reference);
            }


            Iterator<WeakReference<IUpdateListener>> listenerIterator = UPDATE_LISTENERS.iterator();
            while (listenerIterator.hasNext()) {
                WeakReference<IUpdateListener> updateListenerReference = listenerIterator.next();

                IUpdateListener updateListener = updateListenerReference.get();

                if (updateListener == null) {
                    listenerIterator.remove();
                    continue;
                }

                updateListener.update(deltaTime);
            }
        }


        upsTimer += deltaTime;
        while (upsTimer > 1.0) {
            upsTimer -= 1.0;
            updatesPerSecond = updatesProcessedSoFar;
            updatesProcessedSoFar = 0;
        }

    }

    public void loop() {
        final double secondsPerTick = 1.0 / FIXED_UPDATES_PER_SECOND;

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
                BackyardRocketry.getInstance().getMouseInput().updateButtonStates();
                fixedUpdate();
                accumulator -= secondsPerTick;
            }

            update(timeSincePreviousLoopIteration);

        }
    }

    public static int getFixedUpdatesPerSecond() {
        return FIXED_UPDATES_PER_SECOND;
    }

    public int getUpdatesPerSecond() {
        return updatesPerSecond;
    }

    public static Updater get() {
        return BackyardRocketry.getInstance().getUpdater();
    }

}
