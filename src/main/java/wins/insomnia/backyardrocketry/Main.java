package wins.insomnia.backyardrocketry;

import wins.insomnia.backyardrocketry.util.OpenGLWrapper;
import wins.insomnia.backyardrocketry.util.io.FileIO;

public class Main {

    private static BackyardRocketry game;
    public static final Thread MAIN_THREAD = Thread.currentThread();

    public static void main(String[] args) {

        //OpenGLWrapper.trackVaos();

        game = new BackyardRocketry();
        game.run();

    }

    public BackyardRocketry getGame() {
        return game;
    }

}