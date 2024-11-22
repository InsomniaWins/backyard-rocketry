package wins.insomnia.backyardrocketry.util.update;

import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.util.debug.DebugTime;
import wins.insomnia.backyardrocketry.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class Updater {

    public static final int FIXED_UPDATES_PER_SECOND = 20;
    private final List<IUpdateListener> UPDATE_LISTENERS;
    private final List<IFixedUpdateListener> FIXED_UPDATE_LISTENERS;
    private final ConcurrentLinkedQueue<UpdateListenerInstruction> UPDATE_LISTENER_INSTRUCTION_QUEUE;
    private final ConcurrentLinkedQueue<Runnable> MAIN_THREAD_INSTRUCTION_QUEUE;
    private int updatesPerSecond = 0;
    private int updatesProcessedSoFar = 0; // updates processed before ups-polling occurs
    private double upsTimer = 0.0;


    public Updater() {

        UPDATE_LISTENERS = new ArrayList<>();
        FIXED_UPDATE_LISTENERS = new ArrayList<>();
        UPDATE_LISTENER_INSTRUCTION_QUEUE = new ConcurrentLinkedQueue<>();
        MAIN_THREAD_INSTRUCTION_QUEUE = new ConcurrentLinkedQueue<>();
    }

    // is thread-safe
    public void registerUpdateListener(IUpdateListener updateListener) {
        UPDATE_LISTENER_INSTRUCTION_QUEUE.add(new UpdateListenerInstruction(
                UpdateListenerInstruction.InstructionType.REGISTER_LISTENER,
                updateListener
        ));
    }

    // is thread-safe
    public void registerFixedUpdateListener(IFixedUpdateListener updateListener) {
        UPDATE_LISTENER_INSTRUCTION_QUEUE.add(new UpdateListenerInstruction(
                UpdateListenerInstruction.InstructionType.REGISTER_FIXED_LISTENER,
                updateListener
        ));
    }

    // is thread-safe
    public void unregisterUpdateListener(IUpdateListener updateListener) {
        UPDATE_LISTENER_INSTRUCTION_QUEUE.add(new UpdateListenerInstruction(
                UpdateListenerInstruction.InstructionType.UNREGISTER_LISTENER,
                updateListener
        ));
    }

    // is thread-safe
    public void unregisterFixedUpdateListener(IFixedUpdateListener updateListener) {
        UPDATE_LISTENER_INSTRUCTION_QUEUE.add(new UpdateListenerInstruction(
                UpdateListenerInstruction.InstructionType.UNREGISTER_FIXED_LISTENER,
                updateListener
        ));
    }


    private void fixedUpdate() {

        Queue<UpdateListenerInstruction> fixedListenerInstructionsQueue = UPDATE_LISTENER_INSTRUCTION_QUEUE.stream().filter(
                instruction -> (
                        instruction.instructionType() == UpdateListenerInstruction.InstructionType.UNREGISTER_FIXED_LISTENER
                        || instruction.instructionType() == UpdateListenerInstruction.InstructionType.REGISTER_FIXED_LISTENER
                )
        ).collect(Collectors.toCollection(LinkedList::new));

        while (!fixedListenerInstructionsQueue.isEmpty()) {

            UpdateListenerInstruction instruction = fixedListenerInstructionsQueue.poll();
            UPDATE_LISTENER_INSTRUCTION_QUEUE.remove(instruction);

            switch (instruction.instructionType()) {
                case REGISTER_FIXED_LISTENER -> {
                    FIXED_UPDATE_LISTENERS.add((IFixedUpdateListener) instruction.listener());
                    ((IFixedUpdateListener) instruction.listener()).registeredFixedUpdateListener();
                }
                case UNREGISTER_FIXED_LISTENER -> {
                    FIXED_UPDATE_LISTENERS.remove((IFixedUpdateListener) instruction.listener());
                    ((IFixedUpdateListener) instruction.listener()).unregisteredFixedUpdateListener();
                }
            }

        }


        Iterator<IFixedUpdateListener> listenerIterator = FIXED_UPDATE_LISTENERS.iterator();
        while (listenerIterator.hasNext()) {
            IFixedUpdateListener updateListener = listenerIterator.next();

            if (updateListener == null) {
                listenerIterator.remove();
                continue;
            }

            updateListener.fixedUpdate();
        }

        updatesProcessedSoFar++;
    }

    private void update(double deltaTime) {


        Queue<UpdateListenerInstruction> listenerInstructionsQueue = UPDATE_LISTENER_INSTRUCTION_QUEUE.stream().filter(
                instruction -> (
                        instruction.instructionType() == UpdateListenerInstruction.InstructionType.UNREGISTER_LISTENER
                                || instruction.instructionType() == UpdateListenerInstruction.InstructionType.REGISTER_LISTENER
                )
        ).collect(Collectors.toCollection(LinkedList::new));

        while (!listenerInstructionsQueue.isEmpty()) {

            UpdateListenerInstruction instruction = listenerInstructionsQueue.poll();
            UPDATE_LISTENER_INSTRUCTION_QUEUE.remove(instruction);

            switch (instruction.instructionType()) {
                case REGISTER_LISTENER -> {
                    UPDATE_LISTENERS.add((IUpdateListener) instruction.listener());
                    ((IUpdateListener) instruction.listener()).registeredUpdateListener();
                }
                case UNREGISTER_LISTENER -> {
                    UPDATE_LISTENERS.remove((IUpdateListener) instruction.listener());
                    ((IUpdateListener) instruction.listener()).registeredUpdateListener();
                }
            }

        }



        Iterator<IUpdateListener> listenerIterator = UPDATE_LISTENERS.iterator();
        while (listenerIterator.hasNext()) {

            IUpdateListener updateListener = listenerIterator.next();

            if (updateListener == null) {
                listenerIterator.remove();
                continue;
            }

            updateListener.update(deltaTime);
        }


        boolean processedDelayedInstruction = false;
        for (Runnable instruction : MAIN_THREAD_INSTRUCTION_QUEUE) {

            if (instruction instanceof DelayedMainThreadInstruction delayedInstruction) {

                if (delayedInstruction.isOnePerTick()) {

                    if (!processedDelayedInstruction) {
                        processedDelayedInstruction = true;
                        delayedInstruction.run();
                        MAIN_THREAD_INSTRUCTION_QUEUE.remove(instruction);
                    }

                } else if (delayedInstruction.getDelaysRemaining() == 0) {
                    delayedInstruction.run();
                    MAIN_THREAD_INSTRUCTION_QUEUE.remove(instruction);
                } else {
                    delayedInstruction.tickDelay();
                }

                continue;
            }

            instruction.run();
            MAIN_THREAD_INSTRUCTION_QUEUE.remove(instruction);
        }


        upsTimer += deltaTime;
        while (upsTimer > 1.0) {
            upsTimer -= 1.0;
            updatesPerSecond = updatesProcessedSoFar;
            updatesProcessedSoFar = 0;
        }

    }

    // thread-safe
    public void queueMainThreadInstruction(Runnable instruction) {
        MAIN_THREAD_INSTRUCTION_QUEUE.add(instruction);
    }

    // thread-safe
    public void queueDelayedMainThreadInstruction(DelayedMainThreadInstruction instruction) {
        MAIN_THREAD_INSTRUCTION_QUEUE.add(instruction);
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

    public static double getCurrentTime() {
        return glfwGetTime();
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
