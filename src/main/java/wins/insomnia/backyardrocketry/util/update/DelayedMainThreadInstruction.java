package wins.insomnia.backyardrocketry.util.update;

public class DelayedMainThreadInstruction implements Runnable {

    private final Runnable RUNNABLE;
    private int updateDelayCount = 0;
    private boolean onePerTick = false;

    public DelayedMainThreadInstruction(int updateDelayCount, Runnable runnable) {
        RUNNABLE = runnable;
        this.updateDelayCount = updateDelayCount;
    }

    public DelayedMainThreadInstruction(Runnable runnable) {
        RUNNABLE = runnable;
        onePerTick = true;
    }

    public boolean isOnePerTick() {
        return onePerTick;
    }

    public int getDelaysRemaining() {
        return updateDelayCount;
    }

    public void tickDelay() {
        updateDelayCount = Math.max(0, updateDelayCount - 1);
    }

    @Override
    public void run() {
        RUNNABLE.run();
    }
}
