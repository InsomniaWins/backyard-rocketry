package wins.insomnia.backyardrocketry.util.debug;

public class DebugTime {



    public static long getElapsedTime(long previousTime) {
        return System.currentTimeMillis() - previousTime;
    }


}
