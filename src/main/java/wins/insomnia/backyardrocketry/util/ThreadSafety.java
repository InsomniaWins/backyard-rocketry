package wins.insomnia.backyardrocketry.util;

import wins.insomnia.backyardrocketry.Main;

public class ThreadSafety {

    public static boolean isRunningOnThread(Thread thread) {
        return Thread.currentThread() == thread;
    }

    public static void assertThread(Thread thread) {
        assert(isRunningOnThread(thread));
    }

    public static boolean isRunningOnMainThread() {
        return isRunningOnThread(Main.MAIN_THREAD);
    }

}
