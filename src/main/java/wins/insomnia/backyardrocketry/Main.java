package wins.insomnia.backyardrocketry;

public class Main {

    private static BackyardRocketry game;
    public static final Thread MAIN_THREAD = Thread.currentThread();

    public static void main(String[] args) {


        game = new BackyardRocketry();
        game.run();

    }

    public BackyardRocketry getGame() {
        return game;
    }

}