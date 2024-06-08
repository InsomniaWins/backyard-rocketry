package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.Main;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public class Updater {

    public static final int FIXED_UPDATES_PER_SECOND = 20;
    private final List<UpdateListener> MANUAL_UPDATE_LISTENERS;
    private final List<WeakReference<IUpdateListener>> UPDATE_LISTENERS;
    private final List<WeakReference<IFixedUpdateListener>> FIXED_UPDATE_LISTENERS;
    private final ConcurrentLinkedQueue<IUpdateListener> QUEUED_UPDATE_LISTENERS;
    private final ConcurrentLinkedQueue<IFixedUpdateListener> QUEUED_FIXED_UPDATE_LISTENERS;


    private int updatesPerSecond = 0;
    private int updatesProcessedSoFar = 0; // updates processed before ups-polling occurs
    private double upsTimer = 0.0;


    public Updater() {
        MANUAL_UPDATE_LISTENERS = new ArrayList<>();
        UPDATE_LISTENERS = new ArrayList<>();
        FIXED_UPDATE_LISTENERS = new ArrayList<>();
        QUEUED_UPDATE_LISTENERS = new ConcurrentLinkedQueue<>();
        QUEUED_FIXED_UPDATE_LISTENERS = new ConcurrentLinkedQueue<>();
    }


    @CalledFromMainThread
    public void registerManualUpdateListener(UpdateListener updateListener) {
        MANUAL_UPDATE_LISTENERS.add(updateListener);
    }

    @CalledFromMainThread
    public void unregisterManualUpdateListener(UpdateListener updateListener) {
        MANUAL_UPDATE_LISTENERS.remove(updateListener);
    }

    // is thread-safe
    public void registerUpdateListener(IUpdateListener updateListener) {
        QUEUED_UPDATE_LISTENERS.add(updateListener);
    }

    // is thread-safe
    public void registerFixedUpdateListener(IFixedUpdateListener updateListener) {
        QUEUED_FIXED_UPDATE_LISTENERS.add(updateListener);
    }

    private void fixedUpdate() {
        while (!QUEUED_FIXED_UPDATE_LISTENERS.isEmpty()) {
            FIXED_UPDATE_LISTENERS.add(new WeakReference<>(QUEUED_FIXED_UPDATE_LISTENERS.poll()));
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

        Iterator<UpdateListener> updateListenerIterator = MANUAL_UPDATE_LISTENERS.listIterator();
        while (updateListenerIterator.hasNext()) {
            UpdateListener updateListener = updateListenerIterator.next();
            updateListener.fixedUpdate();
        }

        updatesProcessedSoFar++;
    }

    private void update(double deltaTime) {


        while (!QUEUED_UPDATE_LISTENERS.isEmpty()) {
            UPDATE_LISTENERS.add(new WeakReference<>(QUEUED_UPDATE_LISTENERS.poll()));
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

        Iterator<UpdateListener> updateListenerIterator = MANUAL_UPDATE_LISTENERS.listIterator();
        while (updateListenerIterator.hasNext()) {
            UpdateListener updateListener = updateListenerIterator.next();
            updateListener.update(deltaTime);
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
